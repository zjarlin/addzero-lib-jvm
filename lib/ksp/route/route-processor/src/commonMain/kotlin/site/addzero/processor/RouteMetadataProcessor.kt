package site.addzero.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import site.addzero.annotation.Route
import site.addzero.route.processor.context.Settings
import site.addzero.util.str.toUnderLineCase

internal const val ROUTE_TABLE_NAME = "RouteTable"
internal const val ROUTE_KEYS_NAME = "RouteKeys"
internal const val ROUTE_AGGREGATION_ROLE_CONTRIBUTOR = "contributor"
internal const val ROUTE_AGGREGATION_ROLE_OWNER = "owner"

internal data class RouteRecord(
    val parentName: String,
    val title: String,
    val routePath: String,
    val icon: String,
    val order: Double,
    val qualifiedName: String,
    val simpleName: String,
    val sceneName: String,
    val sceneIcon: String,
    val sceneOrder: Int,
    val defaultInScene: Boolean,
) {
    val uniqueId
        get() = qualifiedName.ifBlank { routePath }
}

internal enum class RouteAggregationRole(
    val optionValue: String,
    val shouldGenerateAggregates: Boolean,
) {
    CONTRIBUTOR(
        optionValue = ROUTE_AGGREGATION_ROLE_CONTRIBUTOR,
        shouldGenerateAggregates = false,
    ),
    OWNER(
        optionValue = ROUTE_AGGREGATION_ROLE_OWNER,
        shouldGenerateAggregates = true,
    ),
    ;

    companion object {
        fun fromOption(value: String): RouteAggregationRole {
            return when (value.trim().lowercase()) {
                ROUTE_AGGREGATION_ROLE_OWNER -> OWNER
                else -> CONTRIBUTOR
            }
        }
    }
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
    private val moduleSourceRoots = linkedSetOf<String>()

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
                    processClass(symbol)?.let(collectedRoutes::add)
                }

                is KSFunctionDeclaration -> {
                    processFunction(symbol)?.let(collectedRoutes::add)
                }

                is KSPropertyDeclaration -> {
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
            deprecatedSharedSourceDir = Settings.sharedSourceDir,
            routeGenPkg = Settings.routeGenPkg,
            routeOwnerModuleDir = Settings.routeOwnerModule,
            aggregationRole = RouteAggregationRole.fromOption(Settings.routeAggregationRole),
            moduleKeyHint = options["routeModuleKey"].orEmpty(),
            moduleSourceRoots = moduleSourceRoots.toList(),
            routeItems = sortRoutes(collectedRoutes),
            logger = logger,
        )
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
            declaration.containingFile
                ?.filePath
                ?.let(::sourcePathToModuleRoot)
                ?.let(moduleSourceRoots::add)
            val qualifiedName = declaration.qualifiedName?.asString().orEmpty()
            val simpleName = declaration.simpleName.asString()
            val annotation = declaration.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == Route::class.qualifiedName
            }
            val enabled = annotation.arguments.firstOrNull { it.name?.asString() == "enabled" }?.value as? Boolean
                ?: true
            if (!enabled) {
                return null
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
            val placement = annotation.arguments.firstOrNull { it.name?.asString() == "placement" }?.value as? KSAnnotation
            val scene = placement?.arguments?.firstOrNull { it.name?.asString() == "scene" }?.value as? KSAnnotation
            val defaultInScene = placement?.arguments
                ?.firstOrNull { it.name?.asString() == "defaultInScene" }
                ?.value as? Boolean
                ?: false

            RouteRecord(
                parentName = group.trim(),
                title = title,
                routePath = routePath,
                icon = icon,
                order = order,
                qualifiedName = qualifiedName,
                simpleName = simpleName,
                sceneName = scene.stringArg("name"),
                sceneIcon = scene.stringArg("icon").ifBlank { "Apps" },
                sceneOrder = scene.intArg("order", Int.MAX_VALUE),
                defaultInScene = defaultInScene,
            )
        } catch (e: Exception) {
            logger.error("Error processing Route annotation: ${e.message}", declaration)
            null
        }
    }
}

