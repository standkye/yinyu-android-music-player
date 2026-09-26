# 音屿 · Android 本地音乐播放器

音屿是一款面向 Android 手机的离线音乐播放器。它只读取设备上的本地音乐，不需要账号、网络连接或广告。当前版本：**v1.0.0**。

## 功能

- 扫描本地歌曲，按歌曲、专辑、歌手、文件夹浏览。
- 播放、暂停、上一首、下一首、拖动进度、随机播放、循环播放。
- 后台播放、系统媒体通知和锁屏控制；拔出耳机时暂停。
- 收藏、最近播放、自建歌单和本地搜索。
- 全屏播放页、专辑封面色调背景、波浪进度条与本地 LRC 歌词。
- 支持深色、浅色主题和可选主题色。

## 在手机上使用

1. 将电脑上的 `E:\MyMusic` 复制到手机，例如 `内部存储/Music/MyMusic`。
2. 将发布页的 `YinYu-v1.0.0.apk` 复制到手机并安装。
3. 首次打开时允许读取音频；也可以在应用内选择音乐文件夹并授权。

推荐的音乐文件夹结构：

```text
Music/MyMusic/
  歌手名/
    singer.jpg
    专辑名/
      folder.jpg
      歌曲.mp3
      歌曲.lrc
```

应用会递归读取 MP3、FLAC、M4A、AAC、OGG、Opus、WAV、AIFF、AMR。歌曲标题、歌手、专辑优先读取音频标签；专辑封面支持内嵌图片或同目录的 `folder.jpg`、`cover.jpg`、`front.jpg` 等，歌手图片使用歌手文件夹中的 `singer.jpg`，歌词使用与歌曲同名的 `.lrc` 文件。Android 媒体库扫描可读取歌曲和内嵌封面；如需读取文件夹中的歌词，请在设置中授权音乐文件夹。

## 构建

需要 JDK 17 和 Android SDK 36。在 Windows 项目目录运行：

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --offline
```

调试 APK 输出到 `app/build/outputs/apk/debug/app-debug.apk`。项目最低支持 Android 8.0（API 26）。

## 技术栈

- Kotlin、Jetpack Compose、Material 3
- AndroidX Media3 ExoPlayer 与 MediaSessionService
- SQLite、MediaStore 与 Android 系统文件夹授权
- Haze 玻璃效果

应用不申请网络权限。系统媒体通知由 Android 和设备厂商绘制，外观可能因系统版本和厂商而异。
