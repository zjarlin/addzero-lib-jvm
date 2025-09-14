package site.addzero.entity.ai

import site.addzero.constant.Promts
import site.addzero.constant.Promts.DEFAULT_SYSTEM


data class ChatDTO(
    val modelName: String = "ollamaChatModel",
    val prompt: String = "",
    val sessionId: String = "",
    val enableVectorStore: Boolean = false,
    val enableFunctionCalling: Boolean = false,
    val cosplay: String = DEFAULT_SYSTEM,

    val fomatJson: String? = "",
    val jsonComment: String? = "",
) {

    val jsonPromt: String = """
   ${Promts.JSON_PATTERN_PROMPT}
       $fomatJson
        ------------------
       以下是json数据的注释：
       $jsonComment 
    """.trimIndent()
}
