package site.addzero.kcp.spreadpack

import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrThrow
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(
    DeprecatedForRemovalCompilerApi::class,
    UnsafeDuringIrConstructionAPI::class,
)
class SpreadPackIrGenerationExtension : IrGenerationExtension {

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
                processFunctionContainer(
                    functions = functions,
                    processWholeClass = false,
                    pluginContext = pluginContext,
                )
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
                    functions = declaration.declarations.filterIsInstance<IrSimpleFunction>(),
                    processWholeClass = declaration.hasAnnotation(SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotation),
                    pluginContext = pluginContext,
                )
                declaration.acceptChildrenVoid(this)
            }
        })
    }

    private fun processFunctionContainer(
        functions: List<IrSimpleFunction>,
        processWholeClass: Boolean,
        pluginContext: IrPluginContext,
    ) {
        val originals = functions.filter { function ->
            isAnnotatedOriginal(function, processWholeClass)
        }
        if (originals.isEmpty()) {
            return
        }

        val candidates = functions.filter { function ->
            isGeneratedByThisPlugin(function) || hasStubBody(function)
        }
        if (candidates.isEmpty()) {
            return
        }

        candidates.forEach { candidate ->
            val match = resolveMatch(candidate, originals) ?: return@forEach
            candidate.body = createDelegatingBody(pluginContext, candidate, match)
        }
    }

    private fun isAnnotatedOriginal(
        function: IrSimpleFunction,
        processWholeClass: Boolean,
    ): Boolean {
        if (!isSupportedOriginalFunction(function)) {
            return false
        }
        if (!processWholeClass && !function.hasAnnotation(SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotation)) {
            return false
        }
        return function.valueParameters.any { parameter -> parameter.getSpreadPackAnnotation() != null }
    }

    private fun resolveMatch(
        generated: IrSimpleFunction,
        originals: List<IrSimpleFunction>,
    ): IrSpreadPackMatch? {
        val matches = originals.mapNotNull { original ->
            resolveAgainstOriginal(generated, original)
        }
        if (matches.isEmpty()) {
            return null
        }
        return matches.first()
    }

    private fun resolveAgainstOriginal(
        generated: IrSimpleFunction,
        original: IrSimpleFunction,
    ): IrSpreadPackMatch? {
        val originalName = original.name.asString()
        val generatedName = generated.name.asString()
        if (generatedName != originalName && !generatedName.startsWith("${originalName}Via")) {
            return null
        }
        if (original.extensionReceiverParameter != null || generated.extensionReceiverParameter != null) {
            return null
        }
        if (original.contextReceiverParametersCount > 0 || generated.contextReceiverParametersCount > 0) {
            return null
        }
        if (original.typeParameters.size != generated.typeParameters.size) {
            return null
        }

        val expansions = original.valueParameters.mapIndexedNotNull { index, parameter ->
            createExpansion(original, index, parameter)
        }
        if (expansions.isEmpty()) {
            return null
        }

        val expectedTypes = buildExpectedParameterTypes(original, expansions)
        if (expectedTypes.size != generated.valueParameters.size) {
            return null
        }
        val signaturesMatch = expectedTypes
            .zip(generated.valueParameters.map { parameter -> parameter.type })
            .all { pair ->
                sameIrType(pair.first, pair.second)
            }
        if (!signaturesMatch) {
            return null
        }

        val expectedRenamed = buildRenamedFunctionName(original.name, expansions)
        if (generated.name != original.name && generated.name != expectedRenamed) {
            return null
        }

        return IrSpreadPackMatch(
            original = original,
            expansions = expansions,
        )
    }

    private fun createExpansion(
        owner: IrSimpleFunction,
        parameterIndex: Int,
        parameter: IrValueParameter,
    ): IrSpreadPackExpansion? {
        val annotation = parameter.getSpreadPackAnnotation() ?: return null
        val parameterType = parameter.type as? IrSimpleType
            ?: invalidTarget(
                owner,
                "spread-pack parameter ${parameter.name.asString()} must reference a regular class with a primary constructor",
            )
        if (parameterType.arguments.isNotEmpty()) {
            invalidTarget(
                owner,
                "generic spread-pack carriers are not supported in v1: ${parameter.name.asString()}",
            )
        }
        val carrierClass = parameterType.classifierOrNull?.owner as? IrClass
            ?: invalidTarget(
                owner,
                "spread-pack parameter ${parameter.name.asString()} must reference a regular class with a primary constructor",
            )
        if (carrierClass.typeParameters.isNotEmpty()) {
            invalidTarget(
                owner,
                "generic spread-pack carriers are not supported in v1: ${carrierClass.name.asString()}",
            )
        }
        val primaryConstructor = carrierClass.declarations
            .filterIsInstance<org.jetbrains.kotlin.ir.declarations.IrConstructor>()
            .firstOrNull { constructor -> constructor.isPrimary }
            ?: invalidTarget(
                owner,
                "spread-pack carrier ${carrierClass.name.asString()} must declare a primary constructor",
            )

        val selectorKind = annotation.selectorKind()
        val excludedNames = annotation.excludedNames()
        val constructorParameterNames = primaryConstructor.valueParameters
            .map { constructorParameter -> constructorParameter.name.asString() }
            .toSet()
        val unknownExcludedNames = excludedNames - constructorParameterNames
        if (unknownExcludedNames.isNotEmpty()) {
            invalidTarget(
                owner,
                "unknown spread-pack exclude names for ${carrierClass.name.asString()}: " +
                    unknownExcludedNames.sorted().joinToString(),
            )
        }

        val selectedParameters = primaryConstructor.valueParameters.filter { constructorParameter ->
            shouldIncludeField(constructorParameter, selectorKind) &&
                constructorParameter.name.asString() !in excludedNames
        }
        val selectedNames = selectedParameters
            .map { constructorParameter -> constructorParameter.name.asString() }
            .toSet()
        primaryConstructor.valueParameters.forEach { constructorParameter ->
            if (constructorParameter.name.asString() !in selectedNames && constructorParameter.defaultValue == null) {
                invalidTarget(
                    owner,
                    "spread-pack carrier ${carrierClass.name.asString()} cannot omit required field " +
                        constructorParameter.name.asString(),
                )
            }
        }

        return IrSpreadPackExpansion(
            parameterIndex = parameterIndex,
            carrierClass = carrierClass,
            constructor = primaryConstructor,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            fields = selectedParameters.map { constructorParameter ->
                IrSpreadPackField(
                    name = constructorParameter.name,
                    type = constructorParameter.type,
                    constructorIndex = primaryConstructor.valueParameters.indexOf(constructorParameter),
                )
            },
        )
    }

    private fun buildExpectedParameterTypes(
        original: IrSimpleFunction,
        expansions: List<IrSpreadPackExpansion>,
    ): List<IrType> {
        val expansionsByIndex = expansions.associateBy { expansion -> expansion.parameterIndex }
        return buildList {
            original.valueParameters.forEachIndexed { index, parameter ->
                val expansion = expansionsByIndex[index]
                if (expansion == null) {
                    add(parameter.type)
                } else {
                    expansion.fields.forEach { field ->
                        add(field.type)
                    }
                }
            }
        }
    }

    private fun createDelegatingBody(
        pluginContext: IrPluginContext,
        generated: IrSimpleFunction,
        match: IrSpreadPackMatch,
    ) = DeclarationIrBuilder(pluginContext, generated.symbol).irBlockBody {
        val expansionsByIndex = match.expansions.associateBy { expansion -> expansion.parameterIndex }
        var generatedParameterCursor = 0
        val originalCall = irCall(match.original.symbol).also { call ->
            generated.dispatchReceiverParameter?.let { receiver ->
                call.dispatchReceiver = irGet(receiver)
            }
            generated.typeParameters.forEachIndexed { index, typeParameter ->
                call.putTypeArgument(index, typeParameter.defaultType)
            }
            match.original.valueParameters.forEachIndexed { index, originalParameter ->
                val expansion = expansionsByIndex[index]
                if (expansion == null) {
                    call.putValueArgument(index, irGet(generated.valueParameters[generatedParameterCursor]))
                    generatedParameterCursor += 1
                    return@forEachIndexed
                }

                val constructorCall = irCall(expansion.constructor.symbol)
                expansion.fields.forEach { field ->
                    constructorCall.putValueArgument(
                        field.constructorIndex,
                        irGet(generated.valueParameters[generatedParameterCursor]),
                    )
                    generatedParameterCursor += 1
                }
                call.putValueArgument(index, constructorCall)
            }
        }

        if (generated.returnType.isUnit()) {
            +originalCall
        } else {
            +irReturn(originalCall)
        }
    }

    private fun shouldIncludeField(
        parameter: IrValueParameter,
        selectorKind: SelectorKind,
    ): Boolean {
        return when (selectorKind) {
            SelectorKind.PROPS -> true
            SelectorKind.ATTRS -> !isFunctionLike(parameter.type)
            SelectorKind.CALLBACKS -> isFunctionLike(parameter.type)
        }
    }

    private fun isFunctionLike(
        type: org.jetbrains.kotlin.ir.types.IrType,
    ): Boolean {
        val simpleType = type as? IrSimpleType ?: return false
        val fqName = (simpleType.classifierOrNull?.owner as? IrClass)
            ?.fqNameWhenAvailable
            ?.asString()
            ?: return false
        return fqName.startsWith("kotlin.Function") || fqName.startsWith("kotlin.reflect.KFunction")
    }

    private fun isSupportedOriginalFunction(
        function: IrSimpleFunction,
    ): Boolean {
        if (function.fqNameWhenAvailable == null) {
            return false
        }
        if (function.extensionReceiverParameter != null) {
            return false
        }
        if (function.contextReceiverParametersCount > 0) {
            return false
        }
        val parent = function.parent as? IrFile
        if (parent != null && function.visibility == org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PRIVATE) {
            return false
        }
        return true
    }

    private fun buildRenamedFunctionName(
        originalName: Name,
        expansions: List<IrSpreadPackExpansion>,
    ): Name {
        val suffix = expansions.joinToString(separator = "And") { expansion ->
            val selectorSuffix = when (expansion.selectorKind) {
                SelectorKind.PROPS -> ""
                SelectorKind.ATTRS -> "Attrs"
                SelectorKind.CALLBACKS -> "Callbacks"
            }
            expansion.carrierClass.name.asString().toPascalCase() + selectorSuffix + "Pack"
        }
        return Name.identifier("${originalName.asString()}Via$suffix")
    }

    private fun IrValueParameter.getSpreadPackAnnotation(): IrConstructorCall? {
        return annotations.firstOrNull { annotation ->
            (annotation.type.classifierOrNull?.owner as? IrClass)?.fqNameWhenAvailable ==
                SpreadPackPluginKeys.spreadPackAnnotation
        }
    }

    private fun IrConstructorCall.excludedNames(): Set<String> {
        val rawArgument = getValueArgument(0) ?: return emptySet()
        return when (rawArgument) {
            is IrVararg -> rawArgument.elements.mapNotNull { element ->
                (element as? IrConst)?.value as? String
            }.toSet()

            is IrConst -> listOfNotNull(rawArgument.value as? String).toSet()
            else -> emptySet()
        }
    }

    private fun IrConstructorCall.selectorKind(): SelectorKind {
        val rawArgument = getValueArgument(1) as? IrGetEnumValue ?: return SelectorKind.PROPS
        return when (rawArgument.symbol.owner.name.asString()) {
            SelectorKind.ATTRS.name -> SelectorKind.ATTRS
            SelectorKind.CALLBACKS.name -> SelectorKind.CALLBACKS
            else -> SelectorKind.PROPS
        }
    }

    private fun isGeneratedByThisPlugin(
        function: IrSimpleFunction,
    ): Boolean {
        val origin = function.origin
        return origin is IrDeclarationOrigin.GeneratedByPlugin &&
            origin.pluginKey == SpreadPackGeneratedDeclarationKey
    }

    private fun hasStubBody(
        function: IrSimpleFunction,
    ): Boolean {
        val body = function.body as? IrBlockBody ?: return false
        if (body.statements.size != 1) {
            return false
        }
        val throwExpression = body.statements.single() as? IrThrow ?: return false
        val constructorCall = throwExpression.value as? IrConstructorCall ?: return false
        val message = constructorCall.getValueArgument(0) as? IrConst ?: return false
        return message.value == SpreadPackPluginKeys.stubErrorMessage
    }

    private fun IrDeclaration.hasAnnotation(
        annotationFqName: FqName,
    ): Boolean {
        return annotations.any { annotation ->
            (annotation.type.classifierOrNull?.owner as? IrClass)?.fqNameWhenAvailable == annotationFqName
        }
    }

    private fun sameIrType(
        left: org.jetbrains.kotlin.ir.types.IrType,
        right: org.jetbrains.kotlin.ir.types.IrType,
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
                leftArgument is org.jetbrains.kotlin.ir.types.IrStarProjection &&
                    rightArgument is org.jetbrains.kotlin.ir.types.IrStarProjection -> true

                leftArgument is org.jetbrains.kotlin.ir.types.IrTypeProjection &&
                    rightArgument is org.jetbrains.kotlin.ir.types.IrTypeProjection -> {
                    leftArgument.variance == rightArgument.variance &&
                        sameIrType(leftArgument.type, rightArgument.type)
                }

                else -> false
            }
        }
    }

    private fun invalidTarget(
        function: IrSimpleFunction,
        reason: String,
    ): Nothing {
        val fqName = function.fqNameWhenAvailable?.asString() ?: function.name.asString()
        throw IllegalStateException("Invalid @GenerateSpreadPackOverloads target $fqName: $reason")
    }

    private fun String.toPascalCase(): String {
        if (isEmpty()) {
            return this
        }
        val builder = StringBuilder(length)
        var uppercaseNext = true
        forEach { character ->
            if (!character.isLetterOrDigit()) {
                uppercaseNext = true
            } else if (uppercaseNext) {
                builder.append(character.uppercaseChar())
                uppercaseNext = false
            } else {
                builder.append(character)
            }
        }
        return builder.toString()
    }
}
