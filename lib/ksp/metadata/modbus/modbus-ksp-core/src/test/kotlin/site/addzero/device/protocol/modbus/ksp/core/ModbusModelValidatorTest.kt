package site.addzero.device.protocol.modbus.ksp.core

import kotlin.test.Test
import kotlin.test.assertTrue

class ModbusModelValidatorTest {
    @Test
    fun validatorRejectsSingleRegisterOverflow() {
        val service =
            sampleService(
                operations =
                    listOf(
                        ModbusOperationModel(
                            methodName = "writeThreshold",
                            operationId = "write-threshold",
                            functionCodeName = "WRITE_SINGLE_REGISTER",
                            address = 64,
                            quantity = 1,
                            requestClassName = "SampleWriteThresholdRequest",
                            requestQualifiedName = "site.addzero.generated.SampleWriteThresholdRequest",
                            parameters =
                                listOf(
                                    ModbusParameterModel(
                                        name = "threshold",
                                        qualifiedType = "kotlin.Int",
                                        valueKind = ModbusValueKind.INT,
                                        order = 0,
                                        codecName = "U32_BE",
                                        registerOffset = 0,
                                        bitOffset = 0,
                                        registerWidth = 2,
                                        doc = "阈值。",
                                    ),
                                ),
                            returnType =
                                ModbusReturnTypeModel(
                                    qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                                    simpleName = "ModbusCommandResult",
                                    kind = ModbusReturnKind.COMMAND_RESULT,
                                ),
                            doc = ModbusDocModel(summary = "写入阈值。"),
                        ),
                    ),
            )

        val errors = ModbusModelValidator.validate(listOf(service))

        assertTrue(errors.any { error -> error.contains("WRITE_SINGLE_REGISTER") })
    }

    @Test
    fun validatorRejectsFakeCoilCodecInsideRegisterWrite() {
        val service =
            sampleService(
                operations =
                    listOf(
                        ModbusOperationModel(
                            methodName = "gpioWrite",
                            operationId = "gpio-write",
                            functionCodeName = "WRITE_MULTIPLE_REGISTERS",
                            address = 1024,
                            quantity = 2,
                            requestClassName = "SampleGpioWriteRequest",
                            requestQualifiedName = "site.addzero.generated.SampleGpioWriteRequest",
                            parameters =
                                listOf(
                                    ModbusParameterModel(
                                        name = "pin",
                                        qualifiedType = "kotlin.Int",
                                        valueKind = ModbusValueKind.INT,
                                        order = 0,
                                        codecName = "U16",
                                        registerOffset = 0,
                                        bitOffset = 0,
                                        registerWidth = 1,
                                        doc = "引脚号。",
                                    ),
                                    ModbusParameterModel(
                                        name = "high",
                                        qualifiedType = "kotlin.Boolean",
                                        valueKind = ModbusValueKind.BOOLEAN,
                                        order = 1,
                                        codecName = "BOOL_COIL",
                                        registerOffset = 1,
                                        bitOffset = 0,
                                        registerWidth = 1,
                                        doc = "是否高电平。",
                                    ),
                                ),
                            returnType =
                                ModbusReturnTypeModel(
                                    qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                                    simpleName = "ModbusCommandResult",
                                    kind = ModbusReturnKind.COMMAND_RESULT,
                                ),
                            doc = ModbusDocModel(summary = "设置 GPIO 电平。"),
                        ),
                    ),
            )

        val errors = ModbusModelValidator.validate(listOf(service))

        assertTrue(errors.any { error -> error.contains("BOOL_COIL") })
    }

    @Test
    fun validatorRejectsFlashWorkflowWhenCommitOperationMissing() {
        val service =
            sampleFlashService().copy(
                operations = listOf(sampleResetDeviceOperation(), sampleFirmwareStartOperation(), sampleFirmwareChunkOperation()),
                workflows = listOf(sampleFlashWorkflowModel()),
            )

        val errors = ModbusModelValidator.validate(listOf(service))

        assertTrue(errors.any { error -> error.contains("缺少必需的低层方法：firmwareCommit") })
    }

    @Test
    fun validatorAcceptsFlashWorkflowWithoutCrcField() {
        val service = sampleFlashWorkflowServiceWithoutCrc()

        val errors = ModbusModelValidator.validate(listOf(service))

        assertTrue(errors.isEmpty(), errors.joinToString("\n"))
    }
}
