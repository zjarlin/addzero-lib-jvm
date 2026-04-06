# compose-native-component-chat

可复用的 Compose 聊天面板组件，适合桌面工作台、AI 助手入口、嵌入式聊天侧栏。

- Maven 坐标：`site.addzero:compose-native-component-chat`
- 本地模块路径：`lib/compose/compose-native-component-chat`

## 提供能力

- 会话列表 + 当前会话消息区
- 内置连接配置表单：后端 URL、传输协议、厂商、模型 Base URL、API Key、模型名、系统提示词
- 输入区支持 `Enter` 发送、`Shift/Ctrl/Alt + Enter` 换行
- 默认紧凑桌面风格，也支持通过 slot 自定义消息内容

## SPI 用法

```kotlin
private class DemoChatSpi : AddChatPanelSpi {
    override suspend fun loadState(): AddChatPanelState {
        return AddChatPanelState(
            sessions = listOf(
                AddChatSessionItem(id = "default", title = "默认会话")
            ),
            selectedSessionId = "default",
            connection = AddChatConnectionConfig(
                backendUrl = "http://127.0.0.1:8080",
                vendor = AddChatVendor.OpenAI,
            ),
        )
    }

    override suspend fun updateConnection(
        currentState: AddChatPanelState,
        connection: AddChatConnectionConfig,
    ): AddChatPanelState {
        return currentState.copy(connection = connection)
    }

    override suspend fun send(
        currentState: AddChatPanelState,
    ): AddChatPanelState {
        return currentState.copy(
            input = "",
            messages = currentState.messages + AddChatMessageItem(
                id = "assistant-1",
                role = AddChatMessageRole.Assistant,
                content = "这是一条来自外部 SPI 的回复。",
            ),
        )
    }
}

val spi = remember { DemoChatSpi() }

AddChatPanel(spi = spi)
```

## 低层用法

如果宿主已经有自己的状态机，也可以继续直接传 `state + actions`：

```kotlin
AddChatPanel(
    state = state,
    actions = actions,
)
```

## 约束

- 当前模块只负责 Compose UI，不内置网络请求实现
- 适合 `commonMain` 复用，具体模型调用和 MCP/ACP 运行时请由宿主通过 `AddChatPanelSpi` 注入

## 桌面预览

- 运行命令：`./gradlew :lib:compose:compose-native-component-chat:previewChat`
- 入口位置：`src/jvmTest/kotlin/site/addzero/component/chat/preview/ChatPreviewMain.kt`
- 可选自动退出：`./gradlew :lib:compose:compose-native-component-chat:previewChat -Dchat.preview.autoExitMillis=1200`
