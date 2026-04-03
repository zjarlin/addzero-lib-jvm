package site.addzero.kcp.spreadpack

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.extractClassFromArgument
import org.jetbrains.kotlin.fir.analysis.checkers.extractClassesFromArgument
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.declarations.extractEnumValueArgumentInfo
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameterCopy
import org.jetbrains.kotlin.fir.declarations.findArgumentByName
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getTargetType
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.declarations.getStringArrayArgument
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirCollectionLiteral
import org.jetbrains.kotlin.fir.expressions.FirClassReferenceExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildThrowExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate.Companion.create
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated as lookupAnnotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.copyFirFunctionWithResolvePhase
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedBaseSymbol
import org.jetbrains.kotlin.fir.resolve.providers.dependenciesSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isNullableString
import org.jetbrains.kotlin.fir.types.renderForDebugging
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

private data class FirCarrierMetadata(
    val classId: ClassId,
    val regularClass: FirRegularClass,
    val primaryConstructor: FirConstructor,
)

private data class FirFlattenedFieldSpec(
    val name: Name,
    val resolvedType: ConeKotlinType,
)

private data class FirReferencedParameterType(
    val classId: ClassId?,
    val qualifierText: String?,
)

private data class FirSpreadArgsReference(
    val functionFqName: String,
    val parameterTypes: List<FirReferencedParameterType>,
)

