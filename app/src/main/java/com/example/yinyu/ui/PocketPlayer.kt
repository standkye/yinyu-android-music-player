package com.example.yinyu.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.yinyu.ui.theme.YinColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

@Composable
private fun Modifier.quietClickable(onClick: () -> Unit): Modifier =
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)

@Composable
internal fun PocketDock(
    song: PreviewSong,
    progress: Float,
    playing: Boolean,
    page: Int,
    hazeState: HazeState,
    onPage: (Int) -> Unit,
    onToggle: () -> Unit,
    onExpand: () -> Unit,
    onFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pull = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val narrow = configuration.screenWidthDp < 350
    val shortScreen = configuration.screenHeightDp.toFloat() / configuration.screenWidthDp < 1.9f
    val tabWidth = if (narrow) 36.dp else 40.dp
    val albumSize = if (narrow) 47.dp else 54.dp
    val dockTopGlow = if (YinColors.isLight) Color.White.copy(alpha = .58f) else Color.White.copy(alpha = .085f)
    val dockBottomShade = if (YinColors.isLight) YinColors.text.copy(alpha = .035f) else Color.Black.copy(alpha = .09f)
    val dockLine = YinColors.outline
    val mutedTrack = YinColors.muted.copy(alpha = .42f)
    val shape = DockSeamShape((-pull.value).coerceIn(0f, 1f))
    Box(
        modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 3.dp).height(if (shortScreen) 85.dp else 90.dp)
            .shadow(17.dp, shape, ambientColor = Color.Black.copy(alpha = .15f), spotColor = Color.Black.copy(alpha = .42f))
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = YinColors.background,
                    tint = HazeTint(YinColors.lavender.copy(alpha = if (YinColors.isLight) .09f else .16f)),
                    blurRadius = 34.dp,
                    noiseFactor = .12f,
                    fallbackTint = HazeTint(YinColors.surface.copy(alpha = .92f)),
                ),
            )
            .background(Brush.verticalGradient(listOf(
                YinColors.surface.copy(alpha = if (YinColors.isLight) .72f else .45f),
                YinColors.surface.copy(alpha = if (YinColors.isLight) .84f else .34f),
                YinColors.surface.copy(alpha = if (YinColors.isLight) .78f else .48f),
            )))
            .border(.8.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = .36f), dockLine, YinColors.text.copy(alpha = .16f))), shape)
            .pointerInput(onExpand) {
                var distance = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        distance += amount
                        scope.launch { pull.snapTo((distance / 220f).coerceIn(-1f, 1f)) }
                    },
                    onDragEnd = {
                        if (distance < -65f) onFullScreen()
                        distance = 0f
                        scope.launch { pull.animateTo(0f, spring(dampingRatio = .48f, stiffness = 280f)) }
                    },
                    onDragCancel = {
                        distance = 0f
                        scope.launch { pull.animateTo(0f, spring(dampingRatio = .48f, stiffness = 280f)) }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(
                0f to dockTopGlow,
                .24f to Color.White.copy(alpha = .018f),
                .74f to Color.Transparent,
                1f to dockBottomShade,
            ))
            drawLine(
                brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .16f), Color.Transparent)),
                start = Offset(size.width * .14f, 7.5.dp.toPx()),
                end = Offset(size.width * .86f, 7.5.dp.toPx()),
                strokeWidth = .6.dp.toPx(),
            )
        }
        Row(Modifier.fillMaxWidth().offset(y = 4.dp).padding(start = if (narrow) 6.dp else 8.dp, end = if (narrow) 8.dp else 13.dp), verticalAlignment = Alignment.CenterVertically) {
            val tabs = listOf(Triple("首页", Icons.Default.Home, 0), Triple("曲库", Icons.Default.MusicNote, 1), Triple("歌单", Icons.Default.QueueMusic, 2))
            tabs.forEach { (label, icon, index) ->
                Column(
                    Modifier.width(tabWidth).offset(y = 3.dp).quietClickable { onPage(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val iconTint = if (page == index) YinColors.lavender else YinColors.muted
                    when (index) {
                        1 -> DoubleNoteIcon(iconTint, Modifier.size(20.dp))
                        2 -> QueueLinesIcon(iconTint, Modifier.size(20.dp))
                        else -> DockHomeIcon(iconTint, Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(label, color = if (page == index) YinColors.lavender else YinColors.muted, fontSize = 10.sp, fontWeight = if (page == index) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
            Spacer(Modifier.width(if (narrow) 5.dp else 8.dp))
            Box(
                Modifier.size(albumSize)
                    .shadow(7.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .45f), spotColor = Color.Black.copy(alpha = .68f))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(YinColors.elevated, YinColors.surface, YinColors.elevated)))
                    .quietClickable(onExpand),
                contentAlignment = Alignment.Center,
            ) {
                SongArtwork(song, Modifier.size(albumSize - 7.dp).clip(CircleShape), "展开播放页")
                val accent = YinColors.lavender
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(mutedTrack, -90f, 360f, false,
                        style = Stroke(width = 2.5.dp.toPx()))
                    drawArc(accent, -90f, progress.coerceIn(0f, 1f) * 360f, false,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            Spacer(Modifier.width(if (narrow) 6.dp else 8.dp))
            Column(Modifier.weight(1f).quietClickable(onExpand)) {
                Text(song.title, color = YinColors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, color = YinColors.muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(4.dp))
            LavenderPlayButton(if (narrow) 36.dp else 38.dp, playing, onToggle)
        }
    }
}

@Composable
internal fun PocketTransportButton(
    icon: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(Modifier.size(44.dp).clip(CircleShape).quietClickable(onClick), contentAlignment = Alignment.Center) {
        Icon(icon, description, tint = tint, modifier = Modifier.size(23.dp))
    }
}

@Composable
internal fun PocketPrimaryPlayButton(playing: Boolean, onClick: () -> Unit) {
    val playInk = if (YinColors.isLight) Color.White else YinColors.background
    Box(
        Modifier.size(62.dp).shadow(12.dp, CircleShape, ambientColor = YinColors.lavender.copy(alpha = .18f))
            .clip(CircleShape)
            .background(YinColors.lavender)
            .border(1.dp, Color.White.copy(alpha = .42f), CircleShape)
            .quietClickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
            if (playing) "暂停" else "播放",
            tint = playInk,
            modifier = Modifier.size(31.dp),
        )
    }
}

@Composable
internal fun LavenderPlayButton(diameter: Dp, playing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val playInk = if (YinColors.isLight) Color.White else YinColors.background
    Box(
        modifier.size(diameter)
            .shadow(7.dp, CircleShape, ambientColor = YinColors.lavender.copy(alpha = .24f))
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(YinColors.lavender.copy(alpha = .88f), YinColors.lavender, YinColors.lavenderDeep)))
            .border(.65.dp, Color.White.copy(alpha = .28f), CircleShape)
            .quietClickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = .20f), Color.Transparent),
                    center = Offset(size.width * .27f, size.height * .20f),
                    radius = size.width * .68f,
                ),
                radius = size.width * .68f,
                center = Offset(size.width * .27f, size.height * .20f),
            )
            val ink = playInk
            if (playing) {
                val barWidth = size.width * .075f
                val barHeight = size.height * .28f
                drawRoundRect(ink, Offset(size.width * .40f, size.height * .5f - barHeight * .5f),
                    Size(barWidth, barHeight), CornerRadius(barWidth * .42f))
                drawRoundRect(ink, Offset(size.width * .525f, size.height * .5f - barHeight * .5f),
                    Size(barWidth, barHeight), CornerRadius(barWidth * .42f))
            } else {
                val play = Path().apply {
                    moveTo(size.width * .40f, size.height * .35f)
                    quadraticTo(size.width * .40f, size.height * .32f, size.width * .44f, size.height * .34f)
                    lineTo(size.width * .67f, size.height * .48f)
                    quadraticTo(size.width * .70f, size.height * .50f, size.width * .67f, size.height * .52f)
                    lineTo(size.width * .44f, size.height * .66f)
                    quadraticTo(size.width * .40f, size.height * .68f, size.width * .40f, size.height * .65f)
                    close()
                }
                drawPath(play, ink)
            }
        }
    }
}

