package com.example.yinyu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yinyu.ui.theme.YinColors

@Composable
internal fun FullScreenPlayer(
    song: PreviewSong,
    progress: Float,
    positionMs: Long,
    playing: Boolean,
    favorite: Boolean,
    shuffle: Boolean,
    repeatMode: Int,
    transition: Float,
    onCollapse: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onFavorite: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onLyrics: () -> Unit,
    onQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberArtworkPalette(song)
    val t = transition.coerceIn(0f, 1f)
    BoxWithConstraints(
        modifier.graphicsLayer {
            alpha = t
            val scale = .975f + .025f * t
            scaleX = scale
            scaleY = scale
        }.pointerInput(onCollapse) {
            var distance = 0f
            detectVerticalDragGestures(
                onVerticalDrag = { change, amount -> change.consume(); distance += amount },
                onDragEnd = { if (distance > 96f) onCollapse(); distance = 0f },
                onDragCancel = { distance = 0f },
            )
        },
    ) {
        val compact = maxHeight.value / maxWidth.value < 1.9f
        val coverSize = minOf(maxWidth - 56.dp, maxHeight * if (compact) .36f else .40f)
            .coerceIn(205.dp, 365.dp)

        ArtworkAmbientBackground(palette, Modifier.fillMaxSize())

        Column(
            Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 25.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "NOW PLAYING  ·  本地曲库",
                color = YinColors.muted.copy(alpha = .86f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.3.sp,
                modifier = Modifier.height(32.dp),
            )

            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(coverSize + 12.dp)
                        .shadow(25.dp, RoundedCornerShape(27.dp), ambientColor = palette.primary.copy(alpha = .19f), spotColor = Color.Black.copy(alpha = .20f))
                        .clip(RoundedCornerShape(27.dp))
                        .background(YinColors.surface)
                        .border(1.dp, Color.White.copy(alpha = if (YinColors.isLight) .74f else .10f), RoundedCornerShape(27.dp))
                        .padding(6.dp),
                ) {
                    SongArtwork(song, Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)), "${song.album} 专辑封面")
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = YinColors.text, fontSize = if (compact) 24.sp else 27.sp,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(song.artist, color = YinColors.muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onFavorite, modifier = Modifier.size(48.dp)) {
                    Icon(
                        if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (favorite) "取消收藏" else "收藏",
                        tint = if (favorite) YinColors.lavender else YinColors.muted,
                        modifier = Modifier.size(23.dp),
                    )
                }
            }

            Spacer(Modifier.height(if (compact) 12.dp else 19.dp))
            ArtworkWavySeekBar(progress, palette.primary, playing, onSeek, Modifier.fillMaxWidth().height(27.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatPocketTime((positionMs / 1_000L).toInt()), color = YinColors.muted, fontSize = 11.sp)
                Text(formatPocketTime(song.durationSeconds), color = YinColors.muted, fontSize = 11.sp)
            }

            Spacer(Modifier.height(if (compact) 7.dp else 13.dp))
            Row(
                Modifier.fillMaxWidth().height(76.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlaybackModeButton(Icons.Default.Shuffle, "随机播放", shuffle, onShuffle)
                PlayerIconButton(Icons.Default.SkipPrevious, "上一首", onPrevious, 31.dp)
                PrimaryPlayButton(playing, onToggle, compact)
                PlayerIconButton(Icons.Default.SkipNext, "下一首", onNext, 31.dp)
                PlaybackModeButton(
                    if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    repeatDescription(repeatMode),
                    repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF,
                    onRepeat,
                )
            }

            Row(
                Modifier.fillMaxWidth().height(45.dp).padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayerTextAction(Icons.Default.FormatQuote, "歌词", onLyrics)
                Box(Modifier.padding(horizontal = 14.dp).size(3.dp).clip(CircleShape).background(YinColors.muted.copy(alpha = .55f)))
                PlayerTextAction(Icons.Default.QueueMusic, "播放队列", onQueue)
                Box(Modifier.padding(horizontal = 14.dp).size(3.dp).clip(CircleShape).background(YinColors.muted.copy(alpha = .55f)))
                PlayerTextAction(Icons.Default.PlaylistAdd, "加入歌单", onAddToPlaylist)
            }
        }
    }
}

