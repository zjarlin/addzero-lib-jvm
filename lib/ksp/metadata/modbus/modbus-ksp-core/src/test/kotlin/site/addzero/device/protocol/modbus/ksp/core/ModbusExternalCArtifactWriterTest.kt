package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModbusExternalCArtifactWriterTest {
    @Test
    fun missingExternalProjectDirWarnsAndSkipsWriterCreation() {
        val missingDir =
            File(System.getProperty("java.io.tmpdir"))
                .resolve("modbus-ksp-missing-external-project")
                .resolve("not-found-${System.nanoTime()}")
                .absoluteFile
        val logger = CollectingKspLogger()
        val environment =
            SymbolProcessorEnvironment(
                options = mapOf("addzero.modbus.c.output.projectDir" to missingDir.absolutePath),
                kotlinVersion = KotlinVersion.CURRENT,
                codeGenerator = noOpCodeGenerator(),
                logger = logger,
            )

        val writer = ModbusExternalCArtifactWriter.from(environment)

        assertNull(writer)
        assertTrue(
            logger.warnings.any { warning ->
                warning.contains("路径不存在") && warning.contains(missingDir.absolutePath)
            },
        )
        assertEquals(emptyList(), logger.errors)
    }
}

private class CollectingKspLogger : KSPLogger {
    val warnings = mutableListOf<String>()
    val errors = mutableListOf<String>()

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
    ) {
        warnings += message
    }

    override fun error(
        message: String,
        symbol: com.google.devtools.ksp.symbol.KSNode?,
    ) {
        errors += message
    }

    override fun exception(
        e: Throwable,
    ) = Unit
}

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
