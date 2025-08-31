package com.addzero.aop.dicttrans.util_internal

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.map.MapUtil
import cn.hutool.core.text.CharSequenceUtil
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.ReflectUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.extra.spring.SpringUtil
import com.addzero.aop.dicttrans.anno.Dict
import com.addzero.aop.dicttrans.dictaop.CommonConstant
import com.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import com.addzero.aop.dicttrans.dictaop.entity.TabMultiIn
import com.addzero.aop.dicttrans.dictaop.entity.TransInfo
import com.addzero.aop.dicttrans.inter.TransApi
import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.Field
import java.util.*

/**
 * 内部翻译api
 * @author zjarlin
 * @date 2025/08/06
 */
internal object TransInternalUtil {


    fun process(rootObj: Any): List<TransInfo<Dict>> {
        val aClass: Class<*> = rootObj.javaClass

        //        List<TransInfo<Dict>> ret = new ArrayList<>();

        // 正常字段列表、集合字段列表、嵌套实体字段列表
        val normalFields = ArrayList<Pair<Field, Any>>()
        val collectionFields = ArrayList<Pair<Field, Any>>()
        val nestedEntityFields = ArrayList<Pair<Field, Any>>()

        // 遍历字段
        val fields: Array<Field> = ReflectUtil.getFields(aClass, { e ->
            val allMergedAnnotations1 = AnnotatedElementUtils.getMergedRepeatableAnnotations(e, Dict::class.java)
            val hasDictAnnotation = CollUtil.isNotEmpty(allMergedAnnotations1)

            val fieldValue = ReflectUtil.getFieldValue(rootObj, e)
            val isEmpty = ObjUtil.isEmpty(fieldValue)
            if (isEmpty) {
                return@getFields false
            }

            val isNestedEntity = RefUtil.isT(fieldValue)
            val isCollection = RefUtil.isCollectionField(e)
            val fieldObjectPair = Pair(e, fieldValue)

            if (hasDictAnnotation && !isEmpty) {
                normalFields.add(fieldObjectPair)
            } else if (isCollection) {
                collectionFields.add(fieldObjectPair)
            } else if (isNestedEntity) {
                nestedEntityFields.add(fieldObjectPair)
            }
            hasDictAnnotation || isNestedEntity || isCollection
        })

        // 处理普通字段
        val normalStream = normalFields.flatMap { toTrans1(rootObj,it) }

        // 处理集合字段
        val collectionStream = collectionFields.flatMap {
            val field = it.first
            val collection: Any = ReflectUtil.getFieldValue(rootObj, field)
            val items = collection as MutableCollection<*>
            items.flatMap { item -> process(item!!) }
        }

        // 处理嵌套实体
        val nestedEntityStream = nestedEntityFields.flatMap {
            val first = it.first
            val second = it.second
            val nestedEntity = ReflectUtil.getFieldValue(second, first)
            process(nestedEntity)
        }

        //合并所有结果
        val collect = normalStream + collectionStream + nestedEntityStream
        return collect
    }


