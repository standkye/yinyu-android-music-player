package com.example.yinyu.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.example.yinyu.R
import com.example.yinyu.ui.PreviewSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

internal data class ScanResult(
    val songs: List<PreviewSong>,
    val artistPictures: Map<String, Uri> = emptyMap(),
    val sourceName: String = "手机媒体库",
)

/** Reads only local content URIs. A selected tree also exposes sibling folder.jpg, singer.jpg and .lrc files. */
internal class MusicCatalog(private val context: Context) {
    private val resolver = context.contentResolver
    private val preferences = context.getSharedPreferences("music_source", Context.MODE_PRIVATE)

    fun selectedTree(): Uri? = preferences.getString("tree_uri", null)?.let(Uri::parse)

    fun useTree(uri: Uri) {
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        preferences.edit().putString("tree_uri", uri.toString()).apply()
    }

    fun useMediaStore() {
        preferences.edit().remove("tree_uri").apply()
    }

    fun hasMediaPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun scan(minDurationSeconds: Int = 0): ScanResult = withContext(Dispatchers.IO) {
        val tree = selectedTree()
        if (tree != null) {
            try {
                return@withContext scanTree(tree, minDurationSeconds)
            } catch (_: SecurityException) {
                useMediaStore()
            } catch (_: IllegalArgumentException) {
                useMediaStore()
            }
        }
        if (hasMediaPermission()) scanMediaStore(minDurationSeconds) else ScanResult(emptyList())
    }

