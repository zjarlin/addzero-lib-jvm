package site.addzero.kcp.transformoverload.plugin

import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrStarProjection
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.io.File

@OptIn(
    DeprecatedForRemovalCompilerApi::class,
    UnsafeDuringIrConstructionAPI::class,
)
class TransformOverloadIrGenerationExtension : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        val converters = collectConverters(moduleFragment)
        if (converters.isEmpty()) {
            return
        }
        val helperSymbols = LiftHelperSymbols(pluginContext)
        moduleFragment.files.forEach { file ->
            processTopLevelFunctions(file, pluginContext, converters, helperSymbols)
            processNestedClasses(file, pluginContext, converters, helperSymbols)
        }
    }

    private fun processTopLevelFunctions(
        file: IrFile,
        pluginContext: IrPluginContext,
        converters: List<IrConverterSpec>,
        helperSymbols: LiftHelperSymbols,
    ) {
        val functions = file.declarations.filterIsInstance<IrSimpleFunction>()
        processFunctionContainer(functions, pluginContext, converters, helperSymbols)
    }

    private fun processNestedClasses(
        declaration: IrElement,
        pluginContext: IrPluginContext,
        converters: List<IrConverterSpec>,
        helperSymbols: LiftHelperSymbols,
    ) {
        declaration.acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                processFunctionContainer(
                    declaration.declarations.filterIsInstance<IrSimpleFunction>(),
                    pluginContext,
                    converters,
                    helperSymbols,
                )
                declaration.acceptChildrenVoid(this)
            }
        })
    }

    private fun processFunctionContainer(
        functions: List<IrSimpleFunction>,
        pluginContext: IrPluginContext,
        converters: List<IrConverterSpec>,
        helperSymbols: LiftHelperSymbols,
    ) {
        val originals = functions.filter { function ->
            !isGeneratedByThisPlugin(function) && isSupportedOriginalFunction(function)
        }
        val generatedFunctions = functions.filter(::isGeneratedByThisPlugin)
        if (generatedFunctions.isEmpty()) {
            return
        }
        generatedFunctions.forEach { generated ->
            val match = resolveMatch(generated, originals, converters) ?: return@forEach
            generated.body = createDelegatingBody(pluginContext, generated, match, helperSymbols)
        }
    }

    private fun collectConverters(moduleFragment: IrModuleFragment): List<IrConverterSpec> {
        val rawConverters = mutableListOf<RawIrConverterSpec>()
        moduleFragment.files.forEach { file ->
            file.acceptChildrenVoid(object : IrVisitorVoid() {
                override fun visitElement(element: IrElement) {
                    element.acceptChildrenVoid(this)
                }

                override fun visitSimpleFunction(declaration: IrSimpleFunction) {
                    if (declaration.hasAnnotation(TransformOverloadPluginKeys.overloadTransformAnnotation)) {
                        rawConverters += createRawConverterSpec(declaration)
                    }
                    declaration.acceptChildrenVoid(this)
                }
            })
        }

        val suffixUsage = rawConverters.groupBy { raw -> raw.defaultSuffix }
        return rawConverters.map { raw ->
            val uniqueSuffix = if ((suffixUsage[raw.defaultSuffix]?.size ?: 0) > 1) {
                raw.containerPrefix + raw.defaultSuffix
            } else {
                raw.defaultSuffix
            }
            IrConverterSpec(
                function = raw.function,
                parameterKind = raw.parameterKind,
                sourceType = raw.sourceType,
                targetType = raw.targetType,
                typeParameters = raw.typeParameters,
                uniqueSuffix = uniqueSuffix,
                callableIdText = raw.callableIdText,
                supportsMemberContainer = raw.supportsMemberContainer,
            )
        }
    }

    private fun createRawConverterSpec(function: IrSimpleFunction): RawIrConverterSpec {
        if (function.contextReceiverParametersCount > 0) {
            invalidConverter(function, "context parameters are not supported")
        }
        val hasExtensionReceiver = function.extensionReceiverParameter != null
        val valueParameterCount = function.valueParameters.size
        if (hasExtensionReceiver && valueParameterCount > 0) {
            invalidConverter(function, "extension converter cannot declare value parameters")
        }
        if (!hasExtensionReceiver && valueParameterCount != 1) {
            invalidConverter(function, "converter must declare exactly one source parameter")
        }
        val parentClass = function.parent as? IrClass
        if (parentClass != null) {
            if (!implementsTransformProvider(parentClass)) {
                invalidConverter(function, "member converter must be declared inside TransformProvider")
            }
            if (parentClass.typeParameters.isNotEmpty()) {
                invalidConverter(function, "generic TransformProvider containers are not supported in v1")
            }
        }

        val sourceType = if (hasExtensionReceiver) {
            function.extensionReceiverParameter!!.type
        } else {
            function.valueParameters.single().type
        }
        val parameterKind = if (hasExtensionReceiver) {
            ConverterParameterKind.EXTENSION_RECEIVER
        } else {
            ConverterParameterKind.VALUE_PARAMETER
        }
        return RawIrConverterSpec(
            function = function,
            parameterKind = parameterKind,
            sourceType = sourceType,
            targetType = function.returnType,
            typeParameters = function.typeParameters,
            defaultSuffix = function.name.asString().toPascalCase(),
            containerPrefix = parentClass?.name?.asString() ?: "TopLevel",
            callableIdText = function.fqNameWhenAvailable?.asString() ?: function.name.asString(),
            supportsMemberContainer = parentClass != null,
        )
    }

    private fun resolveMatch(
        generated: IrSimpleFunction,
        originals: List<IrSimpleFunction>,
        converters: List<IrConverterSpec>,
    ): IrOverloadMatch? {
        val matches = originals.mapNotNull { original ->
            resolveAgainstOriginal(generated, original, converters)
        }
        return matches.singleOrNull()
    }

    private fun resolveAgainstOriginal(
        generated: IrSimpleFunction,
        original: IrSimpleFunction,
        converters: List<IrConverterSpec>,
    ): IrOverloadMatch? {
        val originalName = original.name.asString()
        val generatedName = generated.name.asString()
        if (generatedName != originalName && !generatedName.startsWith("${originalName}Via")) {
            return null
        }
        if (original.valueParameters.size != generated.valueParameters.size) {
            return null
        }
        if (original.typeParameters.size != generated.typeParameters.size) {
            return null
        }
        if (original.extensionReceiverParameter != null || generated.extensionReceiverParameter != null) {
            return null
        }
        if (original.contextReceiverParametersCount > 0 || generated.contextReceiverParametersCount > 0) {
            return null
        }

        val perParameterChoices = mutableListOf<List<IrParameterTransform?>>()
        for (index in original.valueParameters.indices) {
            val originalType = original.valueParameters[index].type
            val generatedType = generated.valueParameters[index].type
            if (sameIrType(originalType, generatedType)) {
                perParameterChoices += listOf(null)
                continue
            }
            val transforms = converters.mapNotNull { converter ->
                resolveParameterTransform(converter, index, generatedType, originalType)
            }
            if (transforms.isEmpty()) {
                return null
            }
            perParameterChoices += transforms
        }

        val resolved = mutableListOf<IrOverloadMatch>()
        fun walk(index: Int, chosen: MutableList<IrParameterTransform>) {
            if (index == perParameterChoices.size) {
                if (chosen.isEmpty()) {
                    return
                }
                val sortedTransforms = chosen.sortedBy { transform -> transform.parameterIndex }
                val expectedRenamed = buildRenamedFunctionName(original.name, sortedTransforms)
                if (generated.name == original.name || generated.name == expectedRenamed) {
                    resolved += IrOverloadMatch(
                        original = original,
                        parameterTransforms = sortedTransforms,
                    )
                }
                return
            }

            perParameterChoices[index].forEach { choice ->
                if (choice != null) {
                    chosen += choice
                }
                walk(index + 1, chosen)
                if (choice != null) {
                    chosen.removeAt(chosen.lastIndex)
                }
            }
        }
        walk(0, mutableListOf())
        return resolved.singleOrNull()
    }

    private fun resolveParameterTransform(
        converter: IrConverterSpec,
        parameterIndex: Int,
        sourceType: IrType,
        targetType: IrType,
    ): IrParameterTransform? {
        val sourceSimpleType = sourceType as? IrSimpleType
        val targetSimpleType = targetType as? IrSimpleType
        if (sourceSimpleType != null && targetSimpleType != null) {
            val liftKind = sourceSimpleType.toLiftKind()
            if (liftKind != null && liftKind == targetSimpleType.toLiftKind()) {
                if (sourceSimpleType.classifier == targetSimpleType.classifier) {
                    val sourceArgument = sourceSimpleType.arguments.singleOrNull() as? IrTypeProjection
                    val targetArgument = targetSimpleType.arguments.singleOrNull() as? IrTypeProjection
                    if (sourceArgument != null && targetArgument != null) {
                        val bindings = linkedMapOf<IrTypeParameterSymbol, IrType>()
                        if (matchIrType(converter.sourceType, sourceArgument.type, bindings) &&
                            matchIrType(converter.targetType, targetArgument.type, bindings)
                        ) {
                            return IrParameterTransform(
                                converter = converter,
                                parameterIndex = parameterIndex,
                                liftKind = liftKind,
                            )
                        }
                    }
                }
            }
        }

        if (!canDirectConvert(converter, sourceType, targetType)) {
            return null
        }
        return IrParameterTransform(
            converter = converter,
            parameterIndex = parameterIndex,
            liftKind = LiftKind.NONE,
        )
    }

    private fun createDelegatingBody(
        pluginContext: IrPluginContext,
        generated: IrSimpleFunction,
        match: IrOverloadMatch,
        helperSymbols: LiftHelperSymbols,
    ) = DeclarationIrBuilder(pluginContext, generated.symbol).irBlockBody {
        val transformsByIndex = match.parameterTransforms.associateBy { transform -> transform.parameterIndex }
        val originalCall = irCall(match.original.symbol).also { call ->
            generated.dispatchReceiverParameter?.let { receiver ->
                call.dispatchReceiver = irGet(receiver)
            }
            generated.typeParameters.forEachIndexed { index, typeParameter ->
                call.putTypeArgument(index, typeParameter.defaultType)
            }
            generated.valueParameters.forEachIndexed { index, parameter ->
                val transform = transformsByIndex[index]
                val argument = if (transform == null) {
                    irGet(parameter)
                } else {
                    buildConvertedArgument(
                        pluginContext = pluginContext,
                        helperSymbols = helperSymbols,
                        transform = transform,
                        sourceExpression = irGet(parameter),
                        targetType = match.original.valueParameters[index].type,
                    )
                }
                call.putValueArgument(index, argument)
            }
        }

        if (generated.returnType.isUnit()) {
            +originalCall
        } else {
            +irReturn(originalCall)
        }
    }

    private fun IrBuilderWithScope.buildConvertedArgument(
        pluginContext: IrPluginContext,
        helperSymbols: LiftHelperSymbols,
        transform: IrParameterTransform,
        sourceExpression: IrExpression,
        targetType: IrType,
    ): IrExpression {
        return if (transform.liftKind == LiftKind.NONE) {
            buildDirectConverterCall(
                converter = transform.converter,
                sourceExpression = sourceExpression,
                targetType = targetType,
            )
        } else {
            buildLiftedConverterCall(
                pluginContext = pluginContext,
                helperSymbols = helperSymbols,
                transform = transform,
                sourceExpression = sourceExpression,
                targetType = targetType,
            )
        }
    }

    private fun IrBuilderWithScope.buildDirectConverterCall(
        converter: IrConverterSpec,
        sourceExpression: IrExpression,
        targetType: IrType,
    ): IrExpression {
        val call = irCall(converter.function.symbol)
        val typeArguments = matchConverterTypeArguments(
            converter = converter,
            sourceType = sourceExpression.type,
            targetType = targetType,
        )
        converter.function.typeParameters.forEachIndexed { index, typeParameter ->
            call.putTypeArgument(index, typeArguments[typeParameter.symbol])
        }
        buildDispatchReceiver(converter)?.let { receiver ->
            call.dispatchReceiver = receiver
        }
        when (converter.parameterKind) {
            ConverterParameterKind.EXTENSION_RECEIVER -> {
                call.extensionReceiver = sourceExpression
            }

            ConverterParameterKind.VALUE_PARAMETER -> {
                call.putValueArgument(0, sourceExpression)
            }
        }
        return call
    }

    private fun IrBuilderWithScope.buildLiftedConverterCall(
        pluginContext: IrPluginContext,
        helperSymbols: LiftHelperSymbols,
        transform: IrParameterTransform,
        sourceExpression: IrExpression,
        targetType: IrType,
    ): IrExpression {
        val sourceElementType = extractElementType(sourceExpression.type)
            ?: error("Unable to resolve lifted source element type for ${transform.converter.callableIdText}")
        val targetElementType = extractElementType(targetType)
            ?: error("Unable to resolve lifted target element type for ${transform.converter.callableIdText}")
        val helperSymbol = helperSymbols.symbolFor(transform.liftKind)
        val call = irCall(helperSymbol)
        call.putTypeArgument(0, sourceElementType)
        call.putTypeArgument(1, targetElementType)
        call.putValueArgument(0, sourceExpression)
        call.putValueArgument(
            1,
            buildDispatchReceiver(transform.converter) ?: irNull(pluginContext.irBuiltIns.anyNType),
        )
        call.putValueArgument(
            2,
            irString(ownerClassName(transform.converter)),
        )
        call.putValueArgument(3, irString(transform.converter.function.name.asString()))
        return call
    }

    private fun IrBuilderWithScope.buildDispatchReceiver(converter: IrConverterSpec): IrExpression? {
        val parentClass = converter.function.parent as? IrClass ?: return null
        if (parentClass.kind == ClassKind.OBJECT) {
            return irGetObject(parentClass.symbol)
        }
        val constructor = parentClass.declarations
            .filterIsInstance<IrConstructor>()
            .firstOrNull { declaration -> declaration.valueParameters.isEmpty() }
            ?: error(
                "TransformProvider container ${parentClass.name} must be an object or expose a zero-arg constructor",
            )
        return irCall(constructor.symbol)
    }

    private fun extractElementType(type: IrType): IrType? {
        val simpleType = type as? IrSimpleType ?: return null
        val argument = simpleType.arguments.singleOrNull() as? IrTypeProjection ?: return null
        return argument.type
    }

    private fun ownerClassName(converter: IrConverterSpec): String {
        val parentClass = converter.function.parent as? IrClass
        if (parentClass != null) {
            return parentClass.fqNameWhenAvailable?.asString()
                ?: error("Unable to resolve owner class name for ${converter.callableIdText}")
        }
        val parentFile = converter.function.parent as? IrFile
            ?: error("Unable to resolve owner file for ${converter.callableIdText}")
        val fileClassName = File(parentFile.fileEntry.name).nameWithoutExtension + "Kt"
        val packageName = parentFile.packageFqName.asString()
        return if (packageName.isEmpty()) {
            fileClassName
        } else {
            "$packageName.$fileClassName"
        }
    }

    private fun matchConverterTypeArguments(
        converter: IrConverterSpec,
        sourceType: IrType,
        targetType: IrType,
    ): Map<IrTypeParameterSymbol, IrType> {
        val bindings = linkedMapOf<IrTypeParameterSymbol, IrType>()
        if (!matchIrType(converter.sourceType, sourceType, bindings)) {
            return emptyMap()
        }
        if (!matchIrType(converter.targetType, targetType, bindings)) {
            return emptyMap()
        }
        return bindings
    }

    private fun canDirectConvert(
        converter: IrConverterSpec,
        sourceType: IrType,
        targetType: IrType,
    ): Boolean {
        val bindings = linkedMapOf<IrTypeParameterSymbol, IrType>()
        return matchIrType(converter.sourceType, sourceType, bindings) &&
            matchIrType(converter.targetType, targetType, bindings)
    }

    private fun matchIrType(
        pattern: IrType,
        actual: IrType,
        bindings: MutableMap<IrTypeParameterSymbol, IrType>,
    ): Boolean {
        val patternSimpleType = pattern as? IrSimpleType ?: return pattern == actual
        val actualSimpleType = actual as? IrSimpleType ?: return false

        val classifier = patternSimpleType.classifier
        if (classifier is IrTypeParameterSymbol) {
            val existing = bindings[classifier]
            if (existing == null) {
                bindings[classifier] = actual
                return true
            }
            return sameIrType(existing, actual)
        }

        if (patternSimpleType.classifier != actualSimpleType.classifier) {
            return false
        }
        if (patternSimpleType.nullability != actualSimpleType.nullability) {
            return false
        }
        if (patternSimpleType.arguments.size != actualSimpleType.arguments.size) {
            return false
        }

        return patternSimpleType.arguments.zip(actualSimpleType.arguments).all { (patternArg, actualArg) ->
            when {
                patternArg is IrStarProjection && actualArg is IrStarProjection -> true
                patternArg is IrTypeProjection && actualArg is IrTypeProjection -> {
                    patternArg.variance == actualArg.variance &&
                        matchIrType(patternArg.type, actualArg.type, bindings)
                }

                else -> false
            }
        }
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
        return leftSimpleType.arguments.zip(rightSimpleType.arguments).all { (leftArg, rightArg) ->
            when {
                leftArg is IrStarProjection && rightArg is IrStarProjection -> true
                leftArg is IrTypeProjection && rightArg is IrTypeProjection -> {
                    leftArg.variance == rightArg.variance &&
                        sameIrType(leftArg.type, rightArg.type)
                }

                else -> false
            }
        }
    }

    private fun isSupportedOriginalFunction(function: IrSimpleFunction): Boolean {
        if (function.extensionReceiverParameter != null) {
            return false
        }
        if (function.contextReceiverParametersCount > 0) {
            return false
        }
        if (function.hasAnnotation(TransformOverloadPluginKeys.overloadTransformAnnotation)) {
            return false
        }
        if (function.parent !is IrClass && function.visibility == org.jetbrains.kotlin.descriptors.Visibilities.Private) {
            return false
        }
        return true
    }

    private fun implementsTransformProvider(
        irClass: IrClass,
        visited: MutableSet<FqName> = linkedSetOf(),
    ): Boolean {
        val fqName = irClass.fqNameWhenAvailable ?: return false
        if (!visited.add(fqName)) {
            return false
        }
        if (fqName == TransformOverloadPluginKeys.transformProvider) {
            return true
        }
        return irClass.superTypes.any { superType ->
            val owner = superType.classifierOrNull?.owner as? IrClass ?: return@any false
            owner.fqNameWhenAvailable == TransformOverloadPluginKeys.transformProvider ||
                implementsTransformProvider(owner, visited)
        }
    }

    private fun buildRenamedFunctionName(
        originalName: Name,
        transforms: List<IrParameterTransform>,
    ): Name {
        val suffix = transforms.joinToString(separator = "And") { transform ->
            transform.converter.uniqueSuffix
        }
        return Name.identifier("${originalName.asString()}Via$suffix")
    }

    private fun IrSimpleType.toLiftKind(): LiftKind? {
        val fqName = (classifier as? org.jetbrains.kotlin.ir.symbols.IrClassSymbol)
            ?.owner
            ?.fqNameWhenAvailable
            ?.asString()
            ?: return null
        return when (fqName) {
            "kotlin.collections.Iterable" -> LiftKind.ITERABLE
            "kotlin.collections.Collection" -> LiftKind.COLLECTION
            "kotlin.collections.List" -> LiftKind.LIST
            "kotlin.collections.Set" -> LiftKind.SET
            "kotlin.sequences.Sequence" -> LiftKind.SEQUENCE
            else -> null
        }
    }

    private fun isGeneratedByThisPlugin(function: IrSimpleFunction): Boolean {
        val origin = function.origin
        return origin is IrDeclarationOrigin.GeneratedByPlugin &&
            origin.pluginKey == TransformOverloadGeneratedDeclarationKey
    }

    private fun IrDeclaration.hasAnnotation(annotationFqName: FqName): Boolean {
        return annotations.any { annotation ->
            (annotation.type.classifierOrNull?.owner as? IrClass)?.fqNameWhenAvailable == annotationFqName
        }
    }

    private fun invalidConverter(
        function: IrSimpleFunction,
        reason: String,
    ): Nothing {
        val fqName = function.fqNameWhenAvailable?.asString() ?: function.name.asString()
        throw IllegalStateException("Invalid @OverloadTransform converter $fqName: $reason")
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

    private data class RawIrConverterSpec(
        val function: IrSimpleFunction,
        val parameterKind: ConverterParameterKind,
        val sourceType: IrType,
        val targetType: IrType,
        val typeParameters: List<org.jetbrains.kotlin.ir.declarations.IrTypeParameter>,
        val defaultSuffix: String,
        val containerPrefix: String,
        val callableIdText: String,
        val supportsMemberContainer: Boolean,
    )

    private class LiftHelperSymbols(
        private val pluginContext: IrPluginContext,
    ) {
        private val cache = linkedMapOf<LiftKind, org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol>()

        fun symbolFor(liftKind: LiftKind): org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol {
            if (liftKind == LiftKind.NONE) {
                error("NONE does not have a lifting helper")
            }
            return cache.getOrPut(liftKind) {
                val fqName = when (liftKind) {
                    LiftKind.ITERABLE -> TransformOverloadPluginKeys.iterableLiftFunction
                    LiftKind.COLLECTION -> TransformOverloadPluginKeys.collectionLiftFunction
                    LiftKind.LIST -> TransformOverloadPluginKeys.listLiftFunction
                    LiftKind.SET -> TransformOverloadPluginKeys.setLiftFunction
                    LiftKind.SEQUENCE -> TransformOverloadPluginKeys.sequenceLiftFunction
                    LiftKind.NONE -> error("NONE does not have a lifting helper")
                }
                pluginContext.referenceFunctions(toCallableId(fqName)).singleOrNull()
                    ?: error("Missing transform-overload lifting helper $fqName on compiler classpath")
            }
        }

        private fun toCallableId(fqName: FqName): CallableId {
            return CallableId(fqName.parent(), fqName.shortName())
        }
    }
}
