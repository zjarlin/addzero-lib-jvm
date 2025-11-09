package site.addzero.tool.coll

/**
 * 流处理相关的扩展函数和工具方法
 */

/**
 * 将序列根据键选择器去重，保持顺序
 */
fun <T, K> distinctBySequence(
    sequence: Sequence<T>,
    keySelector: (T) -> K
): Sequence<T> {
    val observed = mutableSetOf<K>()
    return sequence.filter { element ->
        observed.add(keySelector(element))
    }
}

/**
 * 将可迭代对象按条件分组，并限制每组的数量
 */
fun <T, K> groupAndLimitIterable(
    iterable: Iterable<T>,
    keySelector: (T) -> K,
    limitPerGroup: Int
): Map<K, List<T>> {
    val result = mutableMapOf<K, MutableList<T>>()
    iterable.forEach { element ->
        val key = keySelector(element)
        val group = result.getOrPut(key) { mutableListOf() }
        if (group.size < limitPerGroup) {
            group.add(element)
        }
    }
    return result
}

/**
 * 在列表中找到第一个满足条件的元素及其索引
 */
fun <T> findWithIndexInList(
    list: List<T>,
    predicate: (T) -> Boolean
): Pair<Int, T>? {
    list.forEachIndexed { index, element ->
        if (predicate(element)) {
            return Pair(index, element)
        }
    }
    return null
}

/**
 * 将序列分块为指定大小的列表
 */
fun <T> chunkedSequence(
    sequence: Sequence<T>,
    size: Int
): Sequence<List<T>> {
    return sequence {
        val iterator = sequence.iterator()
        while (iterator.hasNext()) {
            val chunk = mutableListOf<T>()
            for (i in 0 until size) {
                if (iterator.hasNext()) {
                    chunk.add(iterator.next())
                } else {
                    break
                }
            }
            if (chunk.isNotEmpty()) {
                yield(chunk)
            }
        }
    }
}

/**
 * 在序列中查找根据指定选择器函数的最大值元素，如果序列为空则返回null
 */
inline fun <T, R : Comparable<R>> maxOrNullBySelector(
    sequence: Sequence<T>,
    selector: (T) -> R
): T? {
    val iterator = sequence.iterator()
    if (!iterator.hasNext()) return null
    var maxElem = iterator.next()
    var maxValue = selector(maxElem)
    while (iterator.hasNext()) {
        val elem = iterator.next()
        val elemValue = selector(elem)
        if (elemValue > maxValue) {
            maxElem = elem
            maxValue = elemValue
        }
    }
    return maxElem
}

/**
 * 在序列中查找根据指定选择器函数的最小值元素，如果序列为空则返回null
 */
inline fun <T, R : Comparable<R>> minOrNullBySelector(
    sequence: Sequence<T>,
    selector: (T) -> R
): T? {
    val iterator = sequence.iterator()
    if (!iterator.hasNext()) return null
    var minElem = iterator.next()
    var minValue = selector(minElem)
    while (iterator.hasNext()) {
        val elem = iterator.next()
        val elemValue = selector(elem)
        if (elemValue < minValue) {
            minElem = elem
            minValue = elemValue
        }
    }
    return minElem
}
