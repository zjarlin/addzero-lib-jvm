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
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrErrorExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
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
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private data class IrCarrierMetadata(
    val irClass: IrClass,
    val primaryConstructor: org.jetbrains.kotlin.ir.declarations.IrConstructor,
    val generatedProperties: List<IrProperty> = emptyList(),
)

private data class IrFlattenedFieldSpec(
    val name: Name,
    val type: IrType,
    val defaultValue: IrExpressionBody?,
)

private data class IrCarrierDeclaredField(
    val name: Name,
    val type: IrType,
    val constructorIndex: Int,
    val defaultValue: IrExpressionBody?,
)

private data class IrSpreadArgsReference(
    val functionFqName: String,
    val parameterTypeClassIds: List<ClassId>,
)

private val composableAnnotationFqName = FqName("androidx.compose.runtime.Composable")

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
                lowerAnnotatedCarrierClass(declaration, pluginContext)
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
        val candidates = functions.filter { function ->
            isGeneratedByThisPlugin(function) || hasStubBody(function)
        }
        if (candidates.isEmpty()) {
            return
        }

        candidates.forEach { candidate ->
            val match = resolveMatch(candidate, originals, pluginContext)
                ?: resolveMatchFromGeneratedAnnotation(candidate, pluginContext)
                ?: return@forEach
            if (candidate.startOffset < 0) {
                candidate.startOffset = match.original.startOffset
            }
            if (candidate.endOffset < 0) {
                candidate.endOffset = match.original.endOffset
            }
            generatedMarkerConstructor?.let { constructor ->
                annotateGeneratedOverload(candidate, match.original, pluginContext, constructor)
            }
            rewriteGeneratedParameterDefaults(candidate, match)
            candidate.body = createDelegatingBody(pluginContext, candidate, match)
        }
    }

    private fun lowerAnnotatedCarrierClass(
        declaration: IrClass,
        pluginContext: IrPluginContext,
    ) {
        val generatedPrimaryConstructor = declaration.declarations
            .filterIsInstance<IrConstructor>()
            .firstOrNull { constructor ->
                constructor.isGeneratedBySpreadPackPlugin()
            } ?: return
        val generatedProperties = declaration.declarations
            .filterIsInstance<IrProperty>()
            .filter { property -> property.isGeneratedBySpreadPackPlugin() }
        if (generatedProperties.isEmpty()) {
            return
        }
        val parametersByName = generatedPrimaryConstructor.valueParameters.associateBy { parameter ->
            parameter.name.asString()
        }
        val existingStatements = (generatedPrimaryConstructor.body as? IrBlockBody)
            ?.statements
            .orEmpty()
            .toList()
        generatedPrimaryConstructor.body = DeclarationIrBuilder(pluginContext, generatedPrimaryConstructor.symbol).irBlockBody {
            existingStatements.forEach { statement ->
                +statement
            }
            val thisReceiver = declaration.thisReceiver ?: return@irBlockBody
            generatedProperties.forEach { property ->
                val backingField = property.backingField ?: return@forEach
                val parameter = parametersByName[property.name.asString()]
                    ?: invalidCarrierTarget(
                        declaration,
                        "missing constructor parameter for generated property ${property.name.asString()}",
                    )
                +irSetField(
                    receiver = irGet(thisReceiver),
                    field = backingField,
                    value = irGet(parameter),
                )
            }
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
        return function.valueParameters.any { parameter ->
            parameter.getSpreadPackAnnotation() != null || parameter.getSpreadPackOfAnnotation() != null
        }
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

    private fun resolveMatchFromGeneratedAnnotation(
        generated: IrSimpleFunction,
        pluginContext: IrPluginContext,
    ): IrSpreadPackMatch? {
        val annotation = generated.getGeneratedSpreadPackOverloadAnnotation() ?: return null
        val sourceFunctionFqName = annotation.generatedSpreadPackSourceFunctionFqName() ?: return null
        val originals = resolveFunctionSymbolsByFqName(sourceFunctionFqName, pluginContext)
            .filter(::isSupportedOriginalFunction)
        if (originals.isEmpty()) {
            return null
        }
        return originals.firstNotNullOfOrNull { original ->
            resolveAgainstOriginal(generated, original, pluginContext)
        }
    }

    private fun resolveAgainstOriginal(
        generated: IrSimpleFunction,
        original: IrSimpleFunction,
        pluginContext: IrPluginContext,
    ): IrSpreadPackMatch? {
        val originalName = original.name.asString()
        val generatedName = generated.name.asString()
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
        if (!matchesGeneratedName(generatedName, originalName, expectedRenamed.asString())) {
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
        val spreadPackOfAnnotation = parameter.getSpreadPackOfAnnotation()
        val spreadPackAnnotation = parameter.getSpreadPackAnnotation()
        if (spreadPackAnnotation == null && spreadPackOfAnnotation == null) {
            return null
        }
        val carrier = resolveCarrierMetadata(owner, parameter)
        if (spreadPackOfAnnotation != null) {
            if (spreadPackAnnotation != null) {
                invalidTarget(
                    owner,
                    "parameter ${parameter.name.asString()} cannot combine @SpreadPackOf with @SpreadPack",
                )
            }
            if (parameter.getSpreadArgsOfAnnotation() != null) {
                invalidTarget(
                    owner,
                    "parameter ${parameter.name.asString()} cannot combine @SpreadPackOf with @SpreadArgsOf",
                )
            }
            return IrSpreadPackExpansion(
                parameterIndex = parameterIndex,
                carrierClass = carrier.irClass,
                constructor = carrier.primaryConstructor,
                selectorKind = SelectorKind.PROPS,
                excludedNames = emptySet(),
                fields = buildCarrierFields(
                    owner = owner,
                    carrier = carrier,
                    selectorKind = SelectorKind.PROPS,
                    excludedNames = emptySet(),
                    pluginContext = pluginContext,
                ),
            )
        }
        val baseSpreadPackAnnotation = spreadPackAnnotation
            ?: error("spread-pack annotation missing for ${parameter.name.asString()}")
        val spreadArgsAnnotation = parameter.getSpreadArgsOfAnnotation()
        val selectorKind = spreadArgsAnnotation?.selectorKind()
            ?: baseSpreadPackAnnotation.selectorKind()
        val excludedNames = spreadArgsAnnotation?.excludedNames()
            ?: baseSpreadPackAnnotation.excludedNames()
        if (spreadArgsAnnotation != null && !baseSpreadPackAnnotation.isDefaultSpreadPackConfiguration()) {
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
                pluginContext = pluginContext,
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
                pluginContext = pluginContext,
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
        if (carrierClass.getSpreadPackCarrierOfAnnotation() != null) {
            val generatedProperties = carrierClass.declarations
                .filterIsInstance<IrProperty>()
                .filter { property -> property.isGeneratedBySpreadPackPlugin() }
            val generatedConstructor = carrierClass.declarations
                .filterIsInstance<org.jetbrains.kotlin.ir.declarations.IrConstructor>()
                .firstOrNull { constructor -> constructor.isGeneratedBySpreadPackPlugin() }
                ?: invalidTarget(
                    owner,
                    "annotated spread-pack carrier ${carrierClass.name.asString()} is missing generated constructor",
                )
            return IrCarrierMetadata(
                irClass = carrierClass,
                primaryConstructor = generatedConstructor,
                generatedProperties = generatedProperties,
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
        pluginContext: IrPluginContext,
    ): List<IrSpreadPackField> {
        val declaredFields = carrierDeclaredFields(owner, carrier, pluginContext)
        validateExcludedNames(
            owner = owner,
            excludedNames = excludedNames,
            availableNames = declaredFields.map { declaredField -> declaredField.name.asString() },
            contextLabel = carrier.irClass.name.asString(),
        )
        val selectedFields = declaredFields.filter { declaredField ->
            shouldIncludeField(declaredField.type, selectorKind) &&
                declaredField.name.asString() !in excludedNames
        }
        validateCarrierOmissions(
            owner = owner,
            carrier = carrier,
            selectedCarrierNames = selectedFields.map { declaredField -> declaredField.name.asString() }.toSet(),
        )
        return selectedFields.map { declaredField ->
            IrSpreadPackField(
                name = declaredField.name,
                type = declaredField.type,
                constructorIndex = declaredField.constructorIndex,
                defaultValue = declaredField.defaultValue,
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
        pluginContext: IrPluginContext,
    ): List<IrSpreadPackField> {
        val selectedFields = selectFlattenedFields(
            owner = owner,
            fields = flattenedFields,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            contextLabel = referenceDescription,
        )
        val carrierFieldsByName = carrierDeclaredFields(owner, carrier, pluginContext).associateBy { declaredField ->
            declaredField.name.asString()
        }
        val selectedCarrierNames = linkedSetOf<String>()
        return selectedFields.map { field ->
            val fieldName = field.name.asString()
            val carrierField = carrierFieldsByName[fieldName]
                ?: invalidTarget(
                    owner,
                    "spread-pack carrier ${carrier.irClass.name.asString()} is missing argsof field $fieldName from $referenceDescription",
                )
            if (!sameIrType(carrierField.type, field.type)) {
                invalidTarget(
                    owner,
                    "spread-pack carrier ${carrier.irClass.name.asString()} field $fieldName type ${carrierField.type} " +
                        "does not match $referenceDescription field type ${field.type}",
                )
            }
            selectedCarrierNames += fieldName
            IrSpreadPackField(
                name = carrierField.name,
                type = field.type,
                constructorIndex = carrierField.constructorIndex,
                defaultValue = carrierField.defaultValue ?: field.defaultValue,
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
                    defaultValue = parameter.defaultValue,
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
                pluginContext = pluginContext,
            ).map { field ->
                IrFlattenedFieldSpec(
                    name = field.name,
                    type = field.type,
                    defaultValue = field.defaultValue,
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
            pluginContext = pluginContext,
        ).map { field ->
            IrFlattenedFieldSpec(
                name = field.name,
                type = field.type,
                defaultValue = field.defaultValue,
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
        if (carrier.generatedProperties.isNotEmpty()) {
            return
        }
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

    private fun carrierDeclaredFields(
        owner: IrSimpleFunction,
        carrier: IrCarrierMetadata,
        pluginContext: IrPluginContext,
    ): List<IrCarrierDeclaredField> {
        if (carrier.generatedProperties.isNotEmpty()) {
            val carrierAnnotation = carrier.irClass.getSpreadPackCarrierOfAnnotation()
            if (carrierAnnotation != null) {
                val referencedOverload = resolveReferencedOverload(owner, carrierAnnotation, pluginContext)
                val overloadKey = overloadKey(referencedOverload)
                val flattenedFields = flattenFunctionParameters(
                    owner = owner,
                    function = referencedOverload,
                    pluginContext = pluginContext,
                    visitedOverloads = linkedSetOf(overloadKey),
                )
                val selectedFields = selectFlattenedFields(
                    owner = owner,
                    fields = flattenedFields,
                    selectorKind = carrierAnnotation.selectorKind(),
                    excludedNames = carrierAnnotation.excludedNames(),
                    contextLabel = referencedOverload.fqNameWhenAvailable?.asString() ?: referencedOverload.name.asString(),
                )
                val generatedConstructor = carrier.irClass.declarations
                    .filterIsInstance<IrConstructor>()
                    .firstOrNull { constructor ->
                        constructor.isGeneratedBySpreadPackPlugin()
                    }
                    ?: invalidTarget(
                        owner,
                        "annotated spread-pack carrier ${carrier.irClass.name.asString()} is missing generated constructor",
                    )
                val constructorParameterIndexByName = generatedConstructor.valueParameters
                    .mapIndexed { index, parameter -> parameter.name.asString() to index }
                    .toMap()
                val generatedDefaultsByName = generatedConstructor.valueParameters
                    .associateBy({ parameter -> parameter.name.asString() }, { parameter -> parameter.defaultValue })
                val generatedPropertiesByName = carrier.generatedProperties.associateBy { property ->
                    property.name.asString()
                }
                return selectedFields.map { field ->
                    val property = generatedPropertiesByName[field.name.asString()]
                        ?: invalidTarget(
                            owner,
                            "annotated spread-pack carrier ${carrier.irClass.name.asString()} is missing generated property ${field.name.asString()}",
                        )
                    val fieldType = property.backingField?.type ?: property.getter?.returnType
                        ?: error("Generated spread-pack carrier property ${property.name.asString()} is missing a readable type")
                    if (!sameIrType(fieldType, field.type)) {
                        invalidTarget(
                            owner,
                            "annotated spread-pack carrier ${carrier.irClass.name.asString()} field ${field.name.asString()} " +
                                "type ${render(fieldType)} does not match source field type ${render(field.type)}",
                        )
                    }
                    IrCarrierDeclaredField(
                        name = property.name,
                        type = fieldType,
                        constructorIndex = constructorParameterIndexByName[field.name.asString()]
                            ?: invalidTarget(
                                owner,
                                "annotated spread-pack carrier ${carrier.irClass.name.asString()} is missing constructor parameter ${field.name.asString()}",
                            ),
                        defaultValue = generatedDefaultsByName[field.name.asString()]?.deepCopyWithSymbols()
                            ?: field.defaultValue,
                    )
                }
            }
            val generatedPrimaryConstructor = carrier.irClass.declarations
                .filterIsInstance<IrConstructor>()
                .firstOrNull { constructor ->
                    constructor.isPrimary && constructor.isGeneratedBySpreadPackPlugin()
                }
            val generatedDefaultsByName = generatedPrimaryConstructor
                ?.valueParameters
                ?.associateBy({ parameter -> parameter.name.asString() }, { parameter -> parameter.defaultValue })
                .orEmpty()
            return carrier.generatedProperties.map { property ->
                val fieldType = property.backingField?.type ?: property.getter?.returnType
                    ?: error("Generated spread-pack carrier property ${property.name.asString()} is missing a readable type")
                IrCarrierDeclaredField(
                    name = property.name,
                    type = fieldType,
                    constructorIndex = -1,
                    defaultValue = generatedDefaultsByName[property.name.asString()]?.deepCopyWithSymbols(),
                )
            }
        }
        return carrier.primaryConstructor.valueParameters.mapIndexed { index, parameter ->
            IrCarrierDeclaredField(
                name = parameter.name,
                type = parameter.type,
                constructorIndex = index,
                defaultValue = parameter.defaultValue,
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
                    "argsof overload set ${reference.functionFqName} is ambiguous; specify parameterTypes",
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

    private fun matchesGeneratedName(
        generatedName: String,
        originalName: String,
        expectedRenamedName: String,
    ): Boolean {
        if (generatedName == originalName || generatedName == expectedRenamedName) {
            return true
        }
        val sourceLevelName = generatedName.substringBefore('-')
        return sourceLevelName == originalName || sourceLevelName == expectedRenamedName
    }

    private fun rewriteGeneratedParameterDefaults(
        generated: IrSimpleFunction,
        match: IrSpreadPackMatch,
    ) {
        if (match.original.hasAnnotation(composableAnnotationFqName)) {
            generated.valueParameters.forEach { parameter ->
                parameter.defaultValue = null
            }
            return
        }
        val expansionsByIndex = match.expansions.associateBy { expansion -> expansion.parameterIndex }
        var generatedParameterCursor = 0
        match.original.valueParameters.forEachIndexed { index, _ ->
            val expansion = expansionsByIndex[index]
            if (expansion == null) {
                generatedParameterCursor += 1
                return@forEachIndexed
            }
            expansion.fields.forEach { field ->
                val generatedParameter = generated.valueParameters[generatedParameterCursor]
                val validFieldDefaultValue = field.defaultValue?.takeUnless { defaultValue ->
                    defaultValue.hasInvalidDefaultValue()
                }
                val validGeneratedDefaultValue = generatedParameter.defaultValue?.takeUnless { defaultValue ->
                    defaultValue.hasInvalidDefaultValue()
                }
                generatedParameter.defaultValue = validFieldDefaultValue?.deepCopyWithSymbols(generated)
                    ?: validGeneratedDefaultValue
                generatedParameterCursor += 1
            }
        }
    }

    private fun IrExpressionBody?.hasInvalidDefaultValue(): Boolean {
        val body = this ?: return false
        if (body.expression is IrErrorExpression) {
            return true
        }
        var hasInvalidExpression = false
        body.acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (element is IrErrorExpression) {
                    hasInvalidExpression = true
                    return
                }
                if (!hasInvalidExpression) {
                    element.acceptChildrenVoid(this)
                }
            }
        })
        return hasInvalidExpression
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

                if (expansion.fields.all { field -> field.constructorIndex >= 0 }) {
                    val constructorCall = irCall(expansion.constructor.symbol)
                    expansion.fields.forEach { field ->
                        constructorCall.putValueArgument(
                            field.constructorIndex,
                            irGet(generated.valueParameters[generatedParameterCursor]),
                        )
                        generatedParameterCursor += 1
                    }
                    call.putValueArgument(index, constructorCall)
                    return@forEachIndexed
                }

                val constructorCall = irCall(expansion.constructor.symbol)
                val carrierTemp = irTemporary(constructorCall)
                val generatedProperties = expansion.carrierClass.declarations
                    .filterIsInstance<IrProperty>()
                    .filter { property -> property.isGeneratedBySpreadPackPlugin() }
                    .associateBy { property -> property.name.asString() }
                expansion.fields.forEach { field ->
                    val property = generatedProperties[field.name.asString()]
                        ?: invalidTarget(
                            match.original,
                            "annotated spread-pack carrier ${expansion.carrierClass.name.asString()} is missing generated property ${field.name.asString()}",
                        )
                    val setter = property.setter
                        ?: invalidTarget(
                            match.original,
                            "annotated spread-pack carrier ${expansion.carrierClass.name.asString()} property ${field.name.asString()} is missing setter",
                        )
                    +irCall(setter.symbol).apply {
                        dispatchReceiver = irGet(carrierTemp)
                        putValueArgument(0, irGet(generated.valueParameters[generatedParameterCursor]))
                    }
                    generatedParameterCursor += 1
                }
                call.putValueArgument(index, irGet(carrierTemp))
            }
        }

        +irReturn(originalCall)
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

    private fun IrValueParameter.getSpreadPackOfAnnotation(): IrConstructorCall? {
        return annotations.firstOrNull { annotation ->
            (annotation.type.classifierOrNull?.owner as? IrClass)?.fqNameWhenAvailable ==
                SpreadPackPluginKeys.spreadPackOfAnnotation
        }
    }

    private fun IrValueParameter.getSpreadArgsOfAnnotation(): IrConstructorCall? {
        return annotations.firstOrNull { annotation ->
            (annotation.type.classifierOrNull?.owner as? IrClass)?.fqNameWhenAvailable ==
                SpreadPackPluginKeys.spreadArgsOfAnnotation
        }
    }

    private fun IrClass.getSpreadPackCarrierOfAnnotation(): IrConstructorCall? {
        return annotations.firstOrNull { annotation ->
            (annotation.type.classifierOrNull?.owner as? IrClass)?.fqNameWhenAvailable ==
                SpreadPackPluginKeys.spreadPackCarrierOfAnnotation
        }
    }

    private fun IrSimpleFunction.getGeneratedSpreadPackOverloadAnnotation(): IrConstructorCall? {
        return annotations.firstOrNull { annotation ->
            (annotation.type.classifierOrNull?.owner as? IrClass)?.fqNameWhenAvailable ==
                SpreadPackPluginKeys.generatedSpreadPackOverloadAnnotation
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
        val directFunctionFqName = ((getValueArgument(Name.identifier("value")) as? IrConst)
            ?.value as? String)
            ?.takeIf { functionFqName -> functionFqName.isNotBlank() }
        val directParameterTypeClassIds = classIdArguments(Name.identifier("parameterTypes"))
        if (directFunctionFqName == null) {
            error("spread-pack target function must not be blank")
        }
        return IrSpreadArgsReference(
            functionFqName = directFunctionFqName,
            parameterTypeClassIds = directParameterTypeClassIds,
        )
    }

    private fun IrConstructorCall.generatedSpreadPackSourceFunctionFqName(): String? {
        return ((getValueArgument(Name.identifier("sourceFunctionFqName")) as? IrConst)
            ?.value as? String)
            ?.takeIf { functionFqName -> functionFqName.isNotBlank() }
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

    private fun IrDeclaration.isGeneratedBySpreadPackPlugin(): Boolean {
        val declarationOrigin = origin
        return declarationOrigin is IrDeclarationOrigin.GeneratedByPlugin &&
            declarationOrigin.pluginKey == SpreadPackGeneratedDeclarationKey
    }

    private fun sameIrType(
        left: org.jetbrains.kotlin.ir.types.IrType,
        right: org.jetbrains.kotlin.ir.types.IrType,
    ): Boolean {
        if (left == right) {
            return true
        }
        if (left.render() == right.render()) {
            return true
        }
        val leftSimpleType = left as? IrSimpleType ?: return false
        val rightSimpleType = right as? IrSimpleType ?: return false
        if (!sameIrClassifier(leftSimpleType, rightSimpleType)) {
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

    private fun sameIrClassifier(
        left: IrSimpleType,
        right: IrSimpleType,
    ): Boolean {
        if (left.classifier == right.classifier) {
            return true
        }
        val leftClassId = erasureClassId(left)
        val rightClassId = erasureClassId(right)
        return leftClassId != null && leftClassId == rightClassId
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

    private fun invalidCarrierTarget(
        irClass: IrClass,
        reason: String,
    ): Nothing {
        val fqName = irClass.fqNameWhenAvailable?.asString() ?: irClass.name.asString()
        throw IllegalStateException("Invalid @SpreadPackCarrierOf target $fqName: $reason")
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
