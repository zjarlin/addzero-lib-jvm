package site.addzero.gradle.tool

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

/**
 * 通过 Maven 坐标在 Version Catalog 中查找依赖
 *
 * @param libs Catalog 实例（通过 versionCatalogs.named("libs") 获取，类型为 VersionCatalog）
 * @param group Maven groupId，例如 "org.springframework.boot"
 * @param artifact Maven artifactId，例如 "spring-boot-starter-web"
 * @return 匹配的依赖 Provider，如果没找到返回 null
 *
 * 示例（在预编译脚本中）：
 * ```kotlin
 * val libs = versionCatalogs.named("libs")
 * val dep = findByCoords(libs, "org.springframework.boot", "spring-boot-starter-web")
 *     ?: throw GradleException("Required dependency not found in catalog")
 * dependencies { implementation(dep) }
 * ```
 */
@Suppress("UNCHECKED_CAST")
fun findByCoords(
    libs: Any,  // VersionCatalog 类型，但 Gradle API 中类型名可能不同
    group: String,
    artifact: String
): Provider<MinimalExternalModuleDependency>? {
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
        if (lib.module.group == group && lib.module.name == artifact) {
            return libProvider as Provider<MinimalExternalModuleDependency>
        }
    }
    return null
}

/**
 * 查找依赖（支持模糊匹配 artifact 名称前缀）
 *
 * @param libs Catalog 实例
 * @param group Maven groupId
 * @param artifactPrefix artifact 名称前缀
 * @return 匹配的所有依赖
 */
@Suppress("UNCHECKED_CAST")
fun findAllByCoords(
    libs: Any,
    group: String,
    artifactPrefix: String? = null
): List<Provider<MinimalExternalModuleDependency>> {
    val result = mutableListOf<Provider<MinimalExternalModuleDependency>>()

    val aliases = try {
        libs::class.java.getMethod("getLibraryAliases").invoke(libs) as? Collection<String>
    } catch (e: Exception) {
        return emptyList()
    } ?: return emptyList()

    val findLibraryMethod = try {
        libs::class.java.getMethod("findLibrary", String::class.java)
    } catch (e: Exception) {
        return emptyList()
    }

    for (alias in aliases) {
        val libProvider = try {
            findLibraryMethod.invoke(libs, alias) as? Provider<*>
        } catch (e: Exception) {
            continue
        } ?: continue

        val lib = libProvider.orNull as? MinimalExternalModuleDependency ?: continue

        val groupMatches = lib.module.group == group
        val artifactMatches = artifactPrefix == null ||
                lib.module.name == artifactPrefix ||
                lib.module.name.startsWith("$artifactPrefix-")

        if (groupMatches && artifactMatches) {
            result.add(libProvider as Provider<MinimalExternalModuleDependency>)
        }
    }
    return result
}

/**
 * 获取 Catalog 中所有依赖的描述信息（用于调试）
 */
@Suppress("UNCHECKED_CAST")
fun listAllDependencies(libs: Any): Map<String, String> {
    val result = mutableMapOf<String, String>()

    val aliases = try {
        libs::class.java.getMethod("getLibraryAliases").invoke(libs) as? Collection<String>
    } catch (e: Exception) {
        return emptyMap()
    } ?: return emptyMap()

    val findLibraryMethod = try {
        libs::class.java.getMethod("findLibrary", String::class.java)
    } catch (e: Exception) {
        return emptyMap()
    }

    for (alias in aliases) {
        val libProvider = try {
            findLibraryMethod.invoke(libs, alias) as? Provider<*>
        } catch (e: Exception) {
            continue
        } ?: continue

        val lib = libProvider.orNull as? MinimalExternalModuleDependency ?: continue
        result[alias] = "${lib.module.group}:${lib.module.name}:${lib.version}"
    }
    return result
}

/**
 * 获取依赖的完整坐标信息（用于调试）
 */
@Suppress("UNCHECKED_CAST")
fun describeDependency(libs: Any, alias: String): String? {
    val findLibraryMethod = try {
        libs::class.java.getMethod("findLibrary", String::class.java)
    } catch (e: Exception) {
        return null
    }

    val libProvider = try {
        findLibraryMethod.invoke(libs, alias) as? Provider<*>
    } catch (e: Exception) {
        return null
    } ?: return null

    val lib = libProvider.orNull as? MinimalExternalModuleDependency ?: return null

    return buildString {
        appendLine("alias: $alias")
        appendLine("group: ${lib.module.group}")
        appendLine("artifact: ${lib.module.name}")
        append("version: ${lib.version}")
    }
}

/**
 * 类型安全的依赖声明辅助类
 */
class CatalogDeps(private val project: Project) {

    fun implementation(provider: Provider<MinimalExternalModuleDependency>) {
        project.dependencies.add("implementation", provider)
    }

    fun implementation(group: String, artifact: String, version: String? = null) {
        val dep = version?.let { "$group:$artifact:$it" } ?: "$group:$artifact"
        project.dependencies.add("implementation", dep)
    }
}

fun Project.catalogDeps(configure: CatalogDeps.() -> Unit) {
    CatalogDeps(this).apply(configure)
}
