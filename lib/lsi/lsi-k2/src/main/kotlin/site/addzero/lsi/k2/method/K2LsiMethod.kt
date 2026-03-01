@file:OptIn(org.jetbrains.kotlin.analysis.api.KaExperimentalApi::class)

package site.addzero.lsi.k2.method

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KaTypeRendererForSource
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.types.Variance
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.method.LsiParameter
import site.addzero.lsi.type.LsiType
import site.addzero.lsi.k2.anno.K2LsiAnnotation
import site.addzero.lsi.k2.clazz.K2LsiClass
import site.addzero.lsi.k2.type.K2LsiType
import site.addzero.util.str.cleanDocComment

/**
 * 基于 K2 Analysis API 的 LsiMethod 实现
 *
 * 基础属性在构造时计算（eager），关联类属性使用 lazy 延迟加载避免循环依赖
 */
class K2LsiMethod(
    private val ktFunction: KtFunction,
    symbol: KaNamedFunctionSymbol,
    session: KaSession
) : LsiMethod {

    private val ownerKtClass: KtClass? get() = ktFunction.parent?.parent as? KtClass

    // Eager - 基础属性在构造时计算
    override val name: String? = symbol.name.asString()
    override val comment: String? = cleanDocComment(ktFunction.docComment?.text)
    override val isStatic: Boolean = false // K2 中静态性通过 companion object 处理
    override val isAbstract: Boolean = symbol.modality == KaSymbolModality.ABSTRACT

    override val returnType: LsiType?
    override val returnTypeName: String?
    override val annotations: List<LsiAnnotation>
    override val parameters: List<LsiParameter>

    init {
        with(session) {
            returnType = K2LsiType(symbol.returnType, session)
            returnTypeName = symbol.returnType.render(KaTypeRendererForSource.WITH_SHORT_NAMES, Variance.INVARIANT)
            annotations = symbol.annotations.map { K2LsiAnnotation(it) }
            parameters = symbol.valueParameters.map { paramSymbol -> Kt2LsiParameter(paramSymbol, session) }
        }
    }

    // Lazy - 关联类属性延迟加载
    override val declaringClass: LsiClass? by lazy {
        val ktClass = ownerKtClass ?: return@lazy null
        analyze(ktClass) {
            val sym = ktClass.symbol as? KaClassSymbol ?: return@analyze null
            K2LsiClass(ktClass, sym, this@analyze)
        }
    }
}

/**
 * 基于 K2 Analysis API 的 LsiParameter 实现
 *
 * 注意：所有依赖 KaSession 的属性在构造时预先计算（eager evaluation），
 * 因为 KaSession 在 analyze 块结束后会失效。
 */
class Kt2LsiParameter(
    symbol: KaValueParameterSymbol,
    session: KaSession
) : LsiParameter {

    // Eager evaluation - 在构造时计算所有需要 session 的值
    override val name: String? = symbol.name.asString()
    override val type: LsiType?
    override val typeName: String?
    override val annotations: List<LsiAnnotation>

    init {
        with(session) {
            type = K2LsiType(symbol.returnType, session)
            typeName = symbol.returnType.render(KaTypeRendererForSource.WITH_SHORT_NAMES, Variance.INVARIANT)
            annotations = symbol.annotations.map { K2LsiAnnotation(it) }
        }
    }
}
