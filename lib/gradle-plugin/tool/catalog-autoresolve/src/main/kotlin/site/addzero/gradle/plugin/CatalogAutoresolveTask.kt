package site.addzero.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task：扫描消费方 gradle/libs.versions.toml，对比已知 alias 映射表，
 * 将缺失的 [versions] / [libraries] 条目自动追加写入 TOML 文件。
 */
open class CatalogAutoresolveTask : DefaultTask() {

    @TaskAction
    fun resolve() {
        val tomlFile = project.rootDir.resolve("gradle/libs.versions.toml")
        if (!tomlFile.exists()) {
            logger.warn("⚠️  gradle/libs.versions.toml not found at ${tomlFile.absolutePath}, creating one.")
            tomlFile.parentFile.mkdirs()
            tomlFile.writeText("[versions]\n\n[libraries]\n\n[plugins]\n")
        }

        val tomlContent = tomlFile.readText()
        val existingAliases = parseExistingAliases(tomlContent)
        val knownArtifacts = KnownArtifacts.all()

        val missingVersions = mutableMapOf<String, String>()
        val missingLibraries = mutableListOf<String>()

        for ((alias, artifact) in knownArtifacts) {
            val tomlKey = alias.replace("-", ".")
            if (tomlKey !in existingAliases) {
                missingLibraries.add(artifact.toTomlLine(tomlKey))
                if (artifact.versionRef != null && artifact.versionRef !in existingAliases) {
                    missingVersions[artifact.versionRef] = artifact.defaultVersion ?: "FIXME"
                }
            }
        }

        if (missingLibraries.isEmpty()) {
            logger.lifecycle("✅ All catalog aliases are already present in libs.versions.toml")
            return
        }

        logger.lifecycle("📦 Found ${missingLibraries.size} missing library aliases. Auto-adding to libs.versions.toml ...")

        val sb = StringBuilder(tomlContent.trimEnd())
        sb.appendLine()

        if (missingVersions.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("# ── Auto-resolved versions ──")
            for ((ref, ver) in missingVersions.toSortedMap()) {
                val line = "$ref = \"$ver\""
                if (line !in tomlContent) {
                    sb.appendLine(line)
                    logger.lifecycle("  + [versions] $line")
                }
            }
        }

        if (missingLibraries.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("# ── Auto-resolved libraries ──")
            for (line in missingLibraries.sorted()) {
                sb.appendLine(line)
                logger.lifecycle("  + [libraries] $line")
            }
        }

        sb.appendLine()
        tomlFile.writeText(sb.toString())
        logger.lifecycle("✅ Done. Please review gradle/libs.versions.toml and fill in any FIXME versions.")
    }

    private fun parseExistingAliases(toml: String): Set<String> {
        val aliases = mutableSetOf<String>()
        for (line in toml.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || trimmed.startsWith("[") || trimmed.isBlank()) continue
            val key = trimmed.substringBefore("=").trim()
            if (key.isNotBlank()) aliases.add(key)
        }
        return aliases
    }
}

/**
 * 单条工件信息
 */
data class ArtifactEntry(
    val group: String,
    val name: String,
    val versionRef: String?,
    val defaultVersion: String? = null,
) {
    fun toTomlLine(tomlKey: String): String {
        return if (versionRef != null) {
            "$tomlKey = { group = \"$group\", name = \"$name\", version.ref = \"$versionRef\" }"
        } else {
            "$tomlKey = { group = \"$group\", name = \"$name\" }"
        }
    }
}

/**
 * 已知的 alias → 工件映射表。
 * 维护这张表就能让消费方一键自动补全 TOML。
 * 如需扩展，在此添加新条目即可。
 */
object KnownArtifacts {

