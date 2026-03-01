package site.addzero.gradle.tool

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

object CatalogUtils {

    @JvmStatic
    fun catalog(project: Project, name: String = "libs"): VersionCatalog =
        project.extensions.getByType<VersionCatalogsExtension>().named(name)

    @JvmStatic
    fun findLibrary(catalog: VersionCatalog, alias: String): Provider<MinimalExternalModuleDependency> {
        val opt = catalog.findLibrary(alias)
        if (opt.isPresent) return opt.get()
        val tomlKey = alias.replace("-", ".")
        error(buildString {
            appendLine("❌ Library alias '$alias' not found in version catalog.")
            appendLine("   Please add it to your gradle/libs.versions.toml:")
            appendLine()
            appendLine("   [libraries]")
            appendLine("   $tomlKey = { group = \"<GROUP>\", name = \"<ARTIFACT>\", version.ref = \"<VERSION_REF>\" }")
            appendLine()
            appendLine("   Example:")
            appendLine("   $tomlKey = { group = \"io.ktor\", name = \"ktor-client-cio\", version.ref = \"ktor\" }")
        })
    }

    @JvmStatic
    fun findLibraryOrNull(catalog: VersionCatalog, alias: String): Provider<MinimalExternalModuleDependency>? {
        val opt = catalog.findLibrary(alias)
        if (opt.isPresent) return opt.get()
        val tomlKey = alias.replace("-", ".")
        println(buildString {
            appendLine("⚠️  Library alias '$alias' not found in version catalog.")
            appendLine("    Please add to gradle/libs.versions.toml [libraries]:")
            appendLine("    $tomlKey = { group = \"<GROUP>\", name = \"<ARTIFACT>\", version.ref = \"<VERSION_REF>\" }")
        })
        return null
    }

    @JvmStatic
    fun findVersion(catalog: VersionCatalog, alias: String): String {
        val opt = catalog.findVersion(alias)
        if (opt.isPresent) return opt.get().toString()
        error(buildString {
            appendLine("❌ Version alias '$alias' not found in version catalog.")
            appendLine("   Please add it to your gradle/libs.versions.toml:")
            appendLine()
            appendLine("   [versions]")
            appendLine("   $alias = \"<VERSION>\"")
        })
    }

    @JvmStatic
    fun findVersionOrNull(catalog: VersionCatalog, alias: String): String? {
        val opt = catalog.findVersion(alias)
        if (opt.isPresent) return opt.get().toString()
        println("⚠️  Version alias '$alias' not found. Please add to gradle/libs.versions.toml [versions]: $alias = \"<VERSION>\"")
        return null
    }

    @JvmStatic
    fun findPlugin(catalog: VersionCatalog, alias: String): Provider<org.gradle.plugin.use.PluginDependency> {
        val opt = catalog.findPlugin(alias)
        if (opt.isPresent) return opt.get()
        error(buildString {
            appendLine("❌ Plugin alias '$alias' not found in version catalog.")
            appendLine("   Please add it to your gradle/libs.versions.toml:")
            appendLine()
            appendLine("   [plugins]")
            appendLine("   $alias = { id = \"<PLUGIN_ID>\", version.ref = \"<VERSION_REF>\" }")
        })
    }
}

fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    CatalogUtils.findLibrary(this, alias)

fun VersionCatalog.libOrNull(alias: String): Provider<MinimalExternalModuleDependency>? =
    CatalogUtils.findLibraryOrNull(this, alias)

fun VersionCatalog.ver(alias: String): String =
    CatalogUtils.findVersion(this, alias)

fun VersionCatalog.verOrNull(alias: String): String? =
    CatalogUtils.findVersionOrNull(this, alias)