    private fun toTrans1(rootObj: Any, e: Pair<Field, Any>): Sequence<TransInfo<Dict>> {
        val key: Field = e.first
        val fieldName = key.name
        val fieldValue = e.second

//        val fieldValue: Any = ReflectUtil.getFieldValue(``in``, key)

        val needSearchAnnotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(key, Dict::class.java)
        val transInfoStream = needSearchAnnotations.asSequence().mapIndexed { i, anno ->
            val alias = anno.serializationAlias
            val nameColumn = StrUtil.toCamelCase(anno.nameColumn)
            val other = fieldName + CommonConstant.DICT_TEXT_SUFFIX + if (i == 0) "" else i.toString()
            val firstNonBlank = CharSequenceUtil.firstNonBlank(alias, nameColumn, other)

            val transInfo = TransInfo<Dict>(
                superObjectFieldTypeEnum = null,
                superObjectFieldName = null,
                superObject = null,
                fieldEnum = null,
                anno = anno,
                translationProcess = null,
                rootObject = rootObj,
                afterObject = null,
                afterObjectClass = null,
                translatedAttributeNames = firstNonBlank,
                attributeNameBeforeTranslation = fieldName,
                valueBeforeTranslation = fieldValue,
                translatedValue = null,
                translatedType = anno.spelValueType.java,
                classificationOfTranslation = null,
                rootObjectHashBsm = null
            )
            transInfo.copy(classificationOfTranslation = getTranslateType(transInfo))
        }
        val filter = transInfoStream
//        .filter { it.valueBeforeTranslation != "null" }
        return filter
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
        val isMulti = StrUtil.containsAny(string, ",")
        val dictCode = anno.dicCode.ifBlank { anno.value }

        val tab = anno.tab
        val codeColumn = anno.codeColumn
        val nameColumn = anno.nameColumn
        val isUseSysDefaultDict = StrUtil.isNotBlank(dictCode) && StrUtil.isAllBlank(tab, codeColumn, nameColumn)
        val useSpel = StrUtil.isNotBlank(anno.spelExp)

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
        val needAddFields = process.map {
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
        if (MapUtil.isEmpty(translateTypeListMap)) {
            return
        }
        val dictCode = translateTypeListMap.entries.filter {
            val translateType = it.key
            translateType == 0 || translateType == 1
        }.flatMap {
            val transInfos = it.value
            transInfos.map { it ->
                val anno = it.anno
                val value = anno.value
                anno.dicCode.ifBlank { value }
            }
        }.distinct().joinToString(",")
        val fieldRuntimeStrValue = translateTypeListMap.entries.filter { e ->
            val translateType = e.key
            translateType == 0 || translateType == 1
        }.flatMap { e ->
            val transInfos = e.value
            val stringStream = transInfos.map { x -> x.valueBeforeTranslation.toString() }
            stringStream
        }.distinct().joinToString(",")

        val transApi = SpringUtil.getBean(TransApi::class.java)

        val map = transApi.translateDictBatchCode2name(dictCode,
            fieldRuntimeStrValue).groupBy { it.dictCode }
        if (MapUtil.isEmpty(map)) {
            return
        }
        translateTypeListMap.forEach {
            it.value.forEach { e ->
                val rootObject = e.rootObject
                val anno = e.anno
                val dicCode = anno.dicCode.ifBlank { anno.value }
                val dictModels = map.get(dicCode)

                if (CollUtil.isEmpty(dictModels)) {
                    return@forEach
                }
                /** 翻以前的值  */
                val flipPastValues: String = e.valueBeforeTranslation.toString()
                val multi: Boolean = StrUtil.containsAny(flipPastValues, ",")
                if (multi) {
                    val split1 = StrUtil.split(flipPastValues, ",")
                    val collect = split1.map { row ->
                        val one = dictModels?.find { it.value == row }
                        one?.label ?: ""
                    }.filter { it.isNotBlank() }.joinToString(",")

                    ReflectUtil.setFieldValue(rootObject, e.translatedAttributeNames, collect)
                    //                    return;
                }
                val one = dictModels?.find { it.value == flipPastValues }
                if (Objects.isNull(one)) {
                    return@forEach
                }
                val label = one?.label
                e.translatedValue = label
                //                e.setRootObjectHashBsm(rootObject.getClass().getSimpleName() + e.getTranslatedAttributeNames());
                ReflectUtil.setFieldValue(rootObject, e.translatedAttributeNames, label)
            }
        }
    }

    /**
     * 处理任意表翻译
     */
    fun processAnyTableTranslation(translateTypeListMap: Map<Int?, List<TransInfo<Dict>>>) {
        if (MapUtil.isEmpty(translateTypeListMap)) {
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

            if (CollUtil.isEmpty(info)) {
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
                    val split = StrUtil.split(valueBeforeTranslation, ",")
                    val collect1 = split.map { singleValueBeforeTranslation ->
                        val one = info.firstOrNull() { it[codeColumn1].toString() == singleValueBeforeTranslation }
                        if (Objects.nonNull(one)) {
                            val nameValue = one?.get(nameColumn1).toString()
                            needSetInfo.translatedValue = nameValue
                            return@map nameValue
                        }
                        ""
                    }.filter(StrUtil::isNotBlank).joinToString(",")
                    needSetInfo.translatedValue = collect1

                    //                            needSetInfo.setRootObjectHashBsm(rootObject.getClass().getSimpleName() + needSetInfo.getTranslatedAttributeNames());
                    if (Objects.nonNull(rootObject)) {
                        ReflectUtil.setFieldValue(rootObject, translatedName, collect1)
                    }
                    //                            return;
                }
                //                        Object 翻译前的值 = string;
                val one = info.firstOrNull() { it[codeColumn1].toString() == valueBeforeTranslation }
                if (Objects.isNull(one)) {
                    return@forEach
                }
                val nameValue = one?.get(nameColumn1).toString()
                needSetInfo.translatedValue = nameValue
                //                        needSetInfo.setRootObjectHashBsm(rootObject.getClass().getSimpleName() + needSetInfo.getTranslatedAttributeNames());
                ReflectUtil.setFieldValue(rootObject, translatedName, nameValue)
            }
        }
    }

//    /**
//     * 处理spel表达式
//     */
//    fun processingSpelExpressions(translateTypeListMap: MutableMap<Int, MutableList<TransInfo<Dict>>>) {
//        if (MapUtil.isEmpty(translateTypeListMap)) {
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
}
