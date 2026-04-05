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
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.declarations.extractEnumValueArgumentInfo
import org.jetbrains.kotlin.fir.declarations.builder.buildProperty
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.findArgumentByName
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getTargetType
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.declarations.getStringArrayArgument
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyBackingField
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyGetter
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.declarations.validate
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
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildThrowExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirExpressionStub
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate.Companion.create
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated as lookupAnnotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.copyFirFunctionWithResolvePhase
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildPropertyFromParameterResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedBaseSymbol
import org.jetbrains.kotlin.fir.resolve.providers.dependenciesSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.SupertypeSupplier
import org.jetbrains.kotlin.fir.resolve.TypeResolutionConfiguration
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.resolve.typeResolver
import org.jetbrains.kotlin.fir.scopes.createImportingScopes
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.impl.FirMemberTypeParameterScope
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeErrorType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjectionIn
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjectionOut
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.isNullableString
import org.jetbrains.kotlin.fir.types.renderForDebugging
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.toLookupTag
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.text
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.fir.declarations.FirPropertyBodyResolveState

private data class FirCarrierMetadata(
    val classId: ClassId,
    val regularClass: FirRegularClass,
    val primaryConstructor: FirConstructor,
)

private data class FirAnnotatedCarrierRequest(
    val regularClass: FirRegularClass,
    val classId: ClassId,
    val fields: List<FirFlattenedFieldSpec>,
)

private data class FirFlattenedFieldSpec(
    val name: Name,
    val resolvedType: ConeKotlinType,
    val defaultValue: FirExpression?,
)

private data class FirReferencedParameterType(
    val classId: ClassId?,
    val qualifierText: String?,
)

private data class FirSpreadArgsReference(
    val functionFqName: String,
    val parameterTypes: List<FirReferencedParameterType>,
)

private data class FirValidationTarget(
    val messagePrefix: String,
)

private data class FirGeneratedCarrierRequest(
    val owner: FirNamedFunctionSymbol,
    val parameter: FirValueParameter,
    val classId: ClassId,
    val fields: List<FirFlattenedFieldSpec>,
)

private val composableAnnotationClassId = ClassId.topLevel(
    FqName("androidx.compose.runtime.Composable"),
)

