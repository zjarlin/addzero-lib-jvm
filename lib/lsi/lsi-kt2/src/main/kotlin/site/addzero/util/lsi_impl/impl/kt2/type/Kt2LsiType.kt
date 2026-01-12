@file:OptIn(org.jetbrains.kotlin.analysis.api.KaExperimentalApi::class)

package site.addzero.util.lsi_impl.impl.kt2.type

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KaTypeRendererForSource
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.types.Variance
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.assist.TypeChecker
import site.addzero.util.lsi.assist.isCollectionType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.kt2.anno.Kt2LsiAnnotation

/**
 * 基于 K2 Analysis API 的 LsiType 实现
 */
class Kt2LsiType(
    kaType: KaType,
    session: KaSession
) : LsiType {

    // 在构造时就计算并存储这些值，避免 session 生命周期问题
    override val name: String?
    override val qualifiedName: String?
    override val presentableText: String?
    override val annotations: List<LsiAnnotation>
    override val isCollectionType: Boolean
    override val isNullable: Boolean

    private val _typeParameters: List<LsiType>
    private val _isPrimitive: Boolean
    private val _componentType: LsiType?
    private val _isArray: Boolean

    init {
        with(session) {
            name = when (kaType) {
                is KaClassType -> kaType.classId?.shortClassName?.asString()
                is KaTypeParameterType -> kaType.name.asString()
                else -> kaType.render(KaTypeRendererForSource.WITH_SHORT_NAMES, Variance.INVARIANT)
            }

            qualifiedName = when (kaType) {
                is KaClassType -> kaType.classId?.asFqNameString()
                else -> null
            }

            presentableText = kaType.render(KaTypeRendererForSource.WITH_SHORT_NAMES, Variance.INVARIANT)
            annotations = kaType.annotations.map { Kt2LsiAnnotation(it) }
            isNullable = kaType.isMarkedNullable
            isCollectionType = qualifiedName?.isCollectionType() ?: false

            // 计算其他属性
            _typeParameters = when (kaType) {
                is KaClassType -> kaType.typeArguments.mapNotNull { arg ->
                    arg.type?.let { Kt2LsiType(it, session) }
                }
                else -> emptyList()
            }

            _isArray = qualifiedName?.let {
                it == "kotlin.Array" || it.startsWith("kotlin.") && it.endsWith("Array")
            } ?: false

            _componentType = when {
                _isArray -> _typeParameters.firstOrNull()
                else -> null
            }

            _isPrimitive = name?.let { TypeChecker.isKotlinPrimitiveType(it) } ?: false
        }
    }

    override val typeParameters: List<LsiType>
        get() = _typeParameters

    override val isPrimitive: Boolean
        get() = _isPrimitive

    override val componentType: LsiType?
        get() = _componentType

    override val isArray: Boolean
        get() = _isArray

    override val lsiClass: LsiClass?
        get() = null
}
