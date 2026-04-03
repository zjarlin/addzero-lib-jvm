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
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrThrow
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private data class IrCarrierMetadata(
    val irClass: IrClass,
    val primaryConstructor: org.jetbrains.kotlin.ir.declarations.IrConstructor,
)

private data class IrFlattenedFieldSpec(
    val name: Name,
    val type: IrType,
)

private data class IrSpreadArgsReference(
    val functionFqName: String,
    val parameterTypeClassIds: List<ClassId>,
)

@OptIn(
    DeprecatedForRemovalCompilerApi::class,
    UnsafeDuringIrConstructionAPI::class,
)
class SpreadPackIrGenerationExtension : IrGenerationExtension {

    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        val generatedMarkerConstructor = pluginContext.referenceClass(
            SpreadPackPluginKeys.generatedSpreadPackOverloadAnnotationClassId,
        )?.owner
            ?.declarations
            ?.filterIsInstance<IrConstructor>()
            ?.singleOrNull()
        moduleFragment.files.forEach { file ->
            processClasses(file, pluginContext, generatedMarkerConstructor?.symbol)
        }
        processTopLevelPackages(moduleFragment, pluginContext, generatedMarkerConstructor?.symbol)
    }

    private fun processTopLevelPackages(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
        generatedMarkerConstructor: IrConstructorSymbol? = null,
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
                    generatedMarkerConstructor = generatedMarkerConstructor,
                )
            }
    }

    private fun processClasses(
        declaration: IrElement,
        pluginContext: IrPluginContext,
        generatedMarkerConstructor: IrConstructorSymbol?,
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
                    generatedMarkerConstructor = generatedMarkerConstructor,
                )
                declaration.acceptChildrenVoid(this)
            }
        })
    }

    private fun processFunctionContainer(
        functions: List<IrSimpleFunction>,
        processWholeClass: Boolean,
        pluginContext: IrPluginContext,
        generatedMarkerConstructor: IrConstructorSymbol?,
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
            val match = resolveMatch(candidate, originals, pluginContext) ?: return@forEach
            generatedMarkerConstructor?.let { constructor ->
                annotateGeneratedOverload(candidate, match.original, pluginContext, constructor)
            }
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
        pluginContext: IrPluginContext,
    ): IrSpreadPackMatch? {
        val matches = originals.mapNotNull { original ->
            resolveAgainstOriginal(generated, original, pluginContext)
        }
        if (matches.isEmpty()) {
            return null
        }
        return matches.first()
    }

    private fun resolveAgainstOriginal(
        generated: IrSimpleFunction,
        original: IrSimpleFunction,
        pluginContext: IrPluginContext,
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
            createExpansion(original, index, parameter, pluginContext)
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
        pluginContext: IrPluginContext,
    ): IrSpreadPackExpansion? {
        val spreadPackAnnotation = parameter.getSpreadPackAnnotation() ?: return null
        val carrier = resolveCarrierMetadata(owner, parameter)
        val spreadArgsAnnotation = parameter.getSpreadArgsOfAnnotation()
        val selectorKind = spreadArgsAnnotation?.selectorKind()
            ?: spreadPackAnnotation.selectorKind()
        val excludedNames = spreadArgsAnnotation?.excludedNames()
            ?: spreadPackAnnotation.excludedNames()
        if (spreadArgsAnnotation != null && !spreadPackAnnotation.isDefaultSpreadPackConfiguration()) {
            invalidTarget(
                owner,
                "parameter ${parameter.name.asString()} must keep @SpreadPack at default values when @SpreadArgsOf is present",
            )
        }

        val fields = if (spreadArgsAnnotation == null) {
            buildCarrierFields(
                owner = owner,
                carrier = carrier,
                selectorKind = selectorKind,
                excludedNames = excludedNames,
            )
        } else {
            val referencedOverload = resolveReferencedOverload(owner, spreadArgsAnnotation, pluginContext)
            val overloadKey = overloadKey(referencedOverload)
            val flattenedFields = flattenFunctionParameters(
                owner = owner,
                function = referencedOverload,
                pluginContext = pluginContext,
                visitedOverloads = linkedSetOf(overloadKey),
            )
            buildReferencedCarrierFields(
                owner = owner,
                carrier = carrier,
                flattenedFields = flattenedFields,
                selectorKind = selectorKind,
                excludedNames = excludedNames,
                referenceDescription = referencedOverload.fqNameWhenAvailable?.asString() ?: referencedOverload.name.asString(),
            )
        }

        return IrSpreadPackExpansion(
            parameterIndex = parameterIndex,
            carrierClass = carrier.irClass,
            constructor = carrier.primaryConstructor,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            fields = fields,
        )
    }

    private fun resolveCarrierMetadata(
        owner: IrSimpleFunction,
        parameter: IrValueParameter,
    ): IrCarrierMetadata {
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
        return IrCarrierMetadata(
            irClass = carrierClass,
            primaryConstructor = primaryConstructor,
        )
    }

    private fun buildCarrierFields(
        owner: IrSimpleFunction,
        carrier: IrCarrierMetadata,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
    ): List<IrSpreadPackField> {
        val constructorParameters = carrier.primaryConstructor.valueParameters
        validateExcludedNames(
            owner = owner,
            excludedNames = excludedNames,
            availableNames = constructorParameters.map { constructorParameter -> constructorParameter.name.asString() },
            contextLabel = carrier.irClass.name.asString(),
        )
        val selectedParameters = constructorParameters.filter { constructorParameter ->
            shouldIncludeField(constructorParameter, selectorKind) &&
                constructorParameter.name.asString() !in excludedNames
        }
        validateCarrierOmissions(
            owner = owner,
            carrier = carrier,
            selectedCarrierNames = selectedParameters.map { constructorParameter -> constructorParameter.name.asString() }.toSet(),
        )
        return selectedParameters.map { constructorParameter ->
            IrSpreadPackField(
                name = constructorParameter.name,
                type = constructorParameter.type,
                constructorIndex = carrier.primaryConstructor.valueParameters.indexOf(constructorParameter),
            )
        }
    }

    private fun buildReferencedCarrierFields(
        owner: IrSimpleFunction,
        carrier: IrCarrierMetadata,
        flattenedFields: List<IrFlattenedFieldSpec>,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
        referenceDescription: String,
    ): List<IrSpreadPackField> {
        val selectedFields = selectFlattenedFields(
            owner = owner,
            fields = flattenedFields,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            contextLabel = referenceDescription,
        )
        val carrierParametersByName = carrier.primaryConstructor.valueParameters.associateBy { constructorParameter ->
            constructorParameter.name.asString()
        }
        val selectedCarrierNames = linkedSetOf<String>()
        return selectedFields.map { field ->
            val fieldName = field.name.asString()
            val carrierParameter = carrierParametersByName[fieldName]
                ?: invalidTarget(
                    owner,
                    "spread-pack carrier ${carrier.irClass.name.asString()} is missing argsof field $fieldName from $referenceDescription",
                )
            if (!sameIrType(carrierParameter.type, field.type)) {
                invalidTarget(
                    owner,
                    "spread-pack carrier ${carrier.irClass.name.asString()} field $fieldName type ${carrierParameter.type} " +
                        "does not match $referenceDescription field type ${field.type}",
                )
            }
            selectedCarrierNames += fieldName
            IrSpreadPackField(
                name = carrierParameter.name,
                type = field.type,
                constructorIndex = carrier.primaryConstructor.valueParameters.indexOf(carrierParameter),
            )
        }.also { fields ->
            validateCarrierOmissions(
                owner = owner,
                carrier = carrier,
                selectedCarrierNames = selectedCarrierNames,
            )
            validateUniqueFieldNames(
                owner = owner,
                names = fields.map { field -> field.name.asString() },
                contextLabel = referenceDescription,
            )
        }
    }

    private fun flattenFunctionParameters(
        owner: IrSimpleFunction,
        function: IrSimpleFunction,
        pluginContext: IrPluginContext,
        visitedOverloads: Set<String>,
    ): List<IrFlattenedFieldSpec> {
        if (!isSupportedReferencedFunction(function)) {
            invalidTarget(
                owner,
                "argsof target ${function.fqNameWhenAvailable?.asString() ?: function.name.asString()} must not declare receivers or context parameters",
            )
        }
        return function.valueParameters.flatMap { referencedParameter ->
            flattenValueParameter(
                owner = owner,
                parameter = referencedParameter,
                pluginContext = pluginContext,
                visitedOverloads = visitedOverloads,
            )
        }.also { fields ->
            validateUniqueFieldNames(
                owner = owner,
                names = fields.map { field -> field.name.asString() },
                contextLabel = function.fqNameWhenAvailable?.asString() ?: function.name.asString(),
            )
        }
    }

    private fun flattenValueParameter(
        owner: IrSimpleFunction,
        parameter: IrValueParameter,
        pluginContext: IrPluginContext,
        visitedOverloads: Set<String>,
    ): List<IrFlattenedFieldSpec> {
        val spreadPackAnnotation = parameter.getSpreadPackAnnotation()
            ?: return listOf(
                IrFlattenedFieldSpec(
                    name = parameter.name,
                    type = parameter.type,
                ),
            )
        val carrier = resolveCarrierMetadata(owner, parameter)
        val spreadArgsAnnotation = parameter.getSpreadArgsOfAnnotation()
        val selectorKind = spreadArgsAnnotation?.selectorKind()
            ?: spreadPackAnnotation.selectorKind()
        val excludedNames = spreadArgsAnnotation?.excludedNames()
            ?: spreadPackAnnotation.excludedNames()
        if (spreadArgsAnnotation != null && !spreadPackAnnotation.isDefaultSpreadPackConfiguration()) {
            invalidTarget(
                owner,
                "nested spread-pack parameter ${parameter.name.asString()} must keep @SpreadPack at default values when @SpreadArgsOf is present",
            )
        }
        if (spreadArgsAnnotation == null) {
            return buildCarrierFields(
                owner = owner,
                carrier = carrier,
                selectorKind = selectorKind,
                excludedNames = excludedNames,
            ).map { field ->
                IrFlattenedFieldSpec(
                    name = field.name,
                    type = field.type,
                )
            }
        }

        val referencedOverload = resolveReferencedOverload(owner, spreadArgsAnnotation, pluginContext)
        val overloadKey = overloadKey(referencedOverload)
        if (overloadKey in visitedOverloads) {
            invalidTarget(
                owner,
                "detected argsof overload cycle at ${referencedOverload.fqNameWhenAvailable?.asString() ?: referencedOverload.name.asString()}",
            )
        }
        val flattenedFields = flattenFunctionParameters(
            owner = owner,
            function = referencedOverload,
            pluginContext = pluginContext,
            visitedOverloads = visitedOverloads + overloadKey,
        )
        return buildReferencedCarrierFields(
            owner = owner,
            carrier = carrier,
            flattenedFields = flattenedFields,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            referenceDescription = referencedOverload.fqNameWhenAvailable?.asString() ?: referencedOverload.name.asString(),
        ).map { field ->
            IrFlattenedFieldSpec(
                name = field.name,
                type = field.type,
            )
        }
    }

    private fun selectFlattenedFields(
        owner: IrSimpleFunction,
        fields: List<IrFlattenedFieldSpec>,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
        contextLabel: String,
    ): List<IrFlattenedFieldSpec> {
        validateExcludedNames(
            owner = owner,
            excludedNames = excludedNames,
            availableNames = fields.map { field -> field.name.asString() },
            contextLabel = contextLabel,
        )
        return fields.filter { field ->
            shouldIncludeField(field.type, selectorKind) &&
                field.name.asString() !in excludedNames
        }
    }

    private fun validateExcludedNames(
        owner: IrSimpleFunction,
        excludedNames: Set<String>,
        availableNames: List<String>,
        contextLabel: String,
    ) {
        val unknownExcludedNames = excludedNames - availableNames.toSet()
        if (unknownExcludedNames.isNotEmpty()) {
            invalidTarget(
                owner,
                "unknown spread-pack exclude names for $contextLabel: ${unknownExcludedNames.sorted().joinToString()}",
            )
        }
    }

    private fun validateCarrierOmissions(
        owner: IrSimpleFunction,
        carrier: IrCarrierMetadata,
        selectedCarrierNames: Set<String>,
    ) {
        carrier.primaryConstructor.valueParameters.forEach { constructorParameter ->
            if (constructorParameter.name.asString() !in selectedCarrierNames && constructorParameter.defaultValue == null) {
                invalidTarget(
                    owner,
                    "spread-pack carrier ${carrier.irClass.name.asString()} cannot omit required field " +
                        constructorParameter.name.asString(),
                )
            }
        }
    }

    private fun validateUniqueFieldNames(
        owner: IrSimpleFunction,
        names: List<String>,
        contextLabel: String,
    ) {
        val duplicates = names.groupingBy { it }.eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .sorted()
        if (duplicates.isNotEmpty()) {
            invalidTarget(
                owner,
                "duplicate flattened parameter names in $contextLabel: ${duplicates.joinToString()}",
            )
        }
    }

    private fun resolveReferencedOverload(
        owner: IrSimpleFunction,
        annotation: IrConstructorCall,
        pluginContext: IrPluginContext,
    ): IrSimpleFunction {
        val reference = annotation.spreadArgsReference()
        val overloads = resolveFunctionSymbolsByFqName(reference.functionFqName, pluginContext)
            .filter(::isResolvableReferencedFunction)
        if (overloads.isEmpty()) {
            invalidTarget(
                owner,
                "unable to resolve argsof overload set ${reference.functionFqName}",
            )
        }
        if (reference.parameterTypeClassIds.isEmpty()) {
            if (overloads.size != 1) {
                invalidTarget(
                    owner,
                    "argsof overload set ${reference.functionFqName} is ambiguous; specify SpreadOverload.parameterTypes",
                )
            }
            return overloads.single()
        }
        val matches = overloads.filter { overload ->
            overload.valueParameters.map { valueParameter ->
                erasureClassId(valueParameter.type)
                    ?: invalidTarget(
                        owner,
                        "argsof overload ${overload.fqNameWhenAvailable?.asString() ?: overload.name.asString()} has unsupported parameter type ${valueParameter.type}",
                    )
            } == reference.parameterTypeClassIds
        }
        if (matches.size != 1) {
            invalidTarget(
                owner,
                "unable to select a unique argsof overload for ${reference.functionFqName} with parameterTypes=" +
                    reference.parameterTypeClassIds.joinToString { classId -> classId.asString() },
            )
        }
        return matches.single()
    }

    private fun resolveFunctionSymbolsByFqName(
        functionFqName: String,
        pluginContext: IrPluginContext,
    ): List<IrSimpleFunction> {
        val fqName = FqName(functionFqName)
        val topLevelFunctions = pluginContext.referenceFunctions(
            CallableId(fqName.parent(), fqName.shortName()),
        ).map { symbol -> symbol.owner }
        if (topLevelFunctions.isNotEmpty()) {
            return topLevelFunctions
        }

        val pathSegments = fqName.pathSegments()
        if (pathSegments.size < 2) {
            return emptyList()
        }
        val functionName = pathSegments.last()
        for (packageSize in pathSegments.size - 2 downTo 0) {
            val packageFqName = FqName.fromSegments(pathSegments.take(packageSize).map { segment -> segment.asString() })
            val classSegments = pathSegments
                .subList(packageSize, pathSegments.lastIndex)
                .map { segment -> segment.asString() }
            if (classSegments.isEmpty()) {
                continue
            }
            val classId = ClassId(
                packageFqName,
                FqName.fromSegments(classSegments),
                false,
            )
            val classSymbol = pluginContext.referenceClass(classId) ?: continue
            val memberFunctions = classSymbol.owner.declarations
                .filterIsInstance<IrSimpleFunction>()
                .filter { function -> function.name == functionName }
            if (memberFunctions.isNotEmpty()) {
                return memberFunctions
            }
        }
        return emptyList()
    }

    private fun isResolvableReferencedFunction(
        function: IrSimpleFunction,
    ): Boolean {
        return !isGeneratedByThisPlugin(function)
    }

    private fun isSupportedReferencedFunction(
        function: IrSimpleFunction,
    ): Boolean {
        return function.extensionReceiverParameter == null &&
            function.contextReceiverParametersCount == 0
    }

    private fun overloadKey(
        function: IrSimpleFunction,
    ): String {
        return buildString {
            append(function.fqNameWhenAvailable?.asString() ?: function.name.asString())
            append("|")
            function.valueParameters.forEach { valueParameter ->
                append(jvmErasure(valueParameter.type))
                append(";")
            }
        }
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
        return shouldIncludeField(parameter.type, selectorKind)
    }

    private fun shouldIncludeField(
        type: org.jetbrains.kotlin.ir.types.IrType,
        selectorKind: SelectorKind,
    ): Boolean {
        return when (selectorKind) {
            SelectorKind.PROPS -> true
            SelectorKind.ATTRS -> !isFunctionLike(type)
            SelectorKind.CALLBACKS -> isFunctionLike(type)
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

    private fun annotateGeneratedOverload(
        generated: IrSimpleFunction,
        original: IrSimpleFunction,
        pluginContext: IrPluginContext,
        constructor: IrConstructorSymbol,
    ) {
        if (generated.hasAnnotation(SpreadPackPluginKeys.generatedSpreadPackOverloadAnnotation)) {
            return
        }
        val sourceFunctionFqName = original.fqNameWhenAvailable?.asString() ?: return
        val annotation = IrConstructorCallImpl(
            generated.startOffset,
            generated.endOffset,
            constructor.owner.returnType,
            constructor,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
        ).apply {
            putValueArgument(
                0,
                IrConstImpl.string(
                    generated.startOffset,
                    generated.endOffset,
                    pluginContext.irBuiltIns.stringType,
                    sourceFunctionFqName,
                ),
            )
        }
        generated.annotations += annotation
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

    private fun IrValueParameter.getSpreadArgsOfAnnotation(): IrConstructorCall? {
        return annotations.firstOrNull { annotation ->
            (annotation.type.classifierOrNull?.owner as? IrClass)?.fqNameWhenAvailable ==
                SpreadPackPluginKeys.spreadArgsOfAnnotation
        }
    }

    private fun IrConstructorCall.excludedNames(): Set<String> {
        val rawArgument = getValueArgument(Name.identifier("exclude")) ?: return emptySet()
        return when (rawArgument) {
            is IrVararg -> rawArgument.elements.mapNotNull { element ->
                (element as? IrConst)?.value as? String
            }.toSet()

            is IrConst -> listOfNotNull(rawArgument.value as? String).toSet()
            else -> emptySet()
        }
    }

    private fun IrConstructorCall.selectorKind(): SelectorKind {
        val rawArgument = getValueArgument(Name.identifier("selector")) as? IrGetEnumValue
            ?: return SelectorKind.PROPS
        return when (rawArgument.symbol.owner.name.asString()) {
            SelectorKind.ATTRS.name -> SelectorKind.ATTRS
            SelectorKind.CALLBACKS.name -> SelectorKind.CALLBACKS
            else -> SelectorKind.PROPS
        }
    }

    private fun IrConstructorCall.spreadArgsReference(): IrSpreadArgsReference {
        val overloadAnnotation = getValueArgument(Name.identifier("overload")) as? IrConstructorCall
            ?: error("SpreadArgsOf.overload must be an annotation value")
        val overloadsAnnotation = overloadAnnotation.getValueArgument(Name.identifier("of")) as? IrConstructorCall
            ?: error("SpreadOverload.of must be an annotation value")
        val functionFqName = (overloadsAnnotation.getValueArgument(Name.identifier("functionFqName")) as? IrConst)
            ?.value as? String
            ?: error("SpreadOverloadsOf.functionFqName must be a string literal")
        val parameterTypeClassIds = overloadAnnotation.classIdArguments(Name.identifier("parameterTypes"))
        return IrSpreadArgsReference(
            functionFqName = functionFqName,
            parameterTypeClassIds = parameterTypeClassIds,
        )
    }

    private fun IrConstructorCall.classIdArguments(
        name: Name,
    ): List<ClassId> {
        val rawArgument = getValueArgument(name) ?: return emptyList()
        return when (rawArgument) {
            is IrVararg -> rawArgument.elements.mapNotNull { element ->
                (element as? IrClassReference)?.classId()
            }

            is IrClassReference -> listOfNotNull(rawArgument.classId())
            else -> emptyList()
        }
    }

    private fun IrClassReference.classId(): ClassId? {
        val irClass = classType.classifierOrNull?.owner as? IrClass ?: return null
        return irClass.classId()
    }

    private fun IrConstructorCall.isDefaultSpreadPackConfiguration(): Boolean {
        return excludedNames().isEmpty() && selectorKind() == SelectorKind.PROPS
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

    private fun erasureClassId(
        type: org.jetbrains.kotlin.ir.types.IrType,
    ): ClassId? {
        return (type as? IrSimpleType)
            ?.classifierOrNull
            ?.owner
            ?.let { declaration -> declaration as? IrClass }
            ?.classId()
    }

    private fun jvmErasure(
        type: org.jetbrains.kotlin.ir.types.IrType,
    ): String {
        return erasureClassId(type)?.asString() ?: type.toString()
    }

    private fun IrClass.classId(): ClassId? {
        val parentClass = parent as? IrClass
        if (parentClass != null) {
            return parentClass.classId()?.createNestedClassId(name)
        }
        val fqName = fqNameWhenAvailable ?: return null
        return ClassId.topLevel(fqName)
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
