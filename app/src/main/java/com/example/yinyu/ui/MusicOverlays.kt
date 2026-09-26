package com.example.yinyu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.yinyu.data.SavedPlaylist
import com.example.yinyu.ui.theme.YinColors
import com.example.yinyu.ui.theme.YinAccent

@Composable
private fun OverlayShell(title: String, detail: String, onClose: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().zIndex(12f)
        .background(YinColors.background)
        .windowInsetsPadding(WindowInsets.statusBars)
        .windowInsetsPadding(WindowInsets.navigationBars)) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "返回", tint = YinColors.text) }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, color = YinColors.text, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text(detail, color = YinColors.muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(22.dp))
        content()
    }
}

@Composable
internal fun QueueOverlay(songs: List<PreviewSong>, currentIndex: Int, onClose: () -> Unit, onSelect: (Int) -> Unit) {
    OverlayShell("播放队列", "${songs.size} 首本地歌曲", onClose) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(songs) { index, song ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(if (index == currentIndex) Color(0xFF473954) else Color(0xFF302B38))
                    .clickable { onSelect(index); onClose() }.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                    SongArtwork(song, Modifier.size(51.dp).clip(RoundedCornerShape(14.dp)), song.title)
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(song.title, color = YinColors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, color = YinColors.muted, fontSize = 11.sp, maxLines = 1)
                    }
                    Icon(if (index == currentIndex) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                        null, tint = if (index == currentIndex) YinColors.lavender else YinColors.muted,
                        modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
internal fun AddToPlaylistOverlay(playlists: List<SavedPlaylist>, onClose: () -> Unit, onAdd: (Long) -> Unit) {
    OverlayShell("加入歌单", "选择要保存到的歌单", onClose) {
        if (playlists.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(25.dp), contentAlignment = Alignment.Center) {
                Text("还没有歌单，请先在“歌单”页新建", color = YinColors.muted, fontSize = 14.sp)
            }
        }
        playlists.forEach { playlist ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp)
                .clip(RoundedCornerShape(20.dp)).background(YinColors.surface)
                .clickable { onAdd(playlist.id); onClose() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp))
                    .background(YinColors.lavender.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LibraryMusic, null, tint = YinColors.lavender)
                }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(playlist.name, color = YinColors.text, fontWeight = FontWeight.SemiBold)
                    Text("${playlist.songIds.size} 首歌曲", color = YinColors.muted, fontSize = 11.sp)
                }
                Icon(Icons.Default.CheckCircle, null, tint = YinColors.muted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
internal fun SettingsScreen(
    sourceName: String,
    isDark: Boolean,
    accentIndex: Int,
    onThemeChange: (Boolean) -> Unit,
    onAccentChange: (Int) -> Unit,
    onClose: () -> Unit,
    onChooseFolder: () -> Unit,
    onMediaStore: () -> Unit,
) {
    OverlayShell("设置", "外观与本地音乐", onClose) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                SettingSectionHeading("外观", "选择你喜欢的明暗风格")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppearanceChoice("深色", Icons.Default.DarkMode, selected = isDark, Modifier.weight(1f)) { onThemeChange(true) }
                    AppearanceChoice("浅色", Icons.Default.LightMode, selected = !isDark, Modifier.weight(1f)) { onThemeChange(false) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                SettingSectionHeading("主题色", "像封面一样，为播放器选一种氛围")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    YinAccent.values().forEachIndexed { index, accent ->
                        AccentChoice(accent, selected = index == accentIndex, Modifier.weight(1f)) { onAccentChange(index) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                SettingSectionHeading("本地曲库", sourceName)
                SettingChoice(Icons.Default.FolderOpen, "选择音乐文件夹",
                    "读取音频、专辑封面、歌手图片和同名 LRC 歌词", onChooseFolder)
                SettingChoice(Icons.Default.MusicNote, "扫描手机媒体库",
                    "从 Android 本地媒体库重新载入歌曲", onMediaStore)
            }

            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                SettingSectionHeading("播放", "本机播放 · 无账号")
                Text(
                    "后台播放、锁屏与通知栏控制由 Android 媒体会话提供。插拔耳机时会自动暂停。",
                    color = YinColors.muted, fontSize = 12.sp, lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingSectionHeading(title: String, detail: String) {
    Column {
        Text(title, color = YinColors.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(detail, color = YinColors.muted, fontSize = 11.sp)
    }
}

@Composable
private fun AppearanceChoice(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier.clip(RoundedCornerShape(18.dp))
            .background(if (selected) YinColors.lavender.copy(alpha = .17f) else YinColors.surface)
            .border(1.dp, if (selected) YinColors.lavender.copy(alpha = .68f) else YinColors.outline, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) YinColors.lavender else YinColors.muted, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, color = YinColors.text, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun AccentChoice(accent: YinAccent, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(17.dp))
            .background(if (selected) YinColors.elevated else YinColors.surface)
            .border(1.dp, if (selected) YinColors.lavender.copy(alpha = .62f) else YinColors.outline, RoundedCornerShape(17.dp))
            .clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(22.dp).clip(CircleShape).background(accent.color))
        Spacer(Modifier.height(7.dp))
        Text(accent.title, color = YinColors.text, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SettingChoice(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(YinColors.surface)
        .border(.7.dp, YinColors.outline, RoundedCornerShape(20.dp))
        .clickable(onClick = onClick).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(YinColors.lavender.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = YinColors.lavender, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = YinColors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = YinColors.muted, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}
