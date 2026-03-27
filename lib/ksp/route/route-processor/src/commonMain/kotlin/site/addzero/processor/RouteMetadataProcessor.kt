package site.addzero.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import site.addzero.annotation.Route
import site.addzero.context.Settings
import site.addzero.util.str.toUnderLineCase

internal const val ROUTE_TABLE_NAME = "RouteTable"
internal const val ROUTE_KEYS_NAME = "RouteKeys"

internal data class RouteRecord(
    val value: String,
    val title: String,
    val routePath: String,
    val icon: String,
    val order: Double,
    val qualifiedName: String,
    val simpleName: String,
) {
    val uniqueId: String
        get() = qualifiedName.ifBlank { routePath }
}

/**
 * 路由元数据注解处理器
 */
class RouteMetadataProcessor(
    @Suppress("unused")
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    private val collectedRoutes = linkedSetOf<RouteRecord>()
    private val collectedSourceFiles = linkedSetOf<KSFile>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        Settings.fromOptions(options)

        val symbols = resolver.getSymbolsWithAnnotation(Route::class.qualifiedName!!)
            .toList()
        if (symbols.isEmpty()) {
            return emptyList()
        }

        val invalidSymbols = mutableListOf<KSAnnotated>()
        symbols.forEach { symbol ->
            if (!symbol.validate()) {
                invalidSymbols += symbol
                return@forEach
            }

            when (symbol) {
                is KSClassDeclaration -> {
                    recordSourceFile(symbol)
                    processClass(symbol)?.let(collectedRoutes::add)
                }

                is KSFunctionDeclaration -> {
                    recordSourceFile(symbol)
                    processFunction(symbol)?.let(collectedRoutes::add)
                }

                is KSPropertyDeclaration -> {
                    recordSourceFile(symbol)
                    processProperty(symbol)?.let(collectedRoutes::add)
                }

                else -> {
                    logger.warn("Unsupported symbol type for Route: ${symbol::class.simpleName}", symbol)
                }
            }
        }

        return invalidSymbols
    }

    override fun finish() {
        aggregateAndGenerateRoutes(
            sharedSourceDir = Settings.sharedSourceDir,
            routeGenPkg = Settings.routeGenPkg,
            ownerModuleHint = options["routeOwnerModule"].orEmpty(),
            sourceFilePaths = collectedSourceFiles.map { it.filePath },
            routeItems = sortRoutes(collectedRoutes),
            logger = logger,
        )
    }

    private fun recordSourceFile(declaration: KSDeclaration) {
        declaration.containingFile?.let(collectedSourceFiles::add)
    }

    private fun processClass(declaration: KSClassDeclaration): RouteRecord? {
        return processSymbol(declaration)
    }

    private fun processFunction(declaration: KSFunctionDeclaration): RouteRecord? {
        return processSymbol(declaration)
    }

    private fun processProperty(declaration: KSPropertyDeclaration): RouteRecord? {
        return processSymbol(declaration)
    }

    private fun processSymbol(declaration: KSDeclaration): RouteRecord? {
        return try {
            val qualifiedName = declaration.qualifiedName?.asString().orEmpty()
            val simpleName = declaration.simpleName.asString()
            val annotation = declaration.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == Route::class.qualifiedName
            }
            val title = annotation.arguments.firstOrNull { it.name?.asString() == "title" }?.value as? String
                ?: simpleName
            val icon = annotation.arguments.firstOrNull { it.name?.asString() == "icon" }?.value as? String
                ?: "Apps"
            val routePath = annotation.arguments.firstOrNull { it.name?.asString() == "routePath" }?.value as? String
                ?: qualifiedName
            val order = annotation.arguments.firstOrNull { it.name?.asString() == "order" }?.value as? Double
                ?: 0.0
            val group = annotation.arguments.firstOrNull { it.name?.asString() == "value" }?.value as? String
                ?: ""

            RouteRecord(
                value = group,
                title = title,
                routePath = routePath,
                icon = icon,
                order = order,
                qualifiedName = qualifiedName,
                simpleName = simpleName,
            )
        } catch (e: Exception) {
            logger.error("Error processing Route annotation: ${e.message}", declaration)
            null
        }
    }
}