@Composable
internal fun ArtworkAmbientBackground(palette: ArtworkPalette, modifier: Modifier = Modifier) {
    val background = YinColors.background
    val isLight = YinColors.isLight
    Canvas(modifier.background(YinColors.background)) {
        val primaryRadius = size.maxDimension * .72f
        val secondaryRadius = size.maxDimension * .58f
        val primaryCenter = Offset(size.width * .12f, size.height * .48f)
        val secondaryCenter = Offset(size.width * .94f, size.height * .72f)
        drawCircle(
            Brush.radialGradient(
                listOf(
                    palette.primary.copy(alpha = if (isLight) .38f else .48f),
                    palette.primary.copy(alpha = if (isLight) .21f else .29f),
                    Color.Transparent,
                ),
                center = primaryCenter,
                radius = primaryRadius,
            ),
            radius = primaryRadius,
            center = primaryCenter,
        )
        drawCircle(
            Brush.radialGradient(
                listOf(
                    palette.secondary.copy(alpha = if (isLight) .28f else .36f),
                    palette.secondary.copy(alpha = if (isLight) .12f else .20f),
                    Color.Transparent,
                ),
                center = secondaryCenter,
                radius = secondaryRadius,
            ),
            radius = secondaryRadius,
            center = secondaryCenter,
        )
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, background.copy(alpha = if (isLight) .05f else .20f))))
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ArtworkWavySeekBar(progress: Float, accent: Color, playing: Boolean, onSeek: (Float) -> Unit, modifier: Modifier = Modifier) {
    val amplitude by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        label = "playerWaveAmplitude",
    )
    val progressAnimation by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1_000, easing = LinearEasing),
        label = "playerProgress",
    )
    val isLight = YinColors.isLight
    val trackColor = if (isLight) accent.copy(alpha = .16f) else Color.White.copy(alpha = .22f)
    val sideInsetPx = with(LocalDensity.current) { 16.dp.toPx() }

    Box(
        modifier = modifier.height(28.dp)
            .pointerInput(onSeek, sideInsetPx) {
                detectTapGestures { position ->
                    val trackWidth = (size.width - sideInsetPx * 2f).coerceAtLeast(1f)
                    onSeek(((position.x - sideInsetPx) / trackWidth).coerceIn(0f, 1f))
                }
            }
            .pointerInput(onSeek, sideInsetPx) {
                detectHorizontalDragGestures { change, _ ->
                    val trackWidth = (size.width - sideInsetPx * 2f).coerceAtLeast(1f)
                    onSeek(((change.position.x - sideInsetPx) / trackWidth).coerceIn(0f, 1f))
                    change.consume()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        LinearWavyProgressIndicator(
            progress = { progressAnimation },
            trackColor = trackColor,
            amplitude = { amplitude },
            stopSize = 0.dp,
            waveSpeed = 15.dp,
            color = accent,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}

@Composable
private fun PlaybackModeButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, active: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(icon, description, tint = if (active) YinColors.lavender else YinColors.muted, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun PlayerIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit, size: Dp) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(icon, description, tint = YinColors.text, modifier = Modifier.size(size))
    }
}

@Composable
private fun PrimaryPlayButton(playing: Boolean, onClick: () -> Unit, compact: Boolean) {
    val playInk = if (YinColors.isLight) Color.White else YinColors.background
    Box(
        Modifier.size(if (compact) 66.dp else 72.dp)
            .shadow(13.dp, CircleShape, ambientColor = YinColors.lavender.copy(alpha = .23f))
            .clip(CircleShape)
            .background(YinColors.lavender)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (playing) {
                val barW = size.width * .085f
                val barH = size.height * .31f
                drawRoundRect(playInk, Offset(size.width * .39f, size.height * .5f - barH / 2), Size(barW, barH), androidx.compose.ui.geometry.CornerRadius(barW))
                drawRoundRect(playInk, Offset(size.width * .53f, size.height * .5f - barH / 2), Size(barW, barH), androidx.compose.ui.geometry.CornerRadius(barW))
            } else {
                val path = Path().apply {
                    moveTo(size.width * .40f, size.height * .34f)
                    lineTo(size.width * .67f, size.height * .49f)
                    quadraticTo(size.width * .71f, size.height * .52f, size.width * .67f, size.height * .54f)
                    lineTo(size.width * .40f, size.height * .68f)
                    close()
                }
                drawPath(path, playInk)
            }
        }
    }
}

@Composable
private fun PlayerTextAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.clip(CircleShape).clickable(onClick = onClick).padding(horizontal = 7.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = YinColors.muted, modifier = Modifier.size(17.dp))
        Text(label, color = YinColors.muted, fontSize = 11.sp, modifier = Modifier.padding(start = 5.dp))
    }
}

private fun repeatDescription(mode: Int): String = when (mode) {
    androidx.media3.common.Player.REPEAT_MODE_ONE -> "单曲循环"
    androidx.media3.common.Player.REPEAT_MODE_ALL -> "列表循环"
    else -> "循环关闭"
}
