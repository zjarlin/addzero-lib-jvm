package site.addzero.device.protocol.modbus.ksp.contract

import site.addzero.device.protocol.modbus.codegen.ModbusKotlinContractGenerationRequest
import site.addzero.device.protocol.modbus.codegen.ModbusKotlinContractGenerator
import site.addzero.device.protocol.modbus.codegen.model.GeneratedArtifact as CodegenGeneratedArtifact
import site.addzero.device.protocol.modbus.codegen.model.ModbusDocModel as CodegenDocModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusFieldModel as CodegenFieldModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusOperationModel as CodegenOperationModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusParameterModel as CodegenParameterModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusPropertyModel as CodegenPropertyModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusReturnKind as CodegenReturnKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusReturnTypeModel as CodegenReturnTypeModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusServiceModel as CodegenServiceModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusTransportKind as CodegenTransportKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusValueKind as CodegenValueKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusWorkflowKind as CodegenWorkflowKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusWorkflowModel as CodegenWorkflowModel
import site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactGenerator
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusDocModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusFieldModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusOperationModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusParameterModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusPropertyModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusReturnKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusReturnTypeModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusServiceModel
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusValueKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusWorkflowKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusWorkflowModel

class ModbusKotlinContractArtifactGenerator : ModbusArtifactGenerator {
    override val kind: ModbusArtifactKind = ModbusArtifactKind.KOTLIN_CONTRACT

    override fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact> =
        ModbusKotlinContractGenerator.render(
            ModbusKotlinContractGenerationRequest(
                services = context.suite.services.map { service -> service.toCodegenModel() },
            ),
        ).map(CodegenGeneratedArtifact::toKspArtifact)
}

private fun ModbusServiceModel.toCodegenModel(): CodegenServiceModel =
    CodegenServiceModel(
        interfacePackage = interfacePackage,
        interfaceSimpleName = interfaceSimpleName,
        interfaceQualifiedName = interfaceQualifiedName,
        serviceId = serviceId,
        summary = summary,
        basePath = basePath,
        transport = transport.toCodegenTransportKind(),
        doc = doc.toCodegenDocModel(),
        operations = operations.map(ModbusOperationModel::toCodegenOperationModel),
        workflows = workflows.map(ModbusWorkflowModel::toCodegenWorkflowModel),
    )

private fun ModbusOperationModel.toCodegenOperationModel(): CodegenOperationModel =
    CodegenOperationModel(
        methodName = methodName,
        operationId = operationId,
        functionCodeName = functionCodeName,
        address = address,
        quantity = quantity,
        requestClassName = requestClassName,
        requestQualifiedName = requestQualifiedName,
        parameters = parameters.map(ModbusParameterModel::toCodegenParameterModel),
        returnType = returnType.toCodegenReturnTypeModel(),
        doc = doc.toCodegenDocModel(),
    )

private fun ModbusWorkflowModel.toCodegenWorkflowModel(): CodegenWorkflowModel =
    CodegenWorkflowModel(
        kind = kind.toCodegenWorkflowKind(),
        methodName = methodName,
        workflowId = workflowId,
        requestClassName = requestClassName,
        requestQualifiedName = requestQualifiedName,
        bytesParameterName = bytesParameterName,
        returnType = returnType.toCodegenReturnTypeModel(),
        doc = doc.toCodegenDocModel(),
        startMethodName = startMethodName,
        chunkMethodName = chunkMethodName,
        commitMethodName = commitMethodName,
        resetMethodName = resetMethodName,
    )

private fun ModbusReturnTypeModel.toCodegenReturnTypeModel(): CodegenReturnTypeModel =
    CodegenReturnTypeModel(
        qualifiedName = qualifiedName,
        simpleName = simpleName,
        kind = kind.toCodegenReturnKind(),
        docSummary = docSummary,
        valueKind = valueKind?.toCodegenValueKind(),
        codecName = codecName,
        length = length,
        registerWidth = registerWidth,
        properties = properties.map(ModbusPropertyModel::toCodegenPropertyModel),
    )

private fun ModbusPropertyModel.toCodegenPropertyModel(): CodegenPropertyModel =
    CodegenPropertyModel(
        name = name,
        qualifiedType = qualifiedType,
        valueKind = valueKind.toCodegenValueKind(),
        field = field?.toCodegenFieldModel(),
        doc = doc,
    )

private fun ModbusParameterModel.toCodegenParameterModel(): CodegenParameterModel =
    CodegenParameterModel(
        name = name,
        qualifiedType = qualifiedType,
        valueKind = valueKind.toCodegenValueKind(),
        order = order,
        codecName = codecName,
        registerOffset = registerOffset,
        bitOffset = bitOffset,
        registerWidth = registerWidth,
        length = length,
        doc = doc,
    )

private fun ModbusFieldModel.toCodegenFieldModel(): CodegenFieldModel =
    CodegenFieldModel(
        codecName = codecName,
        registerOffset = registerOffset,
        bitOffset = bitOffset,
        length = length,
        registerWidth = registerWidth,
    )

private fun ModbusDocModel.toCodegenDocModel(): CodegenDocModel =
    CodegenDocModel(
        summary = summary,
        descriptionLines = descriptionLines,
        parameterDocs = parameterDocs,
    )

private fun ModbusTransportKind.toCodegenTransportKind(): CodegenTransportKind = enumValueOf(name)

private fun ModbusValueKind.toCodegenValueKind(): CodegenValueKind = enumValueOf(name)

private fun ModbusReturnKind.toCodegenReturnKind(): CodegenReturnKind = enumValueOf(name)

private fun ModbusWorkflowKind.toCodegenWorkflowKind(): CodegenWorkflowKind = enumValueOf(name)

private fun CodegenGeneratedArtifact.toKspArtifact(): GeneratedArtifact =
    GeneratedArtifact(
        packageName = packageName,
        fileName = fileName,
        extensionName = extensionName,
        content = content,
    )
