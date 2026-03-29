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
                            capabilityKey = "control-threshold",
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
                                    )
                                ),
                            returnType =
                                ModbusReturnTypeModel(
                                    qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                                    simpleName = "ModbusCommandResult",
                                    kind = ModbusReturnKind.COMMAND_RESULT,
                                ),
                            doc = ModbusDocModel(summary = "写入阈值。"),
                        )
                    ),
            )

        val errors = ModbusModelValidator.validate(listOf(service))

        assertTrue(errors.any { error -> error.contains("WRITE_SINGLE_REGISTER") })
    }
}
