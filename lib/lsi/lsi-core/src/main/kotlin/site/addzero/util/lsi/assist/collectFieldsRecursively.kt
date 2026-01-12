package site.addzero.util.lsi.assist

import site.addzero.util.lsi.field.LsiField
/**
 * 递归收集字段及其嵌套子字段
 *
 * @param fields 需要处理的字段列表
 * @param result 用于存储收集到的字段的结果列表
 * @param currentDepth 当前递归深度
 * @param maxDepth 最大递归深度，防止无限递归
 */
fun collectFieldsRecursively(
    fields: List<LsiField>,
    result: MutableList<LsiField>,
    currentDepth: Int,
    maxDepth: Int
) {
    if (currentDepth >= maxDepth) return

    for (field in fields) {
        result.add(field)
        if (field.isNestedObject && field.children.isNotEmpty()) {
            collectFieldsRecursively(field.children, result, currentDepth + 1, maxDepth)
        }
    }
}
