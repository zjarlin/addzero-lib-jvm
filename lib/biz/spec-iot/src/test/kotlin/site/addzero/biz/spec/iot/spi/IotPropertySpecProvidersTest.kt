package site.addzero.biz.spec.iot.spi

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.test.spi.DuplicatePropertySpecProvider
import site.addzero.biz.spec.iot.test.spi.TestPropertySpecProvider
import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

class IotPropertySpecProvidersTest {

    @Test
    fun shouldLoadProviderFromServiceLoader() {
        val tempDir = Files.createTempDirectory("spec-iot-provider")
        writeService(tempDir, listOf(TestPropertySpecProvider::class.java.name))

        val original = Thread.currentThread().contextClassLoader
        val loader = URLClassLoader(arrayOf(tempDir.toUri().toURL()), original)
        try {
            Thread.currentThread().contextClassLoader = loader
            IotPropertySpecProviders.reload()

            val provider = IotPropertySpecProviders.load(IotThingRef.of("product", "demo-product"))
            assertEquals("test-provider", provider.name)
        } finally {
            Thread.currentThread().contextClassLoader = original
            IotPropertySpecProviders.reload()
            loader.close()
        }
    }

    @Test
    fun shouldFailWhenDuplicateProvidersMatchSameThing() {
        val tempDir = Files.createTempDirectory("spec-iot-provider-duplicate")
        writeService(
            tempDir,
            listOf(
                TestPropertySpecProvider::class.java.name,
                DuplicatePropertySpecProvider::class.java.name,
            ),
        )

        val original = Thread.currentThread().contextClassLoader
        val loader = URLClassLoader(arrayOf(tempDir.toUri().toURL()), original)
        try {
            Thread.currentThread().contextClassLoader = loader
            IotPropertySpecProviders.reload()

            assertThrows<IllegalStateException> {
                IotPropertySpecProviders.load(IotThingRef.of("product", "demo-product"))
            }
        } finally {
            Thread.currentThread().contextClassLoader = original
            IotPropertySpecProviders.reload()
            loader.close()
        }
    }

    @Test
    fun shouldFailWhenNoProviderMatches() {
        val tempDir = Files.createTempDirectory("spec-iot-provider-empty")
        writeService(tempDir, emptyList())

        val original = Thread.currentThread().contextClassLoader
        val loader = URLClassLoader(arrayOf(tempDir.toUri().toURL()), original)
        try {
            Thread.currentThread().contextClassLoader = loader
            IotPropertySpecProviders.reload()

            assertThrows<IllegalStateException> {
                IotPropertySpecProviders.load(IotThingRef.of("product", "demo-product"))
            }
        } finally {
            Thread.currentThread().contextClassLoader = original
            IotPropertySpecProviders.reload()
            loader.close()
        }
    }

    private fun writeService(root: Path, providers: List<String>) {
        val file = root.resolve("META-INF/services/site.addzero.biz.spec.iot.spi.IotPropertySpecProvider")
        Files.createDirectories(file.parent)
        Files.write(file, providers, StandardCharsets.UTF_8)
    }
}