    private fun scanMediaStore(minDurationSeconds: Int): ScanResult {
        val base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val columns = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
        )
        if (Build.VERSION.SDK_INT >= 29) columns += MediaStore.Audio.Media.RELATIVE_PATH
        val songs = mutableListOf<PreviewSong>()
        resolver.query(
            base,
            columns.toTypedArray(),
            "${MediaStore.Audio.Media.DURATION} > 0 AND ${MediaStore.Audio.Media.DURATION} >= ?",
            arrayOf((minDurationSeconds.coerceAtLeast(0) * 1_000L).toString()),
            "${MediaStore.Audio.Media.DATE_ADDED} DESC",
        )?.use { cursor ->
            fun value(column: String): String? = cursor.getColumnIndex(column).takeIf { it >= 0 }
                ?.let { index -> if (cursor.isNull(index)) null else cursor.getString(index) }
            while (cursor.moveToNext()) {
                val id = value(MediaStore.Audio.Media._ID)?.toLongOrNull() ?: continue
                val fileName = value(MediaStore.Audio.Media.DISPLAY_NAME).orEmpty()
                val title = cleanMetadata(value(MediaStore.Audio.Media.TITLE)) ?: fileName.substringBeforeLast('.')
                val artist = cleanMetadata(value(MediaStore.Audio.Media.ARTIST)) ?: "未知歌手"
                val album = cleanMetadata(value(MediaStore.Audio.Media.ALBUM)) ?: "未知专辑"
                val albumId = value(MediaStore.Audio.Media.ALBUM_ID)?.toLongOrNull() ?: -1L
                val path = if (Build.VERSION.SDK_INT >= 29) value(MediaStore.Audio.Media.RELATIVE_PATH).orEmpty() else ""
                val duration = (value(MediaStore.Audio.Media.DURATION)?.toLongOrNull() ?: 0L) / 1_000L
                songs += PreviewSong(
                    title = title,
                    artist = artist,
                    album = album,
                    cover = R.drawable.other_artist,
                    durationSeconds = duration.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    uri = ContentUris.withAppendedId(base, id),
                    artworkUri = albumId.takeIf { it >= 0L }
                        ?.let { Uri.parse("content://media/external/audio/albumart/$it") },
                    folder = path.trimEnd('/').substringAfterLast('/').ifBlank { "本地音乐" },
                    addedAt = value(MediaStore.Audio.Media.DATE_ADDED)?.toLongOrNull() ?: 0L,
                    format = fileName.substringAfterLast('.', "").uppercase(Locale.ROOT),
                )
            }
        }
        return ScanResult(songs, sourceName = "手机媒体库")
    }

    private data class Entry(
        val id: String,
        val name: String,
        val mime: String,
        val folder: List<String>,
        val modified: Long,
    ) {
        val key: String get() = folder.joinToString("/")
        val baseName: String get() = name.substringBeforeLast('.').lowercase(Locale.ROOT)
    }

    private fun scanTree(tree: Uri, minDurationSeconds: Int): ScanResult {
        val files = mutableListOf<Entry>()
        val rootId = DocumentsContract.getTreeDocumentId(tree)
        fun walk(parentId: String, path: List<String>, depth: Int) {
            if (depth > 12) return
            val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parentId)
            resolver.query(
                childUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                ),
                null, null, null,
            )?.use { cursor ->
                val entries = mutableListOf<Entry>()
                while (cursor.moveToNext()) {
                    entries += Entry(
                        id = cursor.getString(0),
                        name = cursor.getString(1).orEmpty(),
                        mime = cursor.getString(2).orEmpty(),
                        folder = path,
                        modified = if (cursor.isNull(3)) 0L else cursor.getLong(3),
                    )
                }
                entries.forEach { entry ->
                    if (entry.mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        walk(entry.id, path + entry.name, depth + 1)
                    } else {
                        files += entry
                    }
                }
            }
        }
        walk(rootId, emptyList(), 0)
        fun uri(entry: Entry) = DocumentsContract.buildDocumentUriUsingTree(tree, entry.id)
        val byFolder = files.groupBy { it.key }
        val portraitsByFolder = files.filter { it.name.equals("singer.jpg", true) }
            .associate { it.key to uri(it) }
        val artistPictures = mutableMapOf<String, Uri>()
        val audioExtensions = setOf("mp3", "flac", "m4a", "aac", "ogg", "opus", "wav", "aiff", "amr")
        val songs = files.asSequence()
            .filter { it.name.substringAfterLast('.', "").lowercase(Locale.ROOT) in audioExtensions }
            .map { file ->
                val audioUri = uri(file)
                val siblings = byFolder[file.key].orEmpty()
                val coverFile = listOf("folder.jpg", "cover.jpg", "front.jpg", "album.jpg", "folder.png", "cover.png")
                    .firstNotNullOfOrNull { desired -> siblings.firstOrNull { it.name.equals(desired, true) } }
                val lyricFile = siblings.firstOrNull { it.name.equals("${file.name.substringBeforeLast('.')}.lrc", true) }
                var title: String? = null
                var artist: String? = null
                var album: String? = null
                var duration = 0
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, audioUri)
                    title = cleanMetadata(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
                    artist = cleanMetadata(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
                    album = cleanMetadata(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))
                    duration = ((retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                        ?: 0L) / 1_000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                } catch (_: Exception) {
                    // A malformed file remains visible, so the user can see what failed to play.
                } finally {
                    retriever.release()
                }
                val artistFromPath = file.folder.firstOrNull().orEmpty()
                val albumFromPath = file.folder.lastOrNull().orEmpty()
                val name = file.name.substringBeforeLast('.')
                // 歌手以音乐库第一层目录为准，避免音频元数据中的“歌手1/歌手2”把同一歌手拆散。
                val resolvedArtist = artistFromPath.ifBlank { artist ?: "未知歌手" }
                val portrait = (file.folder.size downTo 0).firstNotNullOfOrNull { depth ->
                    portraitsByFolder[file.folder.take(depth).joinToString("/")]
                }
                if (portrait != null) {
                    artistPictures.putIfAbsent(resolvedArtist, portrait)
                    if (artistFromPath.isNotBlank()) artistPictures.putIfAbsent(artistFromPath, portrait)
                }
                PreviewSong(
                    title = title ?: name.removePrefix("$artistFromPath - "),
                    artist = resolvedArtist,
                    album = album ?: albumFromPath.ifBlank { "未知专辑" },
                    cover = R.drawable.other_artist,
                    durationSeconds = duration,
                    uri = audioUri,
                    artworkUri = coverFile?.let(::uri),
                    lyricsUri = lyricFile?.let(::uri),
                    folder = file.key.ifBlank { "所选文件夹" },
                    addedAt = file.modified,
                    format = file.name.substringAfterLast('.', "").uppercase(Locale.ROOT),
                )
            }.sortedBy { it.title.lowercase(Locale.ROOT) }.toList()
        return ScanResult(
            songs.filter { it.durationSeconds >= minDurationSeconds.coerceAtLeast(0) },
            artistPictures,
            "所选音乐文件夹",
        )
    }

    private fun cleanMetadata(raw: String?): String? = raw?.trim()
        ?.takeUnless { it.isBlank() || it.equals("<unknown>", true) || it.equals("unknown", true) }
}
