package site.addzero.ksp.serviceloader

/**
 * Service interface for code generation strategies
 */
interface CodeGeneratorService {
    /**
     * The name of this generator
     */
    val name: String

    /**
     * Generate code based on the symbol
     */
    fun generate(context: String): String

    /**
     * Check if this generator supports the given symbol
     */
    fun supports(context: String): Boolean
}