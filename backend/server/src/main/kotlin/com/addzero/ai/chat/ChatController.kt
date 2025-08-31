package com.addzero.ai.chat

import com.addzero.ai.config.ChatModels
import com.addzero.ai.config.gettooldef
import com.addzero.ai.util.ai.ai_abs_builder.AiUtil
import com.addzero.constant.Promts.DBASIMPLE_JSON_PATTERN_PROMPT_CHINESE
import com.addzero.entity.ai.FormDTO
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


    @GetMapping("/dbatest")
    @Operation(summary = "设计表test")
    fun jdaoisdj(): FormDTO? {
        val ask = AiUtil(
            ChatModels.QWEN2_5_CODER_0_5B, "设计一张用户表", DBASIMPLE_JSON_PATTERN_PROMPT_CHINESE
        ).ask(FormDTO::class.java)
        return ask
    }

    @PostMapping("/chatClient")
    fun chat(text: String): String? {
        val call = chatClient.prompt().user {
            it.text(text)
        }.call().content()
        return call

    }


}
