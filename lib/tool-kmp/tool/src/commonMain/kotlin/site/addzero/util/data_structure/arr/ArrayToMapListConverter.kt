package site.addzero.util.data_structure.arr


/**
 * 将二维数组转换为Map列表，第一行作为键名，其余行作为值
 *
 * @param T 数组元素的类型
 * @param array 二维数组，第一行为键名，其余行为对应的值
 * @return 包含Map的列表，每个Map代表一行数据
 */
fun <T> convertWithFirstRowAsKeys(array: Array<Array<T>>): List<Map<T, T>> {
    if (array.isEmpty()) return emptyList()

    val headers = array[0]
    val rows = array.drop(1)

    return convertRowsToMaps(rows, headers)
}

/**
 * 将二维数组转换为Map列表，使用指定的键名列表
 *
 * @param T 数组元素的类型
 * @param array 二维数组，仅包含值
 * @param keys 键名列表
 * @return 包含Map的列表，每个Map代表一行数据
 */
fun <T> convertWithKeys(array: Array<Array<T>>, keys: Array<T>): List<Map<T, T>> {
    if (array.isEmpty()) return emptyList()

    return convertRowsToMaps(array.toList(), keys)
}

/**
 * 将行数据转换为Map列表的核心逻辑
 *
 * @param T 数组元素的类型
 * @param rows 行数据列表
 * @param keys 键名数组
 * @return 包含Map的列表，每个Map代表一行数据
 */
private fun <T> convertRowsToMaps(rows: List<Array<T>>, keys: Array<T>): List<Map<T, T>> {
    return rows.map { row ->
        keys
            .take(row.size.coerceAtMost(keys.size))
            .mapIndexed { index, key -> key to row[index] }
            .toMap()
    }
}