@OptIn(
    SymbolInternals::class,
    ExperimentalTopLevelDeclarationsGenerationApi::class,
    DirectDeclarationsAccess::class,
)
class SpreadPackFirExtension(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    private val targetClassPredicate = DeclarationPredicate.create {
        annotated(SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotation)
    }

    private val targetFunctionPredicate = LookupPredicate.create {
        lookupAnnotated(SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotation)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(targetClassPredicate, targetFunctionPredicate)
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        if (!classSymbol.fir.origin.fromSource) {
            return emptySet()
        }
        return findMemberCandidates(classSymbol, context)
            .mapTo(linkedSetOf()) { candidate -> candidate.generatedName }
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        return if (context == null) {
            findTopLevelCandidates()
                .filter { candidate ->
                    candidate.generatedName == callableId.callableName &&
                        candidate.original.callableId.packageName == callableId.packageName
                }
                .map(::createGeneratedFunction)
        } else {
            findMemberCandidates(context.owner, context)
                .filter { candidate -> candidate.generatedName == callableId.callableName }
                .map(::createGeneratedFunction)
        }
    }

    override fun hasPackage(packageFqName: FqName): Boolean {
        return getTopLevelCallableIds().any { callableId -> callableId.packageName == packageFqName }
    }

    override fun getTopLevelCallableIds(): Set<CallableId> {
        return findTopLevelCandidates()
            .mapTo(linkedSetOf()) { candidate ->
                candidate.original.callableId.copy(candidate.generatedName)
            }
    }

    private fun findMemberCandidates(
        owner: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): List<FirSpreadPackCandidate> {
        val originals = collectMemberOriginals(owner, context)
        if (originals.isEmpty()) {
            return emptyList()
        }
        val existingJvmKeys = collectExistingJvmKeys(owner, context)
        return buildCandidates(originals, existingJvmKeys)
    }

    private fun findTopLevelCandidates(): List<FirSpreadPackCandidate> {
        val originals = collectTopLevelOriginals()
        if (originals.isEmpty()) {
            return emptyList()
        }
        val existingJvmKeys = originals
            .mapTo(linkedSetOf()) { function ->
                jvmSignatureKey(
                    name = function.callableId.callableName,
                    parameterTypes = function.fir.valueParameters.map { parameter -> parameter.returnTypeRef.coneType },
                )
            }
        return buildCandidates(originals, existingJvmKeys)
    }

    private fun collectMemberOriginals(
        owner: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): List<FirNamedFunctionSymbol> {
        val declaredScope = context.declaredScope ?: return emptyList()
        val processWholeClass = session.predicateBasedProvider.matches(targetClassPredicate, owner)
        val originals = linkedMapOf<String, FirNamedFunctionSymbol>()
        declaredScope.getCallableNames().forEach { callableName ->
            declaredScope.processFunctionsByName(callableName) { function ->
                if (!function.fir.origin.fromSource) {
                    return@processFunctionsByName
                }
                if (!isSupportedOriginalFunction(function)) {
                    return@processFunctionsByName
                }
                if (!processWholeClass &&
                    !hasAnnotation(function.fir, SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotationClassId)
                ) {
                    return@processFunctionsByName
                }
                originals[function.callableId.toString()] = function
            }
        }
        return originals.values.toList()
    }

    private fun collectTopLevelOriginals(): List<FirNamedFunctionSymbol> {
        return session.predicateBasedProvider
            .getSymbolsByPredicate(targetFunctionPredicate)
            .mapNotNull { symbol -> symbol as? FirNamedFunctionSymbol }
            .filter { symbol ->
                symbol.fir.origin.fromSource &&
                    symbol.callableId.classId == null &&
                    isSupportedOriginalFunction(symbol)
            }
            .sortedBy { symbol -> symbol.callableId.toString() }
    }

    private fun buildCandidates(
        originals: List<FirNamedFunctionSymbol>,
        existingJvmKeys: Set<String>,
    ): List<FirSpreadPackCandidate> {
        val occupiedJvmKeys = linkedSetOf<String>().apply {
            addAll(existingJvmKeys)
        }
        val generatedCandidates = mutableListOf<FirSpreadPackCandidate>()
        for (original in originals) {
            val rawCandidate = buildCandidate(original) ?: continue
            val sameNameKey = jvmSignatureKey(
                name = rawCandidate.generatedName,
                parameterTypes = rawCandidate.generatedParameterTypes,
            )
            if (sameNameKey in occupiedJvmKeys) {
                val renamed = rawCandidate.copy(
                    generatedName = buildRenamedFunctionName(rawCandidate.original, rawCandidate.expansions),
                )
                val renamedKey = jvmSignatureKey(
                    name = renamed.generatedName,
                    parameterTypes = renamed.generatedParameterTypes,
                )
                if (renamedKey in occupiedJvmKeys) {
                    error(
                        "Spread pack rename conflict for ${original.callableId.asFqNameForDebugInfo()} -> " +
                            renamed.generatedName.asString(),
                    )
                }
                occupiedJvmKeys += renamedKey
                generatedCandidates += renamed
            } else {
                occupiedJvmKeys += sameNameKey
                generatedCandidates += rawCandidate
            }
        }
        return generatedCandidates
    }

    private fun buildCandidate(
        original: FirNamedFunctionSymbol,
    ): FirSpreadPackCandidate? {
        val expansions = original.fir.valueParameters.mapIndexedNotNull { index, parameter ->
            createExpansion(original, index, parameter)
        }
        if (expansions.isEmpty()) {
            return null
        }

        val expansionsByIndex = expansions.associateBy { expansion -> expansion.parameterIndex }
        val generatedParameterTypes = mutableListOf<ConeKotlinType>()
        val generatedParameterNames = linkedSetOf<String>()

        original.fir.valueParameters.forEachIndexed { index, parameter ->
            val expansion = expansionsByIndex[index]
            if (expansion == null) {
                if (!generatedParameterNames.add(parameter.name.asString())) {
                    illegalTarget(
                        original,
                        "duplicate expanded parameter name ${parameter.name.asString()}",
                    )
                }
                generatedParameterTypes += parameter.returnTypeRef.coneType
            } else {
                expansion.fields.forEach { field ->
                    val fieldName = field.parameter.name.asString()
                    if (!generatedParameterNames.add(fieldName)) {
                        illegalTarget(
                            original,
                            "duplicate expanded parameter name $fieldName",
                        )
                    }
                    generatedParameterTypes += field.resolvedType
                }
            }
        }

        return FirSpreadPackCandidate(
            original = original,
            generatedName = original.callableId.callableName,
            expansions = expansions,
            generatedParameterTypes = generatedParameterTypes,
        )
    }

    private fun createExpansion(
        owner: FirNamedFunctionSymbol,
        parameterIndex: Int,
        parameter: FirValueParameter,
    ): FirSpreadPackExpansion? {
        val spreadPackAnnotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadPackAnnotationClassId, session)
            ?: return null
        val carrier = resolveCarrierMetadata(owner, parameter)
        val spreadArgsAnnotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadArgsOfAnnotationClassId, session)
        val selectorKind = spreadArgsAnnotation?.selectorKind()
            ?: spreadPackAnnotation.selectorKind()
        val excludedNames = spreadArgsAnnotation?.excludeNames()
            ?: spreadPackAnnotation.excludeNames()
        if (spreadArgsAnnotation != null && !spreadPackAnnotation.isDefaultSpreadPackConfiguration()) {
            illegalTarget(
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
            val referencedOverload = resolveReferencedOverload(owner, spreadArgsAnnotation)
            val overloadKey = overloadKey(referencedOverload)
            val flattenedFields = flattenFunctionParameters(
                owner = owner,
                function = referencedOverload,
                visitedOverloads = linkedSetOf(overloadKey),
            )
            buildReferencedCarrierFields(
                owner = owner,
                carrier = carrier,
                flattenedFields = flattenedFields,
                selectorKind = selectorKind,
                excludedNames = excludedNames,
                referenceDescription = referencedOverload.callableId.asFqNameForDebugInfo().asString(),
            )
        }

        return FirSpreadPackExpansion(
            parameterIndex = parameterIndex,
            carrierClassId = carrier.classId,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            fields = fields,
        )
    }

    private fun resolveCarrierMetadata(
        owner: FirNamedFunctionSymbol,
        parameter: FirValueParameter,
    ): FirCarrierMetadata {
        val parameterType = parameter.returnTypeRef.coneType as? ConeClassLikeType
            ?: illegalTarget(
                owner,
                "spread-pack parameter ${parameter.name.asString()} must reference a regular class with a primary constructor",
            )
        if (parameterType.typeArguments.isNotEmpty()) {
            illegalTarget(
                owner,
                "generic spread-pack carriers are not supported in v1: ${parameter.name.asString()}",
            )
        }
        val carrierClassId = parameterType.lookupTag.classId
        val carrierClass = resolveRegularClassById(carrierClassId)
            ?: illegalTarget(
                owner,
                "unable to resolve spread-pack carrier ${carrierClassId.asString()}",
            )
        if (carrierClass.classKind != ClassKind.CLASS) {
            illegalTarget(
                owner,
                "spread-pack carrier ${carrierClassId.asString()} must be a class",
            )
        }
        if (carrierClass.typeParameters.isNotEmpty()) {
            illegalTarget(
                owner,
                "generic spread-pack carriers are not supported in v1: ${carrierClassId.asString()}",
            )
        }
        val primaryConstructor = carrierClass.declarations
            .filterIsInstance<FirConstructor>()
            .firstOrNull { constructor -> constructor.isPrimary }
            ?: illegalTarget(
                owner,
                "spread-pack carrier ${carrierClassId.asString()} must declare a primary constructor",
            )
        return FirCarrierMetadata(
            classId = carrierClassId,
            regularClass = carrierClass,
            primaryConstructor = primaryConstructor,
        )
    }

    private fun buildCarrierFields(
        owner: FirNamedFunctionSymbol,
        carrier: FirCarrierMetadata,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
    ): List<FirSpreadPackField> {
        val constructorParameters = carrier.primaryConstructor.valueParameters
        validateExcludedNames(
            owner = owner,
            excludedNames = excludedNames,
            availableNames = constructorParameters.map { constructorParameter -> constructorParameter.name.asString() },
            contextLabel = carrier.classId.shortClassName.asString(),
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
            FirSpreadPackField(
                parameter = constructorParameter,
                resolvedType = constructorParameter.returnTypeRef.coneType,
            )
        }
    }

    private fun buildReferencedCarrierFields(
        owner: FirNamedFunctionSymbol,
        carrier: FirCarrierMetadata,
        flattenedFields: List<FirFlattenedFieldSpec>,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
        referenceDescription: String,
    ): List<FirSpreadPackField> {
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
                ?: illegalTarget(
                    owner,
                    "spread-pack carrier ${carrier.classId.shortClassName.asString()} is missing argsof field $fieldName from $referenceDescription",
                )
            if (!sameConeType(carrierParameter.returnTypeRef.coneType, field.resolvedType)) {
                illegalTarget(
                    owner,
                    "spread-pack carrier ${carrier.classId.shortClassName.asString()} field $fieldName " +
                        "type ${carrierParameter.returnTypeRef.coneType.renderForDebugging()} does not match " +
                        "$referenceDescription field type ${field.resolvedType.renderForDebugging()}",
                )
            }
            selectedCarrierNames += fieldName
            FirSpreadPackField(
                parameter = carrierParameter,
                resolvedType = field.resolvedType,
            )
        }.also { fields ->
            validateCarrierOmissions(
                owner = owner,
                carrier = carrier,
                selectedCarrierNames = selectedCarrierNames,
            )
            validateUniqueFieldNames(
                owner = owner,
                names = fields.map { field -> field.parameter.name.asString() },
                contextLabel = referenceDescription,
            )
        }
    }

    private fun flattenFunctionParameters(
        owner: FirNamedFunctionSymbol,
        function: FirNamedFunctionSymbol,
        visitedOverloads: Set<String>,
    ): List<FirFlattenedFieldSpec> {
        if (!isSupportedReferencedFunction(function)) {
            illegalTarget(
                owner,
                "argsof target ${function.callableId.asFqNameForDebugInfo()} must not declare receivers or context parameters",
            )
        }
        return function.fir.valueParameters.flatMap { referencedParameter ->
            flattenValueParameter(
                owner = owner,
                parameter = referencedParameter,
                visitedOverloads = visitedOverloads,
            )
        }.also { fields ->
            validateUniqueFieldNames(
                owner = owner,
                names = fields.map { field -> field.name.asString() },
                contextLabel = function.callableId.asFqNameForDebugInfo().asString(),
            )
        }
    }

    private fun flattenValueParameter(
        owner: FirNamedFunctionSymbol,
        parameter: FirValueParameter,
        visitedOverloads: Set<String>,
    ): List<FirFlattenedFieldSpec> {
        val spreadPackAnnotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadPackAnnotationClassId, session)
            ?: return listOf(
                FirFlattenedFieldSpec(
                    name = parameter.name,
                    resolvedType = parameter.returnTypeRef.coneType,
                ),
            )
        val carrier = resolveCarrierMetadata(owner, parameter)
        val spreadArgsAnnotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadArgsOfAnnotationClassId, session)
        val selectorKind = spreadArgsAnnotation?.selectorKind()
            ?: spreadPackAnnotation.selectorKind()
        val excludedNames = spreadArgsAnnotation?.excludeNames()
            ?: spreadPackAnnotation.excludeNames()
        if (spreadArgsAnnotation != null && !spreadPackAnnotation.isDefaultSpreadPackConfiguration()) {
            illegalTarget(
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
                FirFlattenedFieldSpec(
                    name = field.parameter.name,
                    resolvedType = field.resolvedType,
                )
            }
        }

        val referencedOverload = resolveReferencedOverload(owner, spreadArgsAnnotation)
        val overloadKey = overloadKey(referencedOverload)
        if (overloadKey in visitedOverloads) {
            illegalTarget(
                owner,
                "detected argsof overload cycle at ${referencedOverload.callableId.asFqNameForDebugInfo()}",
            )
        }
        val flattenedFields = flattenFunctionParameters(
            owner = owner,
            function = referencedOverload,
            visitedOverloads = visitedOverloads + overloadKey,
        )
        return buildReferencedCarrierFields(
            owner = owner,
            carrier = carrier,
            flattenedFields = flattenedFields,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            referenceDescription = referencedOverload.callableId.asFqNameForDebugInfo().asString(),
        ).map { field ->
            FirFlattenedFieldSpec(
                name = field.parameter.name,
                resolvedType = field.resolvedType,
            )
        }
    }

    private fun selectFlattenedFields(
        owner: FirNamedFunctionSymbol,
        fields: List<FirFlattenedFieldSpec>,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
        contextLabel: String,
    ): List<FirFlattenedFieldSpec> {
        validateExcludedNames(
            owner = owner,
            excludedNames = excludedNames,
            availableNames = fields.map { field -> field.name.asString() },
            contextLabel = contextLabel,
        )
        return fields.filter { field ->
            shouldIncludeField(field.resolvedType, selectorKind) &&
                field.name.asString() !in excludedNames
        }
    }

    private fun validateExcludedNames(
        owner: FirNamedFunctionSymbol,
        excludedNames: Set<String>,
        availableNames: List<String>,
        contextLabel: String,
    ) {
        val unknownExcludedNames = excludedNames - availableNames.toSet()
        if (unknownExcludedNames.isNotEmpty()) {
            illegalTarget(
                owner,
                "unknown spread-pack exclude names for $contextLabel: ${unknownExcludedNames.sorted().joinToString()}",
            )
        }
    }

    private fun validateCarrierOmissions(
        owner: FirNamedFunctionSymbol,
        carrier: FirCarrierMetadata,
        selectedCarrierNames: Set<String>,
    ) {
        carrier.primaryConstructor.valueParameters.forEach { constructorParameter ->
            if (constructorParameter.name.asString() !in selectedCarrierNames && constructorParameter.defaultValue == null) {
                illegalTarget(
                    owner,
                    "spread-pack carrier ${carrier.classId.shortClassName.asString()} cannot omit required field " +
                        constructorParameter.name.asString(),
                )
            }
        }
    }

    private fun validateUniqueFieldNames(
        owner: FirNamedFunctionSymbol,
        names: List<String>,
        contextLabel: String,
    ) {
        val duplicates = names.groupingBy { it }.eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .sorted()
        if (duplicates.isNotEmpty()) {
            illegalTarget(
                owner,
                "duplicate flattened parameter names in $contextLabel: ${duplicates.joinToString()}",
            )
        }
    }

    private fun resolveReferencedOverload(
        owner: FirNamedFunctionSymbol,
        annotation: FirAnnotation,
    ): FirNamedFunctionSymbol {
        val reference = annotation.spreadArgsReference()
        val overloads = resolveFunctionSymbolsByFqName(reference.functionFqName)
            .filter(::isResolvableReferencedFunction)
        if (overloads.isEmpty()) {
            illegalTarget(
                owner,
                "unable to resolve argsof overload set ${reference.functionFqName}",
            )
        }
        if (reference.parameterTypes.isEmpty()) {
            if (overloads.size != 1) {
                illegalTarget(
                    owner,
                    "argsof overload set ${reference.functionFqName} is ambiguous; specify SpreadOverload.parameterTypes",
                )
            }
            return overloads.single()
        }
        val matches = overloads.filter { overload ->
            val overloadParameterTypes = overload.fir.valueParameters.map { valueParameter ->
                erasureClassId(valueParameter.returnTypeRef.coneType)
                    ?: illegalTarget(
                        owner,
                        "argsof overload ${overload.callableId.asFqNameForDebugInfo()} has unsupported parameter type " +
                            valueParameter.returnTypeRef.coneType.renderForDebugging(),
                    )
            }
            overloadParameterTypes.size == reference.parameterTypes.size &&
                overloadParameterTypes.zip(reference.parameterTypes).all { (actualClassId, expectedType) ->
                    expectedType.matches(actualClassId)
                }
        }
        if (matches.size != 1) {
            illegalTarget(
                owner,
                "unable to select a unique argsof overload for ${reference.functionFqName} with parameterTypes=" +
                    reference.parameterTypes.joinToString { parameterType -> parameterType.renderForDiagnostics() },
            )
        }
        return matches.single()
    }

    private fun resolveFunctionSymbolsByFqName(
        functionFqName: String,
    ): List<FirNamedFunctionSymbol> {
        val fqName = FqName(functionFqName)
        val packageMatches = buildList {
            addAll(
                session.firProvider.symbolProvider.getTopLevelFunctionSymbols(
                    fqName.parent(),
                    fqName.shortName(),
                ),
            )
            addAll(
                session.dependenciesSymbolProvider.getTopLevelFunctionSymbols(
                    fqName.parent(),
                    fqName.shortName(),
                ),
            )
        }
        if (packageMatches.isNotEmpty()) {
            return packageMatches
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
            val classSymbol = resolveRegularClassById(classId)?.symbol ?: continue
            val functions = classSymbol.fir.declarations
                .filterIsInstance<FirNamedFunction>()
                .filter { function -> function.name == functionName }
                .map { function -> function.symbol }
            if (functions.isNotEmpty()) {
                return functions
            }
        }
        return emptyList()
    }

    private fun resolveRegularClassById(
        classId: ClassId,
    ): FirRegularClass? {
        return session.firProvider.getFirClassifierByFqName(classId) as? FirRegularClass
            ?: (session.dependenciesSymbolProvider.getClassLikeSymbolByClassId(classId)?.fir as? FirRegularClass)
    }

    private fun isResolvableReferencedFunction(
        symbol: FirNamedFunctionSymbol,
    ): Boolean {
        return symbol.fir.origin != SpreadPackGeneratedDeclarationKey.origin
    }

    private fun isSupportedReferencedFunction(
        symbol: FirNamedFunctionSymbol,
    ): Boolean {
        val fir = symbol.fir
        return fir.receiverParameter == null && fir.contextParameters.isEmpty()
    }

    private fun FirAnnotation.spreadArgsReference(): FirSpreadArgsReference {
        val overloadExpression = findArgumentByName(
            Name.identifier("overload"),
            returnFirstWhenNotFound = false,
        ) ?: error("SpreadArgsOf.overload must be an annotation value")
        val overloadsExpression = overloadExpression.findNestedCallArgument(
            Name.identifier("of"),
        ) ?: error("SpreadOverload.of must be an annotation value, but was ${overloadExpression.debugKind()}")
        val functionFqName = overloadsExpression.findNestedCallArgument(
            Name.identifier("functionFqName"),
        )?.stringLiteralValue()
            ?: error(
                "SpreadOverloadsOf.functionFqName must be a string literal, but was " +
                    overloadsExpression.debugKind(),
            )
        val parameterTypesExpression = overloadExpression.findNestedCallArgument(
            Name.identifier("parameterTypes"),
        )
        val parameterTypes = parameterTypesExpression
            ?.extractParameterTypeReferences()
            .orEmpty()
        return FirSpreadArgsReference(
            functionFqName = functionFqName,
            parameterTypes = parameterTypes,
        )
    }

    private fun FirExpression.findNestedCallArgument(
        name: Name,
    ): FirExpression? {
        return when (this) {
            is FirAnnotation -> findArgumentByName(
                name,
                returnFirstWhenNotFound = false,
            )

            is FirFunctionCall -> {
                val resolvedArgument = (argumentList as? FirResolvedArgumentList)
                    ?.mapping
                    ?.entries
                    ?.firstOrNull { entry -> entry.value.name == name }
                    ?.key
                if (resolvedArgument != null) {
                    return resolvedArgument
                }
                val arguments = argumentList.arguments
                arguments.firstNotNullOfOrNull { argument ->
                    val namedArgument = argument as? FirNamedArgumentExpression
                    if (namedArgument?.name == name) {
                        namedArgument.expression
                    } else {
                        null
                    }
                } ?: if (arguments.size == 1) {
                    val argument = arguments.single()
                    (argument as? FirNamedArgumentExpression)?.expression ?: argument
                } else {
                    null
                }
            }

            else -> null
        }
    }

    private fun FirExpression.stringLiteralValue(): String? {
        return (this as? FirLiteralExpression)?.value as? String
    }

    private fun FirExpression.extractParameterTypeReferences(): List<FirReferencedParameterType> {
        val directMatches = extractClassesFromArgument(session).map { classSymbol ->
            FirReferencedParameterType(
                classId = classSymbol.classId,
                qualifierText = classSymbol.classId.asSingleFqName().asString(),
            )
        }
        if (directMatches.isNotEmpty()) {
            return directMatches
        }
        val collectionLiteral = this as? FirCollectionLiteral ?: return emptyList()
        return collectionLiteral.argumentList.arguments.mapNotNull { argument ->
            when (argument) {
                is FirGetClassCall -> extractParameterTypeReferenceFromGetClassCall(argument)
                else -> argument.extractClassFromArgument(session)?.classId?.let { classId ->
                    FirReferencedParameterType(
                        classId = classId,
                        qualifierText = classId.asSingleFqName().asString(),
                    )
                } ?: argument.extractUnresolvedParameterTypeReference()
            }
        }
    }

    private fun FirExpression.debugKind(): String {
        return this::class.qualifiedName ?: this::class.simpleName.orEmpty()
    }

    private fun extractParameterTypeReferenceFromGetClassCall(
        expression: FirGetClassCall,
    ): FirReferencedParameterType? {
        return when (val target = expression.argument) {
            is FirResolvedQualifier -> FirReferencedParameterType(
                classId = target.symbol?.classId,
                qualifierText = target.classId?.asSingleFqName()?.asString(),
            )
            is FirClassReferenceExpression -> {
                val classId = (target.classTypeRef.coneType as? ConeClassLikeType)?.lookupTag?.classId
                FirReferencedParameterType(
                    classId = classId,
                    qualifierText = classId?.asSingleFqName()?.asString(),
                )
            }
            is FirPropertyAccessExpression -> FirReferencedParameterType(
                classId = (target.calleeReference.toResolvedBaseSymbol() as? FirClassLikeSymbol<*>)?.classId,
                qualifierText = target.asQualifierText(),
            )
            else -> target.extractUnresolvedParameterTypeReference()
        }
    }

    private fun FirExpression.extractUnresolvedParameterTypeReference(): FirReferencedParameterType? {
        return when (this) {
            is FirResolvedQualifier -> FirReferencedParameterType(
                classId = symbol?.classId,
                qualifierText = classId?.asSingleFqName()?.asString(),
            )
            is FirClassReferenceExpression -> {
                val classId = (classTypeRef.coneType as? ConeClassLikeType)?.lookupTag?.classId
                FirReferencedParameterType(
                    classId = classId,
                    qualifierText = classId?.asSingleFqName()?.asString(),
                )
            }
            is FirPropertyAccessExpression -> FirReferencedParameterType(
                classId = (calleeReference.toResolvedBaseSymbol() as? FirClassLikeSymbol<*>)?.classId,
                qualifierText = asQualifierText(),
            )
            else -> null
        }
    }

    private fun FirPropertyAccessExpression.asQualifierText(): String? {
        val receiverText = explicitReceiver?.asQualifierText()
            ?: dispatchReceiver?.asQualifierText()
            ?: extensionReceiver?.asQualifierText()
        val simpleName = calleeReference.name.asString()
        return listOfNotNull(receiverText, simpleName)
            .joinToString(".")
            .ifBlank { null }
    }

    private fun FirExpression.asQualifierText(): String? {
        return when (this) {
            is FirResolvedQualifier -> classId?.asSingleFqName()?.asString()
            is FirClassReferenceExpression -> (classTypeRef.coneType as? ConeClassLikeType)
                ?.lookupTag
                ?.classId
                ?.asSingleFqName()
                ?.asString()
            is FirPropertyAccessExpression -> asQualifierText()
            else -> null
        }
    }

    private fun FirReferencedParameterType.matches(
        actualClassId: ClassId,
    ): Boolean {
        if (classId != null) {
            return classId == actualClassId
        }
        val qualifierText = qualifierText ?: return false
        return qualifierText == actualClassId.asSingleFqName().asString() ||
            qualifierText == actualClassId.relativeClassName.asString() ||
            qualifierText.substringAfterLast('.') == actualClassId.shortClassName.asString()
    }

    private fun FirReferencedParameterType.renderForDiagnostics(): String {
        return classId?.asString() ?: qualifierText ?: "<unresolved>"
    }

    private fun FirAnnotation.isDefaultSpreadPackConfiguration(): Boolean {
        val hasExclude = excludeNames().isNotEmpty()
        return !hasExclude && selectorKind() == SelectorKind.PROPS
    }

    private fun overloadKey(
        function: FirNamedFunctionSymbol,
    ): String {
        return buildString {
            append(function.callableId.asFqNameForDebugInfo())
            append("|")
            function.fir.valueParameters.forEach { valueParameter ->
                append(jvmErasure(valueParameter.returnTypeRef.coneType))
                append(";")
            }
        }
    }

    private fun createGeneratedFunction(
        candidate: FirSpreadPackCandidate,
    ): FirNamedFunctionSymbol {
        val original = candidate.original.fir
        val expansionsByIndex = candidate.expansions.associateBy { expansion -> expansion.parameterIndex }
        val generated = copyFirFunctionWithResolvePhase(
            original = original,
            callableId = candidate.original.callableId.copy(candidate.generatedName),
            key = SpreadPackGeneratedDeclarationKey,
            firResolvePhase = FirResolvePhase.BODY_RESOLVE,
        ) {
            source = original.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
            annotations.clear()
            name = candidate.generatedName

            val ownerClassSymbol = candidate.original.callableId.classId?.let(session::getRegularClassSymbolByClassId)
            val ownerKind = ownerClassSymbol?.fir?.classKind
            val generatedModality = when {
                ownerKind == ClassKind.INTERFACE -> Modality.OPEN
                original.status.modality == Modality.ABSTRACT || original.status.modality == Modality.OPEN -> Modality.OPEN
                else -> Modality.FINAL
            }
            val generatedVisibility = original.status.visibility.takeUnless { visibility ->
                visibility == Visibilities.Unknown
            } ?: Visibilities.Public
            val effectiveVisibility = ownerClassSymbol?.let { classSymbol ->
                generatedVisibility.toEffectiveVisibility(classSymbol, false, false)
            } ?: when (generatedVisibility) {
                Visibilities.Public -> EffectiveVisibility.Public
                Visibilities.Internal -> EffectiveVisibility.Internal
                else -> EffectiveVisibility.Public
            }
            status = FirResolvedDeclarationStatusImpl(
                generatedVisibility,
                generatedModality,
                effectiveVisibility,
            ).apply {
                isExpect = original.status.isExpect
                isActual = original.status.isActual
                isOverride = false
                isOperator = original.status.isOperator
                isInfix = original.status.isInfix
                isInline = original.status.isInline
                isSuspend = original.status.isSuspend
                isTailRec = false
                isExternal = false
            }

            val updatedParameters = mutableListOf<FirValueParameter>()
            valueParameters.forEachIndexed { index, parameter ->
                val expansion = expansionsByIndex[index]
                if (expansion == null) {
                    updatedParameters += parameter
                    return@forEachIndexed
                }
                expansion.fields.forEach { field ->
                    updatedParameters += buildValueParameterCopy(field.parameter) {
                        returnTypeRef = field.resolvedType.toFirResolvedTypeRef()
                        symbol = FirValueParameterSymbol()
                        containingDeclarationSymbol = this@copyFirFunctionWithResolvePhase.symbol
                        origin = SpreadPackGeneratedDeclarationKey.origin
                        resolvePhase = FirResolvePhase.BODY_RESOLVE
                    }
                }
            }
            valueParameters.clear()
            valueParameters += updatedParameters
            body = createStubBody()
        }
        return generated.symbol
    }

    private fun collectExistingJvmKeys(
        owner: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<String> {
        val keys = linkedSetOf<String>()
        (owner.fir as? FirRegularClass)
            ?.declarations
            ?.filterIsInstance<FirNamedFunction>()
            ?.forEach { function ->
                keys += jvmSignatureKey(
                    name = function.name,
                    parameterTypes = function.valueParameters.map { parameter -> parameter.returnTypeRef.coneType },
                )
            }
        val declaredScope = context.declaredScope ?: return keys
        declaredScope.getCallableNames().forEach { callableName ->
            declaredScope.processFunctionsByName(callableName) { function ->
                keys += jvmSignatureKey(
                    name = function.callableId.callableName,
                    parameterTypes = function.fir.valueParameters.map { parameter -> parameter.returnTypeRef.coneType },
                )
            }
        }
        return keys
    }

    private fun jvmSignatureKey(
        name: Name,
        parameterTypes: List<ConeKotlinType>,
    ): String {
        return buildString {
            append(name.asString())
            append("|")
            parameterTypes.forEach { type ->
                append(jvmErasure(type))
                append(";")
            }
        }
    }

    private fun buildRenamedFunctionName(
        original: FirNamedFunctionSymbol,
        expansions: List<FirSpreadPackExpansion>,
    ): Name {
        val suffix = expansions.joinToString(separator = "And") { expansion ->
            val selectorSuffix = when (expansion.selectorKind) {
                SelectorKind.PROPS -> ""
                SelectorKind.ATTRS -> "Attrs"
                SelectorKind.CALLBACKS -> "Callbacks"
            }
            expansion.carrierClassId.shortClassName.asString().toPascalCase() + selectorSuffix + "Pack"
        }
        return Name.identifier("${original.callableId.callableName.asString()}Via$suffix")
    }

    private fun shouldIncludeField(
        parameter: FirValueParameter,
        selectorKind: SelectorKind,
    ): Boolean {
        return shouldIncludeField(parameter.returnTypeRef.coneType, selectorKind)
    }

    private fun shouldIncludeField(
        type: ConeKotlinType,
        selectorKind: SelectorKind,
    ): Boolean {
        return when (selectorKind) {
            SelectorKind.PROPS -> true
            SelectorKind.ATTRS -> !isFunctionLike(type)
            SelectorKind.CALLBACKS -> isFunctionLike(type)
        }
    }

    private fun isFunctionLike(
        type: ConeKotlinType,
    ): Boolean {
        val classLikeType = type as? ConeClassLikeType ?: return false
        val fqName = classLikeType.lookupTag.classId.asSingleFqName().asString()
        return fqName.startsWith("kotlin.Function") || fqName.startsWith("kotlin.reflect.KFunction")
    }

    private fun isSupportedOriginalFunction(
        symbol: FirNamedFunctionSymbol,
    ): Boolean {
        val fir = symbol.fir
        if (fir.receiverParameter != null) {
            return false
        }
        if (fir.contextParameters.isNotEmpty()) {
            return false
        }
        if (symbol.callableId.classId == null && fir.status.visibility == Visibilities.Private) {
            return false
        }
        return true
    }

    private fun createStubBody(): FirBlock {
        val throwableType = session.builtinTypes.throwableType.coneType
        val constructorSymbol = throwableType.toRegularClassSymbol(session)
            ?.constructors(session)
            ?.firstOrNull { constructor ->
                constructor.valueParameterSymbols.size == 1 &&
                    constructor.valueParameterSymbols.first().resolvedReturnType.isNullableString
            }
            ?: error("Unable to locate Throwable(String) constructor for spread-pack stub")

        val throwExpression = buildThrowExpression {
            exception = buildFunctionCall {
                coneTypeOrNull = throwableType
                calleeReference = buildResolvedNamedReference {
                    name = constructorSymbol.callableId.callableName
                    resolvedSymbol = constructorSymbol
                }
                val messageExpression = buildLiteralExpression(
                    source = null,
                    kind = ConstantValueKind.String,
                    value = SpreadPackPluginKeys.stubErrorMessage,
                    setType = true,
                )
                argumentList = buildResolvedArgumentList(
                    original = null,
                    mapping = linkedMapOf<FirExpression, FirValueParameter>().apply {
                        put(messageExpression, constructorSymbol.valueParameterSymbols.first().fir)
                    },
                )
            }
        }
        return buildSingleExpressionBlock(throwExpression)
    }

    private fun hasAnnotation(
        declaration: FirDeclaration,
        classId: ClassId,
    ): Boolean {
        return declaration.annotations.getAnnotationByClassId(classId, session) != null
    }

    private fun FirAnnotation.selectorKind(): SelectorKind {
        val enumName = findArgumentByName(
            Name.identifier("selector"),
            returnFirstWhenNotFound = false,
        )?.extractEnumValueArgumentInfo()?.enumEntryName?.asString()
        return when (enumName) {
            SelectorKind.ATTRS.name -> SelectorKind.ATTRS
            SelectorKind.CALLBACKS.name -> SelectorKind.CALLBACKS
            else -> SelectorKind.PROPS
        }
    }

    private fun FirAnnotation.excludeNames(): Set<String> {
        val directMatches = getStringArrayArgument(Name.identifier("exclude"), session)
            ?.toSet()
            .orEmpty()
        if (directMatches.isNotEmpty()) {
            return directMatches
        }
        return findArgumentByName(
            Name.identifier("exclude"),
            returnFirstWhenNotFound = false,
        )?.extractStringArrayLiteralValues()
            ?.toSet()
            .orEmpty()
    }

    private fun FirExpression.extractStringArrayLiteralValues(): List<String> {
        return when (this) {
            is FirCollectionLiteral -> argumentList.arguments.mapNotNull { argument ->
                (argument as? FirLiteralExpression)?.value as? String
            }
            is FirLiteralExpression -> listOfNotNull(value as? String)
            else -> emptyList()
        }
    }

    private fun jvmErasure(type: ConeKotlinType): String {
        return when (type) {
            is ConeClassLikeType -> type.lookupTag.classId.asString()
            else -> type.renderForDebugging().substringBefore("<").substringBefore("?")
        }
    }

    private fun erasureClassId(
        type: ConeKotlinType,
    ): ClassId? {
        return (type as? ConeClassLikeType)?.lookupTag?.classId
    }

    private fun sameConeType(
        left: ConeKotlinType,
        right: ConeKotlinType,
    ): Boolean {
        if (left == right) {
            return true
        }
        return left.renderForDebugging() == right.renderForDebugging()
    }

    private fun illegalTarget(
        symbol: FirNamedFunctionSymbol,
        reason: String,
    ): Nothing {
        throw IllegalStateException(
            "Invalid @GenerateSpreadPackOverloads target ${symbol.callableId.asFqNameForDebugInfo()}: $reason",
        )
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
