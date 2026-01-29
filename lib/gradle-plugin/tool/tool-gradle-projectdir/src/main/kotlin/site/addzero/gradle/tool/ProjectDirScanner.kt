package site.addzero.gradle.tool

import java.io.File
import java.util.Locale

object ProjectDirScanner {

    private val defaultBlacklist = setOf("build", ".gradle", ".idea", ".git", "out", "buildSrc", "build-logic")

    fun scanAndMapProjectPaths(rootDir: File, blacklist: Set<String> = defaultBlacklist): Map<String, String> {
        val pathMap = linkedMapOf<String, String>()
        if (!rootDir.isDirectory) {
            return pathMap
        }

        rootDir.walkTopDown()
            .onEnter { dir -> shouldEnter(dir, rootDir, blacklist) }
            .filter { it.isDirectory }
            .forEach { projectDir ->
                val buildFile = projectDir.resolve("build.gradle.kts")
                if (!buildFile.isFile || projectDir == rootDir) {
                    return@forEach
                }

                val moduleName = moduleNameFor(rootDir, projectDir) ?: return@forEach
                val buildContent = buildFile.readText()
                val isKmp = isKmpProject(buildContent)
                val isJvm = isJvmProject(buildContent)
                val (sourceDir, buildDir) = when {
                    isKmp -> "src/commonMain/kotlin" to "build/generated/ksp/metadata/commonMain/kotlin"
                    isJvm -> "src/main/kotlin" to "build/generated/ksp/main/kotlin"
                    else -> return@forEach
                }

                pathMap["${moduleName}SourceDir"] = projectDir.resolve(sourceDir).absolutePath
                pathMap["${moduleName}BuildDir"] = projectDir.resolve(buildDir).absolutePath
            }

        return pathMap
    }

    private fun shouldEnter(dir: File, rootDir: File, blacklist: Set<String>): Boolean {
        if (dir == rootDir) {
            return true
        }
        val name = dir.name
        if (name.startsWith(".")) {
            return false
        }
        return !blacklist.contains(name)
    }

    private fun moduleNameFor(rootDir: File, projectDir: File): String? {
        val relative = rootDir.toPath().relativize(projectDir.toPath()).toString()
        if (relative.isEmpty()) {
            return null
        }
        val clean = relative.replace(File.separatorChar, '-')
            .replace('/', '-')
            .replace('\\', '-')
        return toCamelCase(clean)
    }

    private fun isKmpProject(buildContent: String): Boolean {
        return buildContent.contains("kotlinMultiplatform", ignoreCase = true) ||
            buildContent.contains("org.jetbrains.kotlin.multiplatform", ignoreCase = true) ||
            buildContent.contains("kmp-", ignoreCase = true)
    }

    private fun isJvmProject(buildContent: String): Boolean {
        return buildContent.contains("org.jetbrains.kotlin.jvm", ignoreCase = true) ||
            buildContent.contains("kotlin(\"jvm\")", ignoreCase = true) ||
            buildContent.contains("java-library", ignoreCase = true)
    }

    private fun toCamelCase(value: String): String {
        val parts = value.split('-', ':')
        return parts.mapIndexed { index, part ->
            if (part.isEmpty()) {
                part
            } else if (index == 0) {
                part.substring(0, 1).lowercase(Locale.ROOT) + part.substring(1)
            } else {
                part.substring(0, 1).uppercase(Locale.ROOT) + part.substring(1)
            }
        }.joinToString("")
    }
}
