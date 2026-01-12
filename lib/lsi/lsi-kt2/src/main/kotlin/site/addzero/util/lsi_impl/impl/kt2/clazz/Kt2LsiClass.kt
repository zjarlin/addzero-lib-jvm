package site.addzero.util.lsi_impl.impl.kt2.clazz

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.assist.checkIsPojo
import site.addzero.util.lsi.assist.isCollectionType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi_impl.impl.kt2.anno.Kt2LsiAnnotation
import site.addzero.util.lsi_impl.impl.kt2.field.Kt2LsiField
import site.addzero.util.lsi_impl.impl.kt2.method.Kt2LsiMethod
import site.addzero.util.str.cleanDocComment
import site.addzero.util.str.toUnderLineCase

/**
 * 基于 K2 Analysis API 的 LsiClass 实现
 * 
 * 基础属性在构造时计算（eager），关联类属性使用 lazy 延迟加载避免循环依赖
 */
class Kt2LsiClass(
    private val ktClass: KtClass,
    symbol: KaClassSymbol,
    session: KaSession
) : LsiClass {

    // Eager - 基础属性在构造时计算
    override val name: String? = symbol.name?.asString()
    override val qualifiedName: String? = symbol.classId?.asFqNameString()
    override val comment: String? = cleanDocComment(ktClass.docComment?.text)
    override val isInterface: Boolean = symbol.classKind == KaClassKind.INTERFACE
    override val isEnum: Boolean = symbol.classKind == KaClassKind.ENUM_CLASS
    
    private val _isAbstract: Boolean = symbol.modality == KaSymbolModality.ABSTRACT
    private val _annotationNames: List<String>
    private val _ownFields: List<LsiField>
    
    override val annotations: List<LsiAnnotation>
    
    init {
        with(session) {
            _ownFields = ktClass.getProperties().mapNotNull { property ->
                val propSymbol = property.symbol
                if (propSymbol is KaPropertySymbol) {
                    Kt2LsiField(property, propSymbol, session)
                } else null
            }
            
            annotations = symbol.annotations.map { Kt2LsiAnnotation(it) }
            _annotationNames = annotations.mapNotNull { it.simpleName }
        }
    }

    // Lazy - 关联类属性延迟加载，需要时重新获取 session
    override val superClasses: List<LsiClass> by lazy {
        analyze(ktClass) {
            val sym = ktClass.symbol as? KaClassSymbol ?: return@analyze emptyList()
            sym.superTypes.mapNotNull { superType ->
                (superType as? KaClassType)?.symbol?.let { classLikeSymbol ->
                    (classLikeSymbol as? KaClassSymbol)?.takeIf { it.classKind == KaClassKind.CLASS }?.let { superSymbol ->
                        (superSymbol.psi as? KtClass)?.let { Kt2LsiClass(it, superSymbol, this@analyze) }
                    }
                }
            }
        }
    }
    
    override val interfaces: List<LsiClass> by lazy {
        analyze(ktClass) {
            val sym = ktClass.symbol as? KaClassSymbol ?: return@analyze emptyList()
            sym.superTypes.mapNotNull { superType ->
                (superType as? KaClassType)?.symbol?.let { classLikeSymbol ->
                    (classLikeSymbol as? KaClassSymbol)?.takeIf { it.classKind == KaClassKind.INTERFACE }?.let { interfaceSymbol ->
                        (interfaceSymbol.psi as? KtClass)?.let { Kt2LsiClass(it, interfaceSymbol, this@analyze) }
                    }
                }
            }
        }
    }
    
    override val methods: List<LsiMethod> by lazy {
        analyze(ktClass) {
            ktClass.declarations.filterIsInstance<KtFunction>().mapNotNull { function ->
                val funcSymbol = function.symbol
                if (funcSymbol is KaNamedFunctionSymbol) {
                    Kt2LsiMethod(function, funcSymbol, this@analyze)
                } else null
            }
        }
    }
    
    // 递归获取父类字段
    override val fields: List<LsiField> by lazy {
        val parentFields = superClasses.flatMap { it.fields }
        parentFields + _ownFields
    }

    override val isCollectionType: Boolean
        get() = qualifiedName?.isCollectionType() ?: false

    override val isPojo: Boolean
        get() = checkIsPojo(
            isInterface = isInterface,
            isEnum = isEnum,
            isAbstract = _isAbstract,
            isDataClass = ktClass.isData(),
            annotationNames = _annotationNames,
            isShortName = true
        )

    val guessTableName: String
        get() = name?.toUnderLineCase() ?: ""
}
