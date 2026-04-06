package site.addzero.component.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 聊天面板控制器。
 *
 * 它负责把 `AddChatPanelSpi` 暴露出来的外部能力，
 * 收敛成 `AddChatPanel` 可以直接消费的 `state + actions`。
 */
@Stable
class AddChatPanelController internal constructor(
    initialState: AddChatPanelState = addChatPanelLoadingState(),
) {
    private lateinit var scope: CoroutineScope
    private lateinit var spi: AddChatPanelSpi
    private var resolveLoadErrorMessage: (Throwable) -> String = { it.message ?: "加载聊天面板失败" }
    private var resolveActionErrorMessage: (Throwable) -> String = { it.message ?: "聊天操作失败" }

    private var loadRequestToken by mutableStateOf(0)
    private var mutationRequestToken by mutableStateOf(0)

    var state by mutableStateOf(initialState)
        private set

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    internal fun bind(
        scope: CoroutineScope,
        spi: AddChatPanelSpi,
        resolveLoadErrorMessage: (Throwable) -> String,
        resolveActionErrorMessage: (Throwable) -> String,
    ) {
        this.scope = scope
        this.spi = spi
        this.resolveLoadErrorMessage = resolveLoadErrorMessage
        this.resolveActionErrorMessage = resolveActionErrorMessage
    }

    fun load() {
        val requestToken = ++loadRequestToken
        loading = true
        errorMessage = null
        state = state.copy(
            isLoadingMessages = state.messages.isEmpty(),
            statusText = state.statusText.ifBlank { "正在加载聊天配置..." },
        )

        scope.launch {
            runCatching {
                spi.loadState()
            }.onSuccess { nextState ->
                if (requestToken != loadRequestToken) {
                    return@launch
                }

                loading = false
                errorMessage = null
                state = nextState.copy(
                    isLoadingMessages = false,
                    isSending = false,
                )
            }.onFailure { error ->
                if (requestToken != loadRequestToken) {
                    return@launch
                }

                loading = false
                val message = resolveLoadErrorMessage(error)
                errorMessage = message
                state = rollbackStateWithError(
                    rollbackState = state,
                    message = message,
                )
            }
        }
    }

    fun reload() {
        load()
    }

    fun updateInput(input: String) {
        applyLocalStateChange {
            spi.updateInput(
                currentState = state,
                input = input,
            )
        }
    }

    fun usePrompt(prompt: AddChatQuickPrompt) {
        applyLocalStateChange {
            spi.usePrompt(
                currentState = state,
                prompt = prompt,
            )
        }
    }

    fun toggleConnectionEditor(visible: Boolean) {
        applyLocalStateChange {
            spi.toggleConnectionEditor(
                currentState = state,
                visible = visible,
            )
        }
    }

    fun createSession() {
        mutate(
            optimisticState = state.copy(statusText = "正在创建会话..."),
            execute = { optimisticState ->
                spi.createSession(optimisticState)
            },
        )
    }

    fun deleteSession(sessionId: String?) {
        if (sessionId == null) {
            return
        }

        mutate(
            optimisticState = state.copy(statusText = "正在删除会话..."),
            execute = { optimisticState ->
                spi.deleteSession(
                    currentState = optimisticState,
                    sessionId = sessionId,
                )
            },
        )
    }

    fun selectSession(sessionId: String) {
        if (sessionId.isBlank()) {
            return
        }

        mutate(
            rollbackState = state,
            optimisticState = state.copy(
                selectedSessionId = sessionId,
                isLoadingMessages = true,
            ),
            execute = { optimisticState ->
                spi.selectSession(
                    currentState = optimisticState,
                    sessionId = sessionId,
                )
            },
            normalizeSuccess = { nextState ->
                nextState.copy(isLoadingMessages = false)
            },
        )
    }

    fun updateConnection(connection: AddChatConnectionConfig) {
        val rollbackState = state
        val optimisticState = state.copy(connection = connection)
        mutate(
            rollbackState = rollbackState,
            optimisticState = optimisticState,
            execute = { currentState ->
                spi.updateConnection(
                    currentState = currentState,
                    connection = connection,
                )
            },
        )
    }

    fun send() {
        if (state.input.isBlank() || state.isSending) {
            return
        }

        mutate(
            rollbackState = state,
            optimisticState = state.copy(isSending = true),
            execute = { optimisticState ->
                spi.send(optimisticState)
            },
            normalizeSuccess = { nextState ->
                nextState.copy(isSending = false)
            },
        )
    }

    fun retryMessage(message: AddChatMessageItem) {
        if (state.isSending) {
            return
        }

        mutate(
            rollbackState = state,
            optimisticState = state.copy(isSending = true),
            execute = { optimisticState ->
                spi.retryMessage(
                    currentState = optimisticState,
                    message = message,
                )
            },
            normalizeSuccess = { nextState ->
                nextState.copy(isSending = false)
            },
        )
    }

    fun clearError() {
        errorMessage = null
    }

    /**
     * 执行本地同步状态变更，例如输入框草稿、快捷提示词或配置面板开关。
     */
    private fun applyLocalStateChange(
        transform: () -> AddChatPanelState,
    ) {
        val rollbackState = state
        runCatching(transform).onSuccess { nextState ->
            errorMessage = null
            state = nextState
        }.onFailure { error ->
            val message = resolveActionErrorMessage(error)
            errorMessage = message
            state = rollbackStateWithError(
                rollbackState = rollbackState,
                message = message,
            )
        }
    }

    /**
     * 执行异步聊天动作，并保证“最后一次请求结果生效”。
     */
    private fun mutate(
        rollbackState: AddChatPanelState = state,
        optimisticState: AddChatPanelState = rollbackState,
        execute: suspend (AddChatPanelState) -> AddChatPanelState,
        normalizeSuccess: (AddChatPanelState) -> AddChatPanelState = { it },
    ) {
        val requestToken = ++mutationRequestToken
        errorMessage = null
        state = optimisticState

        scope.launch {
            runCatching {
                execute(optimisticState)
            }.onSuccess { nextState ->
                if (requestToken != mutationRequestToken) {
                    return@launch
                }

                errorMessage = null
                state = normalizeSuccess(nextState)
            }.onFailure { error ->
                if (requestToken != mutationRequestToken) {
                    return@launch
                }

                val message = resolveActionErrorMessage(error)
                errorMessage = message
                state = rollbackStateWithError(
                    rollbackState = rollbackState,
                    message = message,
                )
            }
        }
    }
}

