package site.addzero.kcp.reified.plugin

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.references.builder.*
import org.jetbrains.kotlin.fir.resolve.substitution.*
import org.jetbrains.kotlin.fir.symbols.ConeTypeParameterLookupTag
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.*
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.name.*

@OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)
class ReifiedFirDeclarationGenerationExtension(session: FirSession) : FirDeclarationGenerationExtension(session) {
    private companion object {
        object ReifiedPluginKey : GeneratedDeclarationKey()

        val generateReifiedClassId = ClassId.topLevel(FqName("site.addzero.kcp.annotations.GenerateReified"))
        val kClassClassId = ClassId.topLevel(FqName("kotlin.reflect.KClass"))
        val javaClassClassId = ClassId.topLevel(FqName("java.lang.Class"))
    }

    private enum class ClassParamKind { KCLASS, JAVA_CLASS }

    private data class ReifiedTargets(
        val indices: List<Int>,
        val kinds: List<ClassParamKind>,
        val typeParamNames: List<Name>
    )

    override fun getCallableNamesForClass(
        classSymbol: org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        if (!classSymbol.isGenerateReifiedEnabled()) return emptySet()

        val customName = classSymbol.getCustomFunctionName()
        val names = mutableSetOf<Name>()

        classSymbol.fir.declarations.forEach { decl ->
            if (decl is FirSimpleFunction && decl.hasKClassParams()) {
                val name = customName ?: decl.name.asString()
                names.add(Name.identifier(name))
            }
        }

        return names
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        if (!owner.isGenerateReifiedEnabled()) return emptyList()

        val customName = owner.getCustomFunctionName()
        val results = mutableListOf<FirNamedFunctionSymbol>()

        owner.fir.declarations.forEach { decl ->
            if (decl is FirSimpleFunction && decl.hasKClassParams()) {
                val targetName = customName ?: decl.name.asString()
                if (targetName == callableId.callableName.asString()) {
                    buildWrapperFunctionSymbol(callableId, owner, decl)?.let { results.add(it) }
                }
            }
        }

        return results
    }

    private fun buildWrapperFunctionSymbol(
        wrapperCallableId: CallableId,
        owner: org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol<*>,
        originalFn: FirSimpleFunction
    ): FirNamedFunctionSymbol? {
        originalFn.symbol.lazyResolveToPhase(FirResolvePhase.TYPES)

        val targets = originalFn.findReifiedTargets() ?: return null
        val wrapperSymbol = FirNamedFunctionSymbol(wrapperCallableId)

        val wrapperTypeParameters = originalFn.typeParameters.map { typeParam ->
            buildTypeParameterCopy(typeParam) {
                resolvePhase = FirResolvePhase.RAW_FIR
                moduleData = session.moduleData
                origin = ReifiedPluginKey.origin
                symbol = FirTypeParameterSymbol()
                containingDeclarationSymbol = wrapperSymbol
                isReified = typeParam.name in targets.typeParamNames
            }
        }

        val typeSubstitutor = substitutorByMap(
            substitution = originalFn.typeParameters
                .map { it.symbol }
                .zip(wrapperTypeParameters.map { it.symbol })
                .associate { (from, to) ->
                    from to ConeTypeParameterTypeImpl(
                        ConeTypeParameterLookupTag(to),
                        isMarkedNullable = false
                    )
                },
            useSiteSession = session
        )

        val substitutedReturnTypeRef = substituteTypeRef(originalFn.returnTypeRef, typeSubstitutor)

        val classArgExpressions = targets.indices.zip(targets.kinds).zip(targets.typeParamNames).map { (indexKind, typeParamName) ->
            val (index, kind) = indexKind
            index to buildClassArgExpression(kind, typeParamName, wrapperTypeParameters, originalFn.valueParameters[index])
        }.toMap()

        val wrapperValueParameters = originalFn.valueParameters
            .filterIndexed { index, _ -> index !in targets.indices }
            .map { valueParam ->
                buildValueParameterCopy(valueParam) {
                    resolvePhase = FirResolvePhase.RAW_FIR
                    moduleData = session.moduleData
                    origin = ReifiedPluginKey.origin
                    returnTypeRef = substituteTypeRef(valueParam.returnTypeRef, typeSubstitutor)
                    symbol = FirValueParameterSymbol()
                    containingDeclarationSymbol = wrapperSymbol
                }
            }

        val wrapperValueParamsByName = wrapperValueParameters.associateBy { it.name }

        val resolvedArgsMapping = LinkedHashMap<FirExpression, FirValueParameter>().apply {
            originalFn.valueParameters.forEachIndexed { index, originalParam ->
                val argExpr = classArgExpressions[index] ?: run {
                    val wrapperParam = wrapperValueParamsByName[originalParam.name] ?: return null
                    buildForwardArgExpression(wrapperParam.symbol, wrapperParam.name, originalParam.isVararg)
                }
                put(argExpr, originalParam)
            }
        }

        val dispatchReceiverExpr = originalFn.dispatchReceiverType?.let {
            buildThisReceiverExpression {
                isImplicit = true
                calleeReference = buildImplicitThisReference { boundSymbol = owner }
            }
        }

        val originalCall = buildFunctionCall {
            calleeReference = buildResolvedNamedReference {
                name = originalFn.name
                resolvedSymbol = originalFn.symbol
            }
            dispatchReceiver = dispatchReceiverExpr
            argumentList = buildResolvedArgumentList(original = null, mapping = resolvedArgsMapping)
            coneTypeOrNull = substitutedReturnTypeRef.coneType
        }

        val wrapper = buildSimpleFunction {
            resolvePhase = FirResolvePhase.RAW_FIR
            moduleData = session.moduleData
            origin = ReifiedPluginKey.origin
            name = wrapperCallableId.callableName
            symbol = wrapperSymbol

            status = FirDeclarationStatusImpl(
                visibility = originalFn.status.visibility,
                modality = Modality.FINAL
            ).apply {
                isInline = true
                isExpect = false
                isActual = false
                isOverride = false
                isOperator = originalFn.status.isOperator
                isInfix = originalFn.status.isInfix
                isExternal = false
                isTailRec = false
                isSuspend = originalFn.status.isSuspend
            }
            returnTypeRef = substitutedReturnTypeRef
            dispatchReceiverType = originalFn.dispatchReceiverType
            receiverParameter = originalFn.receiverParameter
            contextParameters.addAll(originalFn.contextParameters)
            typeParameters.addAll(wrapperTypeParameters)
            valueParameters.addAll(wrapperValueParameters)

            body = buildSingleExpressionBlock(originalCall)
        }

        return wrapper.symbol
    }

