package site.addzero.gradle.tool

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

/**
 * 智能依赖解析器 - 解决多版本冲突问题
 *
 * 使用方式（在预编译脚本 .gradle.kts 中）：
 * ```kotlin
 * val libs = versionCatalogs.named("libs")
 * val webDep = smartFind(libs, "org.springframework.boot", "spring-boot-starter-web")
 *     ?: error("Not found")
 * ```
 */
object SmartDependencyResolver {

    /**
     * 版本选择策略
     */
    enum class VersionStrategy {
        /** 选择最高版本 */
        LATEST,
        /** 选择最低版本 */
        OLDEST,
        /** 优先找无后缀的默认版本 */
        DEFAULT_FIRST,
        /** 使用 preferredVersion 参数指定的版本 */
        PREFERRED,
        /** 报错要求显式指定 */
        STRICT
    }

    /**
     * 通过 Maven 坐标查找，支持多版本选择策略
     *
     * @param libs Catalog 实例（通过 versionCatalogs.named("libs") 获取）
     * @param group Maven groupId
     * @param artifact Maven artifactId（不含版本后缀）
     * @param strategy 遇到多版本时的选择策略
     * @param preferredVersion 优先考虑的版本（如 "6", "5.3"）
     * @return 匹配的依赖，如果没找到返回 null
     */
    @Suppress("UNCHECKED_CAST")
    fun smartFind(
        libs: Any,
        group: String,
        artifact: String,
        strategy: VersionStrategy = VersionStrategy.LATEST,
        preferredVersion: String? = null
    ): Provider<MinimalExternalModuleDependency>? {
        val candidates = mutableListOf<Pair<String, MinimalExternalModuleDependency>>()

        // 通过反射获取 libraryAliases 和 findLibrary
        val aliases = try {
            libs::class.java.getMethod("getLibraryAliases").invoke(libs) as? Collection<String>
        } catch (e: Exception) {
            return null
        } ?: return null

        val findLibraryMethod = try {
            libs::class.java.getMethod("findLibrary", String::class.java)
        } catch (e: Exception) {
            return null
        }

        for (alias in aliases) {
            val libProvider = try {
                findLibraryMethod.invoke(libs, alias) as? Provider<*>
            } catch (e: Exception) {
                continue
            } ?: continue

            val lib = libProvider.orNull as? MinimalExternalModuleDependency ?: continue
            if (lib.module.group == group &&
                (lib.module.name == artifact || lib.module.name.startsWith("$artifact-"))) {
                candidates.add(alias to lib)
            }
        }

        if (candidates.isEmpty()) return null
        if (candidates.size == 1) {
            return try {
                findLibraryMethod.invoke(libs, candidates.first().first) as? Provider<MinimalExternalModuleDependency>
            } catch (e: Exception) {
                null
            }
        }

        val selected: Pair<String, MinimalExternalModuleDependency> = when (strategy) {
            VersionStrategy.STRICT -> {
                error(buildStrictErrorMessage(group, artifact, candidates))
            }
            VersionStrategy.PREFERRED -> {
                preferredVersion?.let { pref ->
                    candidates.find { (_, lib) ->
                        lib.version.toString().startsWith(pref)
                    } ?: error("No version starting with $pref found for $group:$artifact")
                } ?: selectLatest(candidates)
            }
            VersionStrategy.LATEST -> selectLatest(candidates)
            VersionStrategy.OLDEST -> selectOldest(candidates)
            VersionStrategy.DEFAULT_FIRST -> {
                candidates.find { (alias, _) ->
                    !alias.contains("-v") && !alias.contains("-version")
                } ?: selectLatest(candidates)
            }
        }

        return try {
            findLibraryMethod.invoke(libs, selected.first) as? Provider<MinimalExternalModuleDependency>
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解析 version alias（如 spring-web-v3 → 3.2.18.RELEASE）
     */
    @Suppress("UNCHECKED_CAST")
    fun resolveVersionAlias(libs: Any, versionAlias: String): String? {
        val findVersionMethod = try {
            libs::class.java.getMethod("findVersion", String::class.java)
        } catch (e: Exception) {
            return null
        }

        return try {
            val version = findVersionMethod.invoke(libs, versionAlias)
            version?.toString()
        } catch (e: Exception) {
            null
        }
    }

    // ==================== 内部实现 ====================

    private fun selectLatest(
        candidates: List<Pair<String, MinimalExternalModuleDependency>>
    ): Pair<String, MinimalExternalModuleDependency> {
        return candidates.maxByOrNull { (_, lib) ->
            parseVersion(lib.version.toString())
        } ?: candidates.first()
    }

    private fun selectOldest(
        candidates: List<Pair<String, MinimalExternalModuleDependency>>
    ): Pair<String, MinimalExternalModuleDependency> {
        return candidates.minByOrNull { (_, lib) ->
            parseVersion(lib.version.toString())
        } ?: candidates.first()
    }

    private fun parseVersion(version: String): Version {
        val clean = version.replace(Regex("[-_].*$"), "")
        val parts = clean.split(".").mapNotNull { it.toIntOrNull() }
        return Version(
            major = parts.getOrNull(0) ?: 0,
            minor = parts.getOrNull(1) ?: 0,
            patch = parts.getOrNull(2) ?: 0
        )
    }

    private data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
        override fun compareTo(other: Version): Int {
            return compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch })
        }
    }

    private fun buildStrictErrorMessage(
        group: String,
        artifact: String,
        candidates: List<Pair<String, MinimalExternalModuleDependency>>
    ): String {
        return buildString {
            appendLine("Multiple versions found for $group:$artifact:")
            candidates.forEach { (alias, lib) ->
                appendLine("  - $alias: ${lib.module.group}:${lib.module.name}:${lib.version}")
            }
            appendLine()
            appendLine("Please either:")
            appendLine("1. Keep only one version in catalog")
            appendLine("2. Use smartFind with strategy = PREFERRED and preferredVersion")
            appendLine("3. Explicitly specify alias in dependency declaration")
        }
    }
}

/**
 * 项目扩展 - 让消费方声明版本偏好
 */
open class DependencyResolutionExtension {
    val versionPreferences = mutableMapOf<String, String>()
    val versionLocks = mutableMapOf<String, String>()

    fun prefer(library: String, version: String) {
        versionPreferences[library] = version
    }

    fun lock(coordinates: String, version: String) {
        versionLocks[coordinates] = version
    }
}

fun Project.configureSmartResolver() {
    extensions.create("dependencyResolution", DependencyResolutionExtension::class.java)
}

fun Project.getPreferredVersion(library: String): String? {
    return extensions.findByType(DependencyResolutionExtension::class.java)
        ?.versionPreferences?.get(library)
}
