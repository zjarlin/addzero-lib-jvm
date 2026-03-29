# media-playlist-player

一个基于 Compose Multiplatform 的高阶列表播放器组件，默认提供完整的播放 UI、歌词同步、播放列表管理，并内置 Android / iOS / Wasm / Desktop 的原生播放引擎适配层。

Maven 工件坐标：

```text
site.addzero:media-playlist-player
```

## Features

- 泛型列表渲染，不强绑业务实体
- 默认内置主流播放器布局：Now Playing、播放列表、进度拖拽、音量调节、上一首/下一首、歌词实时高亮
- 内置音频 URL 异步解析、缓存、错误提示和无音源禁用逻辑
- 支持同一宿主下多个页面共享同一套播放器核心
- 原生适配 Android / iOS / Wasm / Desktop 音频播放，不要求业务层直接依赖第三方播放器或第三方播放器 UI
- 默认原生 Material 3 蓝色风格，不耦合业务主题

## Usage

```kotlin
ProvidePlaylistPlayerHost {
    DefaultPlaylistPlayer(
        items = songs,
        itemKey = { it.id },
        titleOf = { it.name },
        subtitleOf = { it.artist },
        durationMsOf = { it.durationMs },
        coverUrlOf = { it.coverUrl },
        hasResolvableAudioOf = { !it.audioUrl.isNullOrBlank() },
        resolveAudioSource = { song ->
            PlaylistAudioSource(url = song.audioUrl)
        },
        resolveLyrics = { song -> song.lrcText },
        itemActions = { song ->
            FilledTonalButton(onClick = { onImport(song) }) {
                Text("导入")
            }
        },
    )
}
```

## APIs

- `DefaultPlaylistPlayer<T>`: 业务默认入口，传列表和映射函数即可；如果业务本身已经知道 URL 为空，可以用 `hasResolvableAudioOf` 让“试听 / 复制 / 下载”按钮直接禁用。
- `rememberPlaylistPlayerController(...)`: 需要外部驱动时使用的语义化控制器，内置 `play / pause / resume / replay / playPrevious / playNext / seekTo / setVolume`。
- `ProvidePlaylistPlayerHost { ... }`: 让多个页面或弹窗共享同一套播放核心。
- `MediaPlaylistPlayer<T>`: 旧 API 兼容层，建议逐步迁移到 `DefaultPlaylistPlayer<T>`。

## Build Notes

- 默认构建启用 Android / Wasm / Desktop。
- 如果当前环境已经能正常解析 Apple 依赖，可以用 `-PmediaPlaylistPlayer.enableIos=true` 打开 iOS target。