@OptIn(
    SymbolInternals::class,
    ExperimentalTopLevelDeclarationsGenerationApi::class,
    DirectDeclarationsAccess::class,
)
class SpreadPackFirExtension(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    private val typeResolutionScopeSession = ScopeSession()

    private val targetClassPredicate = DeclarationPredicate.create {
        annotated(SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotation)
    }

    private val carrierClassPredicate = DeclarationPredicate.create {
        annotated(SpreadPackPluginKeys.spreadPackCarrierOfAnnotation)
    }

    private val targetFunctionPredicate = LookupPredicate.create {
        lookupAnnotated(SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotation)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(targetClassPredicate, carrierClassPredicate, targetFunctionPredicate)
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        val generatedCarrierRequest = findGeneratedCarrierRequest(classSymbol)
        if (generatedCarrierRequest != null) {
            return linkedSetOf<Name>().apply {
                add(SpecialNames.INIT)
                addAll(generatedCarrierRequest.fields.map { field -> field.name })
            }
        }
        if (!classSymbol.fir.origin.fromSource) {
            return emptySet()
        }
        return linkedSetOf<Name>().apply {
            addAll(
                findMemberCandidates(classSymbol, context)
                    .map { candidate -> candidate.generatedName },
            )
            val carrierRequest = findAnnotatedCarrierRequest(classSymbol)
            if (carrierRequest != null) {
                add(SpecialNames.INIT)
                addAll(carrierRequest.fields.map { field -> field.name })
            }
        }
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

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        if (!classSymbol.fir.origin.fromSource) {
            return emptySet()
        }
        return findMemberGeneratedCarrierRequests(classSymbol)
            .mapTo(linkedSetOf()) { request -> request.classId.shortClassName }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        return findMemberGeneratedCarrierRequests(owner)
            .firstOrNull { request -> request.classId.shortClassName == name }
            ?.let(::createGeneratedCarrierClass)
    }

    override fun hasPackage(packageFqName: FqName): Boolean {
        return collectTopLevelOriginals().any { symbol -> symbol.callableId.packageName == packageFqName } ||
            getTopLevelClassIds().any { classId -> classId.packageFqName == packageFqName }
    }

    override fun getTopLevelCallableIds(): Set<CallableId> {
        return findTopLevelCandidates()
            .mapTo(linkedSetOf()) { candidate ->
                candidate.original.callableId.copy(candidate.generatedName)
            }
    }

    override fun getTopLevelClassIds(): Set<ClassId> {
        return findTopLevelGeneratedCarrierClassIds().toCollection(linkedSetOf())
    }

    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        return findTopLevelGeneratedCarrierRequests()
            .firstOrNull { request -> request.classId == classId }
            ?.let(::createGeneratedCarrierClass)
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
                    parameterTypes = function.fir.valueParameters.map { parameter -> parameterTypeKey(parameter.returnTypeRef) },
                )
        }
        return buildCandidates(originals, existingJvmKeys)
    }

    private fun findTopLevelGeneratedCarrierRequests(): List<FirGeneratedCarrierRequest> {
        return buildGeneratedCarrierRequests(collectTopLevelOriginals())
    }

    private fun findTopLevelGeneratedCarrierClassIds(): List<ClassId> {
        return collectTopLevelOriginals().flatMap { original ->
            original.fir.valueParameters.mapNotNull { parameter ->
                val annotation = parameter.getSpreadPackOfAnnotation() ?: return@mapNotNull null
                generatedCarrierClassId(
                    owner = original,
                    parameter = parameter,
                    annotation = annotation,
                )
            }
        }
    }

    private fun findMemberGeneratedCarrierRequests(
        owner: FirClassSymbol<*>,
    ): List<FirGeneratedCarrierRequest> {
        val regularClass = owner.fir as? FirRegularClass ?: return emptyList()
        val processWholeClass = session.predicateBasedProvider.matches(targetClassPredicate, owner)
        val originals = regularClass.declarations
            .filterIsInstance<FirNamedFunction>()
            .map { function -> function.symbol }
            .filter { symbol ->
                symbol.fir.origin.fromSource &&
                    isSupportedOriginalFunction(symbol) &&
                    (processWholeClass ||
                        hasAnnotation(symbol.fir, SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotationClassId))
            }
            .sortedBy { symbol -> symbol.callableId.toString() }
        return buildGeneratedCarrierRequests(originals)
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
        val originals = session.firProvider.symbolProvider.symbolNamesProvider.getPackageNames()
            .orEmpty()
            .asSequence()
            .map(::FqName)
            .flatMap { packageFqName ->
                session.firProvider.getFirFilesByPackage(packageFqName).asSequence()
            }
            .flatMap(FirFile::declarations)
            .filterIsInstance<FirNamedFunction>()
            .map(FirNamedFunction::symbol)
            .filter { symbol ->
                symbol.fir.origin.fromSource &&
                    symbol.callableId.classId == null &&
                    isSupportedOriginalFunction(symbol) &&
                    hasAnnotation(symbol.fir, SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotationClassId)
            }
            .distinctBy { symbol -> symbol.callableId.toString() }
            .sortedBy { symbol -> symbol.callableId.toString() }
            .toList()
        return originals
    }

    private fun buildGeneratedCarrierRequests(
        originals: List<FirNamedFunctionSymbol>,
    ): List<FirGeneratedCarrierRequest> {
        val requests = originals.flatMap { original ->
            original.fir.valueParameters.mapNotNull { parameter ->
                val annotation = parameter.getSpreadPackOfAnnotation() ?: return@mapNotNull null
                buildGeneratedCarrierRequest(
                    owner = original,
                    parameter = parameter,
                    annotation = annotation,
                )
            }
        }
        validateGeneratedCarrierConflicts(requests)
        return requests
    }

    private fun validateGeneratedCarrierConflicts(
        requests: List<FirGeneratedCarrierRequest>,
    ) {
        val requestsByClassId = requests.groupBy { request -> request.classId }
        requestsByClassId.forEach { (classId, classRequests) ->
            if (classRequests.size <= 1) {
                return@forEach
            }
            val signatures = classRequests.map { request ->
                request.fields.joinToString(separator = "|") { field ->
                    "${field.name.asString()}:${jvmErasure(field.resolvedType)}:${field.defaultValue != null}"
                }
            }.toSet()
            if (signatures.size > 1) {
                error(
                    "Conflicting generated spread-pack carrier definitions for ${classId.asString()}: " +
                        classRequests.joinToString { request -> request.owner.callableId.asFqNameForDebugInfo().asString() },
                )
            }
        }
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol> {
        val memberContext = context ?: return emptyList()
        val annotatedRequest = findAnnotatedCarrierRequest(memberContext.owner)
        if (annotatedRequest != null) {
            val field = annotatedRequest.fields.firstOrNull { candidate -> candidate.name == callableId.callableName }
                ?: return emptyList()
            return listOf(buildAnnotatedCarrierProperty(annotatedRequest, field).symbol)
        }
        val generatedRequest = findGeneratedCarrierRequest(memberContext.owner) ?: return emptyList()
        val field = generatedRequest.fields.firstOrNull { candidate -> candidate.name == callableId.callableName }
            ?: return emptyList()
        return listOf(buildGeneratedCarrierProperty(memberContext.owner, field).symbol)
    }

    override fun generateConstructors(
        context: MemberGenerationContext,
    ): List<FirConstructorSymbol> {
        val annotatedRequest = findAnnotatedCarrierRequest(context.owner)
        if (annotatedRequest != null) {
            return listOf(buildAnnotatedCarrierConstructor(annotatedRequest).symbol)
        }
        val generatedRequest = findGeneratedCarrierRequest(context.owner) ?: return emptyList()
        val constructor = createConstructor(
            owner = context.owner,
            key = SpreadPackGeneratedDeclarationKey,
            isPrimary = true,
            generateDelegatedNoArgConstructorCall = true,
        ) {
            generatedRequest.fields.forEach { field ->
                valueParameter(
                    name = field.name,
                    type = field.resolvedType,
                    hasDefaultValue = field.defaultValue != null,
                )
            }
        }
        constructor.valueParameters.zip(generatedRequest.fields).forEach { (parameter, field) ->
            parameter.replaceDefaultValue(generatedDefaultValueOrNull(field.defaultValue))
        }
        return listOf(constructor.symbol)
    }

    private fun findAnnotatedCarrierRequest(
        owner: FirClassSymbol<*>,
    ): FirAnnotatedCarrierRequest? {
        val regularClass = owner.fir as? FirRegularClass ?: return null
        return buildAnnotatedCarrierRequest(regularClass)
    }

    private fun findGeneratedCarrierRequest(
        owner: FirClassSymbol<*>,
    ): FirGeneratedCarrierRequest? {
        val ownerClassId = (owner as? FirRegularClassSymbol)?.classId ?: return null
        findTopLevelGeneratedCarrierRequests()
            .firstOrNull { request -> request.classId == ownerClassId }
            ?.let { request -> return request }
        val outerClassId = ownerClassId.outerClassId ?: return null
        val outerClassSymbol = session.getRegularClassSymbolByClassId(outerClassId) ?: return null
        return findMemberGeneratedCarrierRequests(outerClassSymbol)
            .firstOrNull { request -> request.classId == ownerClassId }
    }

    private fun buildAnnotatedCarrierRequest(
        regularClass: FirRegularClass,
    ): FirAnnotatedCarrierRequest? {
        val annotation = regularClass.annotations.getAnnotationByClassId(
            SpreadPackPluginKeys.spreadPackCarrierOfAnnotationClassId,
            session,
        ) ?: return null
        val target = carrierTarget(regularClass)
        val classId = regularClass.symbol.classId
        if (classId.isLocal) {
            target.invalid("annotated spread-pack carrier must not be local: ${classId.asString()}")
        }
        if (regularClass.classKind != ClassKind.CLASS) {
            target.invalid("spread-pack carrier ${classId.asString()} must be a class")
        }
        if (regularClass.typeParameters.isNotEmpty()) {
            target.invalid("generic spread-pack carriers are not supported in v1: ${classId.asString()}")
        }
        val sourceConstructorsWithParameters = regularClass.declarations
            .filterIsInstance<FirConstructor>()
            .filter { constructor -> constructor.origin.fromSource }
            .filter { constructor -> constructor.valueParameters.isNotEmpty() }
        if (sourceConstructorsWithParameters.isNotEmpty()) {
            target.invalid("annotated spread-pack carrier ${classId.asString()} must not declare constructor parameters")
        }
        val referencedOverload = resolveReferencedOverload(target, annotation)
        val overloadKey = overloadKey(referencedOverload)
        val flattenedFields = flattenFunctionParameters(
            target = target,
            function = referencedOverload,
            visitedOverloads = linkedSetOf(overloadKey),
        )
        val selectedFields = selectFlattenedFields(
            target = target,
            fields = flattenedFields,
            selectorKind = annotation.selectorKind(),
            excludedNames = annotation.excludeNames(),
            contextLabel = referencedOverload.callableId.asFqNameForDebugInfo().asString(),
        )
        val existingProperties = regularClass.declarations
            .filterIsInstance<org.jetbrains.kotlin.fir.declarations.FirProperty>()
            .filter { property -> property.origin.fromSource }
            .map { property -> property.name.asString() }
            .toSet()
        val conflictingProperties = selectedFields
            .map { field -> field.name.asString() }
            .filter(existingProperties::contains)
        if (conflictingProperties.isNotEmpty()) {
            target.invalid(
                "annotated spread-pack carrier ${classId.asString()} already declares properties " +
                    conflictingProperties.distinct().sorted().joinToString(),
            )
        }
        return FirAnnotatedCarrierRequest(
            regularClass = regularClass,
            classId = classId,
            fields = selectedFields,
        )
    }

    private fun buildAnnotatedCarrierConstructor(
        request: FirAnnotatedCarrierRequest,
    ): FirConstructor {
        val constructor = createConstructor(
            owner = request.regularClass.symbol,
            key = SpreadPackGeneratedDeclarationKey,
            isPrimary = false,
            generateDelegatedNoArgConstructorCall = true,
        ) {
            request.fields.forEach { field ->
                valueParameter(
                    name = field.name,
                    type = field.resolvedType,
                    hasDefaultValue = field.defaultValue != null,
                )
            }
        }
        constructor.valueParameters.zip(request.fields).forEach { (parameter, field) ->
            parameter.replaceDefaultValue(generatedDefaultValueOrNull(field.defaultValue))
        }
        return constructor
    }

    private fun buildAnnotatedCarrierProperty(
        request: FirAnnotatedCarrierRequest,
        field: FirFlattenedFieldSpec,
    ): org.jetbrains.kotlin.fir.declarations.FirProperty {
        return createMemberProperty(
            owner = request.regularClass.symbol,
            key = SpreadPackGeneratedDeclarationKey,
            name = field.name,
            returnType = field.resolvedType,
            isVal = false,
            hasBackingField = true,
        )
    }

    private fun buildGeneratedCarrierProperty(
        owner: FirClassSymbol<*>,
        field: FirFlattenedFieldSpec,
    ): org.jetbrains.kotlin.fir.declarations.FirProperty {
        return createMemberProperty(
            owner = owner,
            key = SpreadPackGeneratedDeclarationKey,
            name = field.name,
            returnType = field.resolvedType,
            isVal = false,
            hasBackingField = true,
        )
    }

    private fun buildGeneratedCarrierRequest(
        owner: FirNamedFunctionSymbol,
        parameter: FirValueParameter,
        annotation: FirAnnotation,
    ): FirGeneratedCarrierRequest {
        val target = functionTarget(owner)
        if (parameter.annotations.getAnnotationByClassId(SpreadPackPluginKeys.spreadPackAnnotationClassId, session) != null) {
            target.invalid("parameter ${parameter.name.asString()} cannot combine @SpreadPackOf with @SpreadPack")
        }
        if (parameter.annotations.getAnnotationByClassId(SpreadPackPluginKeys.spreadArgsOfAnnotationClassId, session) != null) {
            target.invalid("parameter ${parameter.name.asString()} cannot combine @SpreadPackOf with @SpreadArgsOf")
        }
        val generatedClassId = generatedCarrierClassId(owner, parameter, annotation)
        val referencedOverload = resolveReferencedOverload(target, annotation)
        val overloadKey = overloadKey(referencedOverload)
        val flattenedFields = flattenFunctionParameters(
            target = target,
            function = referencedOverload,
            visitedOverloads = linkedSetOf(overloadKey),
        )
        val selectedFields = selectFlattenedFields(
            target = target,
            fields = flattenedFields,
            selectorKind = annotation.selectorKind(),
            excludedNames = annotation.excludeNames(),
            contextLabel = referencedOverload.callableId.asFqNameForDebugInfo().asString(),
        )
        return FirGeneratedCarrierRequest(
            owner = owner,
            parameter = parameter,
            classId = generatedClassId,
            fields = selectedFields,
        )
    }

    private fun buildGeneratedCarrierFields(
        request: FirGeneratedCarrierRequest,
    ): List<FirSpreadPackField> {
        val constructorSymbol = FirConstructorSymbol(request.classId)
        val moduleData = request.owner.fir.moduleData
        return request.fields.map { field ->
            FirSpreadPackField(
                parameter = buildValueParameter {
                    source = request.parameter.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
                    this.moduleData = moduleData
                    origin = SpreadPackGeneratedDeclarationKey.origin
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    returnTypeRef = field.resolvedType.toFirResolvedTypeRef()
                    name = field.name
                    symbol = FirValueParameterSymbol()
                    defaultValue = generatedDefaultValueOrNull(field.defaultValue)
                    containingDeclarationSymbol = constructorSymbol
                },
                resolvedType = field.resolvedType,
            )
        }
    }

    private fun generatedCarrierClassId(
        owner: FirNamedFunctionSymbol,
        parameter: FirValueParameter,
        annotation: FirAnnotation,
    ): ClassId {
        val explicitName = annotation.stringArgumentOrNull("generatedClassName")
        val classIdFromType = (parameter.returnTypeRef as? FirResolvedTypeRef)
            ?.coneType
            ?.let { coneType -> coneType as? ConeClassLikeType }
            ?.lookupTag
            ?.classId
            ?.takeIf(::isUsableGeneratedCarrierClassId)
        val classId = when {
            explicitName != null -> {
                val shortName = Name.identifier(explicitName)
                owner.callableId.classId
                    ?.createNestedClassId(shortName)
                    ?: ClassId.topLevel(owner.callableId.packageName.child(shortName))
            }
            classIdFromType != null -> classIdFromType
            else -> inferGeneratedCarrierClassIdFromSourceTypeReference(owner, parameter)
        } ?: illegalTarget(
            owner,
            "parameter ${parameter.name.asString()} annotated with @SpreadPackOf must use a resolvable class type, " +
                "an unresolved simple class name, or specify generatedClassName",
        )
        if (classId.isLocal) {
            illegalTarget(
                owner,
                "generated spread-pack carrier for ${parameter.name.asString()} must not be local: ${classId.asString()}",
            )
        }
        return classId
    }

    private fun inferGeneratedCarrierClassIdFromSourceTypeReference(
        owner: FirNamedFunctionSymbol,
        parameter: FirValueParameter,
    ): ClassId? {
        val rawTypeText = parameter.returnTypeRef.source?.text
            ?.toString()
            ?.substringBefore("<")
            ?.removeSuffix("?")
            ?.trim()
            ?.takeIf { typeText -> typeText.isNotBlank() }
            ?: return null
        if (!isSimpleGeneratedCarrierTypeName(rawTypeText)) {
            return null
        }
        val shortName = Name.identifier(rawTypeText)
        return owner.callableId.classId
            ?.createNestedClassId(shortName)
            ?: ClassId.topLevel(owner.callableId.packageName.child(shortName))
    }

    private fun isSimpleGeneratedCarrierTypeName(
        typeText: String,
    ): Boolean {
        if ('.' in typeText) {
            return false
        }
        return typeText.firstOrNull()?.let { firstChar ->
            (firstChar == '_' || firstChar.isLetter()) &&
                typeText.drop(1).all { char -> char == '_' || char.isLetterOrDigit() }
        } == true
    }

    private fun isUsableGeneratedCarrierClassId(
        classId: ClassId,
    ): Boolean {
        if (classId.isLocal) {
            return false
        }
        return classId.shortClassName.asString() != "<error>"
    }

    private fun createGeneratedCarrierClass(
        request: FirGeneratedCarrierRequest,
    ): FirRegularClassSymbol {
        val ownerClassSymbol = request.owner.callableId.classId?.let(session::getRegularClassSymbolByClassId)
        val visibility = request.owner.fir.status.visibility.takeUnless { candidate ->
            candidate == Visibilities.Unknown
        } ?: Visibilities.Public
        val effectiveVisibility = ownerClassSymbol?.let { classSymbol ->
            visibility.toEffectiveVisibility(classSymbol, false, false)
        } ?: when (visibility) {
            Visibilities.Public -> EffectiveVisibility.Public
            Visibilities.Internal -> EffectiveVisibility.Internal
            else -> EffectiveVisibility.Public
        }
        val status = FirResolvedDeclarationStatusImpl(
            visibility = visibility,
            modality = Modality.FINAL,
            effectiveVisibility = effectiveVisibility,
        ).apply {
            isExpect = request.owner.fir.status.isExpect
            isActual = request.owner.fir.status.isActual
        }
        val generatedClass = createTopLevelClass(
            classId = request.classId,
            key = SpreadPackGeneratedDeclarationKey,
            classKind = ClassKind.CLASS,
        ) {
            source = request.parameter.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
            this.visibility = visibility
            this.modality = Modality.FINAL
            status {
                isExpect = request.owner.fir.status.isExpect
                isActual = request.owner.fir.status.isActual
            }
        }
        generatedClass.replaceStatus(status)
        return generatedClass.symbol
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
                parameterTypes = rawCandidate.generatedParameterTypes.map(::jvmErasure),
            )
            if (sameNameKey in occupiedJvmKeys) {
                val renamed = rawCandidate.copy(
                    generatedName = buildRenamedFunctionName(rawCandidate.original, rawCandidate.expansions),
                )
                val renamedKey = jvmSignatureKey(
                    name = renamed.generatedName,
                    parameterTypes = renamed.generatedParameterTypes.map(::jvmErasure),
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
                generatedParameterTypes += resolveRequiredType(
                    target = functionTarget(original),
                    typeRef = parameter.returnTypeRef,
                    owner = original,
                    contextLabel = "parameter ${parameter.name.asString()} of ${original.callableId.asFqNameForDebugInfo()}",
                )
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
        val target = functionTarget(owner)
        val spreadPackOfAnnotation = parameter.getSpreadPackOfAnnotation()
        val spreadPackAnnotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadPackAnnotationClassId, session)
        if (spreadPackAnnotation == null && spreadPackOfAnnotation == null) {
            return null
        }
        if (spreadPackOfAnnotation != null) {
            val generatedCarrier = buildGeneratedCarrierRequest(owner, parameter, spreadPackOfAnnotation)
            return FirSpreadPackExpansion(
                parameterIndex = parameterIndex,
                carrierClassId = generatedCarrier.classId,
                selectorKind = SelectorKind.PROPS,
                excludedNames = emptySet(),
                fields = buildGeneratedCarrierFields(generatedCarrier),
            )
        }
        val carrier = resolveCarrierMetadata(target, parameter)
        val baseSpreadPackAnnotation = spreadPackAnnotation
            ?: error("spread-pack annotation missing for ${parameter.name.asString()}")
        val spreadArgsAnnotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadArgsOfAnnotationClassId, session)
        val selectorKind = spreadArgsAnnotation?.selectorKind()
            ?: baseSpreadPackAnnotation.selectorKind()
        val excludedNames = spreadArgsAnnotation?.excludeNames()
            ?: baseSpreadPackAnnotation.excludeNames()
        if (spreadArgsAnnotation != null && !baseSpreadPackAnnotation.isDefaultSpreadPackConfiguration()) {
            target.invalid(
                "parameter ${parameter.name.asString()} must keep @SpreadPack at default values when @SpreadArgsOf is present",
            )
        }

        val fields = if (spreadArgsAnnotation == null) {
            buildCarrierFields(
                target = target,
                carrier = carrier,
                selectorKind = selectorKind,
                excludedNames = excludedNames,
            )
        } else {
            val referencedOverload = resolveReferencedOverload(target, spreadArgsAnnotation)
            val overloadKey = overloadKey(referencedOverload)
            val flattenedFields = flattenFunctionParameters(
                target = target,
                function = referencedOverload,
                visitedOverloads = linkedSetOf(overloadKey),
            )
            buildReferencedCarrierFields(
                target = target,
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
        target: FirValidationTarget,
        parameter: FirValueParameter,
    ): FirCarrierMetadata {
        val parameterType = resolveRequiredType(
            target = target,
            typeRef = parameter.returnTypeRef,
            owner = parameter.containingDeclarationSymbol as? FirCallableSymbol<*>,
            contextLabel = "spread-pack parameter ${parameter.name.asString()}",
        ) as? ConeClassLikeType
            ?: target.invalid(
                "spread-pack parameter ${parameter.name.asString()} must reference a regular class with a primary constructor",
            )
        if (parameterType.typeArguments.isNotEmpty()) {
            target.invalid("generic spread-pack carriers are not supported in v1: ${parameter.name.asString()}")
        }
        val carrierClassId = parameterType.lookupTag.classId
        val carrierClass = resolveRegularClassById(carrierClassId)
            ?: target.invalid("unable to resolve spread-pack carrier ${carrierClassId.asString()}")
        if (carrierClass.classKind != ClassKind.CLASS) {
            target.invalid("spread-pack carrier ${carrierClassId.asString()} must be a class")
        }
        if (carrierClass.typeParameters.isNotEmpty()) {
            target.invalid("generic spread-pack carriers are not supported in v1: ${carrierClassId.asString()}")
        }
        val annotatedCarrier = buildAnnotatedCarrierRequest(carrierClass)
        if (annotatedCarrier != null) {
            return FirCarrierMetadata(
                classId = carrierClassId,
                regularClass = carrierClass,
                primaryConstructor = buildAnnotatedCarrierConstructor(annotatedCarrier),
            )
        }
        val primaryConstructor = carrierClass.declarations
            .filterIsInstance<FirConstructor>()
            .firstOrNull { constructor -> constructor.isPrimary }
            ?: target.invalid("spread-pack carrier ${carrierClassId.asString()} must declare a primary constructor")
        return FirCarrierMetadata(
            classId = carrierClassId,
            regularClass = carrierClass,
            primaryConstructor = primaryConstructor,
        )
    }

    private fun buildCarrierFields(
        target: FirValidationTarget,
        carrier: FirCarrierMetadata,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
    ): List<FirSpreadPackField> {
        val constructorParameters = carrier.primaryConstructor.valueParameters
        val resolvedConstructorParameters = constructorParameters.map { constructorParameter ->
            constructorParameter to resolveRequiredType(
                target = target,
                typeRef = constructorParameter.returnTypeRef,
                owner = carrier.primaryConstructor.symbol,
                contextLabel = "spread-pack carrier ${carrier.classId.asString()} field ${constructorParameter.name.asString()}",
            )
        }
        validateExcludedNames(
            target = target,
            excludedNames = excludedNames,
            availableNames = constructorParameters.map { constructorParameter -> constructorParameter.name.asString() },
            contextLabel = carrier.classId.shortClassName.asString(),
        )
        val selectedParameters = resolvedConstructorParameters.filter { (constructorParameter, resolvedType) ->
            shouldIncludeField(resolvedType, selectorKind) &&
                constructorParameter.name.asString() !in excludedNames
        }
        validateCarrierOmissions(
            target = target,
            carrier = carrier,
            selectedCarrierNames = selectedParameters.map { (constructorParameter, _) -> constructorParameter.name.asString() }.toSet(),
        )
        return selectedParameters.map { (constructorParameter, resolvedType) ->
            FirSpreadPackField(
                parameter = constructorParameter,
                resolvedType = resolvedType,
            )
        }
    }

    private fun buildReferencedCarrierFields(
        target: FirValidationTarget,
        carrier: FirCarrierMetadata,
        flattenedFields: List<FirFlattenedFieldSpec>,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
        referenceDescription: String,
    ): List<FirSpreadPackField> {
        val selectedFields = selectFlattenedFields(
            target = target,
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
                ?: target.invalid(
                    "spread-pack carrier ${carrier.classId.shortClassName.asString()} is missing argsof field $fieldName from $referenceDescription",
                )
            val carrierParameterType = resolveRequiredType(
                target = target,
                typeRef = carrierParameter.returnTypeRef,
                owner = carrier.primaryConstructor.symbol,
                contextLabel = "spread-pack carrier ${carrier.classId.asString()} field $fieldName",
            )
            if (!sameConeType(carrierParameterType, field.resolvedType)) {
                target.invalid(
                    "spread-pack carrier ${carrier.classId.shortClassName.asString()} field $fieldName " +
                        "type ${carrierParameterType.renderForDebugging()} does not match " +
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
                target = target,
                carrier = carrier,
                selectedCarrierNames = selectedCarrierNames,
            )
            validateUniqueFieldNames(
                target = target,
                names = fields.map { field -> field.parameter.name.asString() },
                contextLabel = referenceDescription,
            )
        }
    }

    private fun flattenFunctionParameters(
        target: FirValidationTarget,
        function: FirNamedFunctionSymbol,
        visitedOverloads: Set<String>,
    ): List<FirFlattenedFieldSpec> {
        if (!isSupportedReferencedFunction(function)) {
            target.invalid(
                "argsof target ${function.callableId.asFqNameForDebugInfo()} must not declare receivers or context parameters",
            )
        }
        return function.fir.valueParameters.flatMap { referencedParameter ->
            flattenValueParameter(
                target = target,
                owner = function,
                parameter = referencedParameter,
                visitedOverloads = visitedOverloads,
            )
        }.also { fields ->
            validateUniqueFieldNames(
                target = target,
                names = fields.map { field -> field.name.asString() },
                contextLabel = function.callableId.asFqNameForDebugInfo().asString(),
            )
        }
    }

    private fun flattenValueParameter(
        target: FirValidationTarget,
        owner: FirCallableSymbol<*>,
        parameter: FirValueParameter,
        visitedOverloads: Set<String>,
    ): List<FirFlattenedFieldSpec> {
        val spreadPackAnnotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadPackAnnotationClassId, session)
            ?: return listOf(
                FirFlattenedFieldSpec(
                    name = parameter.name,
                    resolvedType = resolveRequiredType(
                        target = target,
                        typeRef = parameter.returnTypeRef,
                        owner = owner,
                        contextLabel = "parameter ${parameter.name.asString()}",
                    ),
                    defaultValue = generatedDefaultValueOrNull(parameter.defaultValue),
                ),
            )
        val carrier = resolveCarrierMetadata(target, parameter)
        val spreadArgsAnnotation = parameter.annotations
            .getAnnotationByClassId(SpreadPackPluginKeys.spreadArgsOfAnnotationClassId, session)
        val selectorKind = spreadArgsAnnotation?.selectorKind()
            ?: spreadPackAnnotation.selectorKind()
        val excludedNames = spreadArgsAnnotation?.excludeNames()
            ?: spreadPackAnnotation.excludeNames()
        if (spreadArgsAnnotation != null && !spreadPackAnnotation.isDefaultSpreadPackConfiguration()) {
            target.invalid(
                "nested spread-pack parameter ${parameter.name.asString()} must keep @SpreadPack at default values when @SpreadArgsOf is present",
            )
        }
        if (spreadArgsAnnotation == null) {
            return buildCarrierFields(
                target = target,
                carrier = carrier,
                selectorKind = selectorKind,
                excludedNames = excludedNames,
            ).map { field ->
                FirFlattenedFieldSpec(
                    name = field.parameter.name,
                    resolvedType = field.resolvedType,
                    defaultValue = generatedDefaultValueOrNull(field.parameter.defaultValue),
                )
            }
        }

        val referencedOverload = resolveReferencedOverload(target, spreadArgsAnnotation)
        val overloadKey = overloadKey(referencedOverload)
        if (overloadKey in visitedOverloads) {
            target.invalid(
                "detected argsof overload cycle at ${referencedOverload.callableId.asFqNameForDebugInfo()}",
            )
        }
        val flattenedFields = flattenFunctionParameters(
            target = target,
            function = referencedOverload,
            visitedOverloads = visitedOverloads + overloadKey,
        )
        return buildReferencedCarrierFields(
            target = target,
            carrier = carrier,
            flattenedFields = flattenedFields,
            selectorKind = selectorKind,
            excludedNames = excludedNames,
            referenceDescription = referencedOverload.callableId.asFqNameForDebugInfo().asString(),
        ).map { field ->
                FirFlattenedFieldSpec(
                    name = field.parameter.name,
                    resolvedType = field.resolvedType,
                    defaultValue = generatedDefaultValueOrNull(field.parameter.defaultValue),
                )
            }
        }

    private fun selectFlattenedFields(
        target: FirValidationTarget,
        fields: List<FirFlattenedFieldSpec>,
        selectorKind: SelectorKind,
        excludedNames: Set<String>,
        contextLabel: String,
    ): List<FirFlattenedFieldSpec> {
        validateExcludedNames(
            target = target,
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
        target: FirValidationTarget,
        excludedNames: Set<String>,
        availableNames: List<String>,
        contextLabel: String,
    ) {
        val unknownExcludedNames = excludedNames - availableNames.toSet()
        if (unknownExcludedNames.isNotEmpty()) {
            target.invalid(
                "unknown spread-pack exclude names for $contextLabel: ${unknownExcludedNames.sorted().joinToString()}",
            )
        }
    }

    private fun validateCarrierOmissions(
        target: FirValidationTarget,
        carrier: FirCarrierMetadata,
        selectedCarrierNames: Set<String>,
    ) {
        carrier.primaryConstructor.valueParameters.forEach { constructorParameter ->
            if (constructorParameter.name.asString() !in selectedCarrierNames && constructorParameter.defaultValue == null) {
                target.invalid(
                    "spread-pack carrier ${carrier.classId.shortClassName.asString()} cannot omit required field " +
                        constructorParameter.name.asString(),
                )
            }
        }
    }

    private fun validateUniqueFieldNames(
        target: FirValidationTarget,
        names: List<String>,
        contextLabel: String,
    ) {
        val duplicates = names.groupingBy { it }.eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .sorted()
        if (duplicates.isNotEmpty()) {
            target.invalid(
                "duplicate flattened parameter names in $contextLabel: ${duplicates.joinToString()}",
            )
        }
    }

    private fun resolveReferencedOverload(
        target: FirValidationTarget,
        annotation: FirAnnotation,
    ): FirNamedFunctionSymbol {
        val reference = annotation.spreadArgsReference()
        val overloads = resolveFunctionSymbolsByFqName(reference.functionFqName)
            .filter(::isResolvableReferencedFunction)
        if (overloads.isEmpty()) {
            target.invalid("unable to resolve argsof overload set ${reference.functionFqName}")
        }
        if (reference.parameterTypes.isEmpty()) {
            if (overloads.size != 1) {
                target.invalid(
                    "argsof overload set ${reference.functionFqName} is ambiguous; specify parameterTypes",
                )
            }
            return overloads.single()
        }
        val matches = overloads.filter { overload ->
            val overloadParameterTypes = overload.fir.valueParameters.map { valueParameter ->
                erasureClassId(
                    resolveRequiredType(
                        target = target,
                        typeRef = valueParameter.returnTypeRef,
                        owner = overload,
                        contextLabel = "argsof overload ${overload.callableId.asFqNameForDebugInfo()} parameter ${valueParameter.name.asString()}",
                    ),
                )
                    ?: target.invalid(
                        "argsof overload ${overload.callableId.asFqNameForDebugInfo()} has unsupported parameter type " +
                            renderTypeRefForDiagnostics(valueParameter.returnTypeRef),
                    )
            }
            overloadParameterTypes.size == reference.parameterTypes.size &&
                overloadParameterTypes.zip(reference.parameterTypes).all { (actualClassId, expectedType) ->
                    expectedType.matches(actualClassId)
                }
        }
        if (matches.size != 1) {
            target.invalid(
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
        val directFunctionFqName = stringArgumentOrNull("value")
        val directParameterTypes = findArgumentByName(
            Name.identifier("parameterTypes"),
            returnFirstWhenNotFound = false,
        )?.extractParameterTypeReferences().orEmpty()
        if (directFunctionFqName == null) {
            error("spread-pack target function must not be blank")
        }
        return FirSpreadArgsReference(
            functionFqName = directFunctionFqName,
            parameterTypes = directParameterTypes,
        )
    }

    private fun FirAnnotation.findValueArgumentExpression(): FirExpression? {
        val annotationCall = this as? FirAnnotationCall ?: return null
        val resolvedArgument = (annotationCall.argumentList as? FirResolvedArgumentList)
            ?.mapping
            ?.entries
            ?.firstOrNull { (_, parameter) -> parameter.name.asString() == "value" }
            ?.key
        if (resolvedArgument != null) {
            return resolvedArgument
        }
        val arguments = annotationCall.argumentList.arguments
        if (arguments.isEmpty()) {
            return null
        }
        val firstArgument = arguments.first()
        return when (firstArgument) {
            is FirNamedArgumentExpression -> {
                if (firstArgument.name.asString() == "value") {
                    firstArgument.expression
                } else {
                    null
                }
            }

            else -> firstArgument
        }
    }

    private fun FirExpression.stringLiteralValue(): String? {
        return (this as? FirLiteralExpression)?.value as? String
    }

    private fun FirAnnotation.stringArgumentOrNull(
        argumentName: String,
    ): String? {
        val name = Name.identifier(argumentName)
        val directValue = getStringArgument(name, session)
            ?.takeIf { value -> value.isNotBlank() }
        if (directValue != null) {
            return directValue
        }
        val rawArgument = when (argumentName) {
            "value" -> findValueArgumentExpression()
            else -> findArgumentByName(name, returnFirstWhenNotFound = false)
        }
        return rawArgument
            ?.stringLiteralValue()
            ?.takeIf { value -> value.isNotBlank() }
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
                append(parameterTypeKey(valueParameter.returnTypeRef))
                append(";")
            }
        }
    }

    private fun createGeneratedOverloadAnnotation(
        sourceFunctionFqName: String,
    ): FirAnnotation {
        val annotationType = SpreadPackPluginKeys.generatedSpreadPackOverloadAnnotationClassId.constructClassLikeType(
            emptyArray(),
            false,
        )
        val argument = buildLiteralExpression(
            source = null,
            kind = ConstantValueKind.String,
            value = sourceFunctionFqName,
            setType = true,
        )
        return buildAnnotation {
            annotationTypeRef = annotationType.toFirResolvedTypeRef()
            argumentMapping = buildAnnotationArgumentMapping {
                mapping[Name.identifier("sourceFunctionFqName")] = argument
            }
        }
    }

    private fun createGeneratedFunction(
        candidate: FirSpreadPackCandidate,
    ): FirNamedFunctionSymbol {
        val original = candidate.original.fir
        val isComposableFunction = original.annotations.any { annotation ->
            annotation.matchesAnnotationClassId(composableAnnotationClassId)
        }
        val expansionsByIndex = candidate.expansions.associateBy { expansion -> expansion.parameterIndex }
        val generated = copyFirFunctionWithResolvePhase(
            original = original,
            callableId = candidate.original.callableId.copy(candidate.generatedName),
            key = SpreadPackGeneratedDeclarationKey,
            firResolvePhase = FirResolvePhase.BODY_RESOLVE,
        ) {
            source = original.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
            annotations.clear()
            annotations += original.annotations.filterNot { annotation ->
                annotation.matchesAnnotationClassId(SpreadPackPluginKeys.generateSpreadPackOverloadsAnnotationClassId)
            }
            annotations += createGeneratedOverloadAnnotation(
                candidate.original.callableId.asFqNameForDebugInfo().asString(),
            )
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
                    updatedParameters += buildValueParameter {
                        source = field.parameter.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
                            ?: original.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
                        moduleData = original.moduleData
                        origin = SpreadPackGeneratedDeclarationKey.origin
                        resolvePhase = FirResolvePhase.BODY_RESOLVE
                        returnTypeRef = field.resolvedType.toFirResolvedTypeRef()
                        name = field.parameter.name
                        symbol = FirValueParameterSymbol()
                        defaultValue = generatedDefaultValueOrNull(field.parameter.defaultValue)
                        containingDeclarationSymbol = this@copyFirFunctionWithResolvePhase.symbol
                        annotations += field.parameter.annotations
                        isCrossinline = field.parameter.isCrossinline
                        isNoinline = field.parameter.isNoinline
                        isVararg = field.parameter.isVararg
                    }
                }
            }
            valueParameters.clear()
            valueParameters += updatedParameters
            if (isComposableFunction) {
                valueParameters.forEach { parameter ->
                    parameter.replaceDefaultValue(null)
                }
            }
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
                    parameterTypes = function.valueParameters.map { parameter -> parameterTypeKey(parameter.returnTypeRef) },
                )
            }
        val declaredScope = context.declaredScope ?: return keys
        declaredScope.getCallableNames().forEach { callableName ->
            declaredScope.processFunctionsByName(callableName) { function ->
                keys += jvmSignatureKey(
                    name = function.callableId.callableName,
                    parameterTypes = function.fir.valueParameters.map { parameter -> parameterTypeKey(parameter.returnTypeRef) },
                )
            }
        }
        return keys
    }

    private fun jvmSignatureKey(
        name: Name,
        parameterTypes: List<String>,
    ): String {
        return buildString {
            append(name.asString())
            append("|")
            parameterTypes.forEach { type ->
                append(type)
                append(";")
            }
        }
    }

    private fun parameterTypeKey(
        typeRef: FirTypeRef,
    ): String {
        val resolvedType = (typeRef as? FirResolvedTypeRef)?.coneType
        if (resolvedType != null) {
            return jvmErasure(resolvedType)
        }
        return typeRef.source?.text
            ?.toString()
            ?.substringBefore("<")
            ?.substringBefore("?")
            ?.trim()
            ?.takeIf { typeText -> typeText.isNotBlank() }
            ?: "<unresolved>"
    }

    private fun resolveRequiredType(
        target: FirValidationTarget,
        typeRef: FirTypeRef,
        owner: FirCallableSymbol<*>?,
        contextLabel: String,
    ): ConeKotlinType {
        return resolveTypeOrNull(typeRef, owner)
            ?: target.invalid(
                "unable to resolve type for $contextLabel: ${renderTypeRefForDiagnostics(typeRef)}",
            )
    }

    private fun resolveTypeOrNull(
        typeRef: FirTypeRef,
        owner: FirCallableSymbol<*>?,
    ): ConeKotlinType? {
        (typeRef as? FirResolvedTypeRef)?.coneType?.let { resolvedType ->
            if (resolvedType !is ConeErrorType) {
                return resolvedType
            }
        }
        val callableOwner = owner ?: return null
        val file = session.firProvider.getFirCallableContainerFile(callableOwner) ?: return null
        val containingClasses = collectContainingClasses(callableOwner)
        val scopes = buildTypeResolutionScopes(
            file = file,
            containingClasses = containingClasses,
            owner = callableOwner,
        )
        try {
            val result = session.typeResolver.resolveType(
                typeRef = typeRef,
                configuration = TypeResolutionConfiguration(
                    scopes = scopes.asReversed(),
                    containingClassDeclarations = containingClasses,
                    useSiteFile = file,
                    topContainer = null,
                ),
                areBareTypesAllowed = false,
                isOperandOfIsOperator = false,
                resolveDeprecations = false,
                supertypeSupplier = SupertypeSupplier.Default,
            )
            return result.type.takeUnless { resolvedType -> resolvedType is ConeErrorType }
        } catch (_: Throwable) {
            return parseTypeFromSourceText(typeRef.source?.text?.toString(), file)
        }
    }

    private fun collectContainingClasses(
        owner: FirCallableSymbol<*>,
    ): List<FirClass> {
        val containingClasses = ArrayDeque<FirClass>()
        var current = session.firProvider.getContainingClass(owner)
        while (current != null) {
            (current.fir as? FirClass)?.let(containingClasses::addFirst)
            current = session.firProvider.getContainingClass(current)
        }
        return containingClasses.toList()
    }

    private fun buildTypeResolutionScopes(
        file: FirFile,
        containingClasses: List<FirClass>,
        owner: FirCallableSymbol<*>,
    ): List<FirScope> {
        return buildList {
            addAll(createImportingScopes(file, session, typeResolutionScopeSession))
            containingClasses.forEach { containingClass ->
                session.kotlinScopeProvider
                    .getNestedClassifierScope(containingClass, session, typeResolutionScopeSession)
                    ?.let { nestedClassifierScope -> add(nestedClassifierScope) }
                (containingClass as? FirMemberDeclaration)
                    ?.takeIf { memberDeclaration -> memberDeclaration.typeParameters.isNotEmpty() }
                    ?.let { memberDeclaration -> add(FirMemberTypeParameterScope(memberDeclaration)) }
            }
            (owner.fir as? FirMemberDeclaration)
                ?.takeIf { memberDeclaration -> memberDeclaration.typeParameters.isNotEmpty() }
                ?.let { memberDeclaration -> add(FirMemberTypeParameterScope(memberDeclaration)) }
        }
    }

    private fun renderTypeRefForDiagnostics(
        typeRef: FirTypeRef,
    ): String {
        return (typeRef as? FirResolvedTypeRef)?.coneType?.renderForDebugging()
            ?: typeRef.source?.text?.toString()?.trim().takeUnless { typeText -> typeText.isNullOrEmpty() }
            ?: "<unresolved>"
    }

    private fun parseTypeFromSourceText(
        rawTypeText: String?,
        file: FirFile,
    ): ConeKotlinType? {
        val typeText = rawTypeText?.trim()?.takeIf { text -> text.isNotEmpty() } ?: return null
        return parseTypeText(typeText, file)
    }

    private fun parseTypeText(
        rawTypeText: String,
        file: FirFile,
    ): ConeKotlinType? {
        var typeText = rawTypeText.trim()
        if (typeText.isEmpty()) {
            return null
        }
        val isMarkedNullable = typeText.endsWith("?")
        if (isMarkedNullable) {
            typeText = typeText.dropLast(1).trim()
        }
        while (isWrappedByMatchingParentheses(typeText)) {
            typeText = typeText.substring(1, typeText.length - 1).trim()
        }
        val arrowIndex = findTopLevelArrow(typeText)
        if (arrowIndex >= 0) {
            val parameterListText = typeText.substring(0, arrowIndex).trim()
            val returnTypeText = typeText.substring(arrowIndex + 2).trim()
            if (!isWrappedByMatchingParentheses(parameterListText)) {
                return null
            }
            val parameterTexts = splitTopLevel(
                parameterListText.substring(1, parameterListText.length - 1),
                ',',
            )
            val parameterTypes = parameterTexts
                .map(String::trim)
                .filter(String::isNotEmpty)
                .mapNotNull { parameterText ->
                    parseTypeText(
                        parameterText.substringAfter(':', parameterText).trim(),
                        file,
                    )
                }
            if (parameterTypes.size != parameterTexts.count { parameterText -> parameterText.isNotBlank() }) {
                return null
            }
            val returnType = parseTypeText(returnTypeText, file) ?: return null
            return ConeClassLikeTypeImpl(
                StandardClassIds.FunctionN(parameterTypes.size).toLookupTag(),
                (parameterTypes + returnType).toTypedArray(),
                isMarkedNullable = isMarkedNullable,
            )
        }
        val genericStart = findTopLevelCharacter(typeText, '<')
        val baseTypeName: String
        val typeArguments: Array<ConeTypeProjection>
        if (genericStart >= 0) {
            if (!typeText.endsWith(">")) {
                return null
            }
            baseTypeName = typeText.substring(0, genericStart).trim()
            val argumentTexts = splitTopLevel(
                typeText.substring(genericStart + 1, typeText.length - 1),
                ',',
            )
            typeArguments = argumentTexts.mapNotNull { argumentText ->
                parseTypeArgument(argumentText.trim(), file)
            }.toTypedArray()
            if (typeArguments.size != argumentTexts.size) {
                return null
            }
        } else {
            baseTypeName = typeText
            typeArguments = ConeTypeProjection.EMPTY_ARRAY
        }
        val classId = resolveClassIdFromTypeName(baseTypeName, file) ?: return null
        return ConeClassLikeTypeImpl(
            classId.toLookupTag(),
            typeArguments,
            isMarkedNullable = isMarkedNullable,
        )
    }

    private fun parseTypeArgument(
        argumentText: String,
        file: FirFile,
    ): ConeTypeProjection? {
        return when {
            argumentText == "*" -> ConeStarProjection
            argumentText.startsWith("out ") -> parseTypeText(argumentText.removePrefix("out ").trim(), file)
                ?.let(::ConeKotlinTypeProjectionOut)
            argumentText.startsWith("in ") -> parseTypeText(argumentText.removePrefix("in ").trim(), file)
                ?.let(::ConeKotlinTypeProjectionIn)
            else -> parseTypeText(argumentText, file)
        }
    }

    private fun resolveClassIdFromTypeName(
        typeName: String,
        file: FirFile,
    ): ClassId? {
        resolveDefaultClassId(typeName)?.let { classId -> return classId }
        if ('.' in typeName) {
            resolveQualifiedLikeClassId(typeName)?.let { classId -> return classId }
        }
        file.imports.forEach { import ->
            val importedFqName = import.importedFqName ?: return@forEach
            if (import.aliasName?.asString() == typeName) {
                resolveQualifiedLikeClassId(importedFqName.asString())?.let { classId -> return classId }
            }
            if (!import.isAllUnder && import.aliasName == null && importedFqName.shortName().asString() == typeName) {
                resolveQualifiedLikeClassId(importedFqName.asString())?.let { classId -> return classId }
            }
        }
        file.imports.forEach { import ->
            val importedFqName = import.importedFqName ?: return@forEach
            if (!import.isAllUnder) {
                return@forEach
            }
            val candidate = ClassId(
                importedFqName,
                FqName.fromSegments(typeName.split('.')),
                false,
            )
            if (hasClassLikeSymbol(candidate)) {
                return candidate
            }
        }
        val samePackageCandidate = ClassId(
            file.packageFqName,
            FqName.fromSegments(typeName.split('.')),
            false,
        )
        if (hasClassLikeSymbol(samePackageCandidate)) {
            return samePackageCandidate
        }
        StandardClassIds.builtInsPackagesWithDefaultNamedImport.forEach { packageFqName ->
            val candidate = ClassId(
                packageFqName,
                FqName.fromSegments(typeName.split('.')),
                false,
            )
            if (hasClassLikeSymbol(candidate)) {
                return candidate
            }
        }
        return null
    }

    private fun resolveDefaultClassId(
        typeName: String,
    ): ClassId? {
        return when (typeName) {
            "Any" -> StandardClassIds.Any
            "Nothing" -> StandardClassIds.Nothing
            "Unit" -> StandardClassIds.Unit
            "String" -> StandardClassIds.String
            "CharSequence" -> StandardClassIds.CharSequence
            "Throwable" -> StandardClassIds.Throwable
            "Boolean" -> StandardClassIds.Boolean
            "Char" -> StandardClassIds.Char
            "Byte" -> StandardClassIds.Byte
            "Short" -> StandardClassIds.Short
            "Int" -> StandardClassIds.Int
            "Long" -> StandardClassIds.Long
            "Float" -> StandardClassIds.Float
            "Double" -> StandardClassIds.Double
            "UByte" -> StandardClassIds.UByte
            "UShort" -> StandardClassIds.UShort
            "UInt" -> StandardClassIds.UInt
            "ULong" -> StandardClassIds.ULong
            "Array" -> StandardClassIds.Array
            "List" -> StandardClassIds.List
            "MutableList" -> StandardClassIds.MutableList
            "Set" -> StandardClassIds.Set
            "MutableSet" -> StandardClassIds.MutableSet
            "Map" -> StandardClassIds.Map
            "MutableMap" -> StandardClassIds.MutableMap
            "Iterable" -> StandardClassIds.Iterable
            "MutableIterable" -> StandardClassIds.MutableIterable
            "Collection" -> StandardClassIds.Collection
            "MutableCollection" -> StandardClassIds.MutableCollection
            else -> null
        }
    }

    private fun resolveQualifiedLikeClassId(
        typeName: String,
    ): ClassId? {
        val segments = typeName.split('.').filter(String::isNotBlank)
        if (segments.size < 2) {
            return null
        }
        for (packageSegmentCount in segments.size - 1 downTo 1) {
            val candidate = ClassId(
                FqName.fromSegments(segments.take(packageSegmentCount)),
                FqName.fromSegments(segments.drop(packageSegmentCount)),
                false,
            )
            if (hasClassLikeSymbol(candidate)) {
                return candidate
            }
        }
        return null
    }

    private fun hasClassLikeSymbol(
        classId: ClassId,
    ): Boolean {
        return session.symbolProvider.getClassLikeSymbolByClassId(classId) != null ||
            session.dependenciesSymbolProvider.getClassLikeSymbolByClassId(classId) != null
    }

    private fun isWrappedByMatchingParentheses(
        text: String,
    ): Boolean {
        if (text.length < 2 || text.first() != '(' || text.last() != ')') {
            return false
        }
        var depth = 0
        text.forEachIndexed { index, char ->
            when (char) {
                '(' -> depth += 1
                ')' -> {
                    depth -= 1
                    if (depth == 0 && index != text.lastIndex) {
                        return false
                    }
                }
            }
        }
        return depth == 0
    }

    private fun findTopLevelArrow(
        text: String,
    ): Int {
        var angleDepth = 0
        var parenDepth = 0
        text.indices.forEach { index ->
            when (text[index]) {
                '<' -> angleDepth += 1
                '>' -> angleDepth = (angleDepth - 1).coerceAtLeast(0)
                '(' -> parenDepth += 1
                ')' -> parenDepth = (parenDepth - 1).coerceAtLeast(0)
                '-' -> {
                    if (angleDepth == 0 && parenDepth == 0 && index + 1 < text.length && text[index + 1] == '>') {
                        return index
                    }
                }
            }
        }
        return -1
    }

    private fun findTopLevelCharacter(
        text: String,
        target: Char,
    ): Int {
        var angleDepth = 0
        var parenDepth = 0
        text.indices.forEach { index ->
            when (text[index]) {
                '<' -> angleDepth += 1
                '>' -> angleDepth = (angleDepth - 1).coerceAtLeast(0)
                '(' -> parenDepth += 1
                ')' -> parenDepth = (parenDepth - 1).coerceAtLeast(0)
                target -> if (angleDepth == 0 && parenDepth == 0) return index
            }
        }
        return -1
    }

    private fun splitTopLevel(
        text: String,
        separator: Char,
    ): List<String> {
        if (text.isBlank()) {
            return emptyList()
        }
        val result = mutableListOf<String>()
        var angleDepth = 0
        var parenDepth = 0
        var segmentStart = 0
        text.forEachIndexed { index, char ->
            when (char) {
                '<' -> angleDepth += 1
                '>' -> angleDepth = (angleDepth - 1).coerceAtLeast(0)
                '(' -> parenDepth += 1
                ')' -> parenDepth = (parenDepth - 1).coerceAtLeast(0)
                separator -> if (angleDepth == 0 && parenDepth == 0) {
                    result += text.substring(segmentStart, index)
                    segmentStart = index + 1
                }
            }
        }
        result += text.substring(segmentStart)
        return result
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

    private fun generatedDefaultValueOrNull(
        expression: FirExpression?,
    ): FirExpression? {
        expression ?: return null
        if (expression is FirExpressionStub) {
            return null
        }
        return try {
            expression.validate()
            expression
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun hasAnnotation(
        declaration: FirDeclaration,
        classId: ClassId,
    ): Boolean {
        return declaration.annotations.getAnnotationByClassId(classId, session) != null ||
            declaration.annotations.any { annotation -> annotation.matchesAnnotationClassId(classId) }
    }

    private fun FirValueParameter.getSpreadPackOfAnnotation(): FirAnnotation? {
        return annotations.getAnnotationByClassId(SpreadPackPluginKeys.spreadPackOfAnnotationClassId, session)
            ?: annotations.firstOrNull { annotation ->
                annotation.matchesAnnotationClassId(SpreadPackPluginKeys.spreadPackOfAnnotationClassId)
            }
    }

    private fun FirAnnotation.matchesAnnotationClassId(
        classId: ClassId,
    ): Boolean {
        val sourceText = source?.text?.toString()?.trim().orEmpty()
        if (sourceText.isEmpty()) {
            return false
        }
        val rawName = sourceText
            .removePrefix("@")
            .substringBefore("(")
            .substringAfter(":", missingDelimiterValue = sourceText.removePrefix("@").substringBefore("("))
            .trim()
        if (rawName.isEmpty()) {
            return false
        }
        val fqName = classId.asSingleFqName().asString()
        val shortName = classId.shortClassName.asString()
        return rawName == fqName || rawName == shortName || rawName.substringAfterLast('.') == shortName
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
        return functionTarget(symbol).invalid(reason)
    }

    private fun functionTarget(
        symbol: FirNamedFunctionSymbol,
    ): FirValidationTarget {
        return FirValidationTarget(
            "Invalid @GenerateSpreadPackOverloads target ${symbol.callableId.asFqNameForDebugInfo()}",
        )
    }

    private fun carrierTarget(
        regularClass: FirRegularClass,
    ): FirValidationTarget {
        val classId = regularClass.symbol.classId
        return FirValidationTarget(
            "Invalid @SpreadPackCarrierOf target ${classId.asString()}",
        )
    }

    private fun FirValidationTarget.invalid(
        reason: String,
    ): Nothing {
        throw IllegalStateException("$messagePrefix: $reason")
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
