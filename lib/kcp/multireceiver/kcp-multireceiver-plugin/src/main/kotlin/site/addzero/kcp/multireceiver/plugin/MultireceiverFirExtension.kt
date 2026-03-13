package site.addzero.kcp.multireceiver.plugin

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.builder.buildReceiverParameter
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameterCopy
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildThrowExpression
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated as lookupAnnotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.copyFirFunctionWithResolvePhase
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirReceiverParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isNullableString
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

@OptIn(SymbolInternals::class, ExperimentalTopLevelDeclarationsGenerationApi::class)
class MultireceiverFirExtension(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    private val targetFunctionPredicate = LookupPredicate.create {
        lookupAnnotated(MultireceiverPluginKeys.addGenerateExtensionAnnotation)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(targetFunctionPredicate)
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        if (!classSymbol.fir.origin.fromSource) {
            return emptySet()
        }
        return collectMemberCandidates(context)
            .mapTo(linkedSetOf()) { candidate -> candidate.original.callableId.callableName }
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        val candidates = if (context == null) {
            collectTopLevelCandidates().filter { candidate ->
                candidate.original.callableId.packageName == callableId.packageName &&
                    candidate.original.callableId.callableName == callableId.callableName
            }
        } else {
            collectMemberCandidates(context).filter { candidate ->
                candidate.original.callableId.callableName == callableId.callableName
            }
        }
        return candidates.map(::createGeneratedFunction)
    }

    override fun hasPackage(packageFqName: FqName): Boolean {
        return getTopLevelCallableIds().any { callableId -> callableId.packageName == packageFqName }
    }

    override fun getTopLevelCallableIds(): Set<CallableId> {
        return collectTopLevelCandidates()
            .mapTo(linkedSetOf()) { candidate -> candidate.original.callableId }
    }

    private fun collectMemberCandidates(
        context: MemberGenerationContext,
    ): List<FirWrapperCandidate> {
        val declaredScope = context.declaredScope ?: return emptyList()
        val candidates = linkedMapOf<String, FirWrapperCandidate>()
        declaredScope.getCallableNames().forEach { callableName ->
            declaredScope.processFunctionsByName(callableName) { function ->
                if (!isSupportedOriginalFunction(function)) {
                    return@processFunctionsByName
                }
                if (!hasAnnotation(function.fir, MultireceiverPluginKeys.addGenerateExtensionAnnotation)) {
                    return@processFunctionsByName
                }
                createCandidate(function)?.let { candidate ->
                    candidates[function.callableId.toString()] = candidate
                }
            }
        }
        return candidates.values.toList()
    }

    private fun collectTopLevelCandidates(): List<FirWrapperCandidate> {
        return session.predicateBasedProvider
            .getSymbolsByPredicate(targetFunctionPredicate)
            .mapNotNull { symbol -> symbol as? FirNamedFunctionSymbol }
            .filter { symbol ->
                symbol.callableId.classId == null &&
                    isSupportedOriginalFunction(symbol)
            }
            .sortedBy { symbol -> symbol.callableId.toString() }
            .mapNotNull(::createCandidate)
    }

    private fun createCandidate(
        symbol: FirNamedFunctionSymbol,
    ): FirWrapperCandidate? {
        val valueParameters = symbol.fir.valueParameters
        if (valueParameters.isEmpty()) {
            return null
        }

        if (valueParameters.size == 1) {
            val receiverParameter = valueParameters.single()
            if (receiverParameter.isVararg) {
                illegalTarget(symbol, "single generated receiver parameter cannot be vararg")
            }
            if (receiverParameter.defaultValue != null) {
                illegalTarget(symbol, "single generated receiver parameter cannot declare a default value")
            }
            return FirWrapperCandidate(
                original = symbol,
                generationKind = GenerationKind.EXTENSION,
                receiverParameterIndex = 0,
                contextParameterIndices = emptyList(),
                generatedJvmName = buildGeneratedJvmName(
                    symbol.callableId.callableName.asString(),
                    GenerationKind.EXTENSION,
                    listOf(receiverParameter.name.asString()),
                ),
            )
        }

        val contextParameterIndices = valueParameters.indices.filter { index ->
            hasAnnotation(valueParameters[index], MultireceiverPluginKeys.receiverAnnotation)
        }
        if (contextParameterIndices.isEmpty()) {
            return null
        }

        contextParameterIndices.forEach { index ->
            val parameter = valueParameters[index]
            if (parameter.isVararg) {
                illegalTarget(symbol, "context parameters cannot be vararg")
            }
            if (parameter.defaultValue != null) {
                illegalTarget(symbol, "context parameters cannot declare default values")
            }
        }

        return FirWrapperCandidate(
            original = symbol,
            generationKind = GenerationKind.CONTEXT,
            receiverParameterIndex = null,
            contextParameterIndices = contextParameterIndices,
            generatedJvmName = buildGeneratedJvmName(
                symbol.callableId.callableName.asString(),
                GenerationKind.CONTEXT,
                contextParameterIndices.map { index -> valueParameters[index].name.asString() },
            ),
        )
    }

    private fun createGeneratedFunction(
        candidate: FirWrapperCandidate,
    ): FirNamedFunctionSymbol {
        val original = candidate.original.fir
        val generated = copyFirFunctionWithResolvePhase(
            original = original,
            callableId = candidate.original.callableId,
            key = MultireceiverGeneratedDeclarationKey,
            firResolvePhase = FirResolvePhase.BODY_RESOLVE,
        ) {
            source = original.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
            name = candidate.original.callableId.callableName

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

            val copiedParameters = valueParameters.toList()
            val receiverIndex = candidate.receiverParameterIndex
            val contextIndices = candidate.contextParameterIndices.toSet()

            annotations.clear()
            annotations += original.annotations.filter(::keepFunctionAnnotation)
            annotations += createJvmNameAnnotation(
                generatedJvmName = candidate.generatedJvmName,
            )

            receiverParameter = receiverIndex?.let { index ->
                val parameter = copiedParameters[index]
                buildReceiverParameter {
                    source = parameter.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    moduleData = parameter.moduleData
                    origin = MultireceiverGeneratedDeclarationKey.origin
                    attributes = parameter.attributes.copy()
                    symbol = FirReceiverParameterSymbol()
                    typeRef = parameter.returnTypeRef
                    containingDeclarationSymbol = this@copyFirFunctionWithResolvePhase.symbol
                    annotations += parameter.annotations.filter(::keepParameterAnnotation)
                }
            }

            contextParameters.clear()
            candidate.contextParameterIndices.forEach { index ->
                val parameter = copiedParameters[index]
                contextParameters += buildValueParameterCopy(parameter) {
                    source = parameter.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    origin = MultireceiverGeneratedDeclarationKey.origin
                    symbol = FirValueParameterSymbol()
                    containingDeclarationSymbol = this@copyFirFunctionWithResolvePhase.symbol
                    defaultValue = null
                    annotations.clear()
                    annotations += parameter.annotations.filter(::keepParameterAnnotation)
                }
            }

            val remainingParameters = copiedParameters.filterIndexed { index, _ ->
                index != receiverIndex && index !in contextIndices
            }
            valueParameters.clear()
            remainingParameters.forEach { parameter ->
                valueParameters += buildValueParameterCopy(parameter) {
                    source = parameter.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    origin = MultireceiverGeneratedDeclarationKey.origin
                    symbol = FirValueParameterSymbol()
                    containingDeclarationSymbol = this@copyFirFunctionWithResolvePhase.symbol
                    annotations.clear()
                    annotations += parameter.annotations.filter(::keepParameterAnnotation)
                }
            }

            body = createStubBody()
        }
        return generated.symbol
    }

    private fun createJvmNameAnnotation(
        generatedJvmName: String,
    ): FirAnnotation {
        val annotationType = MultireceiverPluginKeys.jvmNameAnnotationClassId.constructClassLikeType(
            emptyArray(),
            false,
        )
        val argument = buildLiteralExpression(
            source = null,
            kind = ConstantValueKind.String,
            value = generatedJvmName,
            setType = true,
        )
        return buildAnnotation {
            annotationTypeRef = annotationType.toFirResolvedTypeRef()
            argumentMapping = buildAnnotationArgumentMapping {
                mapping[Name.identifier("name")] = argument
            }
        }
    }

    private fun keepFunctionAnnotation(
        annotation: FirAnnotation,
    ): Boolean {
        val fqName = annotation.annotationFqName() ?: return true
        return fqName != MultireceiverPluginKeys.addGenerateExtensionAnnotation &&
            fqName != MultireceiverPluginKeys.jvmNameAnnotation
    }

    private fun keepParameterAnnotation(
        annotation: FirAnnotation,
    ): Boolean {
        return annotation.annotationFqName() != MultireceiverPluginKeys.receiverAnnotation
    }

    private fun createStubBody(): FirBlock {
        val throwableType = session.builtinTypes.throwableType.coneType
        val constructorSymbol = throwableType.toRegularClassSymbol(session)
            ?.constructors(session)
            ?.firstOrNull { constructor ->
                constructor.valueParameterSymbols.size == 1 &&
                    constructor.valueParameterSymbols.first().resolvedReturnType.isNullableString
            }
            ?: error("Unable to locate Throwable(String) constructor for multireceiver stub")

        val messageExpression = buildLiteralExpression(
            source = null,
            kind = ConstantValueKind.String,
            value = MultireceiverPluginKeys.stubErrorMessage,
            setType = true,
        )
        val throwExpression = buildThrowExpression {
            exception = buildFunctionCall {
                coneTypeOrNull = throwableType
                calleeReference = buildResolvedNamedReference {
                    name = constructorSymbol.callableId.callableName
                    resolvedSymbol = constructorSymbol
                }
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

    private fun isSupportedOriginalFunction(
        symbol: FirNamedFunctionSymbol,
    ): Boolean {
        val fir = symbol.fir
        if (!fir.origin.fromSource) {
            return false
        }
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

    private fun hasAnnotation(
        declaration: FirDeclaration,
        annotationFqName: FqName,
    ): Boolean {
        return declaration.annotations.any { annotation ->
            annotation.annotationFqName() == annotationFqName
        }
    }

    private fun FirAnnotation.annotationFqName(): FqName? {
        val type = annotationTypeRef.coneType as? ConeClassLikeType ?: return null
        return type.lookupTag.classId.asSingleFqName()
    }

    private fun illegalTarget(
        symbol: FirNamedFunctionSymbol,
        reason: String,
    ): Nothing {
        throw IllegalStateException(
            "Invalid @AddGenerateExtension target ${symbol.callableId.asFqNameForDebugInfo()}: $reason",
        )
    }

    private fun buildGeneratedJvmName(
        functionName: String,
        generationKind: GenerationKind,
        parameterNames: List<String>,
    ): String {
        val suffix = parameterNames.joinToString(separator = "") { name ->
            name.toPascalCase()
        }.ifBlank { "Value" }
        return when (generationKind) {
            GenerationKind.EXTENSION -> "${functionName}ByAddzeroExtension$suffix"
            GenerationKind.CONTEXT -> "${functionName}ByAddzeroContext$suffix"
        }
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
