package site.addzero.aop.dicttrans.util_internal

import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.aop.dicttrans.dictaop.CommonConstant
import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import site.addzero.aop.dicttrans.dictaop.entity.TabMultiIn
import site.addzero.aop.dicttrans.dictaop.entity.TransInfo
import site.addzero.aop.dicttrans.inter.TransApi
import site.addzero.aop.dicttrans.util.ObjUtil
import site.addzero.aop.dicttrans.util.SpringUtil
import org.springframework.core.annotation.AnnotatedElementUtils
import site.addzero.util.ImprovedReflectUtil
import site.addzero.util.RefUtil
import site.addzero.util.str.containsAny
import site.addzero.util.str.isNotBlank
import site.addzero.util.str.toCamelCase
import java.util.*

/**
 * 内部翻译api
 * @author zjarlin
 * @date 2025/08/06
 */
internal object TransInternalUtil {


    fun process(rootObj: Any): List<TransInfo<Dict>> {
        val result = mutableListOf<TransInfo<Dict>>()
        // 使用队列进行广度优先遍历，避免深层递归
        val queue = LinkedList<Any>()
        queue.add(rootObj)

        // 对象去重，避免循环引用和重复处理
        val processedObjects = mutableSetOf<Any>()

        while (queue.isNotEmpty()) {
            val currentObj = queue.poll()

            // 避免重复处理相同对象和null对象
            if (currentObj == null || !processedObjects.add(currentObj)) {
                continue
            }

            val aClass: Class<*> = currentObj.javaClass

            // 遍历所有字段
            for (field in ImprovedReflectUtil.getFields(aClass)) {
                field.isAccessible = true
                val fieldValue = ImprovedReflectUtil.getFieldValue(currentObj, field)
                if (ObjUtil.isEmpty(fieldValue)) {
                    continue
                }

                // 检查字段是否有 Dict 注解
                val dictAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, Dict::class.java)
                if (dictAnnotations.isNotEmpty()) {
                    dictAnnotations.forEach { anno ->
                        val alias = anno.serializationAlias
                        val nameColumn = anno.nameColumn.toCamelCase()
                        val other = field.name + CommonConstant.DICT_TEXT_SUFFIX
                        val resolvedName = firstNonBlank(alias, nameColumn, other) ?: other

                        val transInfo = TransInfo(
                            superObjectFieldTypeEnum = null,
                            superObjectFieldName = null,
                            superObject = null,
                            fieldEnum = null,
                            anno = anno,
                            translationProcess = null,
                            rootObject = currentObj,
                            afterObject = null,
                            afterObjectClass = null,
                            translatedAttributeNames = resolvedName,
                            attributeNameBeforeTranslation = field.name,
                            valueBeforeTranslation = fieldValue ?: "",
                            translatedValue = null,
                            translatedType = anno.spelValueType.java,
                            classificationOfTranslation = null,
                            rootObjectHashBsm = null
                        )
                        result.add(transInfo.copy(classificationOfTranslation = getTranslateType(transInfo)))
                    }
                }

                when {
                    RefUtil.isCollectionField(field) -> {
                        val collection = fieldValue as? Collection<*>
                        collection?.filterNotNull()?.forEach(queue::add)
                    }

                    fieldValue != null && RefUtil.isT(fieldValue) -> {
                        queue.add(fieldValue)
                    }
                }
            }
        }

