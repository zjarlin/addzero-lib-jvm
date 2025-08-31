package com.addzero.kmp.kaleidoscope.codegen.pureksp.jimmer

data class TemlateContext(
    val templatePath: String = "templates/jimmer/entity-metadata.vm",
    val fileNamePattern: String = "\${className}Metadata.kt",
    val packageNamePattern: String = "\${packageName}.metadata"

)
