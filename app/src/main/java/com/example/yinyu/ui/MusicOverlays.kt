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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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
    themeMode: Int,
    accentIndex: Int,
    notificationsEnabled: Boolean,
    minimumTrackDurationSeconds: Int,
    sleepTimerMinutes: Int,
    volumePercent: Int,
    playbackSpeedPercent: Int,
    librarySortOrder: Int,
    lyricsAutoFollow: Boolean,
    lyricsTextSize: Int,
    onThemeChange: (Int) -> Unit,
    onAccentChange: (Int) -> Unit,
    onManageNotifications: () -> Unit,
    onMinimumDurationChange: (Int) -> Unit,
    onSleepTimerChange: (Int) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onPlaybackSpeedChange: (Int) -> Unit,
    onLibrarySortChange: (Int) -> Unit,
    onLyricsAutoFollowChange: (Boolean) -> Unit,
    onLyricsTextSizeChange: (Int) -> Unit,
    onClose: () -> Unit,
    onChooseFolder: () -> Unit,
    onMediaStore: () -> Unit,
) {
    OverlayShell("设置", "按你的聆听习惯整理音屿", onClose) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(19.dp),
        ) {
            SettingsGroup("外观与个性", "延续 Retro Music 的多主题与强调色思路") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppearanceChoice("浅色", Icons.Default.LightMode, selected = themeMode == 1, Modifier.weight(1f)) { onThemeChange(1) }
                    AppearanceChoice("深色", Icons.Default.DarkMode, selected = themeMode == 0, Modifier.weight(1f)) { onThemeChange(0) }
                    AppearanceChoice("纯黑", Icons.Default.DarkMode, selected = themeMode == 2, Modifier.weight(1f)) { onThemeChange(2) }
                }
                SettingSectionHeading("强调色", "用于播放控件、进度与选中状态")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    YinAccent.values().forEachIndexed { index, accent ->
                        AccentChoice(accent, selected = index == accentIndex, Modifier.weight(1f)) { onAccentChange(index) }
                    }
                }
            }

            SettingsGroup("本地曲库", "$sourceName · 歌曲筛选会立即重新扫描") {
                SettingChoice(Icons.Default.FolderOpen, "选择音乐文件夹",
                    "读取音频、封面、歌手图片和同名 LRC 歌词", onChooseFolder)
                SettingChoice(Icons.Default.MusicNote, "扫描手机媒体库",
                    "从 Android 本地媒体库重新载入歌曲", onMediaStore)
                SettingSectionHeading("忽略短音频", "适合排除提示音与不完整文件")
                SettingsOptions(
                    options = listOf(0 to "不过滤", 30 to "30 秒", 60 to "1 分钟"),
                    selected = minimumTrackDurationSeconds,
                    onSelect = onMinimumDurationChange,
                )
                SettingSectionHeading("默认歌曲排序", "曲库歌曲列表和专辑内曲目共用此顺序")
                SettingsOptions(
                    options = listOf(0 to "名称", 1 to "最近加入", 2 to "时长"),
                    selected = librarySortOrder,
                    onSelect = onLibrarySortChange,
                )
            }

            SettingsGroup("歌词显示", "按你的阅读习惯调整同步歌词") {
                SettingSectionHeading("歌词自动跟随", "关闭后可自由滚动浏览，点击歌词仍可跳转播放")
                SettingsOptions(
                    options = listOf(0 to "手动", 1 to "自动跟随"),
                    selected = if (lyricsAutoFollow) 1 else 0,
                    onSelect = { onLyricsAutoFollowChange(it == 1) },
                )
                SettingSectionHeading("歌词字号", "立即应用到全屏歌词页面")
                SettingsOptions(
                    options = listOf(0 to "小", 1 to "标准", 2 to "大"),
                    selected = lyricsTextSize,
                    onSelect = onLyricsTextSizeChange,
                )
            }

            SettingsGroup("播放辅助", "本地播放 · 锁屏控制 · 自动暂停") {
                SettingSectionHeading("播放速度", "只影响音频播放，不修改原文件")
                SettingsOptions(
                    options = listOf(75 to "0.75×", 100 to "1.0×", 125 to "1.25×", 150 to "1.5×"),
                    selected = playbackSpeedPercent,
                    onSelect = onPlaybackSpeedChange,
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SettingSectionHeading("播放器音量", "独立于手机媒体音量")
                    Spacer(Modifier.weight(1f))
                    Text("$volumePercent%", color = YinColors.lavender, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Slider(
                    value = volumePercent / 100f,
                    onValueChange = { onVolumeChange((it * 100).toInt()) },
                    valueRange = 0f..1f,
                    steps = 9,
                )
                SettingSectionHeading(
                    "睡眠定时器",
                    if (sleepTimerMinutes > 0) "已开启 · $sleepTimerMinutes 分钟后暂停" else "设定停止播放的时间",
                )
                SettingsOptions(
                    options = listOf(0 to "关闭", 15 to "15 分钟", 30 to "30 分钟", 60 to "60 分钟"),
                    selected = sleepTimerMinutes,
                    onSelect = onSleepTimerChange,
                )
                Text(
                    "耳机拔出时自动暂停。播放队列与随机、循环控制仍由播放器统一管理。",
                    color = YinColors.muted, fontSize = 12.sp, lineHeight = 18.sp,
                )
            }

            SettingsGroup("通知与锁屏", "控制系统媒体卡片是否可见") {
                SettingChoice(
                    Icons.Default.NotificationsActive,
                    if (notificationsEnabled) "通知已开启" else "开启播放通知",
                    if (notificationsEnabled) "可在系统设置中调整音屿的通知频道" else "首次播放会申请权限；也可以前往系统通知设置开启",
                    onManageNotifications,
                )
                Text(
                    "媒体通知由 Android 系统绘制，包含封面、歌曲信息、上一首、播放/暂停和下一首。",
                    color = YinColors.muted, fontSize = 12.sp, lineHeight = 18.sp,
                )
            }

            SettingsGroup("关于音屿", "版本 1.0.0 · 完全本地 · 无广告") {
                Text(
                    "音乐文件、封面与歌词只从你授权的手机文件夹或本地媒体库读取。\n\n原创代码采用 GPL-3.0-only；第三方依赖和素材遵循各自许可。",
                    color = YinColors.muted, fontSize = 12.sp, lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp))
            .background(YinColors.surface).border(.8.dp, YinColors.outline, RoundedCornerShape(23.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        SettingSectionHeading(title, subtitle)
        content()
    }
}

@Composable
private fun SettingsOptions(
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        options.forEach { (value, label) ->
            val chosen = selected == value
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .background(if (chosen) YinColors.lavender.copy(alpha = .18f) else YinColors.elevated.copy(alpha = .72f))
                    .border(1.dp, if (chosen) YinColors.lavender.copy(alpha = .55f) else YinColors.outline, RoundedCornerShape(14.dp))
                    .clickable { onSelect(value) }.padding(horizontal = 5.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = if (chosen) YinColors.text else YinColors.muted, fontSize = 11.sp,
                    fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
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
