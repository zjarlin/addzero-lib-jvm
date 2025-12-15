package site.addzero.aop.dicttrans.util

import site.addzero.aop.dicttrans.util.CollUtil
import site.addzero.aop.dicttrans.util.ReflectUtil
import site.addzero.aop.dicttrans.util.SpringUtil
import site.addzero.aop.dicttrans.inter.TransApi
import site.addzero.aop.dicttrans.util_internal.TransInternalUtil

/**
 * id2name
 */
fun <T> code2name(res: MutableList<T>): MutableList<T> {
    if (CollUtil.isEmpty(res)) {
        return res
    }

    val transContext = res.flatMap { item ->
        val process = TransInternalUtil.process(item!!)
        process
    }.groupBy {
        it.classificationOfTranslation
    }

    val bean = SpringUtil.getBean(TransApi::class.java)
    transContext.forEach { (k, v) ->
        val equals = k == 0
        val equals1 = k == 1
        val equals2 = k == 2
        val equals3 = k == 3
        k == 4
        //内置字典多翻译    0
        //内置字典但翻译    1
        //任意表多翻译        2
        //任意表但翻译         3
        if (equals || equals1) {
            val dictCodes = v.map { e ->
                e.anno.dicCode.ifBlank { e.anno.value }
            }.distinct().joinToString(",")

            val stringListMap = bean.translateDictBatchCode2name(dictCodes,
                null).groupBy { it.dictCode }

            v.groupBy { e ->
                e.anno.dicCode.takeIf { it.isNotBlank() } ?: e.anno.value
            }.forEach { (dicCode, listTmp) ->
                val dictModels = stringListMap[dicCode] ?: emptyList()

                listTmp.forEach { e ->
                    val valueBeforeTranslation = e.valueBeforeTranslation
                    val rootObject = e.rootObject
                    val fieldName = e.attributeNameBeforeTranslation
                    val id = valueBeforeTranslation.toString()

                    if (id.contains(",")) {
                        val split = id.split(",").filter { it.isNotBlank() }
                        val valueLabelMap = dictModels.associate {
                            it.value to it.label
                        }
                        val translatedValues = split.joinToString(",") { value ->
                            valueLabelMap[value] ?: value
                        }
                        ReflectUtil.setFieldValue(
                            rootObject, fieldName, translatedValues
                        )
                    } else {
                        val one = dictModels.firstOrNull { it.value == id }
                        one?.let {
                            ReflectUtil.setFieldValue(rootObject, fieldName, it.label)
                        }
                    }
                }
            }
        }
        if (equals2 || equals3) {
            v.groupBy { e ->
                val anno = e.anno
                val tab = anno.tab
                val codeColumn = anno.codeColumn
                tab to codeColumn  // 使用Pair中缀形式
            }.forEach { (key, listTmp) ->
                val (tab, codeColumn) = key
                val anno = listTmp.first().anno

                // 收集所有需要查询的值
                val valuesToQuery = listTmp.map { it.valueBeforeTranslation.toString() }.distinct().joinToString(",")

                // 收集所有nameColumn
                val nameColumn = listTmp.mapNotNull { it.anno.nameColumn }.distinct().joinToString(",")

                // 查询字典表
                val jsonObjects = bean.translateTableBatchCode2name(tab, nameColumn, codeColumn, valuesToQuery)
                    ?: return@forEach  // 如果结果为空则跳过

                listTmp.forEach { e ->
                    val rootObject = e.rootObject
                    val attributeName = e.attributeNameBeforeTranslation
                    val stringValue = e.valueBeforeTranslation.toString()

                    if (stringValue.contains(",")) {
                        // 处理逗号分隔的多个值
                        val splitValues = stringValue.split(",").filter { it.isNotBlank() }
                        val valueTextMap = jsonObjects.associate {

                            it[codeColumn].toString() to it[nameColumn].toString()

                        }
                        val translatedValues = splitValues.joinToString(",") { value ->
                            valueTextMap[value] ?: value
                        }
                        ReflectUtil.setFieldValue(rootObject, attributeName, translatedValues)
                    } else {
                        // 处理单个值
                        jsonObjects.firstOrNull { it[codeColumn].toString() == stringValue }?.let { matchedItem ->
                            val fieldToSet = if (anno.ignoreVo) {
                                e.translatedAttributeNames
                            } else {
                                attributeName
                            }
                            ReflectUtil.setFieldValue(
                                rootObject, fieldToSet, matchedItem[nameColumn].toString()
                            )
                        }
                    }
                }
            }
        }
    }


    return res
}

fun <T> name2codeT(t: T): T {
    val list = mutableListOf<T>(t)
    val list1 = name2code<T>(list)
    if (CollUtil.isEmpty(list1)) {
        return t
    }
    return list1[0]
}

fun <T> code2nameT(t: T): T {
    val list = mutableListOf<T>(t)
    val list1 = code2name<T>(list)
    if (CollUtil.isEmpty(list1)) {
        return t
    }
    return list1.get(0)
}

fun <T> name2code(res: MutableList<T>): MutableList<T> {
    if (CollUtil.isEmpty(res)) {
        return res
    }

    val transContext = res.flatMap { item ->
        val process = TransInternalUtil.process(item!!)
        process
    }.groupBy { it.classificationOfTranslation }

    val bean = SpringUtil.getBean(TransApi::class.java)

    transContext.forEach { (k, v) ->
        k == 0
        k == 1
        val equals2 = k == 2
        val equals3 = k == 3
        k == 4
        //内置字典多翻译    0
        //内置字典但翻译    1
        //任意表多翻译        2
        //任意表但翻译         3
        if (equals2 || equals3) {
            v.groupBy { e ->
                val anno = e.anno
                val tab = anno.tab
                val codeColumn = anno.codeColumn
                e.valueBeforeTranslation.toString()
                e.rootObject
                tab to codeColumn
            }.mapValues { (_, listTmp) ->
                // Group by table
                val anno = listTmp[0].anno
                val tab = anno.tab
                val nameColumn = anno.nameColumn
                val codeColumn = anno.codeColumn
                val collect = listTmp.joinToString(",") { it.valueBeforeTranslation.toString() }

                val jsonObjects = bean.translateTableBatchCode2name(
                    table = tab, text = nameColumn, code = codeColumn, keys = collect
                )

                if (jsonObjects.isNullOrEmpty()) {
                    return@mapValues null
                }

                listTmp.forEach { e ->
                    val rootObject = e.rootObject
                    val attributeName = e.attributeNameBeforeTranslation
                    val valueBeforeTranslation = e.valueBeforeTranslation.toString()

                    if (valueBeforeTranslation.contains(",")) {
                        val split = valueBeforeTranslation.split(",").filter { it.isNotEmpty() }
                        val collect1 = jsonObjects.associate {
                            it[nameColumn].toString() to it[codeColumn].toString()
                        }

                        val collect2 = split.joinToString(",") { x ->
                            collect1[x] ?: x
                        }

                        ReflectUtil.setFieldValue(rootObject, attributeName, collect2)
                    } else {
                        val one = jsonObjects.find { d ->
                            val text = d[nameColumn].toString()
                            text == valueBeforeTranslation
                        }

                        if (one != null) {
                            ReflectUtil.setFieldValue(rootObject, attributeName, one[codeColumn])
                        }
                    }
                }
                null
            }
        }
    }


    return res
}