    private fun substituteTypeRef(typeRef: FirTypeRef, substitutor: ConeSubstitutor): FirTypeRef {
        if (typeRef !is FirResolvedTypeRef) return typeRef
        val substituted = substitutor.substituteOrSelf(typeRef.coneType)
        return if (substituted == typeRef.coneType) typeRef
        else buildResolvedTypeRefCopy(typeRef) { coneType = substituted }
    }

    private fun org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol<*>.isGenerateReifiedEnabled(): Boolean {
        return (fir as? FirAnnotationContainer)
            ?.annotations
            ?.any { it.matchesAnnotation(generateReifiedClassId) } == true
    }

    private fun org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol<*>.getCustomFunctionName(): String? {
        val annotation = (fir as? FirAnnotationContainer)
            ?.annotations
            ?.firstOrNull { it.matchesAnnotation(generateReifiedClassId) }
            ?: return null

        return annotation.getStringArgument(Name.identifier("value"), session)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun FirSimpleFunction.hasKClassParams(): Boolean {
        return valueParameters.any { it.returnTypeRef.isKClassOrJavaClass() }
    }

    private fun FirSimpleFunction.findReifiedTargets(): ReifiedTargets? {
        val indices = mutableListOf<Int>()
        val kinds = mutableListOf<ClassParamKind>()
        val typeParamNames = mutableListOf<Name>()

        valueParameters.forEachIndexed { index, param ->
            val kind = param.returnTypeRef.classParamKind() ?: return@forEachIndexed
            val typeArg = param.returnTypeRef.getKClassTypeArgument() ?: return@forEachIndexed

            indices.add(index)
            kinds.add(kind)
            typeParamNames.add(typeArg)
        }

        return if (indices.isEmpty()) null else ReifiedTargets(indices, kinds, typeParamNames)
    }

    private fun FirTypeRef.isKClassOrJavaClass(): Boolean {
        return classParamKind() != null
    }

    private fun FirTypeRef.classParamKind(): ClassParamKind? {
        val resolved = this as? FirResolvedTypeRef ?: return null
        val classLike = resolved.coneType as? ConeClassLikeType ?: return null
        return when (classLike.lookupTag.classId) {
            kClassClassId -> ClassParamKind.KCLASS
            javaClassClassId -> ClassParamKind.JAVA_CLASS
            else -> null
        }
    }

    private fun FirTypeRef.getKClassTypeArgument(): Name? {
        val resolved = this as? FirResolvedTypeRef ?: return null
        val classLike = resolved.coneType as? ConeClassLikeType ?: return null
        val typeArg = classLike.typeArguments.firstOrNull() ?: return null
        val typeParam = (typeArg as? ConeKotlinType) as? ConeTypeParameterType ?: return null
        return typeParam.lookupTag.name
    }

    private fun buildForwardArgExpression(
        paramSymbol: FirValueParameterSymbol,
        paramName: Name,
        isVararg: Boolean
    ): FirExpression {
        val propertyAccess = buildPropertyAccessExpression {
            calleeReference = buildResolvedNamedReference {
                name = paramName
                resolvedSymbol = paramSymbol
            }
        }
        return if (isVararg) {
            buildSpreadArgumentExpression { expression = propertyAccess }
        } else {
            propertyAccess
        }
    }

    private fun buildClassArgExpression(
        classParamKind: ClassParamKind,
        typeParamName: Name,
        wrapperTypeParameters: List<FirTypeParameter>,
        dummyValueParameterForResolvedArgList: FirValueParameter
    ): FirExpression? {
        val typeParam = wrapperTypeParameters.firstOrNull { it.name == typeParamName } ?: return null

        return when (classParamKind) {
            ClassParamKind.KCLASS -> {
                buildClassReferenceExpression {
                    val typeRef = buildResolvedTypeRef {
                        coneType = ConeTypeParameterTypeImpl(
                            ConeTypeParameterLookupTag(typeParam.symbol),
                            isMarkedNullable = false
                        )
                    }
                    classTypeRef = typeRef
                }
            }
            ClassParamKind.JAVA_CLASS -> {
                val classRef = buildClassReferenceExpression {
                    val typeRef = buildResolvedTypeRef {
                        coneType = ConeTypeParameterTypeImpl(
                            ConeTypeParameterLookupTag(typeParam.symbol),
                            isMarkedNullable = false
                        )
                    }
                    classTypeRef = typeRef
                }
                buildGetClassCall {
                    argumentList = buildUnaryArgumentList(classRef)
                }
            }
        }
    }

    private fun FirAnnotation.matchesAnnotation(classId: ClassId): Boolean {
        return toAnnotationClassIdSafe(session) == classId
    }
}
