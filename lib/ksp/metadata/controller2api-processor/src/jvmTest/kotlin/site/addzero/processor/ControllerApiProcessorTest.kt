package site.addzero.processor

import java.nio.file.Files
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ControllerApiProcessorTest {
    @Test
    fun `generated api descriptor keeps controller naming contract`() {
        val descriptor = toGeneratedApiDescriptor(
            ControllerMetadata(
                originalClassName = "UserController",
                packageName = "sample",
                basePath = "/user",
                methods = emptyList(),
                sourceDescription = "test",
            )
        )

        assertEquals("UserApi", descriptor.apiClassName)
        assertEquals("userApi", descriptor.propertyName)
    }

    @Test
    fun `generated api descriptor keeps top level api class name`() {
        val descriptor = toGeneratedApiDescriptor(
            ControllerMetadata(
                originalClassName = "UserRoutes",
                packageName = "sample",
                basePath = "/user",
                methods = emptyList(),
                generatedApiClassName = "UserRoutesApi",
                sourceDescription = "test",
            )
        )

        assertEquals("UserRoutesApi", descriptor.apiClassName)
        assertEquals("userRoutesApi", descriptor.propertyName)
    }

    @Test
    fun `render api provider contains generated apis only`() {
        val code = renderApiProviderCode(
            packageName = "demo.generated.api",
            generatedApis = listOf(
                GeneratedApiDescriptor("UserApi", "userApi"),
                GeneratedApiDescriptor("SystemRoutesApi", "systemRoutesApi"),
            ),
        )

        assertContains(code, "package demo.generated.api")
        assertContains(code, "fun configure(ktorfit: Ktorfit)")
        assertContains(code, "get() = requireKtorfit().create<UserApi>()")
        assertContains(code, "get() = requireKtorfit().create<SystemRoutesApi>()")
    }

    @Test
    fun `rendered api provider can be written into configured output dir`() {
        val tempDir = Files.createTempDirectory("controller2api-provider-test")
        val outputFile = tempDir.resolve("ApiProvider.kt")
        outputFile.writeText(
            renderApiProviderCode(
                packageName = "demo.generated.api",
                generatedApis = listOf(GeneratedApiDescriptor("UserApi", "userApi")),
            )
        )

        val writtenCode = outputFile.readText()
        assertContains(writtenCode, "object ApiProvider")
        assertContains(writtenCode, "private var currentKtorfit: Ktorfit? = null")
        assertContains(writtenCode, "get() = requireKtorfit().create<UserApi>()")
    }
}
