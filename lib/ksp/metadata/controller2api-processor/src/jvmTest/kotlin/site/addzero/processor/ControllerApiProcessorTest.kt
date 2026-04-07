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
        assertContains(code, "object Apis")
        assertContains(code, "fun userApi(ktorfit: Ktorfit): UserApi")
        assertContains(code, "return ktorfit.createUserApi()")
        assertContains(code, "fun systemRoutesApi(ktorfit: Ktorfit): SystemRoutesApi")
        assertContains(code, "return ktorfit.createSystemRoutesApi()")
    }

    @Test
    fun `render koin api aggregator module binds generated apis`() {
        val code = renderApiAggregatorModuleCode(
            packageName = "demo.generated.api",
            aggregatorObjectName = "Apis",
            generatedApis = listOf(
                GeneratedApiDescriptor("UserApi", "userApi"),
                GeneratedApiDescriptor("SystemRoutesApi", "systemRoutesApi"),
            ),
        )

        assertContains(code, "package demo.generated.api")
        assertContains(code, "import org.koin.core.annotation.Configuration")
        assertContains(code, "import org.koin.core.annotation.Module")
        assertContains(code, "import org.koin.core.annotation.Single")
        assertContains(code, "@Configuration")
        assertContains(code, "class ApisModule")
        assertContains(code, "fun userApi(ktorfit: Ktorfit): UserApi")
        assertContains(code, "return Apis.userApi(ktorfit)")
        assertContains(code, "fun systemRoutesApi(ktorfit: Ktorfit): SystemRoutesApi")
        assertContains(code, "return Apis.systemRoutesApi(ktorfit)")
    }

    @Test
    fun `rendered api aggregator files can be written into configured output dir`() {
        val tempDir = Files.createTempDirectory("controller2api-provider-test")
        val generatedFiles = buildApiAggregatorFiles(
            packageName = "demo.generated.api",
            aggregatorObjectName = "Apis",
            aggregatorStyle = ApiAggregatorStyle.KOIN,
            generatedApis = listOf(GeneratedApiDescriptor("UserApi", "userApi")),
        )

        generatedFiles.forEach { generatedFile ->
            tempDir.resolve(generatedFile.fileName).writeText(generatedFile.content)
        }

        val aggregatorCode = tempDir.resolve("Apis.kt").readText()
        val moduleCode = tempDir.resolve("ApisModule.kt").readText()

        assertContains(aggregatorCode, "object Apis")
        assertContains(aggregatorCode, "fun userApi(ktorfit: Ktorfit): UserApi")
        assertContains(aggregatorCode, "return ktorfit.createUserApi()")
        assertContains(moduleCode, "class ApisModule")
        assertContains(moduleCode, "fun userApi(ktorfit: Ktorfit): UserApi")
        assertContains(moduleCode, "return Apis.userApi(ktorfit)")
    }

    @Test
    fun `singleton api aggregator does not emit module file`() {
        val generatedFiles = buildApiAggregatorFiles(
            packageName = "demo.generated.api",
            aggregatorObjectName = "Apis",
            aggregatorStyle = ApiAggregatorStyle.SINGLETON,
            generatedApis = listOf(GeneratedApiDescriptor("UserApi", "userApi")),
        )

        assertEquals(1, generatedFiles.size)
        assertEquals("Apis.kt", generatedFiles.single().fileName)
    }

    @Test
    fun `rendered api aggregator keeps singleton contract`() {
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
    fun `rendered api aggregator file can be written for singleton style`() {
        val tempDir = Files.createTempDirectory("controller2api-singleton-provider-test")
        val outputFile = tempDir.resolve("Apis.kt")
        outputFile.writeText(
            renderApiAggregatorCode(
                packageName = "demo.generated.api",
                aggregatorObjectName = "Apis",
                aggregatorStyle = ApiAggregatorStyle.SINGLETON,
                generatedApis = listOf(GeneratedApiDescriptor("UserApi", "userApi")),
            )
        )

        val writtenCode = outputFile.readText()
        assertContains(writtenCode, "fun configure(ktorfit: Ktorfit)")
    }

    @Test
    fun `parse api aggregator style defaults to koin`() {
        assertEquals(ApiAggregatorStyle.KOIN, parseApiAggregatorStyle(null))
        assertEquals(ApiAggregatorStyle.KOIN, parseApiAggregatorStyle(""))
        assertEquals(ApiAggregatorStyle.KOIN, parseApiAggregatorStyle("koin"))
        assertEquals(ApiAggregatorStyle.SINGLETON, parseApiAggregatorStyle("singleton"))
    }
}