@Composable
private fun DoubleNoteIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val beam = Path().apply {
            moveTo(w * .37f, h * .20f)
            lineTo(w * .85f, h * .11f)
            lineTo(w * .85f, h * .26f)
            lineTo(w * .37f, h * .35f)
            close()
        }
        drawPath(beam, tint)
        drawLine(tint, Offset(w * .37f, h * .28f), Offset(w * .37f, h * .73f), strokeWidth = w * .105f)
        drawLine(tint, Offset(w * .85f, h * .18f), Offset(w * .85f, h * .64f), strokeWidth = w * .105f)
        drawOval(tint, topLeft = Offset(w * .10f, h * .67f), size = Size(w * .32f, h * .22f))
        drawOval(tint, topLeft = Offset(w * .58f, h * .57f), size = Size(w * .32f, h * .22f))
    }
}

@Composable
private fun DockHomeIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val roof = Path().apply {
            moveTo(w * .08f, h * .45f)
            quadraticTo(w * .06f, h * .41f, w * .11f, h * .37f)
            lineTo(w * .45f, h * .08f)
            quadraticTo(w * .50f, h * .04f, w * .55f, h * .08f)
            lineTo(w * .89f, h * .37f)
            quadraticTo(w * .94f, h * .41f, w * .92f, h * .45f)
            lineTo(w * .82f, h * .52f)
            lineTo(w * .82f, h * .86f)
            quadraticTo(w * .82f, h * .91f, w * .77f, h * .91f)
            lineTo(w * .61f, h * .91f)
            lineTo(w * .61f, h * .65f)
            quadraticTo(w * .61f, h * .60f, w * .56f, h * .60f)
            lineTo(w * .44f, h * .60f)
            quadraticTo(w * .39f, h * .60f, w * .39f, h * .65f)
            lineTo(w * .39f, h * .91f)
            lineTo(w * .23f, h * .91f)
            quadraticTo(w * .18f, h * .91f, w * .18f, h * .86f)
            lineTo(w * .18f, h * .52f)
            close()
        }
        drawPath(roof, tint)
    }
}

