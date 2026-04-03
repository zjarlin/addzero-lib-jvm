package site.addzero.component.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

/**
 * 聊天消息角色。
 */
enum class AddChatMessageRole(
    val displayName: String,
) {
    System("系统"),
    User("我"),
    Assistant("助手"),
    Tool("工具"),
}

/**
 * 传输协议。
 */
enum class AddChatTransport(
    val wireValue: String,
    val displayName: String,
) {
    Http(
        wireValue = "http",
        displayName = "HTTP",
    ),
    Acp(
        wireValue = "acp",
        displayName = "ACP",
    ),
}

/**
 * 常见模型厂商。
 */
enum class AddChatVendor(
    val wireValue: String,
    val displayName: String,
) {
    OpenAI(
        wireValue = "openai",
        displayName = "OpenAI",
    ),
    OpenAICompatible(
        wireValue = "openai-compatible",
        displayName = "OpenAI Compatible",
    ),
    OpenRouter(
        wireValue = "openrouter",
        displayName = "OpenRouter",
    ),
    DeepSeek(
        wireValue = "deepseek",
        displayName = "DeepSeek",
    ),
    Anthropic(
        wireValue = "anthropic",
        displayName = "Anthropic",
    ),
    Google(
        wireValue = "google",
        displayName = "Google",
    ),
    Gemini(
        wireValue = "gemini",
        displayName = "Gemini",
    ),
    Ollama(
        wireValue = "ollama",
        displayName = "Ollama",
    ),
}

/**
 * 连接配置。
 */
@Immutable
data class AddChatConnectionConfig(
    val backendUrl: String = "",
    val transport: AddChatTransport = AddChatTransport.Http,
    val vendor: AddChatVendor = AddChatVendor.OpenAI,
    val providerBaseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val systemPrompt: String = "",
)

/**
 * 会话摘要。
 */
@Immutable
data class AddChatSessionItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val badgeText: String = "",
)

/**
 * 快捷提示词。
 */
@Immutable
data class AddChatQuickPrompt(
    val id: String,
    val title: String,
    val content: String,
)

/**
 * 单条消息。
 */
@Immutable
data class AddChatMessageItem(
    val id: String,
    val role: AddChatMessageRole,
    val content: String,
    val timestampLabel: String = "",
    val statusLabel: String = "",
    val canRetry: Boolean = false,
)

/**
 * 聊天面板整体状态。
 */
@Immutable
data class AddChatPanelState(
    val title: String = "AI 助手",
    val subtitle: String = "配置后端与模型后即可开始对话",
    val sessions: List<AddChatSessionItem> = emptyList(),
    val selectedSessionId: String? = null,
    val messages: List<AddChatMessageItem> = emptyList(),
    val quickPrompts: List<AddChatQuickPrompt> = emptyList(),
    val input: String = "",
    val connection: AddChatConnectionConfig = AddChatConnectionConfig(),
    val showConnectionEditor: Boolean = true,
    val isSending: Boolean = false,
    val isLoadingMessages: Boolean = false,
    val statusText: String = "",
    val emptyTitle: String = "开始一段新对话",
    val emptyDescription: String = "填写连接配置后，输入问题即可开始。",
)

/**
 * 聊天面板事件集合。
 */
data class AddChatPanelActions(
    val onCreateSession: () -> Unit = {},
    val onDeleteSession: (String?) -> Unit = {},
    val onSelectSession: (String) -> Unit = {},
    val onInputChange: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onRetryMessage: (AddChatMessageItem) -> Unit = {},
    val onUsePrompt: (AddChatQuickPrompt) -> Unit = {},
    val onConnectionChange: (AddChatConnectionConfig) -> Unit = {},
    val onToggleConnectionEditor: (Boolean) -> Unit = {},
)

/**
 * 可选插槽。
 */
data class AddChatPanelSlots(
    val header: (@Composable () -> Unit)? = null,
    val sessionEmpty: (@Composable () -> Unit)? = null,
    val empty: (@Composable () -> Unit)? = null,
    val messageContent: (@Composable (AddChatMessageItem) -> Unit)? = null,
)
