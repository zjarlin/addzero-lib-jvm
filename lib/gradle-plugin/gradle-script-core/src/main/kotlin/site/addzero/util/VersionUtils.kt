package site.addzero.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.regex.Pattern

/**
 * 版本号工具类：支持带任意字母前缀（不区分大小写）的版本递增
 * 支持格式：
 * 1. 带字母前缀（如 V1.2.3、ver2.0、Alpha0.9.9 → 前缀保留，版本部分递增）
 * 2. 语义化版本（2段/3段）：1.0 → 1.1；0.0.999 → 0.1.0
 * 3. 日期版本：2025.10.19 → 2025.10.20；v2025.12.31 → v2026.01.01
 */
object VersionUtils {
    fun defaultVersion(): String {
        val versionDate: String = SimpleDateFormat("yyyy.MM.dd").format(Date())
        return versionDate
    }


    /**
     * 分离版本号的前缀（字母部分）和核心版本（数字部分）
     * 前缀：所有开头的字母（不区分大小写），如 "Alpha"、"v"、"VER"
     * 核心版本：剩余的数字+点部分，如 "1.2.3"、"0.9"
     */
    private data class VersionParts(val prefix: String, val coreVersion: String)

    /**
     * 获取下一个版本号
     * @param currentVersion 当前版本号（支持带任意字母前缀）
     * @return 递增后的版本号，若格式不支持则返回 null
     */
    fun nextVersion(currentVersion: String): String {
        // 分离前缀和核心版本
        val (prefix, coreVersion) = splitVersionPrefix(currentVersion)

        // 尝试解析为日期版本（yyyy.MM.dd）
        val dateVersion = parseDateVersion(coreVersion)
        if (dateVersion != null) {
            val nextCore = incrementDateVersion(dateVersion)
            return prefix + nextCore
        }

        // 尝试解析为语义化版本（2段或3段数字）
        val semanticParts = parseSemanticVersion(coreVersion)
        if (semanticParts != null) {
            val nextCore = incrementSemanticVersion(semanticParts)
            return prefix + nextCore
        }

        println("not supprt next version,the currentVersion is $currentVersion")
        return currentVersion
    }

    /**
     * 分离版本号的前缀（字母部分）和核心版本
     * 例如：
     * - "V1.2.3" → 前缀 "V"，核心 "1.2.3"
     * - "ver2.0" → 前缀 "ver"，核心 "2.0"
     * - "Alpha0.9.9" → 前缀 "Alpha"，核心 "0.9.9"
     * - "1.0" → 前缀 ""，核心 "1.0"
     */
    private fun splitVersionPrefix(version: String): VersionParts {
        // 匹配开头的所有字母（a-z, A-Z）
        val pattern = Pattern.compile("^[A-Za-z]*")
        val matcher = pattern.matcher(version)
        return if (matcher.find()) {
            val prefix = matcher.group()
            val coreVersion = version.substring(prefix.length)
            VersionParts(prefix, coreVersion)
        } else {
            VersionParts("", version)
        }
    }

    /**
     * 解析日期版本（格式：yyyy.MM.dd）
     */
    private fun parseDateVersion(coreVersion: String): LocalDate? {
        return try {
            LocalDate.parse(coreVersion, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * 递增日期版本（+1天）
     */
    private fun incrementDateVersion(date: LocalDate): String {
        return date.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    }

    /**
     * 解析语义化版本（支持2段x.y或3段x.y.z，数字非负）
     */
    private fun parseSemanticVersion(coreVersion: String): List<Int>? {
        val parts = coreVersion.split('.')
        // 必须是2段或3段数字
        if (parts.size !in 2..3) return null
        return try {
            parts.map { it.toInt() }.also {
                // 每段必须是非负整数
                if (it.any { num -> num < 0 }) return null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * 递增语义化版本
     * - 2段版本（x.y）：y+1，y满999则x+1、y=0
     * - 3段版本（x.y.z）：z+1，z满999则y+1、z=0；y满999则x+1、y=0、z=0
     */
    private fun incrementSemanticVersion(parts: List<Int>): String {
        return when (parts.size) {
            2 -> { // 2段版本：x.y
                var (major, minor) = parts
                minor += 1
                if (minor > 999) {
                    minor = 0
                    major += 1
                }
                "$major.$minor"
            }

            3 -> { // 3段版本：x.y.z
                var (major, minor, patch) = parts
                patch += 1
                if (patch > 999) {
                    patch = 0
                    minor += 1
                    if (minor > 999) {
                        minor = 0
                        major += 1
                    }
                }
                "$major.$minor.$patch"
            }

            else -> "" // 不可能走到这里（已在parse阶段校验）
        }
    }
}