@Composable
private fun QueueLinesIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val line = 1.8.dp.toPx()
        drawLine(tint, Offset(w * .10f, h * .22f), Offset(w * .62f, h * .22f), strokeWidth = line, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * .10f, h * .47f), Offset(w * .55f, h * .47f), strokeWidth = line, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * .10f, h * .72f), Offset(w * .53f, h * .72f), strokeWidth = line, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * .82f, h * .09f), Offset(w * .82f, h * .37f), strokeWidth = line * .9f, cap = StrokeCap.Round)
        drawOval(tint, topLeft = Offset(w * .65f, h * .33f), size = Size(w * .20f, h * .13f))
    }
}

@Composable
internal fun PocketSeekBar(progress: Float, onSeek: (Float) -> Unit, modifier: Modifier = Modifier) {
    val accent = YinColors.lavender
    val track = YinColors.muted.copy(alpha = if (YinColors.isLight) .28f else .45f)
    Canvas(
        modifier.height(21.dp)
            .pointerInput(onSeek) { detectTapGestures { onSeek((it.x / size.width).coerceIn(0f, 1f)) } }
            .pointerInput(onSeek) { detectHorizontalDragGestures { change, _ ->
                onSeek((change.position.x / size.width).coerceIn(0f, 1f))
                change.consume()
            } },
    ) {
        val bar = 3.dp.toPx()
        val y = size.height / 2f - bar / 2f
        val p = progress.coerceIn(0f, 1f)
        drawRoundRect(track, Offset(0f, y), Size(size.width, bar), CornerRadius(bar))
        drawRoundRect(accent, Offset(0f, y), Size(size.width * p, bar), CornerRadius(bar))
        drawCircle(accent, radius = 6.dp.toPx(), center = Offset((size.width * p).coerceIn(6.dp.toPx(), size.width - 6.dp.toPx()), size.height / 2f))
    }
}

internal fun formatPocketTime(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
