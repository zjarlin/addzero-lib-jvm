package site.addzero.str



fun <T, K> addAllIfAbsentByKey(
    target: MutableList<T>,
    other: Collection<T>,
    keySelector: (T) -> K
): Boolean {
    var modified = false
    other.forEach { element ->
        if (target.none { keySelector(it) == keySelector(element) }) {
            target.add(element)
            modified = true
        }
    }
    return modified
}



/**
 * 根据多个比较条件计算两个集合的差集。
 *
 * @param T 集合元素的类型
 * @param collection 当前集合
 * @param other 另一个集合
 * @param predicates 多个 lambda 表达式，用于自定义比较规则
 * @return 差集（当前集合中不存在于 `other` 集合中的元素）
 */
fun <T> differenceBy(
    collection: Collection<T>,
    other: Collection<T>,
    vararg predicates: (T, T) -> Boolean,
): List<T> {
    return collection.filter { item ->
        other.none { otherItem ->
            predicates.all { predicate -> predicate(item, otherItem) }
        }
    }
}

/**
 * 根据多个比较条件计算两个集合的交集。
 *
 * @param T 集合元素的类型
 * @param collection 当前集合
 * @param other 另一个集合
 * @param predicates 多个 lambda 表达式，用于自定义比较规则
 * @return 交集（同时存在于两个集合中的元素）
 */
fun <T> intersectBy(
    collection: Collection<T>,
    other: Collection<T>,
    vararg predicates: (T, T) -> Boolean,
): List<T> {
    return collection.filter { item ->
        other.any { otherItem ->
            predicates.all { predicate -> predicate(item, otherItem) }
        }
    }
}