package com.example.autoinit.ksp.strategy

interface CodeGenerationStrategy {
    fun generateCollectionCode(functions: List<Pair<String, InitType>>): String
    fun generateExecuteMethod(): String
}
