package com.example.yinyu.ui

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.yinyu.data.MusicCatalog
import com.example.yinyu.data.MusicDatabase
import com.example.yinyu.data.SavedPlaylist
import com.example.yinyu.playback.MusicPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

/** Compose-facing state. Playback is owned by MusicPlaybackService, never by an Activity. */
internal class MusicAppController(context: Context) : Player.Listener {
    private val appContext = context.applicationContext
    private val catalog = MusicCatalog(appContext)
    private val database = MusicDatabase(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainExecutor = Executor { Handler(Looper.getMainLooper()).post(it) }
    private var future: ListenableFuture<MediaController>? = null
    private var player: MediaController? = null
    private var pendingQueue: Pair<List<PreviewSong>, Int>? = null

    var songs by mutableStateOf<List<PreviewSong>>(emptyList())
        private set
    var artistPictures by mutableStateOf<Map<String, Uri>>(emptyMap())
        private set
    var sourceName by mutableStateOf("手机媒体库")
        private set
    var scanning by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var ready by mutableStateOf(false)
        private set
    var currentIndex by mutableIntStateOf(-1)
        private set
    var queueSongs by mutableStateOf<List<PreviewSong>>(emptyList())
        private set
    var currentQueueIndex by mutableIntStateOf(-1)
        private set
    var playing by mutableStateOf(false)
        private set
    var positionMs by mutableStateOf(0L)
        private set
    var durationMs by mutableStateOf(0L)
        private set
    var shuffle by mutableStateOf(false)
        private set
    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_OFF)
        private set
    var favorites by mutableStateOf<Set<String>>(emptySet())
        private set
    var playlists by mutableStateOf<List<SavedPlaylist>>(emptyList())
        private set
    var recentIds by mutableStateOf<List<String>>(emptyList())
        private set

    val currentSong: PreviewSong?
        get() = songs.getOrNull(currentIndex)
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val isFavorite: Boolean
        get() = currentSong?.let { it.storageId in favorites || it.uri?.toString() in favorites } ?: false
    val hasMediaPermission: Boolean
        get() = catalog.hasMediaPermission()
    val selectedTree: Uri?
        get() = catalog.selectedTree()

    init {
        scope.launch {
            favorites = withContext(Dispatchers.IO) { database.favoriteIds() }
            playlists = withContext(Dispatchers.IO) { database.playlists() }
            recentIds = withContext(Dispatchers.IO) { database.recentIds() }
        }
        val token = SessionToken(appContext, ComponentName(appContext, MusicPlaybackService::class.java))
        future = MediaController.Builder(appContext, token).buildAsync().also { connection ->
            connection.addListener({
                try {
                    player = connection.get().also { it.addListener(this) }
                    ready = true
                    updateFromPlayer()
                    pendingQueue?.let { (items, index) -> pendingQueue = null; playQueue(items, index) }
                } catch (exception: Exception) {
                    error = "播放服务连接失败：${exception.localizedMessage.orEmpty()}"
                }
            }, mainExecutor)
        }
        scope.launch {
            while (true) {
                delay(250)
                val controller = player ?: continue
                positionMs = controller.currentPosition.coerceAtLeast(0L)
                durationMs = controller.duration.takeIf { it > 0L }
                    ?: currentSong?.durationSeconds?.times(1_000L) ?: 0L
            }
        }
    }

    fun close() {
        player?.removeListener(this)
        future?.let(MediaController::releaseFuture)
        scope.cancel()
        database.close()
    }

    fun scan() {
        scope.launch {
            scanning = true
            error = null
            try {
                val result = catalog.scan()
                songs = result.songs
                artistPictures = result.artistPictures
                sourceName = result.sourceName
                val mediaId = player?.currentMediaItem?.mediaId
                currentIndex = songs.indexOfFirst { it.uri?.toString() == mediaId }
                    .takeIf { it >= 0 } ?: if (songs.isNotEmpty()) 0 else -1
                val connected = player
                queueSongs = if (connected != null && connected.mediaItemCount > 0) {
                    (0 until connected.mediaItemCount).mapNotNull { index ->
                        val id = connected.getMediaItemAt(index).mediaId
                        songs.firstOrNull { it.uri?.toString() == id }
                    }
                } else songs
                updateFromPlayer()
            } catch (exception: Exception) {
                error = "扫描失败：${exception.localizedMessage.orEmpty()}"
            } finally {
                scanning = false
            }
        }
    }

    fun chooseTree(uri: Uri) {
        try {
            catalog.useTree(uri)
            player?.stop()
            player?.clearMediaItems()
            scan()
        } catch (exception: Exception) {
            error = "无法读取所选文件夹：${exception.localizedMessage.orEmpty()}"
        }
    }

    fun useMediaStore() {
        catalog.useMediaStore()
        player?.stop()
        player?.clearMediaItems()
        scan()
    }

    fun dismissError() { error = null }

    fun play(index: Int) = playQueue(songs, index)

