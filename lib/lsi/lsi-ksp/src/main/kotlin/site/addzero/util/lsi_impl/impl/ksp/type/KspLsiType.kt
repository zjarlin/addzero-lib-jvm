package site.addzero.util.lsi_impl.impl.ksp.type

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.ksp.anno.KspLsiAnnotation
import site.addzero.util.lsi_impl.impl.ksp.clazz.KspLsiClass

class KspLsiType(
    private val resolver: Resolver,
    private val ksType: KSType
) : LsiType {

    override val name: String? by lazy {
        ksType.declaration.simpleName.asString()
    }

    override val qualifiedName: String? by lazy {
        ksType.declaration.qualifiedName?.asString()
    }

    override val presentableText: String? by lazy {
        ksType.toString()
    }

    override val annotations: List<LsiAnnotation> by lazy {
        ksType.annotations
            .map { KspLsiAnnotation(it) }
            .toList()
    }

    override val isCollectionType: Boolean by lazy {
        val qualifiedName = this.qualifiedName ?: ""
        qualifiedName.startsWith("kotlin.collections.") || qualifiedName.startsWith("java.util.") &&
                (qualifiedName.contains("List") || qualifiedName.contains("Set") ||
                        qualifiedName.contains("Collection") || qualifiedName.contains("Map"))
    }

    override val isNullable: Boolean by lazy {
        ksType.isMarkedNullable
    }

    override val typeParameters: List<LsiType> by lazy {
        ksType.arguments.mapNotNull { typeArgument ->
            typeArgument.type?.resolve()?.let {
                KspLsiType(resolver, it)
            }
        }
    }

    override val isPrimitive: Boolean by lazy {
        val qualifiedName = this.qualifiedName
        qualifiedName in setOf(
            "kotlin.Int", "kotlin.Long", "kotlin.Double", "kotlin.Float",
            "kotlin.Boolean", "kotlin.Byte", "kotlin.Short", "kotlin.Char",
            "kotlin.Unit"
        )
    }

    override val componentType: LsiType? by lazy {
        // 对于数组类型，获取组件类型
        if (isArray) {
            ksType.arguments.firstOrNull()?.type?.resolve()?.let {
                KspLsiType(resolver, it)
            }
        } else null
    }

    override val isArray: Boolean by lazy {
        val qualifiedName = this.qualifiedName ?: ""
        qualifiedName.startsWith("kotlin.Array") ||
                qualifiedName.endsWith("Array") ||
                qualifiedName in setOf(
            "kotlin.IntArray", "kotlin.LongArray", "kotlin.DoubleArray", "kotlin.FloatArray",
            "kotlin.BooleanArray", "kotlin.ByteArray", "kotlin.ShortArray", "kotlin.CharArray"
        )
    }

    override val lsiClass: LsiClass? by lazy {
        val declaration = ksType.declaration
        if (declaration is KSClassDeclaration) {
            KspLsiClass(resolver, declaration)
        } else null
    }
}

fun KSType.toLsiType(resolver: Resolver): LsiType = KspLsiType(resolver, this)
