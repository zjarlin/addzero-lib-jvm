package site.addzero.kcp.multireceiver.plugin

import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrThrow
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrStarProjection
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

@OptIn(
    DeprecatedForRemovalCompilerApi::class,
    UnsafeDuringIrConstructionAPI::class,
)
class MultireceiverIrGenerationExtension : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        processTopLevelPackages(moduleFragment, pluginContext)
        moduleFragment.files.forEach { file ->
            processClasses(file, pluginContext)
        }
    }

    private fun processTopLevelPackages(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        moduleFragment.files
            .groupBy { file -> file.packageFqName }
            .values
            .map { files ->
                files.flatMap { file ->
                    file.declarations.filterIsInstance<IrSimpleFunction>()
                }
            }
            .filter { functions -> functions.isNotEmpty() }
            .forEach { functions ->
                processFunctionContainer(functions, pluginContext)
            }
    }

    private fun processClasses(
        declaration: IrElement,
        pluginContext: IrPluginContext,
    ) {
        declaration.acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                processFunctionContainer(
                    declaration.declarations.filterIsInstance<IrSimpleFunction>(),
                    pluginContext,
                )
                declaration.acceptChildrenVoid(this)
            }
        })
    }

    private fun processFunctionContainer(
        functions: List<IrSimpleFunction>,
        pluginContext: IrPluginContext,
    ) {
        val originals = functions.filter(::isSupportedOriginalFunction)
        if (originals.isEmpty()) {
            return
        }

        functions
            .filter(::isGeneratedByThisPlugin)
            .forEach { generated ->
                val match = resolveMatch(generated, originals) ?: return@forEach
                generated.body = createDelegatingBody(
                    pluginContext = pluginContext,
                    generated = generated,
                    match = match,
                )
            }
    }

    private fun resolveMatch(
        generated: IrSimpleFunction,
        originals: List<IrSimpleFunction>,
    ): IrWrapperMatch? {
        val matches = originals.mapNotNull { original ->
            resolveAgainstOriginal(generated, original)
        }
        return matches.singleOrNull()
    }

    private fun resolveAgainstOriginal(
        generated: IrSimpleFunction,
        original: IrSimpleFunction,
    ): IrWrapperMatch? {
        if (generated.name != original.name) {
            return null
        }
        if (generated.typeParameters.size != original.typeParameters.size) {
            return null
        }

        if (original.valueParameters.size == 1) {
            val generatedReceiver = generated.extensionReceiverParameter ?: return null
            if (generated.contextParameters().isNotEmpty()) {
                return null
            }
            if (generated.valueParameters.isNotEmpty()) {
                return null
            }
            if (!sameIrType(original.valueParameters.single().type, generatedReceiver.type)) {
                return null
            }
            return IrWrapperMatch(
                original = original,
                generationKind = GenerationKind.EXTENSION,
                receiverParameterIndex = 0,
                contextParameterIndices = emptyList(),
            )
        }

        val contextIndices = original.valueParameters.indices.filter { index ->
            original.valueParameters[index].hasAnnotation(MultireceiverPluginKeys.receiverAnnotation)
        }
        if (contextIndices.isEmpty()) {
            return null
        }
        if (generated.extensionReceiverParameter != null) {
            return null
        }

        val generatedParameters = generated.valueParameters
        if (generatedParameters.size != original.valueParameters.size) {
            return null
        }
        original.valueParameters.forEachIndexed { index, parameter ->
            if (!sameIrType(parameter.type, generatedParameters[index].type)) {
                return null
            }
        }

        return IrWrapperMatch(
            original = original,
            generationKind = GenerationKind.CONTEXT,
            receiverParameterIndex = null,
            contextParameterIndices = contextIndices,
        )
    }

    private fun createDelegatingBody(
        pluginContext: IrPluginContext,
        generated: IrSimpleFunction,
        match: IrWrapperMatch,
    ) = DeclarationIrBuilder(pluginContext, generated.symbol).irBlockBody {
        val originalCall = irCall(match.original.symbol).also { call ->
            generated.dispatchReceiverParameter?.let { receiver ->
                call.dispatchReceiver = irGet(receiver)
            }
            generated.typeParameters.forEachIndexed { index, typeParameter ->
                call.putTypeArgument(index, typeParameter.symbol.defaultType)
            }

            when (match.generationKind) {
                GenerationKind.EXTENSION -> {
                    val extensionReceiver = generated.extensionReceiverParameter
                        ?: error("Missing generated extension receiver for ${generated.name}")
                    call.putValueArgument(match.receiverParameterIndex ?: 0, irGet(extensionReceiver))
                }

                GenerationKind.CONTEXT -> {
                    val parametersInJvmOrder = generated.valueParameters
                    match.original.valueParameters.forEachIndexed { originalIndex, _ ->
                        val argument = irGet(parametersInJvmOrder[originalIndex])
                        call.putValueArgument(originalIndex, argument)
                    }
                }
            }
        }

        if (generated.returnType.isUnit()) {
            +originalCall
        } else {
            +irReturn(originalCall)
        }
    }

    private fun IrSimpleFunction.contextParameters() =
        parameters.filter { parameter -> parameter.kind == IrParameterKind.Context }

    private fun isSupportedOriginalFunction(
        function: IrSimpleFunction,
    ): Boolean {
        if (!function.isSourceFunction()) {
            return false
        }
        if (!function.hasAnnotation(MultireceiverPluginKeys.generateExtensionAnnotation)) {
            return false
        }
        if (function.extensionReceiverParameter != null) {
            return false
        }
        if (function.contextParameters().isNotEmpty()) {
            return false
        }
        if (function.parent !is org.jetbrains.kotlin.ir.declarations.IrClass &&
            function.visibility == org.jetbrains.kotlin.descriptors.Visibilities.Private
        ) {
            return false
        }
        return true
    }

    private fun isGeneratedByThisPlugin(
        function: IrSimpleFunction,
    ): Boolean {
        val origin = function.origin
        if (origin is IrDeclarationOrigin.GeneratedByPlugin &&
            origin.pluginKey == MultireceiverGeneratedDeclarationKey
        ) {
            return true
        }
        return hasStubBody(function)
    }

    private fun hasStubBody(
        function: IrSimpleFunction,
    ): Boolean {
        val body = function.body as? IrBlockBody ?: return false
        if (body.statements.size != 1) {
            return false
        }
        val throwExpression = body.statements.single() as? IrThrow ?: return false
        val constructorCall = throwExpression.value as? org.jetbrains.kotlin.ir.expressions.IrConstructorCall ?: return false
        val message = constructorCall.getValueArgument(0) as? IrConst ?: return false
        return message.value == MultireceiverPluginKeys.stubErrorMessage
    }

    private fun IrSimpleFunction.isSourceFunction(): Boolean {
        return fqNameWhenAvailable != null && origin !is IrDeclarationOrigin.GeneratedByPlugin
    }

    private fun sameIrType(
        left: IrType,
        right: IrType,
    ): Boolean {
        if (left == right) {
            return true
        }
        val leftSimpleType = left as? IrSimpleType ?: return false
        val rightSimpleType = right as? IrSimpleType ?: return false
        if (leftSimpleType.classifier != rightSimpleType.classifier) {
            return false
        }
        if (leftSimpleType.nullability != rightSimpleType.nullability) {
            return false
        }
        if (leftSimpleType.arguments.size != rightSimpleType.arguments.size) {
            return false
        }
        return leftSimpleType.arguments.zip(rightSimpleType.arguments).all { (leftArgument, rightArgument) ->
            when {
                leftArgument is IrStarProjection && rightArgument is IrStarProjection -> true
                leftArgument is IrTypeProjection && rightArgument is IrTypeProjection -> {
                    leftArgument.variance == rightArgument.variance &&
                        sameIrType(leftArgument.type, rightArgument.type)
                }

                else -> false
            }
        }
    }
}
