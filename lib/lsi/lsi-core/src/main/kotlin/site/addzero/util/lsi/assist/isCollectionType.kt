package site.addzero.util.lsi.assist

import site.addzero.util.lsi.constant.COLLECTION_TYPE_FQ_NAMES
import site.addzero.util.lsi.types.CollectionType

fun String?.isCollectionType(): Boolean {
    this ?:return false
    val mayfqCollection = CollectionType.isCollection(this)
    val islastCollection = this.substringAfterLast(".").lowercase() in setOf(
        "list", "mutableList", "set", "mutableset", "map", "mutablemap",
        "collection", "mutablecollection", "arraylist", "linkedhashset",
        "hashset", "linkedhashmap", "hashmap"
    )
    val startWithUtil = this.startsWith("java.util.")
    val mayCollection = startWithUtil && islastCollection

    val sureCollection = mayfqCollection || mayCollection
    return sureCollection
}
