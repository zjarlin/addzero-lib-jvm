package site.addzero.device.protocol.modbus.ksp.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ModbusContractDefaultsResolverTest {
    @Test
    fun defaultServiceIdUsesInterfaceNameKebabCase() {
        assertEquals("mcuinfo", ModbusContractDefaultsResolver.defaultServiceId("McuinfoApi"))
        assertEquals("self-dev-board", ModbusContractDefaultsResolver.defaultServiceId("SelfDevBoardApi"))
    }

    @Test
    fun resolveFunctionCodeNameInfersCommonDefaults() {
        assertEquals(
            "READ_INPUT_REGISTERS",
            ModbusContractDefaultsResolver.resolveFunctionCodeName(
                explicitFunctionCodeName = "AUTO",
                parameters = emptyList(),
                returnType = readIdentityOperation().returnType,
            ),
        )
        assertEquals(
            "WRITE_SINGLE_COIL",
            ModbusContractDefaultsResolver.resolveFunctionCodeName(
                explicitFunctionCodeName = "AUTO",
                parameters = resetDeviceOperation().parameters,
                returnType = commandResultReturnType(),
            ),
        )
        assertEquals(
            "WRITE_SINGLE_REGISTER",
            ModbusContractDefaultsResolver.resolveFunctionCodeName(
                explicitFunctionCodeName = "AUTO",
                parameters =
                    listOf(
                        parameter(name = "threshold", order = 0, codecName = "U16", registerOffset = 0, registerWidth = 1),
                    ),
                returnType = commandResultReturnType(),
            ),
        )
        assertEquals(
            "WRITE_MULTIPLE_REGISTERS",
            ModbusContractDefaultsResolver.resolveFunctionCodeName(
                explicitFunctionCodeName = "AUTO",
                parameters = firmwareChunkOperation().parameters,
                returnType = commandResultReturnType(),
            ),
        )
        assertEquals(
            "WRITE_MULTIPLE_COILS",
            ModbusContractDefaultsResolver.resolveFunctionCodeName(
                explicitFunctionCodeName = "AUTO",
                parameters =
                    listOf(
                        parameter(
                            name = "relay0",
                            order = 0,
                            codecName = "BOOL_COIL",
                            registerOffset = 0,
                            registerWidth = 1,
                            valueKind = ModbusValueKind.BOOLEAN,
                        ),
                        parameter(
                            name = "relay1",
                            order = 1,
                            codecName = "BOOL_COIL",
                            registerOffset = 1,
                            registerWidth = 1,
                            valueKind = ModbusValueKind.BOOLEAN,
                        ),
                    ),
                returnType = commandResultReturnType(),
            ),
        )
    }

    @Test
    fun defaultOperationIdUsesMethodNameKebabCase() {
        assertEquals("read-identity", ModbusContractDefaultsResolver.defaultOperationId("readIdentity"))
        assertEquals("firmware-chunk", ModbusContractDefaultsResolver.defaultOperationId("firmwareChunk"))
    }

    @Test
    fun resolveOperationsInfersCapabilityAndQuantity() {
        val resolved =
            ModbusContractDefaultsResolver.resolveOperations(
                serviceId = "mcuinfo",
                operations = listOf(readIdentityOperation(), readPartitionLayoutOperation()),
            ).associateBy(ModbusOperationModel::methodName)

        assertEquals("read-identity", resolved.getValue("readIdentity").operationId)
        assertEquals("read-identity", resolved.getValue("readIdentity").capabilityKey)
        assertEquals(7, resolved.getValue("readIdentity").quantity)

        assertEquals("read-partition-layout", resolved.getValue("readPartitionLayout").operationId)
        assertEquals("read-partition-layout", resolved.getValue("readPartitionLayout").capabilityKey)
        assertEquals(9, resolved.getValue("readPartitionLayout").quantity)
    }

    @Test
    fun resolveOperationsKeepsExistingReadAddressesStableWhenNewMethodIsAdded() {
        val baseline =
            ModbusContractDefaultsResolver.resolveOperations(
                serviceId = "mcuinfo",
                operations = listOf(readIdentityOperation(), readPartitionLayoutOperation()),
            ).associateBy(ModbusOperationModel::methodName)
        val expanded =
            ModbusContractDefaultsResolver.resolveOperations(
                serviceId = "mcuinfo",
                operations = listOf(readIdentityOperation(), readPartitionLayoutOperation(), readBootConfigOperation()),
            ).associateBy(ModbusOperationModel::methodName)

        assertEquals(baseline.getValue("readIdentity").address, expanded.getValue("readIdentity").address)
        assertEquals(baseline.getValue("readPartitionLayout").address, expanded.getValue("readPartitionLayout").address)
    }

    @Test
    fun resolveOperationsKeepsExistingWriteAddressesStableWhenNewMethodIsAdded() {
        val baseline =
            ModbusContractDefaultsResolver.resolveOperations(
                serviceId = "flash",
                operations = listOf(firmwareStartOperation(), firmwareChunkOperation(), firmwareCommitOperation()),
            ).associateBy(ModbusOperationModel::methodName)
        val expanded =
            ModbusContractDefaultsResolver.resolveOperations(
                serviceId = "flash",
                operations = listOf(firmwareStartOperation(), firmwareChunkOperation(), firmwareCommitOperation(), resetDeviceOperation()),
            ).associateBy(ModbusOperationModel::methodName)

        assertEquals(baseline.getValue("firmwareStart").address, expanded.getValue("firmwareStart").address)
        assertEquals(baseline.getValue("firmwareChunk").address, expanded.getValue("firmwareChunk").address)
        assertEquals(baseline.getValue("firmwareCommit").address, expanded.getValue("firmwareCommit").address)
    }

    private fun readIdentityOperation(): ModbusOperationModel =
        operation(
            methodName = "readIdentity",
            functionCodeName = "READ_INPUT_REGISTERS",
            returnType =
                ModbusReturnTypeModel(
                    qualifiedName = "site.addzero.esp32_host_computer.api.McuIdentityInfo",
                    simpleName = "McuIdentityInfo",
                    kind = ModbusReturnKind.DTO,
                    properties =
                        listOf(
                            property(name = "macWord0", codecName = "U16", registerOffset = 0),
                            property(name = "macWord1", codecName = "U16", registerOffset = 1),
                            property(name = "macWord2", codecName = "U16", registerOffset = 2),
                            property(name = "cpuModelCode", codecName = "U16", registerOffset = 3),
                            property(name = "cpuFrequencyMhz", codecName = "U16", registerOffset = 4),
                            property(name = "crystalFrequencyMhz", codecName = "U16", registerOffset = 5),
                            property(name = "flashSizeMb", codecName = "U16", registerOffset = 6),
                        ),
                ),
        )

    private fun readPartitionLayoutOperation(): ModbusOperationModel =
        operation(
            methodName = "readPartitionLayout",
            functionCodeName = "READ_INPUT_REGISTERS",
            returnType =
                ModbusReturnTypeModel(
                    qualifiedName = "site.addzero.esp32_host_computer.api.McuPartitionLayoutInfo",
                    simpleName = "McuPartitionLayoutInfo",
                    kind = ModbusReturnKind.DTO,
                    properties =
                        listOf(
                            property(name = "partitionTableOffsetKb", codecName = "U16", registerOffset = 0),
                            property(name = "nvsOffsetKb", codecName = "U16", registerOffset = 1),
                            property(name = "nvsSizeKb", codecName = "U16", registerOffset = 2),
                            property(name = "otaDataOffsetKb", codecName = "U16", registerOffset = 3),
                            property(name = "otaDataSizeKb", codecName = "U16", registerOffset = 4),
                            property(name = "app0OffsetKb", codecName = "U16", registerOffset = 5),
                            property(name = "app0SizeKb", codecName = "U16", registerOffset = 6),
                            property(name = "app1OffsetKb", codecName = "U16", registerOffset = 7),
                            property(name = "app1SizeKb", codecName = "U16", registerOffset = 8),
                        ),
                ),
        )

    private fun readBootConfigOperation(): ModbusOperationModel =
        operation(
            methodName = "readBootConfig",
            functionCodeName = "READ_INPUT_REGISTERS",
            returnType =
                ModbusReturnTypeModel(
                    qualifiedName = "site.addzero.esp32_host_computer.api.McuBootConfigInfo",
                    simpleName = "McuBootConfigInfo",
                    kind = ModbusReturnKind.DTO,
                    properties =
                        listOf(
                            property(name = "bootModeCode", codecName = "U16", registerOffset = 0),
                            property(name = "bootCounter", codecName = "U16", registerOffset = 1),
                        ),
                ),
        )

    private fun resetDeviceOperation(): ModbusOperationModel =
        operation(
            methodName = "resetDevice",
            functionCodeName = "WRITE_SINGLE_COIL",
            parameters =
                listOf(
                    parameter(
                        name = "trigger",
                        order = 0,
                        codecName = "BOOL_COIL",
                        registerOffset = 0,
                        registerWidth = 1,
                        valueKind = ModbusValueKind.BOOLEAN,
                    ),
                ),
        )

    private fun firmwareStartOperation(): ModbusOperationModel =
        operation(
            methodName = "firmwareStart",
            functionCodeName = "WRITE_MULTIPLE_REGISTERS",
            capabilityKey = "flash-start",
            parameters =
                listOf(
                    parameter(name = "totalBytes", order = 0, codecName = "U32_BE", registerOffset = 0, registerWidth = 2),
                    parameter(name = "crc32", order = 1, codecName = "U32_BE", registerOffset = 2, registerWidth = 2),
                ),
        )

    private fun firmwareChunkOperation(): ModbusOperationModel =
        operation(
            methodName = "firmwareChunk",
            functionCodeName = "WRITE_MULTIPLE_REGISTERS",
            capabilityKey = "flash-chunk",
            parameters =
                listOf(
                    parameter(name = "sequence", order = 0, codecName = "U16", registerOffset = 0, registerWidth = 1),
                    parameter(name = "usedBytes", order = 1, codecName = "U16", registerOffset = 1, registerWidth = 1),
                    parameter(name = "word0", order = 2, codecName = "U32_BE", registerOffset = 2, registerWidth = 2),
                    parameter(name = "word1", order = 3, codecName = "U32_BE", registerOffset = 4, registerWidth = 2),
                    parameter(name = "word2", order = 4, codecName = "U32_BE", registerOffset = 6, registerWidth = 2),
                    parameter(name = "word3", order = 5, codecName = "U32_BE", registerOffset = 8, registerWidth = 2),
                ),
        )

    private fun firmwareCommitOperation(): ModbusOperationModel =
        operation(
            methodName = "firmwareCommit",
            functionCodeName = "WRITE_SINGLE_REGISTER",
            capabilityKey = "flash-commit",
            parameters =
                listOf(
                    parameter(name = "totalChunks", order = 0, codecName = "U16", registerOffset = 0, registerWidth = 1),
                ),
        )

    private fun operation(
        methodName: String,
        functionCodeName: String,
        capabilityKey: String = "",
        parameters: List<ModbusParameterModel> = emptyList(),
        returnType: ModbusReturnTypeModel = commandResultReturnType(),
    ): ModbusOperationModel =
        ModbusOperationModel(
            methodName = methodName,
            operationId = "",
            functionCodeName = functionCodeName,
            address = -1,
            quantity = -1,
            capabilityKey = capabilityKey,
            requestClassName = "${methodName.replaceFirstChar(Char::uppercase)}Request",
            requestQualifiedName = "site.addzero.generated.${methodName.replaceFirstChar(Char::uppercase)}Request",
            parameters = parameters,
            returnType = returnType,
            doc = ModbusDocModel(summary = "$methodName summary"),
        )

    private fun parameter(
        name: String,
        order: Int,
        codecName: String,
        registerOffset: Int,
        registerWidth: Int,
        valueKind: ModbusValueKind = ModbusValueKind.INT,
    ): ModbusParameterModel =
        ModbusParameterModel(
            name = name,
            qualifiedType = if (valueKind == ModbusValueKind.BOOLEAN) "kotlin.Boolean" else "kotlin.Int",
            valueKind = valueKind,
            order = order,
            codecName = codecName,
            registerOffset = registerOffset,
            bitOffset = 0,
            registerWidth = registerWidth,
            doc = "$name 参数",
        )

    private fun commandResultReturnType(): ModbusReturnTypeModel =
        ModbusReturnTypeModel(
            qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
            simpleName = "ModbusCommandResult",
            kind = ModbusReturnKind.COMMAND_RESULT,
        )

    private fun property(
        name: String,
        codecName: String,
        registerOffset: Int,
        valueKind: ModbusValueKind = ModbusValueKind.INT,
    ): ModbusPropertyModel =
        ModbusPropertyModel(
            name = name,
            qualifiedType = if (valueKind == ModbusValueKind.BOOLEAN) "kotlin.Boolean" else "kotlin.Int",
            valueKind = valueKind,
            field =
                ModbusFieldModel(
                    codecName = codecName,
                    registerOffset = registerOffset,
                    bitOffset = 0,
                    length = 1,
                    registerWidth = if (codecName == "U32_BE") 2 else 1,
                ),
            doc = "$name 字段",
        )
}
