package site.addzero.lsi.ksp.field

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.type.LsiType
import site.addzero.lsi.ksp.anno.KspLsiAnnotation
import site.addzero.lsi.ksp.clazz.KspLsiClass
import site.addzero.lsi.ksp.type.KspLsiType
import site.addzero.util.str.toUnderlineLowerCase

class KspLsiField(
    private val resolver: Resolver,
    private val ksPropertyDeclaration: KSPropertyDeclaration
) : LsiField {

    override val name: String? by lazy {
        ksPropertyDeclaration.simpleName.asString()
    }

    override val type: LsiType? by lazy {
        KspLsiType(resolver, ksPropertyDeclaration.type.resolve())
    }

    override val typeName: String? by lazy {
        ksPropertyDeclaration.type.resolve().declaration.simpleName.asString()
    }

    override val comment: String? by lazy {
        ksPropertyDeclaration.docString
    }

    override val annotations: List<LsiAnnotation> by lazy {
        ksPropertyDeclaration.annotations
            .map { KspLsiAnnotation(it) }
            .toList()
    }

    override val isStatic: Boolean by lazy {
        // Kotlin属性通常不是静态的，除非在companion object中
        val parent = ksPropertyDeclaration.parentDeclaration
        parent is KSClassDeclaration && parent.classKind == ClassKind.OBJECT
    }

    override val isConstant: Boolean by lazy {
        ksPropertyDeclaration.modifiers.contains(Modifier.CONST)
    }

    override val isVar: Boolean by lazy {
        ksPropertyDeclaration.isMutable
    }

    override val isLateInit: Boolean by lazy {
        ksPropertyDeclaration.modifiers.contains(Modifier.LATEINIT)
    }

    override val isCollectionType: Boolean by lazy {
        val resolvedType = ksPropertyDeclaration.type.resolve()
        val qualifiedName = resolvedType.declaration.qualifiedName?.asString() ?: ""
        qualifiedName.startsWith("kotlin.collections.") || qualifiedName.startsWith("java.util.") &&
                (qualifiedName.contains("List") || qualifiedName.contains("Set") ||
                        qualifiedName.contains("Collection") || qualifiedName.contains("Map"))
    }

    override val defaultValue: String? by lazy {
        // KSP中获取默认值比较复杂，这里先返回null
        // 在实际使用中可能需要通过其他方式获取
        null
    }

    override val columnName: String? by lazy {
        // 检查常见的数据库列名注解
        annotations.firstNotNullOfOrNull { annotation ->
            when (annotation.qualifiedName) {
                "org.babyfish.jimmer.sql.Column" -> annotation.getAttribute("name") as? String
                "com.baomidou.mybatisplus.annotation.TableField" -> annotation.getAttribute("value") as? String
                "javax.persistence.Column" -> annotation.getAttribute("name") as? String
                "jakarta.persistence.Column" -> annotation.getAttribute("name") as? String
                else -> null
            }
        } ?: name?.toUnderlineLowerCase() // 兜底策略：将字段名转换为下划线命名格式
    }

    override val declaringClass: LsiClass? by lazy {
        val parent = ksPropertyDeclaration.parentDeclaration
        if (parent is KSClassDeclaration) {
            KspLsiClass(resolver, parent)
        } else null
    }

    override val fieldTypeClass: LsiClass? by lazy {
        val resolvedType = ksPropertyDeclaration.type.resolve()
        val declaration = resolvedType.declaration
        if (declaration is KSClassDeclaration) {
            KspLsiClass(resolver, declaration)
        } else null
    }

    override val isNestedObject: Boolean by lazy {
        val resolvedType = ksPropertyDeclaration.type.resolve()
        val declaration = resolvedType.declaration
        declaration is KSClassDeclaration &&
                declaration.classKind == ClassKind.CLASS &&
                !isCollectionType &&
                !isPrimitiveOrString()
    }

    override val children: List<LsiField> by lazy {
        if (isNestedObject) {
            fieldTypeClass?.fields ?: emptyList()
        } else {
            emptyList()
        }
    }

    // 可空性判断：基于 Kotlin 类型系统和 KSP 类型解析
    override val isNullable: Boolean by lazy {
        val resolvedType = ksPropertyDeclaration.type.resolve()
        // 基于类型可空性标记
        val typeNullability = resolvedType.isMarkedNullable
        return@lazy typeNullability
    }

    private fun isPrimitiveOrString(): Boolean {
        val qualifiedName = ksPropertyDeclaration.type.resolve().declaration.qualifiedName?.asString()
        return qualifiedName in setOf(
            "kotlin.String", "kotlin.Int", "kotlin.Long", "kotlin.Double", "kotlin.Float",
            "kotlin.Boolean", "kotlin.Byte", "kotlin.Short", "kotlin.Char",
            "java.lang.String", "java.lang.Integer", "java.lang.Long", "java.lang.Double",
            "java.lang.Float", "java.lang.Boolean", "java.lang.Byte", "java.lang.Short",
            "java.lang.Character"
        )
    }
}

fun KSPropertyDeclaration.toLsiField(resolver: Resolver): LsiField = KspLsiField(resolver, this)
