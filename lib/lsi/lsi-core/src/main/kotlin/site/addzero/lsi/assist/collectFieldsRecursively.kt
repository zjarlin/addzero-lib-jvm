package site.addzero.lsi.assist
import site.addzero.lsi.field.LsiField
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
