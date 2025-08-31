package com.addzero

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration

/**
 * Route注解处理器的Provider
 */
class RouteProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouteProcessor(environment)
    }
}

/**
 * Route注解处理器
 * 负责处理带有@Route注解的类，生成路由表
 */
class RouteProcessor(environment: SymbolProcessorEnvironment) : AbsProcessor<Route, RouteProcessor>(environment) {

    // KSP 参数键名，用于控制是否生成路由表
    companion object {
        const val OPTION_GENERATE_ROUTE_TABLE = "route.generateTable"
    }

    // 是否生成路由表，默认为 false
    private val generateRouteTable: Boolean = environment.options[OPTION_GENERATE_ROUTE_TABLE]?.toBoolean() ?: false

    override val PKG: String
        get() = "com.addzero.ksp.route"

    // 使用批量生成模式，但根据参数决定是否实际生成
    override val generationMode: GenerationMode = GenerationMode.BULK

    override fun getAnnotationName(): String = Route::class.qualifiedName!!

    override fun extractMetaData(declaration: KSDeclaration, annotation: KSAnnotation): Route {
        val declarationSimpleName = declaration.simpleName.asString()
        val declarationQulifiedName = declaration.qualifiedName?.asString() ?: ""
        val containingClassName =
            (declaration.parentDeclaration as? KSClassDeclaration)?.qualifiedName?.asString() ?: ""
        val value = getAnnoProperty(annotation, "value", String::class)

        val path = getAnnoProperty(annotation, "path", String::class).ifBlank { declarationQulifiedName }

        val title = getAnnoProperty(annotation, "title", String::class).ifBlank { declarationSimpleName }
        val parent = getAnnoProperty(annotation, "parent", String::class)
        val icon = getAnnoProperty(annotation, "icon", String::class)
        val visible = getAnnoProperty(annotation, "visible", Boolean::class)
        val order = getAnnoProperty(annotation, "order", Double::class)
        val permissions = getAnnoProperty(annotation, "permissions", String::class)
        val homePageFlag = getAnnoProperty(annotation, "homePageFlag", Boolean::class)

        return Route(
            value = value,
            path = path,
            title = title,
            parent = parent,
            icon = icon,
            visible = visible,
            order = order,
            permissions = permissions,
            declarationQulifiedName = declarationQulifiedName,
            homePageFlag = homePageFlag
        )
    }

    override fun generateCode(resolver: Resolver, metaList: List<Route>): String {
        // 如果不需要生成路由表或没有数据，返回空字符串
        if (!generateRouteTable || metaList.isEmpty()) return ""

        val routeTableContent = """
            package $PKG
            import com.addzero.Route
            import androidx.compose.runtime.Composable
            
            object RouteTable {
                val routes = mapOf(
                    ${
            metaList.joinToString(",\n                    ") { meta ->

                val parent = meta.parent.ifBlank { meta.value }

                """Route(
                        value = "${meta.value}",
                        path = "${meta.path}",
                        title = "${meta.title}",
                        parent = "$parent",
                        icon = "${meta.icon}",
                        visible = ${meta.visible},
                        order = ${meta.order},
                        declarationQulifiedName = "${meta.declarationQulifiedName}",
                        homePageFlag =  ${meta.homePageFlag},
                        permissions = "${meta.permissions}"
                    ) to @Composable { ${meta.declarationQulifiedName}() }"""
            }
        }
                )
            }
        """.trimIndent()

        // 在日志中输出参数状态，方便调试
        logger.info("生成路由表参数设置: OPTION_GENERATE_ROUTE_TABLE = $generateRouteTable")

        return routeTableContent
    }

    override fun FILE_NAME(
        resolver: Resolver, metaList: List<Route>
    ): String {
        return "RouteTable"
    }
}

