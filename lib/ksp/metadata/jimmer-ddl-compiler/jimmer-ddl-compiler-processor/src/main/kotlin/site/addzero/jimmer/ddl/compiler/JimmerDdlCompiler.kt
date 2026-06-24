package site.addzero.jimmer.ddl.compiler

import site.addzero.ddlgenerator.runtime.AutoDdlRuntime
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.jimmer.isJimmerEntity

object JimmerDdlCompiler {
    fun compile(
        classes: Collection<LsiClass>,
        settings: JimmerDdlCompilerSettings,
    ): JimmerDdlCompilerResult {
        if (!settings.enabled) {
            return JimmerDdlCompilerResult.empty(settings)
        }

        val entities = classes.toJimmerDdlLsiClasses()
            .filter { it.isJimmerEntity }
            .filter { settings.includesClass(it.qualifiedName) }
            .distinctBy { it.qualifiedName ?: it.simpleName.orEmpty() }
            .sortedBy { it.qualifiedName ?: it.simpleName.orEmpty() }
        if (entities.isEmpty()) {
            return JimmerDdlCompilerResult.empty(settings)
        }

        val statements = AutoDdlRuntime.generate(
            lsiClasses = entities,
            databaseType = settings.databaseType,
            options = settings.options,
            includeManyToManyTables = settings.includeManyToManyTables,
        )
        val sql = statements.joinToString(separator = "\n")
            .trim()
            .let { content ->
                if (content.isBlank()) {
                    content
                } else {
                    content + "\n"
                }
            }
        return JimmerDdlCompilerResult(
            settings = settings,
            entities = entities,
            statements = statements,
            sql = sql,
        )
    }
}

data class JimmerDdlCompilerResult(
    val settings: JimmerDdlCompilerSettings,
    val entities: List<LsiClass>,
    val statements: List<String>,
    val sql: String,
) {
    val isEmpty: Boolean
        get() = entities.isEmpty() || sql.isBlank()

    companion object {
        fun empty(settings: JimmerDdlCompilerSettings): JimmerDdlCompilerResult {
            return JimmerDdlCompilerResult(
                settings = settings,
                entities = emptyList(),
                statements = emptyList(),
                sql = "",
            )
        }
    }
}
