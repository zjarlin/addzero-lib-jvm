package site.addzero.lsi.kt.type

import site.addzero.lsi.assist.isCollectionType
import org.jetbrains.kotlin.psi.KtTypeReference

fun KtTypeReference.isCollectionType(): Boolean {
    val qualifiedName = qualifiedName()
    val collectionType = qualifiedName.isCollectionType()
    return collectionType
}

fun KtTypeReference.qualifiedName(): String? {
    val name = this.name
    return name
}

