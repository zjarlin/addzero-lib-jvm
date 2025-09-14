package site.addzero.ai.chat

import site.addzero.ai.config.gettooldef
import io.swagger.v3.oas.annotations.Operation
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.tool.definition.ToolDefinition
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/chat")
class ChatController(
//    private val vectorStore: VectorStore,
//    private val chatmodel :ChatModel,
    private val chatClient: ChatClient,
    private val chatModels: List<ChatModel>,
//    private val methodToolCallback: List<MethodToolCallback>,

    private val methodToolCallbackProvider: MethodToolCallbackProvider,
//    private val imageModel: ImageModel,
) {
//    methodToolCallbackProvider

    @GetMapping("gettools")
    @Operation(summary = "获取所有工具定义")
    fun gettools(): List<ToolDefinition> {
        val gettooldef = gettooldef()
        return gettooldef
    }

    @PostMapping("/ask")
    fun ask(text: String): String? {
        val call = chatClient.prompt().user {
            it.text(text)
        }.call().content()
        return call

    }


}