    fun playQueue(selectedSongs: List<PreviewSong>, index: Int) {
        val song = selectedSongs.getOrNull(index) ?: return
        val uri = song.uri ?: return
        val controller = player ?: run { pendingQueue = selectedSongs to index; return }
        val validSongs = selectedSongs.filter { it.uri != null }
        val queueIndex = validSongs.indexOfFirst { it.uri == uri }
        val items = validSongs.mapNotNull { item -> item.uri?.let { item.toMediaItem(it) } }
        if (items.isEmpty()) return
        controller.setMediaItems(items, queueIndex.coerceAtLeast(0), 0L)
        controller.prepare()
        controller.play()
        queueSongs = validSongs
        currentQueueIndex = queueIndex
        currentIndex = songs.indexOfFirst { it.uri == uri }
        error = null
    }

    fun playFromQueue(index: Int) {
        val controller = player
        if (controller != null && index in 0 until controller.mediaItemCount) {
            controller.seekToDefaultPosition(index)
            controller.play()
        } else playQueue(queueSongs, index)
    }

    fun togglePlayback() {
        val controller = player ?: return
        if (controller.isPlaying) controller.pause()
        else if (controller.currentMediaItem == null) play(currentIndex.coerceAtLeast(0))
        else controller.play()
    }

    fun previous() {
        player?.seekToPrevious()
    }

    fun next() {
        player?.seekToNext()
    }

    fun seekTo(fraction: Float) {
        val duration = durationMs.takeIf { it > 0L } ?: return
        player?.seekTo((duration * fraction.coerceIn(0f, 1f)).toLong())
    }

    fun toggleShuffle() {
        player?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled; shuffle = it.shuffleModeEnabled }
    }

    fun cycleRepeat() {
        player?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            repeatMode = it.repeatMode
        }
    }

    fun toggleFavorite() {
        val song = currentSong ?: return
        val id = if (song.uri?.toString() in favorites) song.uri.toString() else song.storageId
        scope.launch {
            withContext(Dispatchers.IO) { database.toggleFavorite(id) }
            favorites = withContext(Dispatchers.IO) { database.favoriteIds() }
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        scope.launch {
            try {
                withContext(Dispatchers.IO) { database.createPlaylist(name) }
                playlists = withContext(Dispatchers.IO) { database.playlists() }
            } catch (_: Exception) {
                error = "歌单名称已存在"
            }
        }
    }

    fun addCurrentToPlaylist(id: Long) {
        val songId = currentSong?.storageId ?: return
        scope.launch {
            withContext(Dispatchers.IO) { database.addToPlaylist(id, songId) }
            playlists = withContext(Dispatchers.IO) { database.playlists() }
        }
    }

    fun deletePlaylist(id: Long) {
        scope.launch {
            withContext(Dispatchers.IO) { database.deletePlaylist(id) }
            playlists = withContext(Dispatchers.IO) { database.playlists() }
        }
    }

    fun removeFromPlaylist(playlistId: Long, songId: String) {
        scope.launch {
            withContext(Dispatchers.IO) { database.removeFromPlaylist(playlistId, songId) }
            playlists = withContext(Dispatchers.IO) { database.playlists() }
        }
    }

    override fun onEvents(player: Player, events: Player.Events) = updateFromPlayer()

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        mediaItem?.mediaId?.let { id ->
            scope.launch {
                val stableId = songs.firstOrNull { it.uri?.toString() == id }?.storageId ?: id
                withContext(Dispatchers.IO) { database.markPlayed(stableId) }
                recentIds = withContext(Dispatchers.IO) { database.recentIds() }
            }
        }
        updateFromPlayer()
    }

    override fun onPlayerError(exception: PlaybackException) {
        error = "无法播放此文件：${exception.errorCodeName}"
    }

    private fun updateFromPlayer() {
        val controller = player ?: return
        playing = controller.isPlaying
        shuffle = controller.shuffleModeEnabled
        repeatMode = controller.repeatMode
        if (controller.mediaItemCount > 0 && songs.isNotEmpty()) {
            val actualQueue = (0 until controller.mediaItemCount).mapNotNull { queueIndex ->
                val queueId = controller.getMediaItemAt(queueIndex).mediaId
                songs.firstOrNull { it.uri?.toString() == queueId }
            }
            if (actualQueue != queueSongs) queueSongs = actualQueue
        } else if (queueSongs.isEmpty() && songs.isNotEmpty()) queueSongs = songs
        val id = controller.currentMediaItem?.mediaId
        val index = songs.indexOfFirst { it.uri?.toString() == id }
        if (index >= 0) currentIndex = index
        currentQueueIndex = controller.currentMediaItemIndex
        positionMs = controller.currentPosition.coerceAtLeast(0L)
        durationMs = controller.duration.takeIf { it > 0L }
            ?: currentSong?.durationSeconds?.times(1_000L) ?: 0L
    }

    private fun PreviewSong.toMediaItem(uri: Uri): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .apply { artworkUri?.let(::setArtworkUri) }
            .build()
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(uri.toString())
            .setMediaMetadata(metadata)
            .build()
    }
}
