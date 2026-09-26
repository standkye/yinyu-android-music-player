package com.example.yinyu.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.yinyu.ui.theme.YinColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.Charset

internal data class LyricLine(val atMs: Long, val text: String)

private val timeTag = Regex("\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?]")

private fun decodeLyrics(bytes: ByteArray): String {
    if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte())
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
    if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte())
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
    val offset = if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() &&
        bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) 3 else 0
    return try {
        Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes, offset, bytes.size - offset)).toString()
    } catch (_: Exception) {
        String(bytes, offset, bytes.size - offset, Charset.forName("GB18030"))
    }
}

private fun parseLyrics(content: String): List<LyricLine> = buildList {
    content.lineSequence().forEach { raw ->
        val matches = timeTag.findAll(raw).toList()
        val text = timeTag.replace(raw, "").trim()
        if (matches.isEmpty() || text.isEmpty()) return@forEach
        matches.forEach { match ->
            val fraction = match.groupValues[3]
            val milliseconds = when (fraction.length) {
                1 -> fraction.toLong() * 100
                2 -> fraction.toLong() * 10
                else -> fraction.take(3).toLongOrNull() ?: 0L
            }
            add(LyricLine(match.groupValues[1].toLong() * 60_000 +
                match.groupValues[2].toLong() * 1_000 + milliseconds, text))
        }
    }
}.sortedBy { it.atMs }

@Composable
private fun rememberLyrics(song: PreviewSong): List<LyricLine> {
    val context = LocalContext.current
    val lines by produceState(emptyList<LyricLine>(), song.lyricsUri) {
        value = withContext(Dispatchers.IO) {
            try {
                song.lyricsUri?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { parseLyrics(decodeLyrics(it.readBytes())) }
                }.orEmpty()
            } catch (_: Exception) { emptyList() }
        }
    }
    return lines
}

@Composable
internal fun LyricsSnippet(song: PreviewSong, positionMs: Long, compact: Boolean, onClick: () -> Unit) {
    val lines = rememberLyrics(song)
    val current = lines.indexOfLast { it.atMs <= positionMs }.coerceAtLeast(0)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).clickable(onClick = onClick), verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)) {
        if (lines.isEmpty()) {
            Text("本地歌词", color = YinColors.lavender, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(if (song.lyricsUri == null) "此歌曲没有同名歌词文件" else "正在读取歌词…",
                color = YinColors.muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    val forward = targetState >= initialState
                    (slideInVertically(
                        animationSpec = androidx.compose.animation.core.tween(360),
                        initialOffsetY = { if (forward) it / 2 else -it / 2 },
                    ) + fadeIn(androidx.compose.animation.core.tween(260))) togetherWith
                        (slideOutVertically(
                            animationSpec = androidx.compose.animation.core.tween(300),
                            targetOffsetY = { if (forward) -it / 2 else it / 2 },
                        ) + fadeOut(androidx.compose.animation.core.tween(220))) using
                        SizeTransform(clip = false)
                },
                label = "歌词滚动",
            ) { active ->
                Column(verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)) {
                    (active - 1..active + 2).forEach { index ->
                        Text(
                            lines.getOrNull(index)?.text.orEmpty(),
                            color = if (index == active) YinColors.lavender else YinColors.muted,
                            fontSize = if (index == active) 12.sp else 11.sp,
                            fontWeight = if (index == active) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun LyricsOverlay(
    song: PreviewSong,
    positionMs: Long,
    progress: Float,
    playing: Boolean,
    autoFollow: Boolean,
    textScale: Float,
    onClose: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onQueue: () -> Unit,
) {
    val lines = rememberLyrics(song)
    val artworkPalette = rememberArtworkPalette(song)
    val current = lines.indexOfLast { it.atMs <= positionMs }.coerceAtLeast(0)
    val listState = rememberLazyListState()
    LaunchedEffect(current, lines.size, autoFollow) {
        if (autoFollow && lines.isNotEmpty()) {
            listState.animateScrollToItem(current.coerceIn(0, lines.lastIndex), scrollOffset = -360)
        }
    }
    Box(Modifier.fillMaxSize().zIndex(12f)) {
        ArtworkAmbientBackground(artworkPalette, Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(top = 7.dp, bottom = 14.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                LyricsHeaderButton(onClose) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = YinColors.text.copy(alpha = .9f), modifier = Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f).padding(horizontal = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("歌词", color = YinColors.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("本地音乐 · 歌词同步", color = YinColors.muted, fontSize = 10.sp)
                }
                LyricsHeaderButton(onQueue) {
                    Icon(Icons.Default.QueueMusic, "播放队列", tint = YinColors.text.copy(alpha = .9f), modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            if (lines.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("还没有歌词", color = YinColors.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("把同名 .lrc 文件放在歌曲旁边，就能在这里跟随播放显示。",
                            color = YinColors.muted, fontSize = 13.sp, lineHeight = 21.sp)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 10.dp),
                    contentPadding = PaddingValues(horizontal = 27.dp, vertical = 105.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    itemsIndexed(lines) { index, line ->
                        val active = index == current
                        val emphasis by animateFloatAsState(when {
                            active -> 1f
                            kotlin.math.abs(index - current) == 1 -> .68f
                            else -> .40f
                        },
                            spring(dampingRatio = .82f, stiffness = 240f), label = "lyric-emphasis")
                        Text(line.text,
                            color = if (active) YinColors.text else YinColors.muted.copy(alpha = emphasis),
                            fontSize = (if (active) 25f else 18f).times(textScale).sp,
                            lineHeight = (if (active) 34f else 27f).times(textScale).sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().animateContentSize(spring(dampingRatio = .82f, stiffness = 260f))
                                .clickable {
                                    val duration = song.durationSeconds * 1_000L
                                    if (duration > 0) onSeek((line.atMs.toFloat() / duration).coerceIn(0f, 1f))
                                }
                                .padding(vertical = if (active) 10.dp else 8.dp))
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                PocketSeekBar(progress, onSeek, Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatPocketTime((song.durationSeconds * progress).toInt()), color = YinColors.muted, fontSize = 10.sp)
                    Text(formatPocketTime(song.durationSeconds), color = YinColors.muted, fontSize = 10.sp)
                }
                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    PocketTransportButton(Icons.Default.SkipPrevious, "上一首", YinColors.text, onPrevious)
                    Spacer(Modifier.width(28.dp))
                    PocketPrimaryPlayButton(playing, onToggle)
                    Spacer(Modifier.width(28.dp))
                    PocketTransportButton(Icons.Default.SkipNext, "下一首", YinColors.text, onNext)
                }
            }
        }
    }
}

@Composable
private fun LyricsHeaderButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier.size(44.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
