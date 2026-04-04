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
    fun `render api aggregator contains generated apis only`() {
        val code = renderApiAggregatorCode(
            packageName = "demo.generated.api",
            aggregatorObjectName = "Apis",
            aggregatorStyle = ApiAggregatorStyle.KOIN,
            generatedApis = listOf(
                GeneratedApiDescriptor("UserApi", "userApi"),
                GeneratedApiDescriptor("SystemRoutesApi", "systemRoutesApi"),
            ),
        )

        assertContains(code, "package demo.generated.api")
        assertContains(code, "import org.koin.mp.KoinPlatform")
        assertContains(code, "object Apis")
        assertContains(code, "private fun ktorfit(): Ktorfit = KoinPlatform.getKoin().get()")
        assertContains(code, "get() = ktorfit().createUserApi()")
        assertContains(code, "get() = ktorfit().createSystemRoutesApi()")
    }

    @Test
    fun `rendered api aggregator can be written into configured output dir`() {
        val tempDir = Files.createTempDirectory("controller2api-provider-test")
        val outputFile = tempDir.resolve("Apis.kt")
        outputFile.writeText(
            renderApiAggregatorCode(
                packageName = "demo.generated.api",
                aggregatorObjectName = "Apis",
                aggregatorStyle = ApiAggregatorStyle.KOIN,
                generatedApis = listOf(GeneratedApiDescriptor("UserApi", "userApi")),
            )
        )

        val writtenCode = outputFile.readText()
        assertContains(writtenCode, "object Apis")
        assertContains(writtenCode, "private fun ktorfit(): Ktorfit = KoinPlatform.getKoin().get()")
        assertContains(writtenCode, "get() = ktorfit().createUserApi()")
    }

    @Test
    fun `render singleton api aggregator keeps configure contract`() {
        val code = renderApiAggregatorCode(
            packageName = "demo.generated.api",
            aggregatorObjectName = "Apis",
            aggregatorStyle = ApiAggregatorStyle.SINGLETON,
            generatedApis = listOf(GeneratedApiDescriptor("UserApi", "userApi")),
        )

        assertContains(code, "fun configure(ktorfit: Ktorfit)")
        assertContains(code, "private var currentKtorfit: Ktorfit? = null")
        assertContains(code, "get() = ktorfit().createUserApi()")
        assertContains(code, "Apis 尚未配置 Ktorfit")
    }

    @Test
    fun `parse api aggregator style defaults to koin`() {
        assertEquals(ApiAggregatorStyle.KOIN, parseApiAggregatorStyle(null))
        assertEquals(ApiAggregatorStyle.KOIN, parseApiAggregatorStyle(""))
        assertEquals(ApiAggregatorStyle.KOIN, parseApiAggregatorStyle("koin"))
        assertEquals(ApiAggregatorStyle.SINGLETON, parseApiAggregatorStyle("singleton"))
    }
}
