package site.addzero.component.chat

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddChatPanelControllerTest {
    @Test
    fun latestLoadStateWinsWhenPreviousLoadReturnsLater() = runTest {
        var loadCount = 0
        val spi = FakeAddChatPanelSpi(
            loadHandler = {
                loadCount += 1
                if (loadCount == 1) {
                    delay(100)
                    samplePanelState(title = "first-load")
                } else {
                    samplePanelState(title = "second-load")
                }
            },
        )
        val controller = AddChatPanelController()

        controller.bind(
            scope = this,
            spi = spi,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveActionErrorMessage = { it.message ?: "动作失败" },
        )

        controller.load()
        controller.load()
        advanceUntilIdle()

        assertEquals("second-load", controller.state.title)
        assertFalse(controller.loading)
    }

    @Test
    fun sendUsesReturnedStateAndClearsSendingFlag() = runTest {
        val spi = FakeAddChatPanelSpi(
            loadHandler = {
                samplePanelState(
                    input = "请生成一份回归清单",
                )
            },
            sendHandler = { currentState ->
                delay(60)
                currentState.copy(
                    input = "",
                    messages = currentState.messages + AddChatMessageItem(
                        id = "assistant-1",
                        role = AddChatMessageRole.Assistant,
                        content = "这里是一份来自外部 SPI 的模拟回复。",
                        timestampLabel = "刚刚",
                    ),
                    statusText = "发送完成",
                )
            },
        )
        val controller = AddChatPanelController()

        controller.bind(
            scope = this,
            spi = spi,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveActionErrorMessage = { it.message ?: "动作失败" },
        )

        controller.load()
        advanceUntilIdle()
        controller.send()

        assertTrue(controller.state.isSending)
        advanceUntilIdle()

        assertFalse(controller.state.isSending)
        assertEquals("", controller.state.input)
        assertEquals(2, controller.state.messages.size)
        assertEquals("发送完成", controller.state.statusText)
    }

    @Test
    fun latestConnectionChangeWinsWhenEarlierSaveReturnsLater() = runTest {
        val spi = FakeAddChatPanelSpi(
            loadHandler = { samplePanelState() },
            updateConnectionHandler = { currentState, connection ->
                if (connection.backendUrl.contains("slow")) {
                    delay(100)
                }
                currentState.copy(
                    connection = connection.copy(
                        model = if (connection.backendUrl.contains("fast")) {
                            "fast-model"
                        } else {
                            "slow-model"
                        },
                    ),
                )
            },
        )
        val controller = AddChatPanelController()

        controller.bind(
            scope = this,
            spi = spi,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveActionErrorMessage = { it.message ?: "动作失败" },
        )

        controller.load()
        advanceUntilIdle()
        controller.updateConnection(
            controller.state.connection.copy(
                backendUrl = "http://slow-host",
            ),
        )
        controller.updateConnection(
            controller.state.connection.copy(
                backendUrl = "http://fast-host",
            ),
        )
        advanceUntilIdle()

        assertEquals("http://fast-host", controller.state.connection.backendUrl)
        assertEquals("fast-model", controller.state.connection.model)
    }
}

private class FakeAddChatPanelSpi(
    private val loadHandler: suspend () -> AddChatPanelState = { samplePanelState() },
    private val sendHandler: suspend (AddChatPanelState) -> AddChatPanelState = { it },
    private val updateConnectionHandler: suspend (AddChatPanelState, AddChatConnectionConfig) -> AddChatPanelState = { currentState, connection ->
        currentState.copy(connection = connection)
    },
) : AddChatPanelSpi {
    override suspend fun loadState(): AddChatPanelState {
        return loadHandler()
    }

    override suspend fun updateConnection(
        currentState: AddChatPanelState,
        connection: AddChatConnectionConfig,
    ): AddChatPanelState {
        return updateConnectionHandler(currentState, connection)
    }

    override suspend fun send(
        currentState: AddChatPanelState,
    ): AddChatPanelState {
        return sendHandler(currentState)
    }
}

private fun samplePanelState(
    title: String = "测试聊天",
    input: String = "",
): AddChatPanelState {
    return AddChatPanelState(
        title = title,
        subtitle = "SPI 测试场景",
        sessions = listOf(
            AddChatSessionItem(
                id = "session-1",
                title = "默认会话",
            ),
        ),
        selectedSessionId = "session-1",
        messages = listOf(
            AddChatMessageItem(
                id = "user-1",
                role = AddChatMessageRole.User,
                content = "hello",
                timestampLabel = "09:00",
            ),
        ),
        quickPrompts = emptyList(),
        input = input,
        connection = AddChatConnectionConfig(
            backendUrl = "http://127.0.0.1:8080",
            transport = AddChatTransport.Http,
            vendor = AddChatVendor.OpenAICompatible,
            providerBaseUrl = "https://api.openai.com/v1",
            apiKey = "test-key",
            model = "gpt-test",
            systemPrompt = "test",
        ),
        statusText = "ready",
    )
}