    fun all(): Map<String, ArtifactEntry> = buildMap {
        // ── Ktor ──
        ktor("ktor-client-core")
        ktor("ktor-client-cio")
        ktor("ktor-client-js")
        ktor("ktor-client-darwin")
        ktor("ktor-client-content-negotiation")
        ktor("ktor-client-logging")
        ktor("ktor-serialization-kotlinx-json")

        // ── Koin ──
        koin("koin-bom")
        koin("koin-core")
        koin("koin-annotations")
        koin("koin-compose")
        koin("koin-compose-viewmodel")
        koin("koin-compose-viewmodel-navigation")
        koin("koin-ksp-compiler")

        // ── Kotlin / KotlinX ──
        put("org-jetbrains-kotlin-kotlin-test", ArtifactEntry("org.jetbrains.kotlin", "kotlin-test", "kotlin"))
        put("org-jetbrains-kotlinx-kotlinx-coroutines-swing", ArtifactEntry("org.jetbrains.kotlinx", "kotlinx-coroutines-swing", "kotlinx-coroutines"))
        put("org-jetbrains-kotlinx-kotlinx-datetime", ArtifactEntry("org.jetbrains.kotlinx", "kotlinx-datetime", "kotlinx-datetime"))
        put("org-jetbrains-kotlinx-kotlinx-serialization-json-json", ArtifactEntry("org.jetbrains.kotlinx", "kotlinx-serialization-json", "kotlinx-serialization"))

        // ── Compose / Lifecycle ──
        put("org-jetbrains-androidx-lifecycle-lifecycle-viewmodel-compose", ArtifactEntry("org.jetbrains.androidx.lifecycle", "lifecycle-viewmodel-compose", "androidx-lifecycle"))
        put("org-jetbrains-androidx-lifecycle-lifecycle-runtime-compose", ArtifactEntry("org.jetbrains.androidx.lifecycle", "lifecycle-runtime-compose", "androidx-lifecycle"))

        // ── KSP ──
        put("com-google-devtools-ksp-symbol-processing-api", ArtifactEntry("com.google.devtools.ksp", "symbol-processing-api", "ksp"))

        // ── Ktorfit ──
        put("de-jensklingenberg-ktorfit-ktorfit-lib", ArtifactEntry("de.jensklingenberg.ktorfit", "ktorfit-lib", "ktorfit"))

        // ── Spring Boot ──
        spring("spring-boot-starter-web")
        spring("spring-boot-autoconfigure")
        spring("spring-boot-configuration-processor")
        spring("spring-boot-dependencies")
        spring("spring-boot-starter-test", name = "boot-spring-boot-starter-test")

        // ── Test ──
        put("junit-junit-junit-jupiter-api", ArtifactEntry("org.junit.jupiter", "junit-jupiter-api", "junit"))
        put("junit-junit-junit-jupiter-engine", ArtifactEntry("org.junit.jupiter", "junit-jupiter-engine", "junit"))
        put("com-h2database-h2", ArtifactEntry("com.h2database", "h2", "h2"))

        // ── Logback ──
        put("ch-qos-logback-logback-classic-classic", ArtifactEntry("ch.qos.logback", "logback-classic", "logback"))

        // ── addzero ──
        put("site-addzero-tool-json", ArtifactEntry("site.addzero", "tool-json", "addzero-tool"))
    }

    private fun MutableMap<String, ArtifactEntry>.ktor(artifact: String) {
        val alias = "io-ktor-$artifact".replace(".", "-")
        put(alias, ArtifactEntry("io.ktor", artifact, "ktor"))
    }

    private fun MutableMap<String, ArtifactEntry>.koin(artifact: String) {
        val alias = "io-insert-koin-$artifact".replace(".", "-")
        put(alias, ArtifactEntry("io.insert-koin", artifact, "koin"))
    }

    private fun MutableMap<String, ArtifactEntry>.spring(artifact: String, name: String? = null) {
        val aliasName = name ?: artifact
        val alias = "org-springframework-boot-$aliasName".replace(".", "-")
        put(alias, ArtifactEntry("org.springframework.boot", artifact, "springBoot"))
    }
}
