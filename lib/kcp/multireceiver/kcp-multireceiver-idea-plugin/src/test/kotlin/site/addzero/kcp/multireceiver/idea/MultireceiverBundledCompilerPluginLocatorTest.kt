package site.addzero.kcp.multireceiver.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class MultireceiverBundledCompilerPluginLocatorTest {

    @Test
    fun detects_multireceiver_compiler_plugin_jar_from_service_file() {
        val jarPath = createJarWithRegistrar(
            registrarClassName = MultireceiverIdeaConstants.compilerPluginRegistrarClassName,
        )

        assertTrue(MultireceiverBundledCompilerPluginLocator.isMultireceiverCompilerPluginJar(jarPath))
    }

    @Test
    fun ignores_unrelated_compiler_plugin_jar() {
        val jarPath = createJarWithRegistrar(
            registrarClassName = "example.OtherCompilerPluginRegistrar",
        )

        assertFalse(MultireceiverBundledCompilerPluginLocator.isMultireceiverCompilerPluginJar(jarPath))
    }

    @Test
    fun finds_bundled_compiler_plugin_jar_under_plugin_lib_directory() {
        val pluginHome = Files.createTempDirectory("multireceiver-plugin-home")
        val libDirectory = Files.createDirectories(pluginHome.resolve("lib"))
        val expected = libDirectory.resolve("kcp-multireceiver-plugin-2026.03.13.jar")
        Files.createFile(expected)
        Files.createFile(libDirectory.resolve("kcp-multireceiver-annotations-jvm-2026.03.13.jar"))

        assertEquals(expected, MultireceiverBundledCompilerPluginLocator.findBundledCompilerPluginJar(pluginHome))
    }

    private fun createJarWithRegistrar(registrarClassName: String): Path {
        val jarPath = Files.createTempFile("multireceiver-locator", ".jar")
        JarOutputStream(Files.newOutputStream(jarPath)).use { jarOutput ->
            jarOutput.putNextEntry(
                JarEntry(MultireceiverIdeaConstants.compilerPluginRegistrarServiceFile),
            )
            jarOutput.write(registrarClassName.toByteArray(StandardCharsets.UTF_8))
            jarOutput.closeEntry()
        }
        return jarPath
    }
}
