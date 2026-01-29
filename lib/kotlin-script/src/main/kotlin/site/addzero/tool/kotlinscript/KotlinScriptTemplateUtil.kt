package site.addzero.tool.kotlinscript

import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

data class KotlinScriptTemplateCtx(
    val metadata: Map<String, Any?> = emptyMap()
) {
    operator fun get(key: String): Any? {
        if (!key.contains('.')) {
            return metadata[key]
        }
        var current: Any? = metadata
        for (segment in key.split('.')) {
            current = when (current) {
                is Map<*, *> -> current[segment]
                is KotlinScriptTemplateCtx -> current.metadata[segment]
                else -> return null
            }
        }
        return current
    }
}

object KotlinScriptTemplateUtil {
    private val tokenRegex = Regex("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}")
    private val host = BasicJvmScriptingHost()
    private val compilationConfig = ScriptCompilationConfiguration {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
        providedProperties("ctx" to KotlinScriptTemplateCtx::class)
    }

    fun fillTemplate(
        template: String,
        ctx: KotlinScriptTemplateCtx,
        keepUnknown: Boolean = true
    ): String = fillTemplate(template, ctx.metadata, keepUnknown)

    fun fillTemplate(
        template: String,
        metadata: Map<String, Any?>,
        keepUnknown: Boolean = true
    ): String = tokenRegex.replace(template) { match ->
        val key = match.groupValues[1]
        val resolved = resolveKey(metadata, key)
        when {
            resolved == null && keepUnknown -> match.value
            resolved == null -> ""
            else -> resolved.toString()
        }
    }

    fun evalTemplate(
        template: String,
        metadata: Map<String, Any?> = emptyMap()
    ): String {
        val script = """
            val __result = ${toRawStringLiteral(template)}
            __result
        """.trimIndent()
        val evalResult = evalScript(script, metadata)
        return extractValue(evalResult) as? String ?: ""
    }

    fun evalScriptText(
        script: String,
        metadata: Map<String, Any?> = emptyMap()
    ): Any? {
        val evalResult = evalScript(script, metadata)
        return extractValue(evalResult)
    }

    private fun resolveKey(metadata: Map<String, Any?>, key: String): Any? {
        if (!key.contains('.')) {
            return metadata[key]
        }
        var current: Any? = metadata
        for (segment in key.split('.')) {
            current = when (current) {
                is Map<*, *> -> current[segment]
                is KotlinScriptTemplateCtx -> current.metadata[segment]
                else -> return null
            }
        }
        return current
    }

    private fun toRawStringLiteral(template: String): String {
        if (!template.contains("\"\"\"")) {
            return "\"\"\"$template\"\"\""
        }
        val parts = template.split("\"\"\"")
        return buildString {
            append("\"\"\"")
            parts.forEachIndexed { index, part ->
                append(part)
                if (index != parts.lastIndex) {
                    append("\${\"\\\"\\\"\\\"\"}")
                }
            }
            append("\"\"\"")
        }
    }

    private fun evalScript(
        script: String,
        metadata: Map<String, Any?>
    ): ResultWithDiagnostics<EvaluationResult> {
        val evaluationConfig = ScriptEvaluationConfiguration {
            providedProperties(mapOf("ctx" to KotlinScriptTemplateCtx(metadata)))
        }
        return host.eval(script.toScriptSource(), compilationConfig, evaluationConfig)
    }

    private fun extractValue(result: ResultWithDiagnostics<EvaluationResult>): Any? {
        val evaluationResult = result.valueOrNull() ?: error(
            result.reports.joinToString("\n") { it.message }
        )
        return when (val returnValue = evaluationResult.returnValue) {
            is ResultValue.Value -> returnValue.value
            is ResultValue.Unit -> Unit
            is ResultValue.Error -> throw returnValue.error
            else -> null
        }
    }
}
