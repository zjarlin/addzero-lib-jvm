package site.addzero.component.chat

/**
 * 聊天面板 SPI。
 *
 * 外部只要实现这个接口，就可以把会话加载、消息发送、
 * 重试、连接配置持久化等能力接入到 `AddChatPanel`。
 */
interface AddChatPanelSpi {
    /**
     * 加载聊天面板初始状态。
     */
    suspend fun loadState(): AddChatPanelState

    /**
     * 新建会话。
     *
     * 默认直接返回当前状态，适合只读或单会话场景。
     */
    suspend fun createSession(
        currentState: AddChatPanelState,
    ): AddChatPanelState {
        return currentState
    }

    /**
     * 删除会话。
     *
     * 默认直接返回当前状态，适合不开放删除入口的场景。
     */
    suspend fun deleteSession(
        currentState: AddChatPanelState,
        sessionId: String?,
    ): AddChatPanelState {
        return currentState
    }

    /**
     * 切换当前会话。
     *
     * 默认只切换选中项，不主动重新拉取消息。
     */
    suspend fun selectSession(
        currentState: AddChatPanelState,
        sessionId: String,
    ): AddChatPanelState {
        return currentState.copy(selectedSessionId = sessionId)
    }

    /**
     * 更新输入框草稿。
     *
     * 默认只在本地状态里更新输入内容。
     */
    fun updateInput(
        currentState: AddChatPanelState,
        input: String,
    ): AddChatPanelState {
        return currentState.copy(input = input)
    }

    /**
     * 使用快捷提示词。
     *
     * 默认把提示词内容直接填入输入框。
     */
    fun usePrompt(
        currentState: AddChatPanelState,
        prompt: AddChatQuickPrompt,
    ): AddChatPanelState {
        return updateInput(
            currentState = currentState,
            input = prompt.content,
        )
    }

    /**
     * 更新连接配置。
     *
     * 默认只在本地状态里覆盖连接配置。
     */
    suspend fun updateConnection(
        currentState: AddChatPanelState,
        connection: AddChatConnectionConfig,
    ): AddChatPanelState {
        return currentState.copy(connection = connection)
    }

    /**
     * 展开或收起连接配置面板。
     *
     * 默认只更新本地开关状态。
     */
    fun toggleConnectionEditor(
        currentState: AddChatPanelState,
        visible: Boolean,
    ): AddChatPanelState {
        return currentState.copy(showConnectionEditor = visible)
    }

    /**
     * 发送当前输入框消息。
     *
     * 实现方需要返回发送完成后的最新聊天状态。
     */
    suspend fun send(
        currentState: AddChatPanelState,
    ): AddChatPanelState

    /**
     * 重试指定消息。
     *
     * 默认直接返回当前状态，适合不开放重试能力的场景。
     */
    suspend fun retryMessage(
        currentState: AddChatPanelState,
        message: AddChatMessageItem,
    ): AddChatPanelState {
        return currentState
    }
}
