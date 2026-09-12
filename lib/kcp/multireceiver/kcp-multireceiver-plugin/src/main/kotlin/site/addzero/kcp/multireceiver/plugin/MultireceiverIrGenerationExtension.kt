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
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
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
        if (generated.typeParameters.size != original.typeParameters.size) {
            return null
        }

        val originalParameters = original.regularParameters()
        if (originalParameters.size == 1) {
            if (!generated.hasExpectedName(
                    original = original,
                    generationKind = GenerationKind.EXTENSION,
                    parameterNames = listOf(originalParameters.single().name.asString()),
                )
            ) {
                return null
            }
            val generatedReceiver = generated.extensionReceiver() ?: return null
            if (generated.contextParameters().isNotEmpty()) {
                return null
            }
            if (generated.regularParameters().isNotEmpty()) {
                return null
            }
            if (!sameIrType(originalParameters.single().type, generatedReceiver.type)) {
                return null
            }
            return IrWrapperMatch(
                original = original,
                generationKind = GenerationKind.EXTENSION,
                receiverParameterIndex = 0,
                contextParameterIndices = emptyList(),
            )
        }

        val contextIndices = originalParameters.indices.filter { index ->
            originalParameters[index].hasAnnotation(MultireceiverPluginKeys.receiverAnnotation)
        }
        if (contextIndices.isEmpty()) {
            return null
        }
        if (!generated.hasExpectedName(
                original = original,
                generationKind = GenerationKind.CONTEXT,
                parameterNames = contextIndices.map { index -> originalParameters[index].name.asString() },
            )
        ) {
            return null
        }
        if (generated.extensionReceiver() != null) {
            return null
        }

        val generatedParameters = generated.parametersInOriginalOrder(
            contextParameterIndices = contextIndices,
            originalParameterCount = originalParameters.size,
        ) ?: return null
        if (generatedParameters.size != originalParameters.size) {
            return null
        }
        originalParameters.forEachIndexed { index, parameter ->
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
                call.typeArguments[index] = typeParameter.symbol.defaultType
            }

            when (match.generationKind) {
                GenerationKind.EXTENSION -> {
                    val extensionReceiver = generated.extensionReceiver()
                        ?: error("Missing generated extension receiver for ${generated.name}")
                    val originalParameter = match.original.regularParameters()[match.receiverParameterIndex ?: 0]
                    call.arguments[originalParameter] = irGet(extensionReceiver)
                }

                GenerationKind.CONTEXT -> {
                    val originalParameters = match.original.regularParameters()
                    val generatedParameters = generated.parametersInOriginalOrder(
                        contextParameterIndices = match.contextParameterIndices,
                        originalParameterCount = originalParameters.size,
                    ) ?: error("Unable to restore generated parameter order for ${generated.name}")
                    originalParameters.forEachIndexed { originalIndex, originalParameter ->
                        call.arguments[originalParameter] = irGet(generatedParameters[originalIndex])
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

    private fun IrSimpleFunction.extensionReceiver() =
        parameters.singleOrNull { parameter -> parameter.kind == IrParameterKind.ExtensionReceiver }

    private fun IrSimpleFunction.regularParameters() =
        parameters.filter { parameter -> parameter.kind == IrParameterKind.Regular }

    private fun IrSimpleFunction.parametersInOriginalOrder(
        contextParameterIndices: List<Int>,
        originalParameterCount: Int,
    ): List<IrValueParameter>? {
        val contextParameters = contextParameters()
        val regularParameters = regularParameters()
        if (contextParameters.isEmpty() && regularParameters.size == originalParameterCount) {
            return regularParameters
        }
        if (contextParameters.size != contextParameterIndices.size ||
            contextParameters.size + regularParameters.size != originalParameterCount
        ) {
            return null
        }

        val contextIndices = contextParameterIndices.toSet()
        val contextIterator = contextParameters.iterator()
        val regularIterator = regularParameters.iterator()
        return List(originalParameterCount) { index ->
            if (index in contextIndices) contextIterator.next() else regularIterator.next()
        }
    }

    private fun IrSimpleFunction.hasExpectedName(
        original: IrSimpleFunction,
        generationKind: GenerationKind,
        parameterNames: List<String>,
    ): Boolean {
        if (name == original.name) {
            return true
        }
        val suffix = parameterNames.joinToString(separator = "") { parameterName ->
            parameterName.toPascalCase()
        }.ifBlank { "Value" }
        val expectedName = when (generationKind) {
            GenerationKind.EXTENSION -> "${original.name}ByAddzeroExtension$suffix"
            GenerationKind.CONTEXT -> "${original.name}ByAddzeroContext$suffix"
        }
        return name.asString() == expectedName
    }

    private fun String.toPascalCase(): String {
        val result = StringBuilder(length)
        var uppercaseNext = true
        forEach { character ->
            if (!character.isLetterOrDigit()) {
                uppercaseNext = true
            } else if (uppercaseNext) {
                result.append(character.uppercaseChar())
                uppercaseNext = false
            } else {
                result.append(character)
            }
        }
        return result.toString()
    }

    private fun isSupportedOriginalFunction(
        function: IrSimpleFunction,
    ): Boolean {
        if (!function.isSourceFunction()) {
            return false
        }
        if (!function.hasAnnotation(MultireceiverPluginKeys.generateExtensionAnnotation)) {
            return false
        }
        if (function.extensionReceiver() != null) {
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
        val message = constructorCall.arguments[0] as? IrConst ?: return false
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