        return result
    }


    /**
     * 0代表内置字典多翻译
     * 1内置字典单翻译
     * 2任意表多翻译
     * 3任意表单翻译
     * 4spel表达式
     * -1其他
     *
     * @param transInfo 入参
     * @return int
     * @author zjarlin
     * @since 2023/10/19
     */
    fun getTranslateType(transInfo: TransInfo<Dict>): Int {
        val anno = transInfo.anno

        /** 翻译前的值  */
        val valueBeforeTranslation: Any = transInfo.valueBeforeTranslation
        val string = valueBeforeTranslation.toString()

        val isMulti = string.containsAny(",")
        val dictCode = anno.dicCode.ifBlank { anno.value }

        val tab = anno.tab
        val codeColumn = anno.codeColumn
        val nameColumn = anno.nameColumn
        val isUseSysDefaultDict = dictCode.isNotBlank() && areAllBlank(tab, codeColumn, nameColumn)
        val useSpel = anno.spelExp.isNotBlank()

        //内置字典多翻译    0
        val one = isMulti && isUseSysDefaultDict && !useSpel
        //内置字典但翻译    1
        val one1 = !isMulti && isUseSysDefaultDict && !useSpel
        //任意表多翻译        2
        val one2 = isMulti && !isUseSysDefaultDict && !useSpel
        //任意表但翻译         3
        val one3 = !isMulti && !isUseSysDefaultDict && !useSpel

        val i = if (one) 0 else if (one1) 1 else if (one2) 2 else if (one3) 3 else if (useSpel) 4 else -1
        return i
    }

    fun getNeedAddFields(obj: Any): List<NeedAddInfo> {
        val process = process(obj)
        // 只返回当前对象的字段需求，不包括嵌套对象的字段需求
        val needAddFields = process.filter { it.rootObject === obj }.map {
            val needAddInfo = NeedAddInfo(
                rootObject = it.rootObject,
                fieldName = it.translatedAttributeNames,
                recur = null,
                isT = null,
                isColl = null,
                type = it.translatedType
            )
            needAddInfo
        }.distinctBy { it.fieldName }
        return needAddFields
    }


    /**
     * 处理内置字典翻译
     */
    fun processBuiltInDictionaryTranslation(translateTypeListMap: Map<Int?, List<TransInfo<Dict>>>) {
        if (translateTypeListMap.isEmpty()) {
            return
        }

        val dictCode = translateTypeListMap.entries
            .filter { it.key == 0 || it.key == 1 }
            .flatMap { entry -> entry.value.map { it.anno.dicCode.ifBlank { it.anno.value } } }
            .distinct()
            .joinToString(",")

        if (dictCode.isBlank()) {
            return
        }

        val fieldRuntimeStrValue = translateTypeListMap.entries
            .filter { it.key == 0 || it.key == 1 }
            .flatMap { it.value.map { info -> info.valueBeforeTranslation.toString() } }
            .distinct()
            .joinToString(",")

        val transApi = SpringUtil.getBean(TransApi::class.java)
        val dictMap = transApi.translateDictBatchCode2name(dictCode, fieldRuntimeStrValue)
            .groupBy { it.dictCode }

        if (dictMap.isEmpty()) {
            return
        }

        translateTypeListMap.forEach { (_, infos) ->
            infos.forEach { info ->
                val rootObject = info.rootObject
                val dicCode = info.anno.dicCode.ifBlank { info.anno.value }
                val dictModels = dictMap[dicCode].orEmpty()
                if (dictModels.isEmpty()) {
                    return@forEach
                }

                val rawValue = info.valueBeforeTranslation.toString()
                val values = rawValue.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (values.size > 1) {
                    val translated = values.map { value ->
                        dictModels.firstOrNull { it.value == value }?.label ?: ""
                    }.filter { it.isNotBlank() }
                        .joinToString(",")
                    ImprovedReflectUtil.setFieldValue(rootObject, info.translatedAttributeNames, translated)
                } else {
                    val match = dictModels.firstOrNull { it.value == rawValue } ?: return@forEach
                    info.translatedValue = match.label
                    ImprovedReflectUtil.setFieldValue(rootObject, info.translatedAttributeNames, match.label)
                }
            }
        }
    }

    /**
     * 处理任意表翻译
     */
    fun processAnyTableTranslation(translateTypeListMap: Map<Int?, List<TransInfo<Dict>>>) {
        if (translateTypeListMap.isEmpty()) {
            return
        }
        //按tab和code分组

        val pairListMap = translateTypeListMap.entries.filter {
            val translateType = it.key
            translateType == 2 || translateType == 3
        }.flatMap { it.value }.groupBy {
            val anno2 = it.anno


            val tab = anno2.tab

            val attributeNameBeforeTranslation = it.attributeNameBeforeTranslation
            val codeCol = anno2.codeColumn.ifBlank { attributeNameBeforeTranslation }
            val rootObject = it.rootObject
            Pair(
                tab, codeCol
            )
        }

        val entries = pairListMap.map {
            val key = it.key
            val tab = key.first
            //一定有值
            val codeColumn = key.second
            val value = it.value
            val nameColumns = value.map { x ->
                val anno = x.anno
                anno.nameColumn
            }.distinct().joinToString(",")

            val keys = value.map { x -> x.valueBeforeTranslation.toString() }.distinct()
            val tabMultiIn = TabMultiIn(
                tab = tab,
                code = codeColumn,
                name = nameColumns,
                keys = keys
            )
            Triple(tabMultiIn, codeColumn, value)
        }

        entries.distinct().forEach { e ->
            val tabMultiIn = e.first
            //默认取字段名
            val codeCol = e.second
            val right = e.third
            val tab = tabMultiIn.tab
            val nameColumn = tabMultiIn.name
            //这里做了处理,如果注解上没指定codeCol那就取翻以前的字段名
            val codeColumn = tabMultiIn.code.ifBlank { codeCol }


            val transApi = SpringUtil.getBean(TransApi::class.java)

            val fieldRuntimeStrValue = tabMultiIn.keys

            val info = transApi.translateTableBatchCode2name(
                table = tab,
                text = nameColumn,
                code = codeColumn,
                keys = fieldRuntimeStrValue.joinToString(",")
            )

            if (info.isNullOrEmpty()) {
                return@forEach
            }
            //                    right.
            right.forEach { needSetInfo ->
                val rootObject = needSetInfo.rootObject
                val afterObject = needSetInfo.afterObject

                /** 翻译后的名  */
                val translatedName = needSetInfo.translatedAttributeNames
                val anno = needSetInfo.anno
                val tab1 = anno.tab
                val codeColumn1 = anno.codeColumn
                val nameColumn1 = anno.nameColumn

                /** 翻译前的值  */
                val valueBeforeTranslation = needSetInfo.valueBeforeTranslation.toString()
                val multi = valueBeforeTranslation.contains(",")
                if (multi) {
                    val split = valueBeforeTranslation.split(",")
                    val collect1 = split.map { singleValueBeforeTranslation ->
                        val one = info.firstOrNull { it[codeColumn1].toString() == singleValueBeforeTranslation }
                        if (one != null) {
                            val nameValue = one[nameColumn1].toString()
                            needSetInfo.translatedValue = nameValue
                            return@map nameValue
                        }
                        ""
                    }.filter { it.isNotBlank() }.joinToString(",")
                    needSetInfo.translatedValue = collect1

                    //                            needSetInfo.setRootObjectHashBsm(rootObject.getClass().getSimpleName() + needSetInfo.getTranslatedAttributeNames());
                    if (Objects.nonNull(rootObject)) {
                        ImprovedReflectUtil.setFieldValue(rootObject, translatedName, collect1)
                    }
                    //                            return;
                }
                //                        Object 翻译前的值 = string;
                val one = info.firstOrNull { it[codeColumn1].toString() == valueBeforeTranslation }
                if (one == null) {
                    return@forEach
                }
                val nameValue = one[nameColumn1].toString()
                needSetInfo.translatedValue = nameValue
                //                        needSetInfo.setRootObjectHashBsm(rootObject.getClass().getSimpleName() + needSetInfo.getTranslatedAttributeNames());
                ImprovedReflectUtil.setFieldValue(rootObject, translatedName, nameValue)
            }
        }
    }

