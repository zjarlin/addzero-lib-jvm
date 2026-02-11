package site.addzero.ioc.strategy


interface CodeGenerationStrategy {
    fun generateCollectionCode(functions: List<BeanInfo>): String
    fun generateExecuteMethod(): String
}
