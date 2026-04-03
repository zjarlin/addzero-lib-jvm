package site.addzero.kcp.spreadpack

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
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
import org.jetbrains.kotlin.fir.declarations.getStringArrayArgument
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildThrowExpression
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
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
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
        val annotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadPackAnnotationClassId, session)
            ?: return null
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
        val carrierClass = session.getRegularClassSymbolByClassId(carrierClassId)?.fir as? FirRegularClass
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

        val selectorKind = annotation.selectorKind()
        val excludedNames = annotation.getStringArrayArgument(Name.identifier("exclude"), session)
            ?.toSet()
            .orEmpty()
        val constructorParameters = primaryConstructor.valueParameters
        val constructorParameterNames = constructorParameters
            .map { valueParameter -> valueParameter.name.asString() }
            .toSet()
        val unknownExcludedNames = excludedNames - constructorParameterNames
        if (unknownExcludedNames.isNotEmpty()) {
            illegalTarget(
                owner,
                "unknown spread-pack exclude names for ${carrierClassId.shortClassName.asString()}: " +
                    unknownExcludedNames.sorted().joinToString(),
            )
        }

        val selectedParameters = constructorParameters.filter { constructorParameter ->
            shouldIncludeField(constructorParameter, selectorKind) &&
                constructorParameter.name.asString() !in excludedNames
        }
        val selectedNames = selectedParameters
            .map { constructorParameter -> constructorParameter.name.asString() }
            .toSet()
        constructorParameters.forEach { constructorParameter ->
            if (constructorParameter.name.asString() !in selectedNames && constructorParameter.defaultValue == null) {
                illegalTarget(
                    owner,
                    "spread-pack carrier ${carrierClassId.shortClassName.asString()} cannot omit required field " +
                        constructorParameter.name.asString(),
                )
            }
        }

        return FirSpreadPackExpansion(
            parameterIndex = parameterIndex,
            carrierClassId = carrierClassId,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            fields = selectedParameters.map { constructorParameter ->
                FirSpreadPackField(
                    parameter = constructorParameter,
                    resolvedType = constructorParameter.returnTypeRef.coneType,
                )
            },
        )
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
        return when (selectorKind) {
            SelectorKind.PROPS -> true
            SelectorKind.ATTRS -> !isFunctionLike(parameter.returnTypeRef.coneType)
            SelectorKind.CALLBACKS -> isFunctionLike(parameter.returnTypeRef.coneType)
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

    private fun jvmErasure(type: ConeKotlinType): String {
        return when (type) {
            is ConeClassLikeType -> type.lookupTag.classId.asString()
            else -> type.renderForDebugging().substringBefore("<").substringBefore("?")
        }
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
