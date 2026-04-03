# compose-native-component-chat

可复用的 Compose 聊天面板组件，适合桌面工作台、AI 助手入口、嵌入式聊天侧栏。

- Maven 坐标：`site.addzero:compose-native-component-chat`
- 本地模块路径：`lib/compose/compose-native-component-chat`

## 提供能力

- 会话列表 + 当前会话消息区
- 内置连接配置表单：后端 URL、传输协议、厂商、模型 Base URL、API Key、模型名、系统提示词
- 输入区支持 `Enter` 发送、`Shift/Ctrl/Alt + Enter` 换行
- 默认紧凑桌面风格，也支持通过 slot 自定义消息内容

## 最小示例

```kotlin
var state by remember {
    mutableStateOf(
        AddChatPanelState(
            sessions = listOf(
                AddChatSessionItem(id = "default", title = "默认会话")
            ),
            selectedSessionId = "default",
            connection = AddChatConnectionConfig(
                backendUrl = "http://127.0.0.1:8080",
                vendor = AddChatVendor.OpenAI,
            ),
        )
    )
}

AddChatPanel(
    state = state,
    actions = AddChatPanelActions(
        onInputChange = { value -> state = state.copy(input = value) },
        onConnectionChange = { config -> state = state.copy(connection = config) },
        onSend = { /* 调后端发送 */ },
        onSelectSession = { id -> state = state.copy(selectedSessionId = id) },
    ),
)
```

## 约束

- 当前模块只负责 Compose UI，不内置网络请求实现
- 适合 `commonMain` 复用，具体模型调用和 MCP/ACP 运行时请由宿主注入
