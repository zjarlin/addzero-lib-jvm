package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.io.File

data class ModbusAddressLockKey(
    val serviceId: String,
    val operationId: String,
    val functionCodeName: String,
    val addressSpace: ModbusAddressSpace,
)

data class ModbusAddressLockEntry(
    val key: ModbusAddressLockKey,
    val address: Int,
    val quantity: Int,
    val registerSpan: Int,
)

/**
 * 可提交到版本库的 Modbus 地址锁定文件。
 *
 * 目的：
 * - 首次生成时自动分配 address
 * - 后续优先复用已锁定的 address，避免源码重构导致协议漂移
 * - 让上下位机在不同电脑上仍能得到一致地址表
 */
class ModbusAddressLockFile internal constructor(
    private val file: File,
) {
    fun load(
        transport: ModbusTransportKind,
        logger: KSPLogger,
    ): Map<ModbusAddressLockKey, ModbusAddressLockEntry> {
        if (!file.exists()) {
            return emptyMap()
        }
        val schemaVersion = mutableMapOf<String, String>()
        val entries = linkedMapOf<ModbusAddressLockKey, ModbusAddressLockEntry>()
        file.readLines(Charsets.UTF_8).forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("#")) {
                return@forEachIndexed
            }
            val separatorIndex = line.indexOf('=')
            require(separatorIndex > 0) {
                "地址锁定文件 ${file.absolutePath} 第 ${index + 1} 行格式非法：$line"
            }
            val key = line.substring(0, separatorIndex).trim()
            val value = line.substring(separatorIndex + 1).trim()
            when {
                key.startsWith("meta.") -> schemaVersion[key.removePrefix("meta.")] = value
                key.startsWith("op.") -> {
                    val keyParts = key.removePrefix("op.").split('|')
                    require(keyParts.size == 4) {
                        "地址锁定文件 ${file.absolutePath} 第 ${index + 1} 行 key 非法：$key"
                    }
                    val valueParts = value.split('|')
                    require(valueParts.size == 3) {
                        "地址锁定文件 ${file.absolutePath} 第 ${index + 1} 行 value 非法：$value"
                    }
                    val addressKey =
                        ModbusAddressLockKey(
                            serviceId = keyParts[0],
                            operationId = keyParts[1],
                            functionCodeName = keyParts[2],
                            addressSpace = ModbusAddressSpace.valueOf(keyParts[3]),
                        )
                    entries[addressKey] =
                        ModbusAddressLockEntry(
                            key = addressKey,
                            address = valueParts[0].toInt(),
                            quantity = valueParts[1].toInt(),
                            registerSpan = valueParts[2].toInt(),
                        )
                }
                else -> {
                    logger.warn("忽略未知的地址锁定项：$key", null)
                }
            }
        }
        val lockedTransport = schemaVersion["transport"]
        require(lockedTransport == null || lockedTransport == transport.transportId) {
            "地址锁定文件 ${file.absolutePath} 绑定的是 transport=$lockedTransport，当前生成的是 ${transport.transportId}。"
        }
        return entries
    }

    fun write(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ) {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        val content =
            buildString {
                appendLine("# Modbus address lock file.")
                appendLine("# Commit this file to keep addresses stable across machines.")
                appendLine("# Do not hand edit unless you are migrating the protocol on purpose.")
                appendLine("meta.schemaVersion=1")
                appendLine("meta.protocol=modbus")
                appendLine("meta.transport=${transport.transportId}")
                services
                    .sortedWith(compareBy(ModbusServiceModel::serviceId, ModbusServiceModel::interfaceQualifiedName))
                    .flatMap { service ->
                        service.operations
                            .sortedWith(compareBy(ModbusOperationModel::operationId, ModbusOperationModel::methodName))
                            .map { operation ->
                                val key =
                                    listOf(
                                        service.serviceId,
                                        operation.operationId,
                                        operation.functionCodeName,
                                        operation.addressSpace.name,
                                    ).joinToString("|")
                                val value =
                                    listOf(
                                        operation.address.toString(),
                                        operation.quantity.toString(),
                                        operation.registerSpan.toString(),
                                    ).joinToString("|")
                                "op.$key=$value"
                            }
                    }.forEach(::appendLine)
            }
        file.writeText(content, Charsets.UTF_8)
    }

    companion object {
        private const val ADDRESS_LOCK_PATH_OPTION = "addzero.modbus.address.lock.path"

        fun from(environment: SymbolProcessorEnvironment): ModbusAddressLockFile? {
            val rawPath = environment.options[ADDRESS_LOCK_PATH_OPTION].orEmpty().trim()
            if (rawPath.isEmpty()) {
                return null
            }
            val file = File(rawPath).absoluteFile
            environment.logger.logging("Modbus address lock file: ${file.absolutePath}")
            return ModbusAddressLockFile(file)
        }
    }
}

