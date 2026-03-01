package site.addzero.lsi.ksp.clazz

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.assist.checkIsPojo
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.ksp.anno.KspLsiAnnotation
import site.addzero.lsi.ksp.field.KspLsiField
import site.addzero.lsi.ksp.method.KspLsiMethod
class KspLsiClass(
    internal val resolver: Resolver,
    internal val ksClassDeclaration: KSClassDeclaration
) : LsiClass {

    override val simpleName: String? by lazy {
        try {
            ksClassDeclaration.simpleName.asString()
        } catch (e: Exception) {
            null
        }
    }

    override val qualifiedName: String? by lazy {
        try {
            ksClassDeclaration.qualifiedName?.asString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override val comment: String? by lazy {
        ksClassDeclaration.docString
    }

    override val fields: List<LsiField> by lazy {
        try {
            ksClassDeclaration.getAllProperties()
                .map { KspLsiField(resolver, it) }
                .toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override val annotations: List<LsiAnnotation> by lazy {
        try {
            ksClassDeclaration.annotations
                .map { KspLsiAnnotation(it) }
                .toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override val isInterface: Boolean by lazy {
        ksClassDeclaration.classKind == ClassKind.INTERFACE
    }

    override val isEnum: Boolean by lazy {
        ksClassDeclaration.classKind == ClassKind.ENUM_CLASS
    }

    override val isCollectionType: Boolean by lazy {
        val name = qualifiedName ?: ""
        name.startsWith("kotlin.collections.") || name.startsWith("java.util.") &&
                (name.contains("List") || name.contains("Set") || name.contains("Collection") || name.contains("Map"))
    }

    override val isPojo: Boolean by lazy {
        val isDataClass = ksClassDeclaration.modifiers.contains(Modifier.DATA)
        checkIsPojo(
            isInterface = isInterface,
            isEnum = isEnum,
            isAbstract = ksClassDeclaration.modifiers.contains(Modifier.ABSTRACT),
            isDataClass = isDataClass,
            annotationNames = annotations.mapNotNull { it.qualifiedName },
            isShortName = false
        )
    }

    override val superClasses: List<LsiClass> by lazy {
        ksClassDeclaration.superTypes
            .mapNotNull { superType ->
                val resolvedType = superType.resolve()
                val declaration = resolvedType.declaration
                if (declaration is KSClassDeclaration && declaration.classKind == ClassKind.CLASS) {
                    KspLsiClass(resolver, declaration)
                } else null
            }
            .toList()
    }

    override val interfaces: List<LsiClass> by lazy {
        ksClassDeclaration.superTypes
            .mapNotNull { superType ->
                val resolvedType = superType.resolve()
                val declaration = resolvedType.declaration
                if (declaration is KSClassDeclaration && declaration.classKind == ClassKind.INTERFACE) {
                    KspLsiClass(resolver, declaration)
                } else null
            }
            .toList()
    }

    override val methods: List<LsiMethod> by lazy {
        ksClassDeclaration.getAllFunctions()
            .map { KspLsiMethod(resolver, it) }
            .toList()
    }

    override val fileName: String? by lazy {
        ksClassDeclaration.containingFile?.fileName?.removeSuffix(".kt")
    }

    override val isObject: Boolean by lazy {
        ksClassDeclaration.classKind == ClassKind.OBJECT
    }

    override val isCompanionObject: Boolean by lazy {
        ksClassDeclaration.isCompanionObject
    }
}

