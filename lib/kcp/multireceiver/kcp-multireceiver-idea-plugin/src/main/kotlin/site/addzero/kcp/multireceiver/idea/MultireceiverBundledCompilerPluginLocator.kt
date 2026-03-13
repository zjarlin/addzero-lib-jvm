package site.addzero.kcp.multireceiver.idea

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile

internal object MultireceiverBundledCompilerPluginLocator {

    fun isMultireceiverCompilerPluginJar(path: Path): Boolean {
        if (!Files.isRegularFile(path) || !path.fileName.toString().endsWith(".jar")) {
            return false
        }
        val registrarContent = readRegistrarContent(path) ?: return false
        return registrarContent
            .lineSequence()
            .map { line -> line.trim() }
            .filter { line -> line.isNotEmpty() }
            .any { line ->
                line == MultireceiverIdeaConstants.compilerPluginRegistrarClassName
            }
    }

    fun findBundledCompilerPluginJar(pluginHome: Path): Path? {
        val libDirectory = pluginHome.resolve("lib")
        if (!Files.isDirectory(libDirectory)) {
            return null
        }
        return Files.list(libDirectory).use { entries ->
            entries
                .filter { path ->
                    val fileName = path.fileName.toString()
                    Files.isRegularFile(path) &&
                        fileName.startsWith(MultireceiverIdeaConstants.bundledCompilerPluginJarPrefix) &&
                        fileName.endsWith(".jar")
                }
                .sorted { left, right ->
                    left.fileName.toString().compareTo(right.fileName.toString())
                }
                .findFirst()
                .orElse(null)
        }
    }

    private fun readRegistrarContent(path: Path): String? {
        return runCatching {
            ZipFile(path.toFile()).use { zipFile ->
                val entry = zipFile.getEntry(MultireceiverIdeaConstants.compilerPluginRegistrarServiceFile)
                    ?: return@runCatching null
                zipFile.getInputStream(entry).use { input ->
                    input.readBytes().toString(StandardCharsets.UTF_8)
                }
            }
        }.getOrNull()
    }
}
