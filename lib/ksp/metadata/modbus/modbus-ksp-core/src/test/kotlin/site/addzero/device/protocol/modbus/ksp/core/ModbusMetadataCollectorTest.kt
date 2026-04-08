package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModbusMetadataCollectorTest {
    @Test
    fun serviceLoaderDiscoversBuiltInMetadataProviders() {
        val providerIds = ModbusMetadataCollector.availableProviderIds()

        assertTrue("interfaces" in providerIds)
        assertTrue("database" in providerIds)
    }

    @Test
    fun configuredInterfacesProviderSkipsDatabaseProvider() {
        val services =
            ModbusMetadataCollector.collect(
                environment =
                    testEnvironment(
                        mapOf(
                            ModbusKspOptions.METADATA_PROVIDERS_OPTION to "interfaces",
                            ModbusKspOptions.DATABASE_DRIVER_CLASS_OPTION to "missing.Driver",
                            ModbusKspOptions.DATABASE_JDBC_URL_OPTION to "jdbc:invalid",
                            ModbusKspOptions.DATABASE_QUERY_OPTION to "select payload from modbus_metadata",
                        ),
                    ),
                resolver = emptyResolver(),
                transport = ModbusTransportKind.RTU,
                contractPackages = listOf("site.addzero.device.contract"),
            )

        assertTrue(services.isEmpty())
    }

    @Test
    fun unknownProviderFailsFast() {
        val exception =
            assertFailsWith<IllegalStateException> {
                ModbusMetadataCollector.collect(
                    environment =
                        testEnvironment(
                            mapOf(
                                ModbusKspOptions.METADATA_PROVIDERS_OPTION to "interfaces,unknown",
                            ),
                        ),
                    resolver = emptyResolver(),
                    transport = ModbusTransportKind.RTU,
                    contractPackages = emptyList(),
                )
            }

        assertTrue(exception.message.orEmpty().contains("未知的 Modbus metadata provider"))
    }
}

private fun testEnvironment(options: Map<String, String>): SymbolProcessorEnvironment =
    SymbolProcessorEnvironment(
        options = options,
        kotlinVersion = KotlinVersion.CURRENT,
        codeGenerator = noOpCodeGenerator(),
        logger = noOpLogger(),
    )

private fun noOpCodeGenerator(): CodeGenerator =
    object : CodeGenerator {
        override fun createNewFile(
            dependencies: Dependencies,
            packageName: String,
            fileName: String,
            extensionName: String,
        ) = ByteArrayOutputStream()

        override fun createNewFileByPath(
            dependencies: Dependencies,
            path: String,
            extensionName: String,
        ) = ByteArrayOutputStream()

        override fun associate(
            sources: List<com.google.devtools.ksp.symbol.KSFile>,
            packageName: String,
            fileName: String,
            extensionName: String,
        ) = Unit

        override fun associateByPath(
            sources: List<com.google.devtools.ksp.symbol.KSFile>,
            path: String,
            extensionName: String,
        ) = Unit

        override fun associateWithClasses(
            classes: List<com.google.devtools.ksp.symbol.KSClassDeclaration>,
            packageName: String,
            fileName: String,
            extensionName: String,
        ) = Unit

        override val generatedFile: Collection<File>
            get() = emptyList()
    }

private fun emptyResolver(): Resolver =
    Proxy
        .newProxyInstance(
            Resolver::class.java.classLoader,
            arrayOf(Resolver::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getDeclarationsFromPackage" -> emptySequence<Any>()
                else -> error("Unexpected resolver call in test: ${method.name}")
            }
        } as Resolver

private fun noOpLogger(): KSPLogger =
    object : KSPLogger {
        override fun logging(
            message: String,
            symbol: com.google.devtools.ksp.symbol.KSNode?,
        ) = Unit

        override fun info(
            message: String,
            symbol: com.google.devtools.ksp.symbol.KSNode?,
        ) = Unit

        override fun warn(
            message: String,
            symbol: com.google.devtools.ksp.symbol.KSNode?,
        ) = Unit

        override fun error(
            message: String,
            symbol: com.google.devtools.ksp.symbol.KSNode?,
        ) = Unit

        override fun exception(
            e: Throwable,
        ) = Unit
    }
