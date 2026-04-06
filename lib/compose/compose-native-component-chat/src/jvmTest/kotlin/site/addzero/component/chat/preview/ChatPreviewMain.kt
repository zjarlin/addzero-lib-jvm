package site.addzero.component.chat.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import site.addzero.component.button.AddButton
import site.addzero.component.chat.AddChatConnectionConfig
import site.addzero.component.chat.AddChatMessageItem
import site.addzero.component.chat.AddChatMessageRole
import site.addzero.component.chat.AddChatOverlay
import site.addzero.component.chat.AddChatOverlayState
import site.addzero.component.chat.AddChatPanel
import site.addzero.component.chat.AddChatPanelSpi
import site.addzero.component.chat.AddChatPanelState
import site.addzero.component.chat.AddChatQuickPrompt
import site.addzero.component.chat.AddChatSessionItem
import site.addzero.component.chat.AddChatTransport
import site.addzero.component.chat.AddChatVendor
import site.addzero.component.chat.rememberAddChatPanelBinding

/**
 * 聊天组件桌面预览入口。
 *
 * 该入口只存在于 `jvmTest`，用于独立启动后手工验证聊天面板与浮层。
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AddZero Chat Preview",
        state = rememberWindowState(
            width = 1480.dp,
            height = 940.dp,
        ),
    ) {
        val autoExitMillis = System.getProperty("chat.preview.autoExitMillis")
            ?.toLongOrNull()
            ?.takeIf { it > 0L }
        if (autoExitMillis != null) {
            LaunchedEffect(autoExitMillis) {
                delay(autoExitMillis)
                exitApplication()
            }
        }

        MaterialTheme {
            ChatPreviewApp()
        }
    }
}

@Composable
private fun ChatPreviewApp() {
    val overlayState = remember { AddChatOverlayState() }
    val spi = remember { PreviewChatPanelSpi() }
    val binding = rememberAddChatPanelBinding(spi = spi)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF4F8FF),
                        Color(0xFFEAF1FB),
                        Color(0xFFF8FBFF),
                    ),
                ),
            )
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChatPreviewHeader(
                sessionCount = binding.state.sessions.size,
                selectedVendor = binding.state.connection.vendor,
                selectedModel = binding.state.connection.model,
                onOpenOverlay = { overlayState.show() },
                onReset = {
                    spi.reset()
                    binding.controller.load()
                },
            )
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.76f),
                tonalElevation = 1.dp,
            ) {
                AddChatPanel(
                    state = binding.state,
                    actions = binding.actions,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        AddChatOverlay(
            visible = overlayState.visible,
            onDismiss = { overlayState.hide() },
            title = "聊天浮层预览",
            subtitle = "用同一份状态同时验证内嵌面板与浮层模式。",
        ) {
            AddChatPanel(
                state = binding.state,
                actions = binding.actions,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ChatPreviewHeader(
    sessionCount: Int,
    selectedVendor: AddChatVendor,
    selectedModel: String,
    onOpenOverlay: () -> Unit,
    onReset: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.86f),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "聊天组件桌面预览",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "运行命令：./gradlew :lib:compose:compose-native-component-chat:previewChat",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PreviewMetricChip(label = "会话数", value = sessionCount.toString())
                    PreviewMetricChip(label = "厂商", value = selectedVendor.displayName)
                    PreviewMetricChip(
                        label = "模型",
                        value = selectedModel.ifBlank { "未填写" },
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AddButton(
                    displayName = "打开浮层",
                    icon = Icons.Default.SmartToy,
                    onClick = onOpenOverlay,
                )
                AddButton(
                    displayName = "重置数据",
                    icon = Icons.Default.Refresh,
                    onClick = onReset,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PreviewMetricChip(
    label: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(999.dp),
                    ),
            )
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

private class PreviewChatPanelSpi : AddChatPanelSpi {
    private var hostState = buildChatPreviewHostState()

    fun reset() {
        hostState = buildChatPreviewHostState()
    }

    override suspend fun loadState(): AddChatPanelState {
        delay(120)
        return hostState.toPanelState()
    }

    override suspend fun createSession(
        currentState: AddChatPanelState,
    ): AddChatPanelState {
        hostState = hostState.createSession()
        return hostState.toPanelState()
    }

    override suspend fun deleteSession(
        currentState: AddChatPanelState,
        sessionId: String?,
    ): AddChatPanelState {
        hostState = hostState.deleteSession(sessionId)
        return hostState.toPanelState()
    }

    override suspend fun selectSession(
        currentState: AddChatPanelState,
        sessionId: String,
    ): AddChatPanelState {
        if (hostState.selectedSessionId == sessionId) {
            return hostState.toPanelState()
        }
        delay(260)
        hostState = hostState.selectSession(sessionId)
        return hostState.toPanelState()
    }

    override fun updateInput(
        currentState: AddChatPanelState,
        input: String,
    ): AddChatPanelState {
        hostState = hostState.copy(input = input)
        return hostState.toPanelState()
    }

    override fun usePrompt(
        currentState: AddChatPanelState,
        prompt: AddChatQuickPrompt,
    ): AddChatPanelState {
        val ensuredState = if (hostState.selectedSessionId == null) {
            hostState.createSession()
        } else {
            hostState
        }
        hostState = ensuredState.copy(
            input = prompt.content,
            statusText = "已填入快捷提示“${prompt.title}”，按 Enter 即可继续验证发送流程。",
        )
        return hostState.toPanelState()
    }

    override suspend fun updateConnection(
        currentState: AddChatPanelState,
        connection: AddChatConnectionConfig,
    ): AddChatPanelState {
        hostState = hostState.copy(
            connection = connection,
            statusText = "连接配置已更新，仅用于预览表单与状态展示。",
        )
        return hostState.toPanelState()
    }

    override fun toggleConnectionEditor(
        currentState: AddChatPanelState,
        visible: Boolean,
    ): AddChatPanelState {
        hostState = hostState.copy(
            showConnectionEditor = visible,
            statusText = if (visible) {
                "连接配置面板已展开。"
            } else {
                "连接配置面板已收起。"
            },
        )
        return hostState.toPanelState()
    }

    override suspend fun send(
        currentState: AddChatPanelState,
    ): AddChatPanelState {
        val sessionId = hostState.selectedSessionId ?: return hostState.toPanelState()
        val input = currentState.input.trim()
        if (input.isBlank()) {
            return hostState.toPanelState()
        }
        val userMessageIndex = hostState.nextMessageIndex
        val userMessage = AddChatMessageItem(
            id = "msg-$userMessageIndex",
            role = AddChatMessageRole.User,
            content = input,
            timestampLabel = "刚刚",
            statusLabel = "已写入本地预览",
        )
        val vendor = currentState.connection.vendor
        val model = currentState.connection.model
        hostState = hostState
            .copy(
                input = currentState.input,
                connection = currentState.connection,
            )
            .appendMessages(sessionId, listOf(userMessage))
            .copy(
                input = "",
                nextMessageIndex = userMessageIndex + 1,
                statusText = "正在模拟发送到 ${vendor.displayName}${model.ifBlank { "" }.let { if (it.isBlank()) "" else " / $it" }}。",
            )
        delay(680)
        val replyStartIndex = hostState.nextMessageIndex
        val replyMessages = buildPreviewReplyMessages(
            startIndex = replyStartIndex,
            input = input,
            vendor = vendor,
            model = model,
        )
        hostState = hostState
            .appendMessages(sessionId, replyMessages)
            .copy(
                nextMessageIndex = replyStartIndex + replyMessages.size,
                statusText = "本轮响应已完成，当前仍是纯本地模拟。",
            )
        return hostState.toPanelState()
    }

    override suspend fun retryMessage(
        currentState: AddChatPanelState,
        message: AddChatMessageItem,
    ): AddChatPanelState {
        val sessionId = hostState.selectedSessionId ?: return hostState.toPanelState()
        val replyIndex = hostState.nextMessageIndex
        hostState = hostState.copy(
            statusText = "正在模拟重试消息 ${message.id}。",
        )
        delay(520)
        val retryMessage = AddChatMessageItem(
            id = "msg-$replyIndex",
            role = AddChatMessageRole.Assistant,
            content = buildRetryReply(message),
            timestampLabel = "刚刚",
            statusLabel = "重试成功",
        )
        hostState = hostState
            .appendMessages(sessionId, listOf(retryMessage))
            .copy(
                nextMessageIndex = replyIndex + 1,
                statusText = "已完成一次本地重试演练。",
            )
        return hostState.toPanelState()
    }
}

private data class ChatPreviewConversation(
    val session: AddChatSessionItem,
    val messages: List<AddChatMessageItem>,
)

private data class ChatPreviewHostState(
    val conversations: List<ChatPreviewConversation>,
    val selectedSessionId: String?,
    val quickPrompts: List<AddChatQuickPrompt>,
    val input: String,
    val connection: AddChatConnectionConfig,
    val showConnectionEditor: Boolean,
    val isSending: Boolean,
    val isLoadingMessages: Boolean,
    val statusText: String,
    val nextSessionIndex: Int,
    val nextMessageIndex: Int,
)

private fun buildChatPreviewHostState(): ChatPreviewHostState {
    val conversations = listOf(
        ChatPreviewConversation(
            session = AddChatSessionItem(
                id = "session-1",
                title = "发布回归检查",
                subtitle = "需要确认 KMP 预览入口和 Gradle 任务。",
                badgeText = "4 条消息",
            ),
            messages = listOf(
                AddChatMessageItem(
                    id = "msg-1",
                    role = AddChatMessageRole.System,
                    content = "你是组件库的本地预览助手，需要聚焦界面结构与交互验证。",
                    timestampLabel = "09:12",
                    statusLabel = "系统提示",
                ),
                AddChatMessageItem(
                    id = "msg-2",
                    role = AddChatMessageRole.User,
                    content = "帮我确认聊天组件能不能单独拉起 desktop 做手工验证。",
                    timestampLabel = "09:13",
                    statusLabel = "已发送",
                ),
                AddChatMessageItem(
                    id = "msg-3",
                    role = AddChatMessageRole.Tool,
                    content = "tool://gradle.tasks\n- previewSheetWorkbench\n- previewTable\n- test\n- publishToMavenLocal",
                    timestampLabel = "09:13",
                    statusLabel = "工具输出",
                ),
                AddChatMessageItem(
                    id = "msg-4",
                    role = AddChatMessageRole.Assistant,
                    content = "可以，建议直接沿用 `jvmTest + JavaExec` 的仓库现有做法，避免把预览入口塞进正式产物。",
                    timestampLabel = "09:14",
                    statusLabel = "模拟历史回复",
                ),
            ),
        ),
        ChatPreviewConversation(
            session = AddChatSessionItem(
                id = "session-2",
                title = "失败重试演练",
                subtitle = "保留一条可重试消息，便于验证气泡区操作。",
                badgeText = "2 条消息",
            ),
            messages = listOf(
                AddChatMessageItem(
                    id = "msg-5",
                    role = AddChatMessageRole.User,
                    content = "刚才那次模型请求超时了，再试一次。",
                    timestampLabel = "10:02",
                    statusLabel = "待处理",
                ),
                AddChatMessageItem(
                    id = "msg-6",
                    role = AddChatMessageRole.Assistant,
                    content = "上次响应超时，你可以点击右下角“重试”验证组件的操作入口。",
                    timestampLabel = "10:03",
                    statusLabel = "超时后保留",
                    canRetry = true,
                ),
            ),
        ),
    )
    return ChatPreviewHostState(
        conversations = conversations,
        selectedSessionId = conversations.firstOrNull()?.session?.id,
        quickPrompts = buildPreviewQuickPrompts(),
        input = "",
        connection = AddChatConnectionConfig(
            backendUrl = "http://127.0.0.1:8080/api/ai/chat",
            transport = AddChatTransport.Http,
            vendor = AddChatVendor.OpenAICompatible,
            providerBaseUrl = "https://api.openai.com/v1",
            apiKey = "sk-preview-local",
            model = "gpt-4.1-mini",
            systemPrompt = "你是组件库预览里的本地助手，只返回用于演示界面的模拟内容。",
        ),
        showConnectionEditor = true,
        isSending = false,
        isLoadingMessages = false,
        statusText = "当前为本地预览入口，可直接测试会话切换、配置面板、消息发送和浮层模式。",
        nextSessionIndex = 3,
        nextMessageIndex = 7,
    )
}

private fun buildPreviewQuickPrompts(): List<AddChatQuickPrompt> {
    return listOf(
        AddChatQuickPrompt(
            id = "prompt-1",
            title = "检查 Gradle 任务",
            content = "请列出这个组件预览相关的 Gradle 任务，并说明哪个命令适合手工验证。",
        ),
        AddChatQuickPrompt(
            id = "prompt-2",
            title = "模拟工具调用",
            content = "请先调用 tool 输出一段本地任务清单，再给我一个总结。",
        ),
        AddChatQuickPrompt(
            id = "prompt-3",
            title = "生成回归清单",
            content = "帮我生成一份聊天组件手工回归清单，重点覆盖空态、发送态、重试和连接配置。",
        ),
    )
}

private fun ChatPreviewHostState.toPanelState(): AddChatPanelState {
    val selectedConversation = conversations.firstOrNull { it.session.id == selectedSessionId }
    return AddChatPanelState(
        title = "AI 组件验证台",
        subtitle = "jvmTest 独立 desktop 预览，用于本地手工验证。",
        sessions = conversations.map { it.session },
        selectedSessionId = selectedSessionId,
        messages = selectedConversation?.messages.orEmpty(),
        quickPrompts = quickPrompts,
        input = input,
        connection = connection,
        showConnectionEditor = showConnectionEditor,
        isSending = isSending,
        isLoadingMessages = isLoadingMessages,
        statusText = statusText,
        emptyTitle = if (selectedSessionId == null) {
            "先创建一个预览会话"
        } else {
            "这个会话还没有消息"
        },
        emptyDescription = if (selectedSessionId == null) {
            "点击左侧“新建”，或直接使用快捷提示开始验证输入区。"
        } else {
            "你可以修改连接配置、输入消息，或用快捷提示生成第一轮对话。"
        },
    )
}

private fun ChatPreviewHostState.selectSession(
    sessionId: String,
): ChatPreviewHostState {
    return copy(
        selectedSessionId = sessionId,
        statusText = "当前为本地模拟聊天，可直接验证列表、消息区和配置表单交互。",
    )
}

private fun ChatPreviewHostState.createSession(): ChatPreviewHostState {
    val sessionId = "session-$nextSessionIndex"
    val conversation = ChatPreviewConversation(
        session = AddChatSessionItem(
            id = sessionId,
            title = "新会话 $nextSessionIndex",
            subtitle = "刚刚创建，等待第一条消息。",
            badgeText = "",
        ),
        messages = emptyList(),
    )
    return copy(
        conversations = listOf(conversation) + conversations,
        selectedSessionId = sessionId,
        isLoadingMessages = false,
        statusText = "已创建新会话 $nextSessionIndex，可以直接输入内容验证空态转消息态。",
        nextSessionIndex = nextSessionIndex + 1,
    )
}

private fun ChatPreviewHostState.deleteSession(
    sessionId: String?,
): ChatPreviewHostState {
    if (sessionId == null) {
        return copy(statusText = "当前没有选中会话，删除操作已忽略。")
    }
    val remaining = conversations.filterNot { it.session.id == sessionId }
    val nextSelectedSessionId = if (selectedSessionId == sessionId) {
        remaining.firstOrNull()?.session?.id
    } else {
        selectedSessionId
    }
    return copy(
        conversations = remaining,
        selectedSessionId = nextSelectedSessionId,
        isSending = false,
        isLoadingMessages = false,
        statusText = if (remaining.isEmpty()) {
            "已删除最后一个会话，现在可以验证组件的空会话状态。"
        } else {
            "会话已删除，当前切换到了新的本地预览会话。"
        },
    )
}

private fun ChatPreviewHostState.appendMessages(
    sessionId: String,
    messages: List<AddChatMessageItem>,
): ChatPreviewHostState {
    if (messages.isEmpty()) {
        return this
    }
    val updatedConversations = conversations.map { conversation ->
        if (conversation.session.id != sessionId) {
            return@map conversation
        }
        val mergedMessages = conversation.messages + messages
        conversation.copy(
            session = conversation.session.copy(
                subtitle = mergedMessages.last().content.previewSubtitle(),
                badgeText = "${mergedMessages.size} 条消息",
            ),
            messages = mergedMessages,
        )
    }
    return copy(conversations = updatedConversations)
}

private fun buildPreviewReplyMessages(
    startIndex: Int,
    input: String,
    vendor: AddChatVendor,
    model: String,
): List<AddChatMessageItem> {
    val normalized = input.lowercase()
    val toolMessage = if (
        "tool" in normalized ||
        "任务" in input ||
        "gradle" in normalized ||
        "清单" in input
    ) {
        listOf(
            AddChatMessageItem(
                id = "msg-$startIndex",
                role = AddChatMessageRole.Tool,
                content = "tool://preview.local\n- previewChat\n- jvmTest\n- 手工验证浮层与列表\n- 不触发真实网络请求",
                timestampLabel = "刚刚",
                statusLabel = "模拟工具输出",
            ),
        )
    } else {
        emptyList()
    }
    val assistantMessage = AddChatMessageItem(
        id = "msg-${startIndex + toolMessage.size}",
        role = AddChatMessageRole.Assistant,
        content = buildAssistantReply(
            input = input,
            vendor = vendor,
            model = model,
            includeToolSummary = toolMessage.isNotEmpty(),
        ),
        timestampLabel = "刚刚",
        statusLabel = "${vendor.displayName} 本地模拟",
    )
    return toolMessage + assistantMessage
}

private fun buildAssistantReply(
    input: String,
    vendor: AddChatVendor,
    model: String,
    includeToolSummary: Boolean,
): String {
    val toolSummary = if (includeToolSummary) {
        "我先插入了一段模拟工具输出，方便你验证 Tool 气泡的排版、等宽字体和状态标签。"
    } else {
        "这是一条纯本地生成的模拟回复，不会访问真实后端。"
    }
    val modelLabel = model.ifBlank { "未填写模型名" }
    return """
        $toolSummary

        你刚才输入的是：
        $input

        当前连接配置：
        - 厂商：${vendor.displayName}
        - 模型：$modelLabel

        建议你继续验证：
        1. Enter 发送与组合键换行
        2. 会话切换时的加载条
        3. 浮层模式下的遮罩与关闭动作
    """.trimIndent()
}

private fun buildRetryReply(
    message: AddChatMessageItem,
): String {
    return """
        已完成一次本地重试演练。

        原消息内容摘录：
        ${message.content}

        这个回复用于确认：
        1. “重试”按钮可正常触发
        2. 发送中气泡会出现
        3. 重试成功后会把新消息追加到当前会话尾部
    """.trimIndent()
}

private fun String.previewSubtitle(): String {
    return replace("\n", " ")
        .take(28)
        .let { value ->
            if (length > 28) {
                "$value..."
            } else {
                value
            }
        }
}
