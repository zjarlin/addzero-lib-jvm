package site.addzero.aop.dicttrans.strategy

import cn.hutool.core.text.CharSequenceUtil.isBlank
import cn.hutool.core.util.StrUtil
import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.aop.dicttrans.inter.TransApi
import site.addzero.aop.dicttrans.inter.TransStrategy
import org.springframework.stereotype.Component

/**
 * @author zjarlin
 * @since 2023/11/8 11:05
 */
@Component
open class StringStrategy(private val transApi: TransApi) : TransStrategy<String> {
//    private val spelContextMap: MutableMap<String, Any> = HashMap<String, Any>()

    lateinit var dict: Dict



    public override fun trans(s: String): String {
        if (isBlank(s)) {
            return s
        }
        return extractSingleAttributeTranslation(s, dict)
    }

    override fun support(t: Any): Boolean {
        return String::class.java.isAssignableFrom(t.javaClass)
    }

    private fun <T> extractSingleAttributeTranslation(fieldRuntimeValue: T, dict: Dict): String {
        val dictCode: String = dict.dicCode.ifBlank { dict.value }
        val tab: String = dict.tab
        var codeColumn = dict.codeColumn
        var nameColumn = dict.nameColumn
        val spelExp: String = dict.spelExp
        if (StrUtil.isAllBlank(dictCode, tab, codeColumn, nameColumn)) {
            return fieldRuntimeValue.toString()
        }
        val fieldRuntimeStrValue = fieldRuntimeValue.toString()
        codeColumn = StrUtil.toUnderlineCase(codeColumn)
        nameColumn = StrUtil.toUnderlineCase(nameColumn)
        //dictCode不空 这仨参数全是空说明是内置字典翻译
        val isUseSysDefaultDict = StrUtil.isNotBlank(dictCode) && StrUtil.isAllBlank(tab, codeColumn, nameColumn)
        val string: String = fieldRuntimeStrValue
        val isMulti: Boolean = StrUtil.contains(string, ",")
        val useSpel = StrUtil.isNotBlank(spelExp)
        var retStr = ""

        if (!isUseSysDefaultDict ) {
            val translateTableBatchCode2name =
                transApi.translateTableBatchCode2name(table = tab, text = nameColumn, code = codeColumn, keys = fieldRuntimeStrValue)
            val joinToString = translateTableBatchCode2name.joinToString(",") {
             it[nameColumn].toString()
            }
            retStr = joinToString
        }


        if (isUseSysDefaultDict ) {
            val translateDictBatchCode2name = transApi.translateDictBatchCode2name(dictCode, fieldRuntimeStrValue,)
            val associate = translateDictBatchCode2name.associate { it.value to it.label }
            val joinToString1 = fieldRuntimeStrValue.split(",").joinToString("") { associate[it].toString() }
            retStr =joinToString1
        }
        //多表翻译
//        if (isUseSysDefaultDict && isMulti) {
//            val stringListMap = transApi
//            .translateDictBatchCode2name (dictCode, fieldRuntimeStrValue)
//            .groupBy { it.dictCode }
//            val dictModels = stringListMap[dictCode]
//            retStr = dictModels?.joinToString(",") { it.label } ?: ""
//        }
//        if (useSpel) {
//            val aClass= dict.spelValueType
//            val o = SpELUtils.evaluateExpression(fieldRuntimeValue, spelContextMap, spelExp, aClass)
//            retStr = o.toString()
//        }
        return retStr
    }
}