/**
 * `AddChatPanel` 直接可消费的绑定结果。
 */
data class AddChatPanelBinding(
    val state: AddChatPanelState,
    val actions: AddChatPanelActions,
    val controller: AddChatPanelController,
)

/**
 * 记住一个聊天面板控制器，并把外部 SPI 绑定进去。
 */
@Composable
fun rememberAddChatPanelController(
    spi: AddChatPanelSpi,
    initialState: AddChatPanelState = addChatPanelLoadingState(),
    autoLoad: Boolean = true,
    reloadKey: Any? = spi,
    resolveLoadErrorMessage: (Throwable) -> String = { it.message ?: "加载聊天面板失败" },
    resolveActionErrorMessage: (Throwable) -> String = { it.message ?: "聊天操作失败" },
): AddChatPanelController {
    val scope = rememberCoroutineScope()
    val controller = remember(initialState) {
        AddChatPanelController(initialState)
    }

    controller.bind(
        scope = scope,
        spi = spi,
        resolveLoadErrorMessage = resolveLoadErrorMessage,
        resolveActionErrorMessage = resolveActionErrorMessage,
    )

    LaunchedEffect(
        controller,
        autoLoad,
        reloadKey,
    ) {
        if (autoLoad) {
            controller.load()
        }
    }

    return controller
}

/**
 * 记住一个聊天面板绑定结果。
 */
@Composable
fun rememberAddChatPanelBinding(
    spi: AddChatPanelSpi,
    initialState: AddChatPanelState = addChatPanelLoadingState(),
    autoLoad: Boolean = true,
    reloadKey: Any? = spi,
    resolveLoadErrorMessage: (Throwable) -> String = { it.message ?: "加载聊天面板失败" },
    resolveActionErrorMessage: (Throwable) -> String = { it.message ?: "聊天操作失败" },
): AddChatPanelBinding {
    val controller = rememberAddChatPanelController(
        spi = spi,
        initialState = initialState,
        autoLoad = autoLoad,
        reloadKey = reloadKey,
        resolveLoadErrorMessage = resolveLoadErrorMessage,
        resolveActionErrorMessage = resolveActionErrorMessage,
    )
    val actions = remember(controller) {
        AddChatPanelActions(
            onCreateSession = controller::createSession,
            onDeleteSession = controller::deleteSession,
            onSelectSession = controller::selectSession,
            onInputChange = controller::updateInput,
            onSend = controller::send,
            onRetryMessage = controller::retryMessage,
            onUsePrompt = controller::usePrompt,
            onConnectionChange = controller::updateConnection,
            onToggleConnectionEditor = controller::toggleConnectionEditor,
        )
    }
    return AddChatPanelBinding(
        state = controller.state,
        actions = actions,
        controller = controller,
    )
}

private fun addChatPanelLoadingState(): AddChatPanelState {
    return AddChatPanelState(
        statusText = "正在加载聊天配置...",
        isLoadingMessages = true,
        emptyTitle = "正在准备聊天面板",
        emptyDescription = "请稍候，正在加载会话与连接配置。",
    )
}

/**
 * 回滚到安全可展示状态，避免发送中或加载中状态卡死在界面上。
 */
private fun rollbackStateWithError(
    rollbackState: AddChatPanelState,
    message: String,
): AddChatPanelState {
    return rollbackState.copy(
        isSending = false,
        isLoadingMessages = false,
        statusText = message,
    )
}