object ModbusAddressPlanner {
    fun resolveServices(
        services: List<ModbusServiceModel>,
        lockFile: ModbusAddressLockFile?,
        logger: KSPLogger,
    ): List<ModbusServiceModel> {
        if (lockFile == null) {
            return services.map { service ->
                service.copy(
                    operations = ModbusContractDefaultsResolver.resolveAddresses(service.serviceId, service.operations),
                )
            }
        }

        val lockedEntries = lockFile.load(services.first().transport, logger)
        val assignments = linkedMapOf<ModbusAddressLockKey, Int>()
        val occupied = linkedMapOf<ModbusAddressSpace, MutableList<IntRange>>()

        services.forEach { service ->
            service.operations.forEach { operation ->
                val key = operation.lockKey(service.serviceId)
                val address =
                    when {
                        operation.address >= 0 -> operation.address
                        else -> lockedEntries[key]?.address
                    }
                if (address != null) {
                    assignments[key] = address
                    occupied.getOrPut(operation.addressSpace, ::mutableListOf) += address until (address + operation.registerSpan)
                }
            }
        }

        services
            .sortedWith(compareBy(ModbusServiceModel::serviceId, ModbusServiceModel::interfaceQualifiedName))
            .flatMap { service ->
                service.operations
                    .sortedWith(compareBy(ModbusOperationModel::operationId, ModbusOperationModel::methodName))
                    .map { operation -> service to operation }
            }.forEach { (service, operation) ->
                val key = operation.lockKey(service.serviceId)
                if (assignments.containsKey(key)) {
                    return@forEach
                }
                val address = firstFitAddress(operation, occupied.getOrPut(operation.addressSpace, ::mutableListOf))
                assignments[key] = address
                occupied.getValue(operation.addressSpace) += address until (address + operation.registerSpan)
            }

        return services.map { service ->
            service.copy(
                operations =
                    service.operations.map { operation ->
                        operation.copy(address = assignments.getValue(operation.lockKey(service.serviceId)))
                    },
            )
        }
    }

    fun persist(
        services: List<ModbusServiceModel>,
        lockFile: ModbusAddressLockFile?,
    ) {
        if (lockFile == null || services.isEmpty()) {
            return
        }
        lockFile.write(services.first().transport, services)
    }

    private fun firstFitAddress(
        operation: ModbusOperationModel,
        occupiedRanges: List<IntRange>,
    ): Int {
        val span = operation.registerSpan
        var candidate =
            occupiedRanges
                .maxOfOrNull { range -> range.last + 1 }
                ?.coerceAtLeast(ModbusContractDefaultsResolver.defaultBaseAddress(operation.addressSpace))
                ?: ModbusContractDefaultsResolver.defaultBaseAddress(operation.addressSpace)
        while ((candidate + span - 1) <= MAX_MODBUS_ADDRESS) {
            val overlap =
                occupiedRanges.any { range ->
                    val endExclusive = candidate + span
                    candidate < range.last + 1 && endExclusive > range.first
                }
            if (!overlap) {
                return candidate
            }
            candidate += 1
        }
        error("地址空间 ${operation.addressSpace} 已耗尽，无法为 ${operation.methodName} 分配跨度 $span 的地址。")
    }

    private fun ModbusOperationModel.lockKey(serviceId: String): ModbusAddressLockKey =
        ModbusAddressLockKey(
            serviceId = serviceId,
            operationId = operationId,
            functionCodeName = functionCodeName,
            addressSpace = addressSpace,
        )

    private const val MAX_MODBUS_ADDRESS = 0xFFFF
}
