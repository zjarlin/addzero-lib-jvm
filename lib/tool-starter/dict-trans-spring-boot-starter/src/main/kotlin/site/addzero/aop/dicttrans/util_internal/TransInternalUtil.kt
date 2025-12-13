package site.addzero.aop.dicttrans.util_internal

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.map.MapUtil
import cn.hutool.core.text.CharSequenceUtil
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.extra.spring.SpringUtil
import org.slf4j.LoggerFactory
import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.aop.dicttrans.dictaop.DictConstant
import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import site.addzero.aop.dicttrans.dictaop.entity.TabMultiIn
import site.addzero.aop.dicttrans.dictaop.entity.TransInfo
import site.addzero.aop.dicttrans.inter.TransApi
import site.addzero.aop.dicttrans.tracking.WeakReferenceTracker
import org.springframework.core.annotation.AnnotatedElementUtils
import site.addzero.util.RefUtil
import java.util.*

/**
 * 内部翻译api (Enhanced with weak reference tracking)
 * @author zjarlin
 * @date 2025/08/06
 */
internal object TransInternalUtil {
    
    private val logger = LoggerFactory.getLogger(TransInternalUtil::class.java)
    
    // Lazy initialization to avoid circular dependencies
    private val weakReferenceTracker: WeakReferenceTracker by lazy {
        SpringUtil.getBean(WeakReferenceTracker::class.java)
    }

    fun process(rootObj: Any): List<TransInfo<Dict>> {
        val result = mutableListOf<TransInfo<Dict>>()
        // 使用队列进行广度优先遍历，避免深层递归
        val queue = LinkedList<Any>()
        queue.add(rootObj)

        // 使用弱引用跟踪器避免循环引用和重复处理
        while (queue.isNotEmpty()) {
            val currentObj = queue.poll()

            // 避免重复处理相同对象和null对象
            if (currentObj == null) {
                continue
            }
            
            // 使用弱引用跟踪器检查是否已处理
            if (weakReferenceTracker.isTracked(currentObj)) {
                // 检查是否存在循环引用
                if (weakReferenceTracker.hasCircularReference(currentObj)) {
                    logger.debug("Skipping object with known circular reference: {}", currentObj.javaClass.name)
                }
                continue
            }
            
            // 跟踪当前对象
            currentObj?.let { weakReferenceTracker.track(it) }

            val aClass: Class<*> = currentObj.javaClass

            // 遍历所有字段 (使用缓存的反射操作)
            val fields = EnhancedRefUtil.getFields(aClass)
            for (field in fields) {
                field.isAccessible = true
                val fieldValue = EnhancedRefUtil.getFieldValue(currentObj, field)
                if (ObjUtil.isEmpty(fieldValue)) {
                    continue
                }

                // 检查字段是否有 Dict 注解
                val dictAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, Dict::class.java)
                if (CollUtil.isNotEmpty(dictAnnotations)) {
                    // 处理带有 Dict 注解的字段
                    dictAnnotations.forEach { anno ->
                        val alias = anno.serializationAlias
                        val nameColumn = StrUtil.toCamelCase(anno.nameColumn)
                        val other = field.name + DictConstant.DICT_TEXT_SUFFIX
                        val firstNonBlank = CharSequenceUtil.firstNonBlank(alias, nameColumn, other)

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
                            translatedAttributeNames = firstNonBlank,
                            attributeNameBeforeTranslation = field.name,
                            valueBeforeTranslation = fieldValue!!,
                            translatedValue = null,
                            translatedType = anno.spelValueType.java,
                            classificationOfTranslation = null,
                            rootObjectHashBsm = null
                        )
                        result.add(transInfo.copy(classificationOfTranslation = getTranslateType(transInfo)))
                    }
                }

                // 处理集合类型字段 (使用缓存的类型检查)
                if (EnhancedRefUtil.isCollectionField(field)) {
                    val collection = fieldValue as? MutableCollection<*>
                    collection?.filterNotNull()?.forEach { item ->
                        // 检查循环引用
                        if (item == currentObj || item == rootObj) {
                            logger.debug("Detected circular reference in collection field: {} of class: {}", 
                                field.name, currentObj.javaClass.name)
                            weakReferenceTracker.markCircularReference(item)
                        } else {
                            // 将集合中的元素加入队列，等待处理
                            queue.add(item)
                        }
                    }
                }
                // 处理嵌套实体字段
                else if (fieldValue != null && RefUtil.isT(fieldValue)) {
                    // 检查循环引用
                    if (fieldValue == currentObj || fieldValue == rootObj) {
                        logger.debug("Detected circular reference in object field: {} of class: {}", 
                            field.name, currentObj?.javaClass?.name)
                        weakReferenceTracker.markCircularReference(fieldValue)
                    } else {
                        // 将嵌套对象加入队列，等待处理
                        queue.add(fieldValue)
                    }
                }
            }
        }

        // 处理完成后触发清理
        try {
            weakReferenceTracker.cleanup()
            
            // 记录统计信息
            val stats = weakReferenceTracker.getStatistics()
            logger.debug("Processing completed. Tracking stats: tracked={}, cleaned={}, circular={}", 
                stats.currentlyTracked, stats.cleanedUp, stats.circularReferences)
        } catch (e: Exception) {
            logger.error("Error during weak reference cleanup", e)
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

                    EnhancedRefUtil.setFieldValue(rootObject, EnhancedRefUtil.getField(rootObject.javaClass, e.translatedAttributeNames)!!, collect as Any)
                    //                    return;
                }
                val one = dictModels?.find { it.value == flipPastValues }
                if (Objects.isNull(one)) {
                    return@forEach
                }
                val label = one?.label
                e.translatedValue = label
                //                e.setRootObjectHashBsm(rootObject.getClass().getSimpleName() + e.getTranslatedAttributeNames());
                val field = EnhancedRefUtil.getField(rootObject.javaClass, e.translatedAttributeNames)
                if (field != null) {
                    EnhancedRefUtil.setFieldValue(rootObject, field, label as Any)
                }
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
                        val field = EnhancedRefUtil.getField(rootObject.javaClass, translatedName)
                        if (field != null) {
                            EnhancedRefUtil.setFieldValue(rootObject, field, collect1)
                        }
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
                val field = EnhancedRefUtil.getField(rootObject.javaClass, translatedName)
                if (field != null) {
                    EnhancedRefUtil.setFieldValue(rootObject, field, nameValue)
                }
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
