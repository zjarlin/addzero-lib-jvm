package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.io.File
import java.util.ServiceLoader

/**
 * 协议生成后的工程集成 SPI。
 *
 * 目的：
 * - 代码生成仍由 KSP 主链负责
 * - `.uvprojx` / 其他 IDE 工程文件同步交给独立 SPI，避免把协议逻辑和 IDE 私有格式揉在一起
 */
interface ModbusProjectSyncTool {
    val toolId: String

    fun isEnabled(context: ModbusProjectSyncContext): Boolean

    fun sync(context: ModbusProjectSyncContext)
}

data class ModbusProjectSyncContext(
    val environment: SymbolProcessorEnvironment,
    val transport: ModbusTransportKind,
    val externalSourceFiles: List<File>,
)

object ModbusProjectSyncRunner {
    fun syncIfNeeded(
        environment: SymbolProcessorEnvironment,
        transport: ModbusTransportKind,
        externalSourceFiles: List<File>,
    ) {
        if (externalSourceFiles.isEmpty()) {
            return
        }
        val context =
            ModbusProjectSyncContext(
                environment = environment,
                transport = transport,
                externalSourceFiles = externalSourceFiles.distinctBy { file -> file.absolutePath },
            )
        tools().forEach { tool ->
            if (tool.isEnabled(context)) {
                environment.logger.logging("Running Modbus project sync tool: ${tool.toolId}")
                tool.sync(context)
            }
        }
    }

    private fun tools(): List<ModbusProjectSyncTool> =
        ServiceLoader
            .load(ModbusProjectSyncTool::class.java, ModbusProjectSyncRunner::class.java.classLoader)
            .toList()
}

internal fun KSPLogger.errorWithFileContext(
    message: String,
    file: File,
) {
    error("$message: ${file.absolutePath}")
}
