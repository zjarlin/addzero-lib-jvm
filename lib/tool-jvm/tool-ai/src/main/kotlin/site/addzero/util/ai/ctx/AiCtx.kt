package site.addzero.util.ai.ctx

import cn.hutool.core.util.ReflectUtil
import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson2.JSON
import site.addzero.util.ai.invoker.AiUtil
import site.addzero.util.ai.consts.Promts
import site.addzero.util.str.addPrefixIfNot
import site.addzero.util.str.cleanBlank
import site.addzero.util.str.isBlank
import site.addzero.util.str.isNotBlank

object AiCtx {

    /**
     * 结构化输出上下文
     * @param [question] 问题
     * @param [promptTemplate] 提示模板
     * @param [clazz] 提供类以自动生成 formatJson 和 jsonComment（可选）
     * @return [StructureOutPutPrompt]
     */
    fun structuredOutputContext(
        question: String,
        promptTemplate: String,
        clazz: Class<*>,
    ): StructureOutPutPrompt {
        val finalFormatJson = JSON.toJSONString(ReflectUtil.newInstance(clazz))

        val finalJsonComment = AiUtil.Companion.buildStructureOutPutPrompt(clazz)
        // 生成结构化输出的内容
        return structuredOutputContext(
            question.cleanBlank(), promptTemplate.cleanBlank(), finalFormatJson, finalJsonComment
        )
    }


    fun structuredOutputContext(
        question: String,
        promptTemplate: String?,
        formatJson: String?,
        jsonComment: String?,
    ): StructureOutPutPrompt {


        val que = "the context question is :  {question}---"
        if (promptTemplate.isBlank() && formatJson.isBlank()) {
            val promptTemplate1 = promptTemplate.cleanBlank()
            val promptTemplate2 = StrUtil.addPrefixIfNot(promptTemplate1, que)
            return StructureOutPutPrompt(promptTemplate2, mapOf("question" to question))
        }
        if (promptTemplate.isBlank() && formatJson.isNotBlank()) {
            val promptTemplate1 = promptTemplate.cleanBlank()
            val promptTemplate2 = StrUtil.addPrefixIfNot(promptTemplate1, que)
            return StructureOutPutPrompt(promptTemplate2, mapOf("question" to question))
        }

        val formatPrompt: String = """
       ${Promts.JSON_PATTERN_PROMPT}
           {formatJson}}
            ------------------
           以下是json数据的注释：
           {jsonComment}
        """.trimIndent()
        val newPromptTemplate = promptTemplate + formatPrompt
        val quesCtx = mapOf(
            "question" to question, "formatJson" to formatJson, "jsonComment" to jsonComment
        )
        return StructureOutPutPrompt(newPromptTemplate.addPrefixIfNot(que), quesCtx)
    }

    data class StructureOutPutPrompt(val newPrompt: String, val quesCtx: Map<String, String?>)


}