internal fun renderRouteTableCode(routeItems: List<RouteRecord>): String {
    val routeKeyNames = buildRouteKeyNames(routeItems)
    val routeMappings = routeItems.joinToString(",\n        ") { route ->
        val routeKeyName = routeKeyNames.getValue(route.uniqueId)
        "RouteKeys.$routeKeyName to @Composable { ${route.qualifiedName}() }"
    }

    return """
        |package ${Settings.routeGenPkg}
        |
        |import androidx.compose.runtime.Composable
        |
        |/**
        | * 路由表
        | * 请勿手动修改此文件
        | */
        |object $ROUTE_TABLE_NAME {
        |    /**
        |     * 所有路由映射
        |     */
        |    val allRoutes = mapOf(
        |        $routeMappings
        |    )
        |
        |    /**
        |     * 根据路由键获取对应的Composable函数
        |     */
        |    operator fun get(routeKey: String): @Composable () -> Unit {
        |        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: ${'$'}routeKey")
        |    }
        |}
        |""".trimMargin()
}

internal fun renderRouteKeysCode(routeItems: List<RouteRecord>): String {
    val routeKeyNames = buildRouteKeyNames(routeItems)
    val routeKeyConstants = routeItems.joinToString("\n    ") { route ->
        val routeKeyName = routeKeyNames.getValue(route.uniqueId)
        "const val $routeKeyName = \"${route.routePath}\""
    }
    val allMetaItems = routeItems.joinToString(",\n        ") { route ->
        "Route(" +
            "value = \"${route.value}\", " +
            "title = \"${route.title}\", " +
            "routePath = \"${route.routePath}\", " +
            "icon = \"${route.icon}\", " +
            "order = ${route.order}, " +
            "qualifiedName = \"${route.qualifiedName}\", " +
            "simpleName = \"${route.simpleName}\"" +
            ")"
    }

    return """
        |package ${Settings.routeGenPkg}
        |
        |import site.addzero.annotation.Route
        |
        |/**
        | * 路由键
        | * 请勿手动修改此文件
        | */
        |object $ROUTE_KEYS_NAME {
        |    $routeKeyConstants
        |
        |    /**
        |     * 所有路由元数据
        |     */
        |    val allMeta = listOf(
        |        $allMetaItems
        |    )
        |}
        |""".trimMargin()
}

internal fun sortRoutes(routeItems: Collection<RouteRecord>): List<RouteRecord> {
    return routeItems.sortedWith(
        compareBy<RouteRecord> { it.order }
            .thenBy { it.routePath }
            .thenBy { it.qualifiedName }
            .thenBy { it.simpleName }
    )
}

internal fun buildRouteKeyNames(routeItems: List<RouteRecord>): Map<String, String> {
    val baseNameCounts = routeItems.groupingBy { it.baseRouteKeyName() }.eachCount()
    return routeItems.associate { route ->
        val baseName = route.baseRouteKeyName()
        val finalName = if (baseNameCounts.getValue(baseName) == 1) {
            baseName
        } else {
            route.fallbackRouteKeyName()
        }
        route.uniqueId to finalName
    }
}

private fun RouteRecord.baseRouteKeyName(): String {
    return simpleName.toUnderLineCase()
        .uppercase()
        .ifBlank { "ROUTE" }
}

private fun RouteRecord.fallbackRouteKeyName(): String {
    val rawValue = qualifiedName.ifBlank { routePath }
    return rawValue
        .replace(Regex("[^A-Za-z0-9]+"), "_")
        .trim('_')
        .uppercase()
        .ifBlank { baseRouteKeyName() }
}

internal expect fun aggregateAndGenerateRoutes(
    sharedSourceDir: String,
    routeGenPkg: String,
    ownerModuleHint: String,
    sourceFilePaths: List<String>,
    routeItems: List<RouteRecord>,
    logger: KSPLogger,
)

/**
 * 菜单元数据注解处理器提供者
 */
class RouteMetadataProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouteMetadataProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options,
        )
    }
}
