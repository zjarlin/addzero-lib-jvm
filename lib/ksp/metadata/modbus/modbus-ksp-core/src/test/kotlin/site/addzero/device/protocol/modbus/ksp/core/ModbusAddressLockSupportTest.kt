package site.addzero.device.protocol.modbus.ksp.core

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModbusAddressLockSupportTest {
    @Test
    fun plannerReusesLockedAddressAndAppendsNewOperationAfterOccupiedRange() {
        val tempDir = createTempDirectory("modbus-address-lock-test").toFile()
        try {
            val lockFile =
                tempDir.resolve("device.rtu.addresses.lock").apply {
                    writeText(
                        """
                        # Modbus address lock file.
                        meta.schemaVersion=1
                        meta.protocol=modbus
                        meta.transport=rtu
                        op.device|get-device-info|READ_COILS|COIL_READ=0|24|24
                        """.trimIndent(),
                    )
                }
            val resolved =
                ModbusAddressPlanner.resolveServices(
                    services =
                        listOf(
                            semanticCoilService().copy(
                                operations =
                                    listOf(
                                        semanticCoilService().operations.single().copy(address = -1),
                                        ModbusOperationModel(
                                            methodName = "getAlarmInfo",
                                            operationId = "get-alarm-info",
                                            functionCodeName = "READ_COILS",
                                            address = -1,
                                            quantity = 8,
                                            requestClassName = "DeviceServiceRtuGetAlarmInfoRequest",
                                            requestQualifiedName = "site.addzero.generated.DeviceServiceRtuGetAlarmInfoRequest",
                                            parameters = emptyList(),
                                            returnType =
                                                ModbusReturnTypeModel(
                                                    qualifiedName = "kotlin.Boolean",
                                                    simpleName = "Boolean",
                                                    kind = ModbusReturnKind.BOOLEAN,
                                                    valueKind = ModbusValueKind.BOOLEAN,
                                                    codecName = "BOOL_COIL",
                                                    length = 1,
                                                    registerWidth = 1,
                                                ),
                                            doc = ModbusDocModel(summary = "读取报警汇总状态。"),
                                        ),
                                    ),
                            ),
                        ),
                    lockFile = ModbusAddressLockFile(lockFile),
                    logger = noOpLogger(),
                )

            val operations = resolved.single().operations.associateBy(ModbusOperationModel::operationId)
            assertEquals(0, operations.getValue("get-device-info").address)
            assertEquals(24, operations.getValue("get-alarm-info").address)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun persistWritesSortedAddressLockFile() {
        val tempDir = createTempDirectory("modbus-address-lock-write").toFile()
        try {
            val lockPath = tempDir.resolve("device.rtu.addresses.lock")
            val services =
                listOf(
                    semanticCoilService(),
                    sampleService(),
                )

            ModbusAddressPlanner.persist(services, ModbusAddressLockFile(lockPath))

            val text = lockPath.readText()
            assertTrue(text.contains("meta.transport=rtu"))
            assertTrue(text.contains("op.device|get-device-info|READ_COILS|COIL_READ=0|24|24"))
            assertTrue(text.contains("op.self-dev-board|read-info|READ_INPUT_REGISTERS|INPUT_REGISTER=0|4|4"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
    private fun noOpLogger(): com.google.devtools.ksp.processing.KSPLogger =
        object : com.google.devtools.ksp.processing.KSPLogger {
            override fun logging(message: String, symbol: com.google.devtools.ksp.symbol.KSNode?) = Unit

            override fun info(message: String, symbol: com.google.devtools.ksp.symbol.KSNode?) = Unit

            override fun warn(message: String, symbol: com.google.devtools.ksp.symbol.KSNode?) = Unit

            override fun error(message: String, symbol: com.google.devtools.ksp.symbol.KSNode?) = Unit

            override fun exception(e: Throwable) = Unit
        }
}
