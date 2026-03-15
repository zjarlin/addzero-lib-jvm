package site.addzero.gradle.tool

import org.gradle.api.Project
import java.io.File

/**
 * 读取 .env 文件并解析为 Map<String, String>
 *
 * 支持：
 * - KEY=VALUE
 * - KEY="VALUE" / KEY='VALUE'（去除引号）
 * - # 开头的注释行
 * - 空行跳过
 * - VALUE 中的行内注释（未被引号包裹时 # 后的内容会被忽略）
 * - 变量引用：$VAR 或 ${VAR}（引用已解析的变量或系统环境变量）
 * - 单引号内不做变量替换（与 bash 行为一致）
 */
fun parseEnvFile(file: File, base: Map<String, String> = emptyMap()): Map<String, String> {
    if (!file.exists()) return base.toMap()
    val result = linkedMapOf<String, String>()
    result.putAll(base)
    file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .forEach { line ->
            val idx = line.indexOf('=')
            if (idx <= 0) return@forEach
            val key = line.substring(0, idx).trim()
            var value = line.substring(idx + 1).trim()
            var singleQuoted = false
            // 去除引号包裹
            if (value.length >= 2) {
                if (value.startsWith("'") && value.endsWith("'")) {
                    value = value.substring(1, value.length - 1)
                    singleQuoted = true
                } else if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length - 1)
                }
            }
            if (!singleQuoted) {
                // 去除行内注释
                val commentIdx = value.indexOf(" #")
                if (commentIdx >= 0) {
                    value = value.substring(0, commentIdx).trim()
                }
                // 解析变量引用 $VAR 和 ${VAR}
                value = resolveVariables(value, result)
            }
            // 值为空时 fallback 到系统环境变量
            if (value.isEmpty()) {
                value = System.getenv(key) ?: ""
            }
            result[key] = value
        }
    return result
}

private val VAR_PATTERN = Regex("""\$\{([^}]+)}|\$([A-Za-z_][A-Za-z0-9_]*)""")

private fun resolveVariables(value: String, resolved: Map<String, String>): String {
    return VAR_PATTERN.replace(value) { match ->
        val varName = match.groupValues[1].ifEmpty { match.groupValues[2] }
        // 系统环境变量优先于 .env 文件（与 Docker Compose / Spring 行为一致）
        System.getenv(varName) ?: resolved[varName] ?: match.value
    }
}

/**
 * 根据 gradle.properties 中的 buildkonfig.flavor 加载环境配置
 *
 * 加载顺序（后者覆盖前者）：
 * 1. .env          — 始终加载，公共基础配置
 * 2. .env.{flavor} — 按 flavor 加载，如 .env.dev / .env.prod
 */
fun Project.loadEnv(): Map<String, String> {
    val flavor = rootProject.findProperty("buildkonfig.flavor")?.toString() ?: "dev"
    // 1. 始终加载 .env
    val base = parseEnvFile(rootProject.file(".env"))
    // 2. 按 flavor 加载 .env.dev / .env.prod，覆盖 base
    return parseEnvFile(rootProject.file(".env.$flavor"), base)
}
