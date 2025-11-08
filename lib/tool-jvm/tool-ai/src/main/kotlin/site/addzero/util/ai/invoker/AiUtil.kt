package site.addzero.util.ai.invoker

import cn.hutool.core.util.ReflectUtil
import com.alibaba.fastjson2.parseObject
import com.alibaba.fastjson2.toJSONString
import site.addzero.util.ai.agent.dbdesign.FieldDTOUseDocList
import site.addzero.util.ai.consts.ChatModels
import site.addzero.util.ai.inter.SettingContext
import site.addzero.util.metainfo.MetaInfoUtils
import java.util.stream.Collectors

abstract class AiUtil(
    // 将 SettingContext 作为构造函数参数
    protected val settingContext: SettingContext,
    private val modelName: String, // 从 settingContext 获取 modelName
    protected val question: String,
    protected val promptTemplate: String = "",
) {
    // 添加构造函数重载，只需要 SettingContext，其他参数有默认值
    constructor(settingContext: SettingContext, question: String, promptTemplate: String = "") : this(
        settingContext,
        settingContext.settings.modelNameOnline,
        question,
        promptTemplate
    )

    /**
     * 构建结构化输出提示
     * @param [fieldComment] 字段注释
     * @return [String]
     */
    private fun buildStructureOutPutPrompt(fieldComment: Map<String, String>): String {
        return fieldComment.entries.joinToString { "${it.key}:${it.value}" }
    }

    /**
     * 构建结构化输出提示
     * @param [formatClass] 类
     * @return [String]
     */
    private fun <T> buildStructureOutPutPrompt(formatClass: Class<T>?): String {
        return if (formatClass != null) "Expected output: ${formatClass.simpleName}" else ""
    }


    /**
     * 生成答案的抽象方法，需要由子类实现
     * @param [input] 输入数据
     *
     * @return [String] 答案
     */
    abstract fun ask(clazz: Class<*>): String

    /**
     * 生成答案的抽象方法，需要由子类实现
     * @param [input] 输入数据
     * @return [String] 答案
     */
    abstract fun ask(json: String, comment: String): String


    companion object {

        // 根据modelName动态返回子类实例的静态方法
        fun INIT(settingContext: SettingContext, modelName: String, question: String, promptTemplate: String = ""): AiUtil {
            return when (modelName) {
                ChatModels.OLLAMA -> OllamaAiUtil(settingContext, modelName, question, promptTemplate)
                ChatModels.DASH_SCOPE -> DashScopeAiUtil(settingContext, modelName, question, promptTemplate)
                ChatModels.DeepSeek -> DeepSeekAiUtil(settingContext, modelName, question, promptTemplate)
                else -> throw IllegalArgumentException("Unknown modelName: $modelName")
            }
        }


        fun INIT(settingContext: SettingContext, question: String, promptTemplate: String = ""): AiUtil {
            val settings = settingContext.settings
            val modelManufacturer = settings.modelManufacturer
            val modelNameOffline = settings.modelNameOffline
            val modelNameOnline = settings.modelNameOnline
            return when (modelManufacturer) {
                ChatModels.OLLAMA -> OllamaAiUtil(settingContext, modelNameOffline, question, promptTemplate)
                ChatModels.DASH_SCOPE -> DashScopeAiUtil(settingContext, modelNameOnline, question, promptTemplate)
                ChatModels.DeepSeek -> DeepSeekAiUtil(settingContext, modelNameOnline, question, promptTemplate)
                else -> throw IllegalArgumentException("Unknown modelName")
            }
        }


        fun buildStructureOutPutPrompt(fieldComment: Map<String, String>): String {
            val collect2 = fieldComment.entries.stream().map { e: Map.Entry<String, String> ->
                val key = e.key
                val value = e.value
                val s = "$key:$value"
                s
            }.collect(Collectors.joining(System.lineSeparator()))
            return collect2
        }


        fun buildStructureOutPutPrompt(clazz: Class<*>?): String {
            if (clazz == null) {
                return ""
            }
            val fieldInfosRecursive = MetaInfoUtils.getSimpleFieldInfoStr(clazz)
// 收集所有字段及其描述
            val fieldDescriptions = StringBuilder()
            val fields = ReflectUtil.getFields(clazz)
// 过滤带有 @field:JsonPropertyDescription 注解的字段
            val ret = emptyList<String>()
// 返回生成的描述信息
            val prompt = """
结构化输出字段定义 内容如下:
$fieldInfosRecursive
""".trimIndent()
            return prompt
        }

        fun batchGetComments(settingContext: SettingContext, noCommentFields: MutableMap<String, Any>): Map<out String?, String>? {
            val keys = noCommentFields.keys
            val associate: Map<String, String> = keys.associateWith { it }

            val ask = AiUtil.INIT(
                settingContext,
                keys.toJSONString(), """
中括号中的字段名尽可能的推测生成对应的注释信息，字段可能是拼音命名风格,也可能是英文风格,实在推测不出字段什么意思的可以返回空字符串: 
            """.trimIndent()
            ).ask(FieldDTOUseDocList::class.java)

            try {
                val translated = ask.parseObject<FieldDTOUseDocList> ()

                val fieldInfo = translated.fieldInfo
                if (fieldInfo?.isEmpty() == true) {
                    return null
                }

                val associate = fieldInfo?.associate { it.fieldName to it.fieldChineseName }
                return associate
            } catch (e: Exception) {
                return associate
            }


        }


//        fun toEventStream(spec: ChatClientRequestSpec): Flux<ServerSentEvent<String>> {
//            val httpServletResponse: HttpServletResponse? = SpringUtil.getBean(HttpServletResponse::class.java)
//            if (httpServletResponse != null) {
//                httpServletResponse.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
//            }
//
//            return spec.stream().chatResponse().map { chatResponse: ChatResponse ->
//                ServerSentEvent.builder(chatResponse.toJson()).event("message").build()
//            }
//        }

    }

}
