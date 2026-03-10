package site.addzero.kcp.transformoverload.plugin

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameterCopy
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildThrowExpression
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate.Companion.create
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated as lookupAnnotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.copyFirFunctionWithResolvePhase
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjection
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isNullableString
import org.jetbrains.kotlin.fir.types.replaceType
import org.jetbrains.kotlin.fir.types.renderForDebugging
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

@OptIn(SymbolInternals::class, ExperimentalTopLevelDeclarationsGenerationApi::class)
class TransformOverloadFirExtension(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    private val targetClassPredicate = DeclarationPredicate.create {
        annotated(TransformOverloadPluginKeys.generateTransformOverloadsAnnotation)
    }

    private val targetFunctionPredicate = LookupPredicate.create {
        lookupAnnotated(TransformOverloadPluginKeys.generateTransformOverloadsAnnotation)
    }

    private val converterPredicate = LookupPredicate.create {
        lookupAnnotated(TransformOverloadPluginKeys.overloadTransformAnnotation)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(targetClassPredicate, targetFunctionPredicate, converterPredicate)
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
    ): List<FirOverloadCandidate> {
        val converters = collectConverters()
        if (converters.isEmpty()) {
            return emptyList()
        }
        val originals = collectMemberOriginals(owner, context)
        if (originals.isEmpty()) {
            return emptyList()
        }
        val existingJvmKeys = collectExistingJvmKeys(context)
        return buildCandidates(originals, existingJvmKeys, converters)
    }

    private fun findTopLevelCandidates(): List<FirOverloadCandidate> {
        val converters = collectConverters()
        if (converters.isEmpty()) {
            return emptyList()
        }
        val originals = collectTopLevelOriginals()
        if (originals.isEmpty()) {
            return emptyList()
        }
        val existingJvmKeys = originals
            .mapTo(linkedSetOf()) { function -> jvmSignatureKey(function, function.callableId.callableName) }
        return buildCandidates(originals, existingJvmKeys, converters)
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
                if (!processWholeClass && !hasAnnotation(function.fir, TransformOverloadPluginKeys.generateTransformOverloadsAnnotation)) {
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

    private fun collectConverters(): List<FirConverterSpec> {
        val rawConverters = session.predicateBasedProvider
            .getSymbolsByPredicate(converterPredicate)
            .mapNotNull { symbol -> symbol as? FirNamedFunctionSymbol }
            .filter { symbol -> symbol.fir.origin.fromSource }
            .sortedBy { symbol -> symbol.callableId.asFqNameForDebugInfo().asString() }
            .map(::createRawConverterSpec)

        val suffixUsage = rawConverters.groupBy { raw -> raw.defaultSuffix }
        return rawConverters.map { raw ->
            val uniqueSuffix = if ((suffixUsage[raw.defaultSuffix]?.size ?: 0) > 1) {
                raw.containerPrefix + raw.defaultSuffix
            } else {
                raw.defaultSuffix
            }
            FirConverterSpec(
                symbol = raw.symbol,
                parameterKind = raw.parameterKind,
                sourceType = raw.sourceType,
                targetType = raw.targetType,
                typeParameters = raw.typeParameters,
                uniqueSuffix = uniqueSuffix,
                callableIdText = raw.symbol.callableId.asFqNameForDebugInfo().asString(),
            )
        }
    }

    private fun createRawConverterSpec(symbol: FirNamedFunctionSymbol): RawFirConverterSpec {
        val fir = symbol.fir
        if (fir.contextParameters.isNotEmpty()) {
            invalidConverter(symbol, "context parameters are not supported")
        }
        if (fir.body == null) {
            invalidConverter(symbol, "converter body is required")
        }
        val hasExtensionReceiver = fir.receiverParameter != null
        val valueParameterCount = fir.valueParameters.size
        if (hasExtensionReceiver && valueParameterCount > 0) {
            invalidConverter(symbol, "extension converter cannot declare value parameters")
        }
        if (!hasExtensionReceiver && valueParameterCount != 1) {
            invalidConverter(symbol, "converter must declare exactly one source parameter")
        }

        val classId = symbol.callableId.classId
        if (classId != null) {
            val owner = session.getRegularClassSymbolByClassId(classId)
                ?: invalidConverter(symbol, "unable to resolve provider container $classId")
            if (!implementsTransformProvider(owner)) {
                invalidConverter(symbol, "member converter must be declared inside TransformProvider")
            }
            if (owner.fir.typeParameters.isNotEmpty()) {
                invalidConverter(symbol, "generic TransformProvider containers are not supported in v1")
            }
        }

        val sourceType = if (hasExtensionReceiver) {
            fir.receiverParameter!!.typeRef.coneType
        } else {
            fir.valueParameters.single().returnTypeRef.coneType
        }
        val parameterKind = if (hasExtensionReceiver) {
            ConverterParameterKind.EXTENSION_RECEIVER
        } else {
            ConverterParameterKind.VALUE_PARAMETER
        }
        return RawFirConverterSpec(
            symbol = symbol,
            parameterKind = parameterKind,
            sourceType = sourceType,
            targetType = fir.returnTypeRef.coneType,
            typeParameters = fir.typeParameters.map { typeParameter -> typeParameter.symbol },
            defaultSuffix = symbol.callableId.callableName.asString().toPascalCase(),
            containerPrefix = classId?.shortClassName?.asString() ?: "TopLevel",
        )
    }

    private fun implementsTransformProvider(
        classSymbol: FirClassSymbol<*>,
        visited: MutableSet<ClassId> = linkedSetOf(),
    ): Boolean {
        val classId = classSymbol.classId
        if (!visited.add(classId)) {
            return false
        }
        if (classId.asSingleFqName() == TransformOverloadPluginKeys.transformProvider) {
            return true
        }
        return classSymbol.resolvedSuperTypeRefs.any { typeRef ->
            val type = typeRef.coneType as? ConeClassLikeType ?: return@any false
            val superClassId = type.lookupTag.classId
            if (superClassId.asSingleFqName() == TransformOverloadPluginKeys.transformProvider) {
                return@any true
            }
            val superClass = session.getRegularClassSymbolByClassId(superClassId) ?: return@any false
            implementsTransformProvider(superClass, visited)
        }
    }

    private fun buildCandidates(
        originals: List<FirNamedFunctionSymbol>,
        existingJvmKeys: Set<String>,
        converters: List<FirConverterSpec>,
    ): List<FirOverloadCandidate> {
        val occupiedJvmKeys = linkedSetOf<String>().apply {
            addAll(existingJvmKeys)
            originals.forEach { original ->
                add(jvmSignatureKey(original, original.callableId.callableName))
            }
        }
        val generatedCandidates = mutableListOf<FirOverloadCandidate>()
        for (original in originals) {
            val parameterOptions = original.fir.valueParameters.mapIndexed { index, parameter ->
                if (parameter.isVararg) {
                    emptyList()
                } else {
                    findParameterTransforms(index, parameter.returnTypeRef.coneType, converters)
                }
            }
            val rawCandidates = enumerateCandidates(original, parameterOptions)
            for (candidate in rawCandidates) {
                val sameNameKey = jvmSignatureKey(
                    candidate.original,
                    candidate.generatedName,
                    candidate.parameterTransforms,
                )
                val mustRenameTopLevel = candidate.original.callableId.classId == null &&
                    candidate.generatedName == candidate.original.callableId.callableName
                if (mustRenameTopLevel || sameNameKey in occupiedJvmKeys) {
                    val renamed = candidate.copy(
                        generatedName = buildRenamedFunctionName(candidate.original, candidate.parameterTransforms),
                    )
                    val renamedKey = jvmSignatureKey(renamed.original, renamed.generatedName, renamed.parameterTransforms)
                    if (renamedKey in occupiedJvmKeys) {
                        error(
                            "Transform overload rename conflict for ${original.callableId.asFqNameForDebugInfo()} -> " +
                                "${renamed.generatedName}",
                        )
                    }
                    occupiedJvmKeys += renamedKey
                    generatedCandidates += renamed
                } else {
                    occupiedJvmKeys += sameNameKey
                    generatedCandidates += candidate
                }
            }
        }
        return generatedCandidates
    }

    private fun findParameterTransforms(
        parameterIndex: Int,
        parameterType: ConeKotlinType,
        converters: List<FirConverterSpec>,
    ): List<FirParameterTransform> {
        return converters.mapNotNull { converter ->
            createParameterTransform(parameterIndex, parameterType, converter)
        }
    }

    private fun createParameterTransform(
        parameterIndex: Int,
        parameterType: ConeKotlinType,
        converter: FirConverterSpec,
    ): FirParameterTransform? {
        val classLikeType = parameterType as? ConeClassLikeType
        if (classLikeType != null) {
            val liftKind = classLikeType.toLiftKind()
            if (liftKind != null) {
                val argument = classLikeType.typeArguments.singleOrNull() as? ConeKotlinTypeProjection
                if (argument != null) {
                    val liftedBindings = matchTypePattern(converter.targetType, argument.type)
                    if (liftedBindings != null) {
                        val liftedArgument = substituteType(converter.sourceType, liftedBindings)
                        val liftedType = classLikeType.lookupTag.classId.constructClassLikeType(
                            typeArguments = arrayOf(argument.replaceType(liftedArgument)),
                            isMarkedNullable = classLikeType.isMarkedNullable,
                        )
                        return FirParameterTransform(
                            converter = converter,
                            parameterIndex = parameterIndex,
                            generatedParameterType = liftedType,
                            liftKind = liftKind,
                        )
                    }
                }
            }
        }

        val directBindings = matchTypePattern(converter.targetType, parameterType) ?: return null
        val generatedType = substituteType(converter.sourceType, directBindings)
        return FirParameterTransform(
            converter = converter,
            parameterIndex = parameterIndex,
            generatedParameterType = generatedType,
            liftKind = LiftKind.NONE,
        )
    }

    private fun enumerateCandidates(
        original: FirNamedFunctionSymbol,
        parameterOptions: List<List<FirParameterTransform>>,
    ): List<FirOverloadCandidate> {
        if (parameterOptions.all { transforms -> transforms.isEmpty() }) {
            return emptyList()
        }
        val results = mutableListOf<FirOverloadCandidate>()
        fun walk(index: Int, chosen: MutableList<FirParameterTransform>) {
            if (index == parameterOptions.size) {
                if (chosen.isNotEmpty()) {
                    results += FirOverloadCandidate(
                        original = original,
                        generatedName = original.callableId.callableName,
                        parameterTransforms = chosen.sortedBy { transform -> transform.parameterIndex },
                    )
                }
                return
            }
            walk(index + 1, chosen)
            parameterOptions[index].forEach { transform ->
                chosen += transform
                walk(index + 1, chosen)
                chosen.removeAt(chosen.lastIndex)
            }
        }
        walk(0, mutableListOf())
        return results
    }

    private fun createGeneratedFunction(candidate: FirOverloadCandidate): FirNamedFunctionSymbol {
        val original = candidate.original.fir
        val transformedByIndex = candidate.parameterTransforms.associateBy { transform -> transform.parameterIndex }
        val generated = copyFirFunctionWithResolvePhase(
            original = original,
            callableId = candidate.original.callableId.copy(candidate.generatedName),
            key = TransformOverloadGeneratedDeclarationKey,
            firResolvePhase = FirResolvePhase.BODY_RESOLVE,
        ) {
            source = original.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
            annotations.clear()
            name = candidate.generatedName

            val ownerClassSymbol = candidate.original.callableId.classId?.let(session::getRegularClassSymbolByClassId)
            val ownerKind = (ownerClassSymbol?.fir as? FirRegularClass)?.classKind
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

            val updatedParameters = valueParameters.mapIndexed { index, parameter ->
                val transform = transformedByIndex[index]
                if (transform == null) {
                    parameter
                } else {
                    buildValueParameterCopy(parameter) {
                        returnTypeRef = transform.generatedParameterType.toFirResolvedTypeRef()
                        defaultValue = null
                        symbol = FirValueParameterSymbol()
                        containingDeclarationSymbol = this@copyFirFunctionWithResolvePhase.symbol
                        origin = TransformOverloadGeneratedDeclarationKey.origin
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

    private fun collectExistingJvmKeys(context: MemberGenerationContext): Set<String> {
        val declaredScope = context.declaredScope ?: return emptySet()
        val keys = linkedSetOf<String>()
        declaredScope.getCallableNames().forEach { callableName ->
            declaredScope.processFunctionsByName(callableName) { function ->
                keys += jvmSignatureKey(function, function.callableId.callableName)
            }
        }
        return keys
    }

    private fun jvmSignatureKey(
        function: FirNamedFunctionSymbol,
        name: Name,
        parameterTransforms: List<FirParameterTransform> = emptyList(),
    ): String {
        val transformedByIndex = parameterTransforms.associateBy { transform -> transform.parameterIndex }
        return buildString {
            append(name.asString())
            append("|")
            function.fir.valueParameters.forEachIndexed { index, parameter ->
                val type = transformedByIndex[index]?.generatedParameterType ?: parameter.returnTypeRef.coneType
                append(jvmErasure(type))
                append(";")
            }
        }
    }

    private fun buildRenamedFunctionName(
        original: FirNamedFunctionSymbol,
        transforms: List<FirParameterTransform>,
    ): Name {
        val suffix = transforms.joinToString(separator = "And") { transform ->
            transform.converter.uniqueSuffix
        }
        return Name.identifier("${original.callableId.callableName.asString()}Via$suffix")
    }

    private fun matchTypePattern(
        pattern: ConeKotlinType,
        actual: ConeKotlinType,
        bindings: MutableMap<FirTypeParameterSymbol, ConeKotlinType> = linkedMapOf(),
    ): Map<FirTypeParameterSymbol, ConeKotlinType>? {
        return when (pattern) {
            is ConeTypeParameterType -> {
                val symbol = pattern.lookupTag.typeParameterSymbol
                val existing = bindings[symbol]
                if (existing == null) {
                    bindings[symbol] = actual
                    bindings
                } else if (sameType(existing, actual)) {
                    bindings
                } else {
                    null
                }
            }

            is ConeClassLikeType -> {
                val actualClassLike = actual as? ConeClassLikeType ?: return null
                if (pattern.lookupTag.classId != actualClassLike.lookupTag.classId) {
                    return null
                }
                if (pattern.isMarkedNullable != actualClassLike.isMarkedNullable) {
                    return null
                }
                if (pattern.typeArguments.size != actualClassLike.typeArguments.size) {
                    return null
                }
                pattern.typeArguments.zip(actualClassLike.typeArguments).forEach { (patternArg, actualArg) ->
                    when {
                        patternArg is ConeStarProjection && actualArg is ConeStarProjection -> Unit
                        patternArg is ConeKotlinTypeProjection && actualArg is ConeKotlinTypeProjection -> {
                            if (patternArg.kind != actualArg.kind) {
                                return null
                            }
                            matchTypePattern(patternArg.type, actualArg.type, bindings) ?: return null
                        }

                        else -> return null
                    }
                }
                bindings
            }

            else -> {
                if (sameType(pattern, actual)) {
                    bindings
                } else {
                    null
                }
            }
        }
    }

    private fun substituteType(
        type: ConeKotlinType,
        substitution: Map<FirTypeParameterSymbol, ConeKotlinType>,
    ): ConeKotlinType {
        return substitutorByMap(substitution, session).substituteOrSelf(type)
    }

    private fun sameType(
        left: ConeKotlinType,
        right: ConeKotlinType,
    ): Boolean {
        return left.renderForDebugging() == right.renderForDebugging()
    }

    private fun isSupportedOriginalFunction(symbol: FirNamedFunctionSymbol): Boolean {
        val fir = symbol.fir
        if (fir.receiverParameter != null) {
            return false
        }
        if (fir.contextParameters.isNotEmpty()) {
            return false
        }
        if (hasAnnotation(fir, TransformOverloadPluginKeys.overloadTransformAnnotation)) {
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
            ?: error("Unable to locate Throwable(String) constructor for transform-overload stub")

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
                    value = "Transform overload stub body should be lowered in IR",
                    setType = true,
                )
                argumentList = buildResolvedArgumentList(
                    original = null,
                    mapping = linkedMapOf<FirExpression, org.jetbrains.kotlin.fir.declarations.FirValueParameter>().apply {
                        put(messageExpression, constructorSymbol.valueParameterSymbols.first().fir)
                    },
                )
            }
        }
        return buildSingleExpressionBlock(throwExpression)
    }

    private fun hasAnnotation(
        declaration: FirDeclaration,
        annotationFqName: FqName,
    ): Boolean {
        return declaration.annotations.any { annotation ->
            val type = annotation.annotationTypeRef.coneType as? ConeClassLikeType
            type?.lookupTag?.classId?.asSingleFqName() == annotationFqName
        }
    }

    private fun ConeClassLikeType.toLiftKind(): LiftKind? {
        return when (lookupTag.classId.asSingleFqName().asString()) {
            "kotlin.collections.Iterable" -> LiftKind.ITERABLE
            "kotlin.collections.Collection" -> LiftKind.COLLECTION
            "kotlin.collections.List" -> LiftKind.LIST
            "kotlin.collections.Set" -> LiftKind.SET
            "kotlin.sequences.Sequence" -> LiftKind.SEQUENCE
            else -> null
        }
    }

    private fun jvmErasure(type: ConeKotlinType): String {
        return when (type) {
            is ConeClassLikeType -> type.lookupTag.classId.asString()
            is ConeTypeParameterType -> "typeParameter"
            else -> type.renderForDebugging().substringBefore("<").substringBefore("?")
        }
    }

    private fun invalidConverter(
        symbol: FirNamedFunctionSymbol,
        reason: String,
    ): Nothing {
        throw IllegalStateException(
            "Invalid @OverloadTransform converter ${symbol.callableId.asFqNameForDebugInfo()}: $reason",
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

    private data class RawFirConverterSpec(
        val symbol: FirNamedFunctionSymbol,
        val parameterKind: ConverterParameterKind,
        val sourceType: ConeKotlinType,
        val targetType: ConeKotlinType,
        val typeParameters: List<FirTypeParameterSymbol>,
        val defaultSuffix: String,
        val containerPrefix: String,
    )
}
