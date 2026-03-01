@file:OptIn(org.jetbrains.kotlin.analysis.api.KaExperimentalApi::class)

package site.addzero.lsi.k2.field

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KaTypeRendererForSource
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.types.Variance
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.assist.isNullable
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.str.toSnakeCaseLowerCase
import site.addzero.lsi.type.LsiType
import site.addzero.lsi.k2.anno.K2LsiAnnotation
import site.addzero.lsi.k2.clazz.K2LsiClass
import site.addzero.lsi.k2.type.K2LsiType
import site.addzero.util.str.cleanDocComment
import site.addzero.util.str.removeAnyQuote
import site.addzero.util.str.toUnderlineLowerCase

/**
 *  K2 Analysis API  比较特殊,需要init的时候 缓存计算
 *
 * 基础属性在构造时计算（eager），关联类属性使用 lazy 延迟加载避免循环依赖(但是lazy的话又有EDT线程问题)
 */
class K2LsiField(
    private val ktProperty: KtProperty,
    symbol: KaPropertySymbol,
    session: KaSession
) : LsiField {

    private val ownerKtClass: KtClass? get() = ktProperty.parent?.parent as? KtClass

    // Eager - 基础属性在构造时计算
    override val name: String? = symbol.name.asString()
    override val isVar: Boolean = !symbol.isVal
    override val isStatic: Boolean = ktProperty.hasModifier(KtTokens.CONST_KEYWORD)
    override val isConstant: Boolean = ktProperty.hasModifier(KtTokens.CONST_KEYWORD)
    override val isLateInit: Boolean = ktProperty.hasModifier(KtTokens.LATEINIT_KEYWORD)
    override val defaultValue: String? = ktProperty.initializer?.text

    override val type: LsiType?
    override val typeName: String?
    override val annotations: List<LsiAnnotation>
    override val isNestedObject: Boolean

    override val isNullable: Boolean
        get() = _isNullable || annotations.isNullable()

    private val _isNullable: Boolean

    private val _docComment: String? = cleanDocComment(ktProperty.docComment?.text)
    private val _fieldTypeClassId: String?

    init {
        with(session) {
            type = K2LsiType(symbol.returnType, session)
            typeName = symbol.returnType.render(KaTypeRendererForSource.WITH_SHORT_NAMES, Variance.INVARIANT)
            annotations = symbol.annotations.map { K2LsiAnnotation(it) }

            val returnType = symbol.returnType
            _fieldTypeClassId = (returnType as? KaClassType)?.classId?.asFqNameString()
            isNestedObject = returnType is KaClassType &&
                    !returnType.classId.asFqNameString().startsWith("kotlin.") &&
                    !returnType.classId.asFqNameString().startsWith("java.")

            // 计算类型可空性 - 在 init 块中计算，避免 EDT 线程问题
            _isNullable = returnType.isMarkedNullable
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

    override val fieldTypeClass: LsiClass? by lazy {
        if (_fieldTypeClassId == null || _fieldTypeClassId.startsWith("kotlin.") || _fieldTypeClassId.startsWith("java.")) {
            return@lazy null
        }
        analyze(ktProperty) {
            val propSymbol = ktProperty.symbol as? KaPropertySymbol ?: return@analyze null
            val returnType = propSymbol.returnType as? KaClassType ?: return@analyze null
            val classSymbol = returnType.symbol as? KaClassSymbol ?: return@analyze null
            (classSymbol.psi as? KtClass)?.let { K2LsiClass(it, classSymbol, this@analyze) }
        }
    }

    override val comment: String?
        get() = extractComment()

    override val isCollectionType: Boolean
        get() = type?.isCollectionType ?: false

    override val columnName: String?
        get() = extractColumnName()

    override val children: List<LsiField>
        get() = fieldTypeClass?.fields ?: emptyList()

    private fun extractComment(): String? {
        annotations.forEach { anno ->
            val desc = when (anno.simpleName) {
                "ApiModelProperty" -> anno.getAttribute("value") as? String
                "Schema" -> anno.getAttribute("description") as? String
                "ExcelProperty" -> anno.getAttribute("value") as? String
                "Excel" -> anno.getAttribute("name") as? String
                else -> null
            }
            if (!desc.isNullOrBlank()) return desc.removeAnyQuote()
        }
        return _docComment
    }

    private fun extractColumnName(): String? {
        annotations.forEach { anno ->
            val columnName = when (anno.simpleName) {
                "JoinColumn" -> anno.getAttribute("name") as? String
                "Column" -> anno.getAttribute("name") as? String
                "TableField" -> anno.getAttribute("value") as? String
                else -> null
            }
            val nullOrBlank = columnName.isNullOrBlank()
            if (!nullOrBlank) return columnName.removeAnyQuote()
        }

        // 兜底策略：将字段名转换为下划线命名格式
        return name?.toUnderlineLowerCase()
    }
}
