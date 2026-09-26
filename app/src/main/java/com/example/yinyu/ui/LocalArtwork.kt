package com.example.yinyu.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.yinyu.ui.theme.YinColors

internal data class ArtworkPalette(val primary: Color, val secondary: Color)

private val artworkCache = object : LruCache<String, Bitmap>(16 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
}

@Composable
internal fun SongArtwork(
    song: PreviewSong,
    modifier: Modifier = Modifier,
    description: String? = null,
    scale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val key = song.artworkUri?.toString() ?: song.uri?.toString()
    // Reuse the already decoded cover on the first transition frame.
    val cached = remember(key) { key?.let(artworkCache::get) }
    val bitmap by produceState<Bitmap?>(cached, key) {
        value = if (key == null) null else withContext(Dispatchers.IO) {
            artworkCache.get(key) ?: loadArtwork(context, song.artworkUri, song.uri)?.also {
                artworkCache.put(key, it)
            }
        }
    }
    if (bitmap == null) {
        Image(painterResource(song.cover), description, modifier, contentScale = scale)
    } else {
        Image(bitmap!!.asImageBitmap(), description, modifier, contentScale = scale)
    }
}

@Composable
internal fun ArtistArtwork(artist: PreviewArtist, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val key = artist.portraitUri?.toString()
    val bitmap by produceState<Bitmap?>(null, key) {
        value = if (key == null) null else withContext(Dispatchers.IO) {
            artworkCache.get(key) ?: loadArtwork(context, artist.portraitUri, null)?.also {
                artworkCache.put(key, it)
            }
        }
    }
    if (bitmap == null) Image(painterResource(artist.portrait), "${artist.name} 歌手图片", modifier, contentScale = ContentScale.Crop)
    else Image(bitmap!!.asImageBitmap(), "${artist.name} 歌手图片", modifier, contentScale = ContentScale.Crop)
}

@Composable
internal fun rememberArtworkPalette(song: PreviewSong): ArtworkPalette {
    val context = LocalContext.current
    val key = song.artworkUri?.toString() ?: song.uri?.toString() ?: "resource:${song.cover}"
    val fallback = YinColors.lavender
    val fallbackSecond = YinColors.citron
    val palette by produceState(ArtworkPalette(fallback, fallbackSecond), key, fallback, fallbackSecond) {
        value = withContext(Dispatchers.IO) {
            val bitmap = artworkCache.get(key) ?: if (song.uri != null || song.artworkUri != null) {
                loadArtwork(context, song.artworkUri, song.uri)
            } else {
                BitmapFactory.decodeResource(context.resources, song.cover)
            }
            bitmap?.let(::extractPalette) ?: ArtworkPalette(fallback, fallbackSecond)
        }
    }
    return palette
}

private fun extractPalette(bitmap: Bitmap): ArtworkPalette {
    val bins = Array(24) { HueBin() }
    val stepX = (bitmap.width / 48).coerceAtLeast(1)
    val stepY = (bitmap.height / 48).coerceAtLeast(1)
    val hsv = FloatArray(3)
    var y = 0
    while (y < bitmap.height) {
        var x = 0
        while (x < bitmap.width) {
            val pixel = bitmap.getPixel(x, y)
            if (AndroidColor.alpha(pixel) > 170) {
                AndroidColor.colorToHSV(pixel, hsv)
                val saturation = hsv[1]
                val brightness = hsv[2]
                if (saturation > .18f && brightness in .12f.. .94f) {
                    val index = (hsv[0] / 15f).toInt().coerceIn(0, bins.lastIndex)
                    val weight = saturation * (1f - kotlin.math.abs(brightness - .57f) * .5f)
                    bins[index].add(hsv, weight)
                }
            }
            x += stepX
        }
        y += stepY
    }
    val main = bins.indices.maxByOrNull { bins[it].weight }
    if (main == null || bins[main].weight == 0f) return ArtworkPalette(Color(0xFF8E7BA9), Color(0xFFB7A4D4))
    val other = bins.indices
        .filter { it != main && circularDistance(it, main, bins.size) >= 3 }
        .maxByOrNull { bins[it].weight }
        ?.takeIf { bins[it].weight > bins[main].weight * .10f }
    return ArtworkPalette(bins[main].toComposeColor(), other?.let { bins[it].toComposeColor() } ?: bins[main].toComposeColor().copy(alpha = .78f))
}

private class HueBin {
    var weight = 0f
        private set
    private var hueSin = 0f
    private var hueCos = 0f
    private var saturation = 0f
    private var brightness = 0f

    fun add(hsv: FloatArray, amount: Float) {
        val radians = Math.toRadians(hsv[0].toDouble())
        hueSin += kotlin.math.sin(radians).toFloat() * amount
        hueCos += kotlin.math.cos(radians).toFloat() * amount
        saturation += hsv[1] * amount
        brightness += hsv[2] * amount
        weight += amount
    }

    fun toComposeColor(): Color {
        if (weight == 0f) return Color(0xFF8E7BA9)
        var hue = Math.toDegrees(kotlin.math.atan2(hueSin.toDouble(), hueCos.toDouble())).toFloat()
        if (hue < 0f) hue += 360f
        val hsv = floatArrayOf(hue, (saturation / weight).coerceIn(.30f, .85f), (brightness / weight).coerceIn(.42f, .82f))
        val pixel = AndroidColor.HSVToColor(hsv)
        return Color(
            AndroidColor.red(pixel) / 255f,
            AndroidColor.green(pixel) / 255f,
            AndroidColor.blue(pixel) / 255f,
        )
    }
}

private fun circularDistance(first: Int, second: Int, count: Int): Int {
    val raw = kotlin.math.abs(first - second)
    return minOf(raw, count - raw)
}

internal fun loadArtwork(context: Context, imageUri: Uri?, audioUri: Uri?): Bitmap? {
    if (imageUri != null) {
        try {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                val bytes = stream.readBytes()
                decodeSmall(bytes)?.let { return it }
            }
        } catch (_: Exception) {
            // Embedded artwork is the fallback for songs without an accessible sidecar image.
        }
    }
    if (audioUri == null) return null
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, audioUri)
        retriever.embeddedPicture?.let(::decodeSmall)
    } catch (_: Exception) {
        null
    } finally {
        retriever.release()
    }
}

private fun decodeSmall(data: ByteArray): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
    var sample = 1
    while (bounds.outWidth / sample > 720 || bounds.outHeight / sample > 720) sample *= 2
    return BitmapFactory.decodeByteArray(data, 0, data.size, BitmapFactory.Options().apply { inSampleSize = sample })
}
