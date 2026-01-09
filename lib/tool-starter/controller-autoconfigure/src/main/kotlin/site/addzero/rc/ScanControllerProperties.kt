package site.addzero.rc

import org.springframework.boot.context.properties.ConfigurationProperties
@ConfigurationProperties(prefix = "expression.scan.controller")
class ScanControllerProperties(
    var pkg: String = "site.addzero",
    /**
     * 支持占位符 ${pkg}，在 resolvedExpression 中会被替换成真实包名
     */
    var expression: String = "execution(* ${'$'}{pkg}..*Controller*+.*(..))"
) {
    @Suppress("unused")
    fun resolvedExpression(customExpression: String? = null, customPkg: String? = null): String {
        val expr = customExpression?.takeIf { it.isNotBlank() } ?: expression
        val pkgValue = customPkg?.takeIf { it.isNotBlank() } ?: pkg
        return expr.replace("\${pkg}", pkgValue)
    }
}