internal fun renderRouteTableCode(routeItems: List<RouteRecord>): String {
    val routeKeyNames = buildRouteKeyNames(routeItems)
    val routeMappings = routeItems.joinToString("\n") { route ->
        val routeKeyName = routeKeyNames.getValue(route.uniqueId)
        "        put(RouteKeys.$routeKeyName, { ${route.qualifiedName}() })"
    }

    return """
        |package ${Settings.routeGenPkg}
        |
        |import androidx.compose.runtime.Composable
        |
        |typealias RouteContent = @Composable () -> Unit
        |
        |/**
        | * 路由表
        | * 请勿手动修改此文件
        | */
        |object $ROUTE_TABLE_NAME {
        |    /**
        |     * 所有路由映射
        |     */
        |    val allRoutes: Map<String, RouteContent> = mutableMapOf<String, RouteContent>().apply {
        |$routeMappings
        |    }
        |
        |    /**
        |     * 根据路由键获取对应的Composable函数
        |     */
        |    operator fun get(routeKey: String): RouteContent {
        |        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: ${'$'}routeKey")
        |    }
        |}
        |""".trimMargin()
}

internal fun renderRouteKeysCode(routeItems: List<RouteRecord>): String {
    val routeKeyNames = buildRouteKeyNames(routeItems)
    val routeKeyConstants = routeItems.joinToString("\n    ") { route ->
        val routeKeyName = routeKeyNames.getValue(route.uniqueId)
        "const val $routeKeyName = ${route.routePath.asKotlinStringLiteral()}"
    }
    val allMetaItems = routeItems.joinToString(",\n        ") { route ->
        "Route(" +
            "value = ${route.parentName.asKotlinStringLiteral()}, " +
            "title = ${route.title.asKotlinStringLiteral()}, " +
            "routePath = ${route.routePath.asKotlinStringLiteral()}, " +
            "icon = ${route.icon.asKotlinStringLiteral()}, " +
            "order = ${route.order}, " +
            "placement = ${route.renderPlacementCode()}, " +
            "qualifiedName = ${route.qualifiedName.asKotlinStringLiteral()}, " +
            "simpleName = ${route.simpleName.asKotlinStringLiteral()}" +
            ")"
    }

    return """
        |package ${Settings.routeGenPkg}
        |
        |import site.addzero.annotation.Route
        |import site.addzero.annotation.RoutePlacement
        |import site.addzero.annotation.RouteScene
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

internal fun sourcePathToModuleRoot(sourceFilePath: String): String? {
    val normalizedPath = sourceFilePath.replace('\\', '/')
    val srcMarkerIndex = normalizedPath.indexOf("/src/")
    if (srcMarkerIndex >= 0) {
        return normalizedPath.substring(0, srcMarkerIndex)
    }

    val lastSlashIndex = normalizedPath.lastIndexOf('/')
    return if (lastSlashIndex > 0) {
        normalizedPath.substring(0, lastSlashIndex)
    } else {
        null
    }
}

internal expect fun aggregateAndGenerateRoutes(
    deprecatedSharedSourceDir: String,
    routeGenPkg: String,
    routeOwnerModuleDir: String,
    aggregationRole: RouteAggregationRole,
    moduleKeyHint: String,
    moduleSourceRoots: List<String>,
    routeItems: List<RouteRecord>,
    logger: KSPLogger,
)

private fun RouteRecord.renderPlacementCode(): String {
    return "RoutePlacement(" +
        "scene = RouteScene(" +
        "name = ${sceneName.asKotlinStringLiteral()}, " +
        "icon = ${sceneIcon.asKotlinStringLiteral()}, " +
        "order = $sceneOrder" +
        "), " +
        "defaultInScene = $defaultInScene" +
        ")"
}

private fun KSAnnotation?.stringArg(name: String): String {
    return this?.arguments
        ?.firstOrNull { argument -> argument.name?.asString() == name }
        ?.value as? String
        ?: ""
}

private fun KSAnnotation?.intArg(name: String, defaultValue: Int): Int {
    return this?.arguments
        ?.firstOrNull { argument -> argument.name?.asString() == name }
        ?.value as? Int
        ?: defaultValue
}

private fun String.asKotlinStringLiteral(): String {
    return buildString(length + 2) {
        append('"')
        this@asKotlinStringLiteral.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(ch)
            }
        }
        append('"')
    }
}

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
