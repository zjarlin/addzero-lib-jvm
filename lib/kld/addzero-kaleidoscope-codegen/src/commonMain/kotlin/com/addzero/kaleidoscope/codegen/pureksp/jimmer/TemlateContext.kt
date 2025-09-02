package com.addzero.kaleidoscope.codegen.pureksp.jimmer

data class TemlateContext(
    val templatePath: String = "templates/jimmer/entity-metadata.vm",
    val fileNamePattern: String = "\${className}Metadata.kt",
    val pkgPattern: String = "\${packageName}.metadata",
    val outputDir: String = "\${packageName}.metadata"
)