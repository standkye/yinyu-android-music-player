package com.example.yinyu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class YinAccent(val title: String, val color: Color, val secondary: Color) {
    LAVENDER("雾紫", Color(0xFFBCA4EA), Color(0xFFE2D2A4)),
    SUNSET("日落金", Color(0xFFE9B769), Color(0xFFDB8A73)),
    ICE("冰川蓝", Color(0xFF86C9DA), Color(0xFFACB8F0)),
    NEON("玫瑰粉", Color(0xFFE89ABC), Color(0xFFB4A1EE)),
    FOREST("森林绿", Color(0xFF9AC7A1), Color(0xFFC7D49A)),
}

data class YinPalette(
    val isLight: Boolean,
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accentDeep: Color,
    val secondary: Color,
    val outline: Color,
)

private val LocalYinPalette = staticCompositionLocalOf {
    YinPalette(
        isLight = false,
        background = Color(0xFF17151D),
        surface = Color(0xFF24212B),
        elevated = Color(0xFF302B38),
        text = Color(0xFFF6F2FA),
        muted = Color(0xFFB8B1C1),
        accent = YinAccent.LAVENDER.color,
        accentDeep = Color(0xFF7864A0),
        secondary = YinAccent.LAVENDER.secondary,
        outline = Color.White.copy(alpha = .12f),
    )
}

object YinColors {
    val background: Color @Composable get() = LocalYinPalette.current.background
    val surface: Color @Composable get() = LocalYinPalette.current.surface
    val elevated: Color @Composable get() = LocalYinPalette.current.elevated
    val text: Color @Composable get() = LocalYinPalette.current.text
    val muted: Color @Composable get() = LocalYinPalette.current.muted
    val lavender: Color @Composable get() = LocalYinPalette.current.accent
    val lavenderDeep: Color @Composable get() = LocalYinPalette.current.accentDeep
    val citron: Color @Composable get() = LocalYinPalette.current.secondary
    val outline: Color @Composable get() = LocalYinPalette.current.outline
    val isLight: Boolean @Composable get() = LocalYinPalette.current.isLight
}

@Composable
fun YinYuTheme(
    darkMode: Boolean = true,
    accentIndex: Int = 0,
    blackMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val accent = YinAccent.values().getOrElse(accentIndex.coerceIn(0, YinAccent.values().lastIndex)) { YinAccent.LAVENDER }
    val palette = if (darkMode) {
        YinPalette(
            isLight = false,
            background = if (blackMode) Color.Black else Color(0xFF17151D),
            surface = if (blackMode) Color(0xFF080808) else Color(0xFF24212B),
            elevated = if (blackMode) Color(0xFF151515) else Color(0xFF302B38),
            text = Color(0xFFF6F2FA),
            muted = Color(0xFFB8B1C1),
            accent = accent.color,
            accentDeep = accent.color.copy(red = accent.color.red * .64f, green = accent.color.green * .64f, blue = accent.color.blue * .70f),
            secondary = accent.secondary,
            outline = Color.White.copy(alpha = .13f),
        )
    } else {
        YinPalette(
            isLight = true,
            background = Color(0xFFF5F3F7),
            surface = Color(0xFFFFFFFF),
            elevated = Color(0xFFECE8F0),
            text = Color(0xFF27232E),
            muted = Color(0xFF77717F),
            accent = accent.color.copy(red = accent.color.red * .70f, green = accent.color.green * .70f, blue = accent.color.blue * .72f),
            accentDeep = accent.color.copy(red = accent.color.red * .52f, green = accent.color.green * .52f, blue = accent.color.blue * .58f),
            secondary = accent.secondary.copy(red = accent.secondary.red * .72f, green = accent.secondary.green * .72f, blue = accent.secondary.blue * .73f),
            outline = Color(0xFF322C3B).copy(alpha = .12f),
        )
    }
    val scheme = if (darkMode) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.background,
            secondary = palette.secondary,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.elevated,
            onSurfaceVariant = palette.muted,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            secondary = palette.secondary,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.elevated,
            onSurfaceVariant = palette.muted,
        )
    }
    CompositionLocalProvider(LocalYinPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
    }
}
