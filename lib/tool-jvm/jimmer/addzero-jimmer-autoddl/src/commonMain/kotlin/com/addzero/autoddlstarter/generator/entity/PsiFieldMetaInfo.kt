package com.addzero.autoddlstarter.generator.entity

data class PsiFieldMetaInfo(
    val pkg: String?,
    val classname: String?,
    val classcomment: String?,
    val javaFieldMetaInfos: List<JavaFieldMetaInfo> ?
)
