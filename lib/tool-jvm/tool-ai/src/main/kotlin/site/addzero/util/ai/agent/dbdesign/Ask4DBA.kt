package site.addzero.util.ai.agent.dbdesign
import site.addzero.util.ai.invoker.AiUtil
import com.alibaba.fastjson2.parseObject
import site.addzero.util.ai.consts.Promts.DBA
import site.addzero.util.ai.inter.SettingContext
import site.addzero.util.str.isNull


data class Qwendto(
    val model: String,
    val messages: List<MyMessage>,
)


data class MyMessage(
    val role: String = "",
    val content: String = "",
)


fun quesDba(question: String, settingContext: SettingContext): FormDTO? {
    if (question.isBlank()) {
        return defaultdTO()
    }
    try {
        val init = AiUtil.INIT(settingContext,question, DBA)
        val dbask = init.ask(FormDTO::class.java)
        val parseObject1 = dbask.parseObject<FormDTO>()

        if (parseObject1.isNull()&&dbask.isNotBlank()) {
            throw RuntimeException("解析出错了但AI原始回答Json为：$dbask")
        }

        return parseObject1
    } catch (e: Exception) {
        e.printStackTrace()
        return defaultdTO()
    }
}

fun defaultdTO(): FormDTO {
    val fieldDTO = FieldDTO("String", "字段名", "字段注释")
    return FormDTO("示例表名", "示例英文名", "示例数据库类型", "示例数据库名称", listOf(fieldDTO))
}