//    /**
//     * 处理spel表达式
//     */
//    fun processingSpelExpressions(translateTypeListMap: MutableMap<Int, MutableList<TransInfo<Dict>>>) {
//        if (translateTypeListMap.isEmpty()) {
//            return
//        }
//        translateTypeListMap.entries.filter { e -> e.key == 4 }
//            .flatMap { e -> e.value }
//            .forEach { e ->
//                val rootObject = e.rootObject
//                val afterObject = e.afterObject
//
//                /** 翻译后的名  */
//                val translatedAttributeNames = e.translatedAttributeNames
//                val anno = e.anno
//                val s = anno.spelExp
//                val aClass= anno.spelValueType
//                //            Object spelContextMapObject = getSpelContextMapObject();
//                val spelContextMap1: MutableMap<String, Any> = spelContextMap()
//                val o = SpELUtils.evaluateExpression(rootObject, spelContextMap1, s, aClass)
//                e.setTranslatedValue(o)
//                //            e.setRootObjectHashBsm(rootObject.getClass().getSimpleName() + e.getTranslatedAttributeNames());
//                ReflectUtil.setFieldValue(rootObject, translatedAttributeNames, o)
//            }
//    }

    private fun firstNonBlank(vararg candidates: String?): String? =
        candidates.firstOrNull { !it.isNullOrBlank() }

    private fun areAllBlank(vararg values: String?): Boolean =
        values.all { it.isNullOrBlank() }
}
