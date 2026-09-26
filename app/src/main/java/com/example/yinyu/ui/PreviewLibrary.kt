package com.example.yinyu.ui

import android.net.Uri
import androidx.annotation.DrawableRes
import com.example.yinyu.R
import java.util.Locale

/** A phone-local audio item. The drawable is used only when the file has no artwork. */
internal data class PreviewSong(
    val title: String,
    val artist: String,
    val album: String,
    @DrawableRes val cover: Int,
    val durationSeconds: Int,
    val uri: Uri? = null,
    val artworkUri: Uri? = null,
    val lyricsUri: Uri? = null,
    val folder: String = "",
    val addedAt: Long = 0L,
    val format: String = "",
) {
    /** Stable across MediaStore and a selected document tree for the same tagged recording. */
    val storageId: String get() = listOf(artist, album, title, durationSeconds.toString())
        .joinToString("\u001F") { it.trim().lowercase(Locale.ROOT) }
}

internal val emptySong = PreviewSong("暂无歌曲", "请选择音乐文件夹", "本地音乐", R.drawable.other_artist, 0)

internal data class PreviewArtist(
    val name: String,
    val count: Int,
    @DrawableRes val portrait: Int = R.drawable.other_artist,
    val portraitUri: Uri? = null,
)
