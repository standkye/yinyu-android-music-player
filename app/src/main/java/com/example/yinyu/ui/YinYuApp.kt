package com.example.yinyu.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.app.Activity
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.yinyu.R
import com.example.yinyu.ui.theme.YinColors
import com.example.yinyu.ui.theme.YinAccent
import com.example.yinyu.ui.theme.YinYuTheme
import androidx.media3.common.Player
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat

private const val PLAYBACK_NOTIFICATION_CHANNEL_ID = "yinyu_media_playback_v2"

private fun playbackNotificationsEnabled(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= 33 &&
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
    ) return false
    val manager = context.getSystemService(NotificationManager::class.java) ?: return false
    if (!manager.areNotificationsEnabled()) return false
    return manager.getNotificationChannel(PLAYBACK_NOTIFICATION_CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
}

@Composable
fun YinYuApp() {
    val context = LocalContext.current
    val music = remember { MusicAppController(context) }
    DisposableEffect(music) { onDispose { music.close() } }
    var page by rememberSaveable { mutableIntStateOf(0) }
    var fullScreen by rememberSaveable { mutableStateOf(false) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var queueOpen by rememberSaveable { mutableStateOf(false) }
    var lyricsOpen by rememberSaveable { mutableStateOf(false) }
    var addToPlaylistOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var switchToMediaStoreAfterPermission by remember { mutableStateOf(false) }
    var folderSelectionCanceled by remember { mutableStateOf(false) }
    val sourcePreferences = remember { context.getSharedPreferences("music_source", Context.MODE_PRIVATE) }
    val appearancePreferences = remember { context.getSharedPreferences("music_appearance", Context.MODE_PRIVATE) }
    val libraryPreferences = remember { context.getSharedPreferences("music_library", Context.MODE_PRIVATE) }
    val lyricsPreferences = remember { context.getSharedPreferences("music_lyrics", Context.MODE_PRIVATE) }
    var librarySortOrder by rememberSaveable { mutableIntStateOf(libraryPreferences.getInt("sort_order", 0).coerceIn(0, 2)) }
    var lyricsAutoFollow by rememberSaveable { mutableStateOf(lyricsPreferences.getBoolean("auto_follow", true)) }
    var lyricsTextSize by rememberSaveable { mutableIntStateOf(lyricsPreferences.getInt("text_size", 1).coerceIn(0, 2)) }
    var themeMode by rememberSaveable {
        mutableIntStateOf(appearancePreferences.getInt(
            "theme_mode",
            if (appearancePreferences.getBoolean("dark_theme", true)) 0 else 1,
        ).coerceIn(0, 2))
    }
    val darkTheme = themeMode != 1
    var accentIndex by rememberSaveable { mutableIntStateOf(appearancePreferences.getInt("accent_index", 0).coerceIn(0, YinAccent.values().lastIndex)) }
    val notificationPreferences = remember { context.getSharedPreferences("music_notifications", Context.MODE_PRIVATE) }
    var notificationsEnabled by remember { mutableStateOf(playbackNotificationsEnabled(context)) }
    var pendingPlayAfterPermission by remember { mutableStateOf<(() -> Unit)?>(null) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        notificationsEnabled = playbackNotificationsEnabled(context)
        val pending = pendingPlayAfterPermission
        pendingPlayAfterPermission = null
        pending?.invoke()
    }
    val requestOrPlay: ((() -> Unit) -> Unit) = { action ->
        val hasRuntimePermission = Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= 33 && !hasRuntimePermission &&
            !notificationPreferences.getBoolean("permission_prompted", false)
        ) {
            notificationPreferences.edit().putBoolean("permission_prompted", true).apply()
            pendingPlayAfterPermission = action
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else action()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) notificationsEnabled = playbackNotificationsEnabled(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val dockHaze = remember { HazeState() }
    val fullTransition = remember { Animatable(0f) }
    val songs = music.songs
    val song = music.currentSong ?: emptySong
    val artists = remember(songs, music.artistPictures) {
        songs.groupBy { it.artist }.map { (name, tracks) ->
            PreviewArtist(name, tracks.size, portraitUri = music.artistPictures[name])
        }.sortedByDescending { it.count }
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) music.chooseTree(uri) else folderSelectionCanceled = true
    }
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            if (switchToMediaStoreAfterPermission) music.useMediaStore() else music.scan()
        }
        switchToMediaStoreAfterPermission = false
    }

    LaunchedEffect(Unit) {
        when {
            music.selectedTree != null -> music.scan()
            !sourcePreferences.getBoolean("complete_folder_prompted", false) -> {
                sourcePreferences.edit().putBoolean("complete_folder_prompted", true).apply()
                folderPicker.launch(null)
            }
            music.hasMediaPermission -> music.scan()
            else -> audioPermission.launch(if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    LaunchedEffect(folderSelectionCanceled) {
        if (folderSelectionCanceled) {
            folderSelectionCanceled = false
            if (music.hasMediaPermission) music.scan()
            else audioPermission.launch(if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    LaunchedEffect(fullScreen) {
        fullTransition.animateTo(
            if (fullScreen) 1f else 0f,
            animationSpec = tween(durationMillis = 640, easing = FastOutSlowInEasing),
        )
    }
    BackHandler(fullScreen || searchOpen || queueOpen || lyricsOpen || addToPlaylistOpen || settingsOpen) {
        if (settingsOpen) settingsOpen = false
        else if (addToPlaylistOpen) addToPlaylistOpen = false
        else if (lyricsOpen) lyricsOpen = false
        else if (queueOpen) queueOpen = false
        else if (searchOpen) searchOpen = false
        else if (fullScreen) fullScreen = false
    }

    val view = LocalView.current
    YinYuTheme(darkMode = darkTheme, accentIndex = accentIndex, blackMode = themeMode == 2) {
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(YinColors.background)) {
        val compactHeight = maxHeight.value / maxWidth.value < 1.9f
        Box(Modifier.fillMaxSize().hazeSource(dockHaze)) {
            when (page) {
                    0 -> HomePage(
                        song = song,
                        songs = songs,
                        artists = artists,
                        recentIds = music.recentIds,
                        scanning = music.scanning,
                        progress = music.progress,
                        playing = music.playing,
                        compactHeight = compactHeight,
                        onSearch = { searchOpen = true },
                        onOpenPlayer = { if (songs.isEmpty()) folderPicker.launch(null) else fullScreen = true },
                        onToggle = {
                            if (songs.isEmpty()) folderPicker.launch(null)
                            else if (music.playing) music.togglePlayback()
                            else requestOrPlay(music::togglePlayback)
                        },
                        onSelectSong = { index -> requestOrPlay { music.play(index); fullScreen = true } },
                        onChooseFolder = { folderPicker.launch(null) },
                        onLibrary = { page = 1 },
                        onPlaylist = { page = 2 },
                        onSettings = { settingsOpen = true },
                    )
                    1 -> LibraryPage(
                        songs = songs,
                        artists = artists,
                        sourceName = music.sourceName,
                        scanning = music.scanning,
                        sortOrder = librarySortOrder,
                        onSortOrderChange = { selected ->
                            librarySortOrder = selected
                            libraryPreferences.edit().putInt("sort_order", selected).apply()
                        },
                        onSearch = { searchOpen = true },
                        onSelectSong = { index -> requestOrPlay { music.play(index); fullScreen = true } },
                        onChooseFolder = { folderPicker.launch(null) },
                        onRescan = { music.scan() },
                        onSettings = { settingsOpen = true },
                    )
                    else -> PlaylistPage(
                        songs = songs,
                        favorites = music.favorites,
                        playlists = music.playlists,
                        onLibrary = { page = 1 },
                        onSelectSong = { items, index -> requestOrPlay { music.playQueue(items, index); fullScreen = true } },
                        onCreatePlaylist = music::createPlaylist,
                        onDeletePlaylist = music::deletePlaylist,
                        onRemoveSong = music::removeFromPlaylist,
                        onSettings = { settingsOpen = true },
                    )
                }
        }

        PocketDock(
            song = song,
            progress = music.progress,
            playing = music.playing,
            page = page,
            hazeState = dockHaze,
            onPage = { page = it; fullScreen = false },
            onToggle = {
                if (songs.isEmpty()) folderPicker.launch(null)
                else if (music.playing) music.togglePlayback()
                else requestOrPlay(music::togglePlayback)
            },
            onExpand = { if (songs.isEmpty()) folderPicker.launch(null) else fullScreen = true },
            onFullScreen = { if (songs.isEmpty()) folderPicker.launch(null) else fullScreen = true },
            modifier = Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).zIndex(5f)
                .graphicsLayer { alpha = (1f - fullTransition.value / .35f).coerceIn(0f, 1f) },
        )

        if (fullScreen || fullTransition.value > 0f) {
            FullScreenPlayer(
                song = song,
                progress = music.progress,
                positionMs = music.positionMs,
                playing = music.playing,
                favorite = music.isFavorite,
                shuffle = music.shuffle,
                repeatMode = music.repeatMode,
                transition = fullTransition.value,
                onCollapse = { fullScreen = false },
                onToggle = { if (music.playing) music.togglePlayback() else requestOrPlay(music::togglePlayback) },
                onPrevious = music::previous,
                onNext = music::next,
                onSeek = music::seekTo,
                onFavorite = music::toggleFavorite,
                onShuffle = music::toggleShuffle,
                onRepeat = music::cycleRepeat,
                onLyrics = { lyricsOpen = true },
                onQueue = { queueOpen = true },
                onAddToPlaylist = { addToPlaylistOpen = true },
                modifier = Modifier.fillMaxSize().zIndex(8f),
            )
        }

        if (searchOpen) {
            SearchOverlay(
                songs = songs,
                onClose = { searchOpen = false },
                onSelectSong = {
                    requestOrPlay {
                        music.play(it)
                        searchOpen = false
                        fullScreen = true
                    }
                },
            )
        }
        if (queueOpen) QueueOverlay(music.queueSongs, music.currentQueueIndex, { queueOpen = false }) {
            requestOrPlay { music.playFromQueue(it) }
        }
        if (lyricsOpen) LyricsOverlay(
            song = song,
            positionMs = music.positionMs,
            progress = music.progress,
            playing = music.playing,
            autoFollow = lyricsAutoFollow,
            textScale = when (lyricsTextSize) { 0 -> .88f; 2 -> 1.16f; else -> 1f },
            onClose = { lyricsOpen = false },
            onToggle = { if (music.playing) music.togglePlayback() else requestOrPlay(music::togglePlayback) },
            onPrevious = music::previous,
            onNext = music::next,
            onSeek = music::seekTo,
            onQueue = { lyricsOpen = false; queueOpen = true },
        )
        if (addToPlaylistOpen) AddToPlaylistOverlay(music.playlists, { addToPlaylistOpen = false }, music::addCurrentToPlaylist)
        if (settingsOpen) SettingsScreen(
            sourceName = "${music.sourceName} · ${music.songs.size} 首",
            themeMode = themeMode,
            accentIndex = accentIndex,
            notificationsEnabled = notificationsEnabled,
            minimumTrackDurationSeconds = music.minimumTrackDurationSeconds,
            sleepTimerMinutes = music.sleepTimerMinutes,
            volumePercent = music.volumePercent,
            playbackSpeedPercent = music.playbackSpeedPercent,
            librarySortOrder = librarySortOrder,
            lyricsAutoFollow = lyricsAutoFollow,
            lyricsTextSize = lyricsTextSize,
            onThemeChange = { selected ->
                themeMode = selected
                appearancePreferences.edit().putInt("theme_mode", selected).putBoolean("dark_theme", selected != 1).apply()
            },
            onAccentChange = { selected ->
                accentIndex = selected
                appearancePreferences.edit().putInt("accent_index", selected).apply()
            },
            onManageNotifications = {
                val runtimeGranted = Build.VERSION.SDK_INT < 33 ||
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (Build.VERSION.SDK_INT >= 33 && !runtimeGranted &&
                    !notificationPreferences.getBoolean("permission_prompted", false)
                ) {
                    notificationPreferences.edit().putBoolean("permission_prompted", true).apply()
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            },
            onMinimumDurationChange = music::setMinimumTrackDuration,
            onSleepTimerChange = music::setSleepTimer,
            onVolumeChange = music::updateVolumePercent,
            onPlaybackSpeedChange = music::updatePlaybackSpeedPercent,
            onLibrarySortChange = { selected ->
                librarySortOrder = selected.coerceIn(0, 2)
                libraryPreferences.edit().putInt("sort_order", librarySortOrder).apply()
            },
            onLyricsAutoFollowChange = { enabled ->
                lyricsAutoFollow = enabled
                lyricsPreferences.edit().putBoolean("auto_follow", enabled).apply()
            },
            onLyricsTextSizeChange = { selected ->
                lyricsTextSize = selected.coerceIn(0, 2)
                lyricsPreferences.edit().putInt("text_size", lyricsTextSize).apply()
            },
            onClose = { settingsOpen = false },
            onChooseFolder = { settingsOpen = false; folderPicker.launch(null) },
            onMediaStore = {
                settingsOpen = false
                if (music.hasMediaPermission) music.useMediaStore()
                else {
                    switchToMediaStoreAfterPermission = true
                    audioPermission.launch(if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                        else Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            },
        )
        music.error?.let { message ->
            Row(Modifier.align(Alignment.TopCenter).zIndex(20f)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF4A3545))
                .clickable(onClick = music::dismissError)
                .padding(horizontal = 15.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(message, color = YinColors.text, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Default.Close, "关闭提示", tint = YinColors.lavender, modifier = Modifier.size(17.dp))
            }
        }
    }
    }
}

@Composable
private fun HomePage(
    song: PreviewSong,
    songs: List<PreviewSong>,
    artists: List<PreviewArtist>,
    recentIds: List<String>,
    scanning: Boolean,
    progress: Float,
    playing: Boolean,
    compactHeight: Boolean,
    onSearch: () -> Unit,
    onOpenPlayer: () -> Unit,
    onToggle: () -> Unit,
    onSelectSong: (Int) -> Unit,
    onChooseFolder: () -> Unit,
    onLibrary: () -> Unit,
    onPlaylist: () -> Unit,
    onSettings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(bottom = 150.dp),
    ) {
        item { BrandHeader(songs.size, artists.size, onSearch, onSettings) }
        if (songs.isEmpty()) {
            item { EmptyLibraryCard(scanning, onChooseFolder) }
            item { Spacer(Modifier.height(20.dp)) }
            item { SectionHeading("本地音乐", "选择文件夹", compactHeight, onChooseFolder) }
            return@LazyColumn
        }
        item { FeaturedTrack(song, progress, playing, compactHeight, onOpenPlayer, onToggle) }
        item {
            QuickActions(
                onFavorite = onPlaylist,
                onHistory = onLibrary,
                onRandom = { onSelectSong(songs.indices.random()) },
                onFolder = onChooseFolder,
                compactHeight = compactHeight,
            )
        }
        item { SectionHeading("最近的专辑", "更多", compactHeight, onLibrary) }
        item {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                songs.withIndex().distinctBy { it.value.album to it.value.artist }.take(12).forEach { (index, item) ->
                    AlbumTile(item, compactHeight) { onSelectSong(index) }
                }
            }
        }
        item { Spacer(Modifier.height(0.dp)) }
        item { SectionHeading("常听的歌手", "更多", compactHeight, onLibrary) }
        item {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                artists.take(12).forEach { ArtistTile(it, compactHeight, onLibrary) }
            }
        }
        item { RandomBanner(songs.first()) { onSelectSong(songs.indices.random()) } }
        item { SectionHeading("最近加入", "查看全部", compactHeight, onLibrary) }
        val recent = if (recentIds.isEmpty()) songs.sortedByDescending { it.addedAt }.take(3)
            else recentIds.mapNotNull { id -> songs.firstOrNull { it.storageId == id || it.uri?.toString() == id } }.take(3)
        itemsIndexed(recent) { _, item ->
            SongRow(item, onClick = { onSelectSong(songs.indexOf(item)) })
        }
    }
}

@Composable
private fun EmptyLibraryCard(scanning: Boolean, onChooseFolder: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(Brush.linearGradient(listOf(YinColors.surface, YinColors.elevated)))
            .border(.7.dp, YinColors.outline, RoundedCornerShape(34.dp))
            .padding(24.dp),
    ) {
        Box(Modifier.size(60.dp).clip(RoundedCornerShape(22.dp)).background(YinColors.elevated), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.FolderOpen, null, tint = YinColors.lavender, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text(if (scanning) "正在扫描本地音乐" else "把你的音乐带进来", color = YinColors.text, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text("选择复制到手机的音乐文件夹，可同时识别 folder.jpg、singer.jpg 和同名歌词。", color = YinColors.muted, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.clip(CircleShape).background(YinColors.lavender).clickable(onClick = onChooseFolder)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text("选择音乐文件夹", color = if (YinColors.isLight) Color.White else YinColors.background, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BrandHeader(songCount: Int, artistCount: Int, onSearch: () -> Unit, onSettings: () -> Unit) {
    val waveColor = YinColors.lavender
    Row(
        Modifier.fillMaxWidth().padding(start = 22.dp, end = 20.dp, top = 0.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(width = 38.dp, height = 32.dp)) {
            val wave = Path().apply {
                moveTo(size.width * .05f, size.height * .60f)
                cubicTo(size.width * .18f, size.height * .05f, size.width * .27f, size.height * 1.02f, size.width * .42f, size.height * .52f)
                cubicTo(size.width * .55f, size.height * -.12f, size.width * .66f, size.height * 1.12f, size.width * .78f, size.height * .49f)
                cubicTo(size.width * .86f, size.height * .28f, size.width * .89f, size.height * .63f, size.width * .96f, size.height * .50f)
            }
            drawPath(wave, waveColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
        }
        Spacer(Modifier.width(6.dp))
        Text("音屿", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = YinColors.text)
        Spacer(Modifier.width(14.dp))
        Text(
            "$songCount 首 · $artistCount 位歌手",
            fontSize = 11.sp,
            color = YinColors.muted,
            modifier = Modifier.clip(CircleShape).background(YinColors.elevated).padding(horizontal = 11.dp, vertical = 7.dp),
        )
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(40.dp).clip(CircleShape).background(YinColors.elevated).clickable(onClick = onSearch), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Search, contentDescription = "搜索", tint = YinColors.text, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(5.dp))
        Box(Modifier.size(40.dp).clip(CircleShape).background(YinColors.elevated).clickable(onClick = onSettings), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Settings, contentDescription = "设置", tint = YinColors.muted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun FeaturedTrack(
    song: PreviewSong,
    progress: Float,
    playing: Boolean,
    compactHeight: Boolean,
    onOpenPlayer: () -> Unit,
    onToggle: () -> Unit,
) {
    val cardShape = RoundedCornerShape(31.dp)
    val themeTint by animateColorAsState(
        targetValue = YinColors.lavender,
        animationSpec = tween(durationMillis = 520),
        label = "featuredThemeTint",
    )
    val themeTintDeep by animateColorAsState(
        targetValue = YinColors.lavenderDeep,
        animationSpec = tween(durationMillis = 520),
        label = "featuredThemeTintDeep",
    )
    val topEdgeAlpha = if (YinColors.isLight) .45f else .07f
    val heroColors = if (YinColors.isLight) {
        listOf(YinColors.surface, YinColors.elevated, YinColors.surface)
    } else {
        listOf(YinColors.background, YinColors.surface, YinColors.lavenderDeep)
    }
    BoxWithConstraints(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(if (compactHeight) 156.dp else 191.dp)
            .shadow(13.dp, cardShape, ambientColor = YinColors.lavender.copy(alpha = if (YinColors.isLight) .10f else .16f))
            .clip(cardShape)
            .background(Brush.horizontalGradient(heroColors))
            .border(.8.dp, YinColors.outline, cardShape)
            .clickable(onClick = onOpenPlayer),
    ) {
        val coverSize = maxHeight * .76f
        val waveformAccent = YinColors.citron
        if (!YinColors.isLight) {
            Image(
                painterResource(R.drawable.featured_glass_background), null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    themeTint.copy(alpha = .42f),
                    blendMode = BlendMode.Color,
                ),
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(if (YinColors.isLight) {
                    listOf(YinColors.surface.copy(alpha = .18f), Color.Transparent, Color.Transparent)
                } else {
                    listOf(
                        YinColors.background.copy(alpha = .43f),
                        YinColors.background.copy(alpha = .09f),
                        Color.Transparent,
                    )
                }),
            ),
        )
        Canvas(Modifier.fillMaxSize()) {
            drawLine(Color.White.copy(alpha = topEdgeAlpha), Offset(18.dp.toPx(), 1.dp.toPx()), Offset(size.width * .5f, 1.dp.toPx()), 1.dp.toPx())
        }
        Box(
            Modifier.align(Alignment.CenterEnd).offset(x = (-10).dp, y = 3.dp).size(coverSize + 11.dp)
                .graphicsLayer { rotationZ = -7f }
                .shadow(14.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = if (YinColors.isLight) .14f else .53f))
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(if (YinColors.isLight) listOf(YinColors.elevated, YinColors.surface, YinColors.elevated)
                    else listOf(themeTint, themeTintDeep, YinColors.background))),
        )
        Box(
            Modifier.align(Alignment.CenterEnd).offset(x = (-18).dp, y = (-3).dp).size(coverSize)
                .graphicsLayer { rotationZ = 2f }
                .shadow(18.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = if (YinColors.isLight) .18f else .65f))
                .clip(RoundedCornerShape(22.dp))
                .border(.8.dp, Color.White.copy(alpha = if (YinColors.isLight) .8f else .22f), RoundedCornerShape(22.dp)),
        ) {
            SongArtwork(song, Modifier.fillMaxSize(), "${song.album} 封面")
        }
        Column(
            Modifier.align(Alignment.CenterStart).fillMaxHeight().width(maxWidth * .54f)
                .padding(start = 19.dp, top = if (compactHeight) 14.dp else 19.dp, bottom = if (compactHeight) 13.dp else 18.dp),
        ) {
            Row(
                Modifier.clip(CircleShape).background((if (YinColors.isLight) YinColors.surface else Color.White).copy(alpha = if (YinColors.isLight) .72f else .075f))
                    .border(.5.dp, YinColors.outline, CircleShape)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Canvas(Modifier.size(width = 13.dp, height = 12.dp)) {
                    val stroke = 1.8.dp.toPx()
                    drawLine(waveformAccent, Offset(size.width * .15f, size.height * .32f), Offset(size.width * .15f, size.height * .72f), stroke)
                    drawLine(waveformAccent, Offset(size.width * .48f, size.height * .12f), Offset(size.width * .48f, size.height * .88f), stroke)
                    drawLine(waveformAccent, Offset(size.width * .81f, size.height * .38f), Offset(size.width * .81f, size.height * .66f), stroke)
                }
                Spacer(Modifier.width(6.dp))
                Text("继续播放", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = YinColors.text)
            }
            Spacer(Modifier.height(if (compactHeight) 7.dp else 11.dp))
            Text(song.title, fontSize = if (compactHeight) 21.sp else 25.sp, fontWeight = FontWeight.Bold, color = YinColors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, fontSize = 12.sp, color = if (YinColors.isLight) YinColors.muted else Color(0xFFD5CBD8), maxLines = 1)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f).padding(bottom = 4.dp)) {
                    Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(YinColors.muted.copy(alpha = if (YinColors.isLight) .28f else .21f))) {
                        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(YinColors.lavender))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("${formatTime((progress * song.durationSeconds).toInt())}  /  ${formatTime(song.durationSeconds)}", color = YinColors.muted, fontSize = 10.sp, maxLines = 1)
                }
                Spacer(Modifier.width(9.dp))
                LavenderPlayButton(if (compactHeight) 39.dp else 43.dp, playing, onToggle)
            }
        }
    }
}

@Composable
private fun QuickActions(onFavorite: () -> Unit, onHistory: () -> Unit, onRandom: () -> Unit, onFolder: () -> Unit, compactHeight: Boolean) {
    val actions = listOf(
        Triple("收藏", Icons.Default.Favorite, onFavorite),
        Triple("最近", Icons.Default.WatchLater, onHistory),
        Triple("随机", Icons.Default.Shuffle, onRandom),
        Triple("文件夹", Icons.Default.Folder, onFolder),
    )
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = if (compactHeight) 5.dp else 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEachIndexed { index, (label, icon, action) ->
            val tileShape = when (index) {
                0 -> RoundedCornerShape(topStart = 28.dp, topEnd = 27.dp, bottomStart = 31.dp, bottomEnd = 25.dp)
                1 -> RoundedCornerShape(topStart = 31.dp, topEnd = 29.dp, bottomStart = 28.dp, bottomEnd = 32.dp)
                2 -> RoundedCornerShape(topStart = 32.dp, topEnd = 27.dp, bottomStart = 28.dp, bottomEnd = 31.dp)
                else -> RoundedCornerShape(topStart = 29.dp, topEnd = 29.dp, bottomStart = 30.dp, bottomEnd = 28.dp)
            }
            Column(
                Modifier.weight(1f).clip(tileShape)
                    .background(YinColors.surface)
                    .border(.8.dp, YinColors.outline, tileShape)
                    .clickable(onClick = action).padding(vertical = if (compactHeight) 5.dp else 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(icon, contentDescription = null, tint = if (index == 2) YinColors.citron else YinColors.lavender, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(5.dp))
                Text(label, fontSize = 12.sp, color = YinColors.text)
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, action: String, compactHeight: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = if (compactHeight) 1.dp else 5.dp, bottom = if (compactHeight) 5.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = YinColors.text)
        Spacer(Modifier.weight(1f))
        Row(Modifier.clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
            Text(action, fontSize = 12.sp, color = YinColors.muted)
            Icon(Icons.Default.ArrowForward, null, tint = YinColors.muted, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun AlbumTile(song: PreviewSong, compactHeight: Boolean, onClick: () -> Unit) {
    val side = if (compactHeight) 80.dp else 112.dp
    Column(Modifier.width(side).clickable(onClick = onClick)) {
        SongArtwork(
            song = song,
            description = "${song.album} 封面",
            modifier = Modifier.fillMaxWidth().height(side)
                .clip(RoundedCornerShape(17.dp)),
        )
        Spacer(Modifier.height(7.dp))
        Text(song.album, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = YinColors.text)
        Text(song.artist, fontSize = 10.sp, color = YinColors.muted)
    }
}

@Composable
private fun ArtistTile(artist: PreviewArtist, compactHeight: Boolean, onClick: () -> Unit) {
    val side = if (compactHeight) 64.dp else 114.dp
    Column(Modifier.width(side).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        ArtistArtwork(artist, Modifier.size(side).clip(CircleShape))
        Spacer(Modifier.height(3.dp))
        Text(artist.name, fontSize = 11.sp, color = YinColors.text, maxLines = 1)
        Text("${artist.count} 首", fontSize = 10.sp, color = YinColors.muted, maxLines = 1)
    }
}

@Composable
private fun RandomBanner(song: PreviewSong, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(108.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.horizontalGradient(if (YinColors.isLight) listOf(YinColors.surface, YinColors.elevated)
                else listOf(YinColors.lavenderDeep, YinColors.surface)))
            .border(.8.dp, YinColors.outline, RoundedCornerShape(30.dp))
            .clickable(onClick = onClick).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("换个心情", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = YinColors.text)
            Text("从本地曲库随机播放", fontSize = 12.sp, color = YinColors.muted)
        }
        SongArtwork(song, Modifier.size(68.dp).clip(RoundedCornerShape(23.dp)).graphicsLayer { rotationZ = 9f })
        Spacer(Modifier.width(12.dp))
        Icon(Icons.Default.PlayArrow, contentDescription = "随机播放", tint = if (YinColors.isLight) Color.White else YinColors.background,
            modifier = Modifier.size(38.dp).clip(CircleShape).background(YinColors.citron).padding(5.dp))
    }
}

@Composable
private fun SongRow(song: PreviewSong, onRemove: (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongArtwork(song, Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = YinColors.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("${song.artist} · ${song.album}", color = YinColors.muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(formatTime(song.durationSeconds), color = YinColors.muted, fontSize = 12.sp)
        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.Close, "从歌单移除", tint = YinColors.muted, modifier = Modifier.size(17.dp))
            }
        }
    }
}

private fun formatTime(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun SearchOverlay(songs: List<PreviewSong>, onClose: () -> Unit, onSelectSong: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().zIndex(5f).background(YinColors.background).windowInsetsPadding(WindowInsets.systemBars)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "返回", tint = YinColors.text) }
            TextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                placeholder = { Text("搜索歌曲、专辑、歌手", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(23.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = YinColors.surface, unfocusedContainerColor = YinColors.surface,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
        }
        val results = songs.withIndex().filter { (_, item) ->
            query.isBlank() || item.title.contains(query, true) || item.artist.contains(query, true) ||
                item.album.contains(query, true) || item.folder.contains(query, true)
        }
        Text("${if (query.isBlank()) "最近的歌曲" else "搜索结果"} · ${results.size}", color = YinColors.muted, fontSize = 13.sp, modifier = Modifier.padding(start = 22.dp, top = 18.dp, bottom = 12.dp))
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(results) { _, result ->
                SongRow(result.value) { onSelectSong(result.index) }
            }
        }
    }
}

@Composable
private fun LibraryPage(
    songs: List<PreviewSong>,
    artists: List<PreviewArtist>,
    sourceName: String,
    scanning: Boolean,
    sortOrder: Int,
    onSortOrderChange: (Int) -> Unit,
    onSearch: () -> Unit,
    onSelectSong: (Int) -> Unit,
    onChooseFolder: () -> Unit,
    onRescan: () -> Unit,
    onSettings: () -> Unit,
) {
    var category by rememberSaveable { mutableIntStateOf(0) }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(selectedGroup != null) { selectedGroup = null }
    val labels = listOf("歌曲", "专辑", "歌手", "文件夹")
    val sortedSongs = when (sortOrder) {
        1 -> songs.sortedByDescending { it.addedAt }
        2 -> songs.sortedByDescending { it.durationSeconds }
        else -> songs.sortedBy { it.title.lowercase() }
    }
    val groups = when (category) {
        1 -> songs.groupBy { it.album }.toList().sortedBy { it.first }
        2 -> songs.groupBy { it.artist }.toList().sortedBy { it.first }
        3 -> songs.groupBy { it.folder }.toList().sortedBy { it.first }
        else -> emptyList()
    }
    val albumPreviews = songs.distinctBy { it.album to it.artist }.take(8)
    val folderCount = songs.map { it.folder }.distinct().size
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 9.dp, top = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("我的曲库", color = YinColors.text, fontSize = 29.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSearch) { Icon(Icons.Default.Search, "搜索", tint = YinColors.text) }
            IconButton(onClick = onRescan) { Icon(Icons.Default.Refresh, "重新扫描", tint = YinColors.text) }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "设置", tint = YinColors.text) }
        }
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 17.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$sourceName · ${songs.size} 首${if (scanning) " · 扫描中" else ""}", color = YinColors.muted, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(if (sourceName == "手机媒体库") "关联歌词与歌手图" else "选择文件夹", color = YinColors.lavender, fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onChooseFolder).padding(5.dp))
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 17.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEachIndexed { index, label ->
                Text(label, color = if (category == index) YinColors.background else YinColors.text,
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(CircleShape).background(if (category == index) YinColors.lavender else YinColors.surface)
                        .clickable { category = index; selectedGroup = null }.padding(horizontal = 20.dp, vertical = 12.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 21.dp, end = 20.dp, top = 20.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(selectedGroup ?: "${labels[category]} · ${if (category == 0) songs.size else groups.size}", color = YinColors.muted, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            if (category == 0 || selectedGroup != null) {
                Text("排序：${listOf("名称", "最近加入", "时长")[sortOrder]}", color = YinColors.lavender, fontSize = 11.sp,
                    modifier = Modifier.clickable { onSortOrderChange((sortOrder + 1) % 3) }.padding(5.dp))
            }
        }
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 112.dp)) {
            if (songs.isEmpty()) {
                item { Text("没有找到本地歌曲，请选择包含音频的文件夹。", color = YinColors.muted, fontSize = 13.sp,
                    modifier = Modifier.padding(22.dp)) }
            } else if (category == 0) {
                itemsIndexed(sortedSongs) { _, item -> SongRow(item) { onSelectSong(songs.indexOf(item)) } }
                if (songs.size <= 8) {
                    item {
                        Spacer(Modifier.height(21.dp))
                        SectionHeading("专辑速览", "全部", false) { category = 1 }
                    }
                    item {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                            albumPreviews.forEach { albumSong ->
                                AlbumTile(albumSong, false) {
                                    category = 1
                                    selectedGroup = albumSong.album
                                }
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(20.dp))
                        SectionHeading("常听的歌手", "全部", false) { category = 2 }
                    }
                    item {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                            artists.take(8).forEach { artist ->
                                ArtistTile(artist, false) {
                                    category = 2
                                    selectedGroup = artist.name
                                }
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 26.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF393242), Color(0xFF302B38))))
                            .clickable { category = 3; selectedGroup = null }
                            .padding(horizontal = 18.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, null, tint = YinColors.lavender, modifier = Modifier.size(26.dp))
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text("按文件夹浏览", color = YinColors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("$folderCount 个文件夹 · 保留手机中的整理方式", color = YinColors.muted, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.ArrowForward, null, tint = YinColors.muted, modifier = Modifier.size(17.dp))
                        }
                    }
                }
            } else if (selectedGroup != null) {
                item { Text("‹ 返回${labels[category]}", color = YinColors.lavender, fontSize = 13.sp,
                    modifier = Modifier.clickable { selectedGroup = null }.padding(horizontal = 22.dp, vertical = 12.dp)) }
                itemsIndexed(sortedSongs.filter { when (category) {
                    1 -> it.album == selectedGroup
                    2 -> it.artist == selectedGroup
                    else -> it.folder == selectedGroup
                } }) { _, item -> SongRow(item) { onSelectSong(songs.indexOf(item)) } }
            } else {
                itemsIndexed(groups) { _, (name, groupSongs) ->
                    Row(
                        Modifier.fillMaxWidth().clickable { selectedGroup = name }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (category == 2) {
                            ArtistArtwork(artists.firstOrNull { it.name == name } ?: PreviewArtist(name, groupSongs.size),
                                Modifier.size(53.dp).clip(CircleShape))
                        } else {
                            SongArtwork(groupSongs.first(), Modifier.size(53.dp).clip(RoundedCornerShape(15.dp)))
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, color = YinColors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text("${groupSongs.size} 首歌曲", color = YinColors.muted, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = YinColors.muted, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistPage(
    songs: List<PreviewSong>,
    favorites: Set<String>,
    playlists: List<com.example.yinyu.data.SavedPlaylist>,
    onLibrary: () -> Unit,
    onSelectSong: (List<PreviewSong>, Int) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onRemoveSong: (Long, String) -> Unit,
    onSettings: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf<Long?>(null) }
    BackHandler(selected != null) { selected = null }
    var creating by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf("") }
    val favoriteSongs = songs.filter { it.storageId in favorites || it.uri?.toString() in favorites }
    val chosen = playlists.firstOrNull { it.id == selected }
    val selectedSongs = if (selected == -1L) favoriteSongs
        else chosen?.songIds?.mapNotNull { id -> songs.firstOrNull { it.storageId == id || it.uri?.toString() == id } }.orEmpty()
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(bottom = 140.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 14.dp, top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            if (selected != null) {
                IconButton(onClick = { selected = null }) { Icon(Icons.Default.ArrowBack, "返回歌单", tint = YinColors.text) }
            }
            Text(if (selected == null) "我的歌单" else if (selected == -1L) "我喜欢的音乐" else chosen?.name.orEmpty(),
                color = YinColors.text, fontSize = if (selected == null) 29.sp else 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if (selected == null) {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, "设置", tint = YinColors.muted)
                }
                IconButton(onClick = { creating = !creating }) {
                    Icon(Icons.Default.Add, "新建歌单", tint = YinColors.lavender)
                }
            }
        }
        if (selected != null) {
            Text("${selectedSongs.size} 首歌曲", color = YinColors.muted, fontSize = 13.sp,
                modifier = Modifier.padding(start = 22.dp, bottom = 15.dp))
            LazyColumn {
                if (selectedSongs.isEmpty()) item {
                    Text("这里还没有歌曲。播放歌曲时可通过播放页添加。", color = YinColors.muted, fontSize = 13.sp,
                        modifier = Modifier.padding(22.dp))
                }
                itemsIndexed(selectedSongs) { _, item ->
                    SongRow(item, onRemove = if (selected == -1L) null else ({
                        chosen?.let { playlist ->
                            val savedId = playlist.songIds.firstOrNull { it == item.storageId || it == item.uri?.toString() }
                            if (savedId != null) onRemoveSong(playlist.id, savedId)
                        }
                    })) { onSelectSong(selectedSongs, selectedSongs.indexOf(item)) }
                }
            }
            return@Column
        }
        Text("把喜欢的声音收在一起", color = YinColors.muted, fontSize = 13.sp,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 24.dp))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(142.dp)
                .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 57.dp, bottomStart = 48.dp, bottomEnd = 31.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF665072), Color(0xFF3D354A))))
                .clickable { selected = -1L }.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Favorite, null, tint = YinColors.citron, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(22.dp))
            Column {
                Text("我喜欢的音乐", color = YinColors.text, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text("${favoriteSongs.size} 首歌曲", color = YinColors.text.copy(alpha = .75f), fontSize = 13.sp)
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("自建歌单", color = YinColors.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${playlists.size} 个", color = YinColors.muted, fontSize = 12.sp)
        }
        if (creating) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(value = draft, onValueChange = { draft = it }, singleLine = true,
                    placeholder = { Text("歌单名称") }, modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(focusedContainerColor = YinColors.surface,
                        unfocusedContainerColor = YinColors.surface, focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent))
                Text("创建", color = YinColors.lavender, fontSize = 14.sp,
                    modifier = Modifier.clickable { onCreatePlaylist(draft); draft = ""; creating = false }.padding(12.dp))
            }
        }
        LazyColumn {
            if (playlists.isEmpty()) item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(22.dp))
                    .background(YinColors.surface).clickable(onClick = onLibrary).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LibraryMusic, null, tint = YinColors.lavender)
                    Spacer(Modifier.width(13.dp))
                    Text("浏览曲库，开始整理歌单", color = YinColors.text, fontSize = 13.sp)
                }
            }
            itemsIndexed(playlists) { _, playlist ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(22.dp)).background(YinColors.surface)
                    .clickable { selected = playlist.id }.padding(start = 16.dp, end = 6.dp, top = 13.dp, bottom = 13.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LibraryMusic, null, tint = YinColors.lavender)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(playlist.name, color = YinColors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("${playlist.songIds.size} 首歌曲", color = YinColors.muted, fontSize = 11.sp)
                    }
                    IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                        Icon(Icons.Default.DeleteOutline, "删除歌单", tint = YinColors.muted, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
