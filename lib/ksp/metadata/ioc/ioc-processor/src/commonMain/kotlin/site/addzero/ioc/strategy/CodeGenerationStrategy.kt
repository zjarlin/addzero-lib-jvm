package site.addzero.ioc.strategy


interface CodeGenerationStrategy {
    fun generateCollectionCode(functions: List<Pair<String, InitType>>): String
    fun generateExecuteMethod(): String
}
