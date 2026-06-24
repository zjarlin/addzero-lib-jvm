package site.addzero.jimmer.ddl.compiler

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.jimmer.isJimmerEntity
import site.addzero.lsi.jimmer.isJimmerType
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.type.LsiType

fun Collection<LsiClass>.toJimmerDdlLsiClasses(): List<LsiClass> {
    val cache = linkedMapOf<String, LsiClass>()
    return filter { it.isJimmerEntity }
        .map { it.toJimmerDdlLsiClass(cache) }
        .distinctBy { it.qualifiedName ?: it.simpleName.orEmpty() }
}

private fun LsiClass.toJimmerDdlLsiClass(cache: MutableMap<String, LsiClass>): LsiClass {
    val key = qualifiedName ?: simpleName.orEmpty()
    cache[key]?.let { return it }

    val convertedSuperClasses = (superClasses + interfaces)
        .filter { it.isJimmerType }
        .map { it.toJimmerDdlLsiClass(cache) }
    val fieldCandidates = fields.ifEmpty {
        methods.mapNotNull { it.toMethodBackedField(cache) }
    }
    val converted = JimmerDdlLsiClass(
        delegate = this,
        ddlFields = fieldCandidates,
        ddlSuperClasses = convertedSuperClasses,
    )
    cache[key] = converted
    return converted
}

private class JimmerDdlLsiClass(
    private val delegate: LsiClass,
    private val ddlFields: List<LsiField>,
    private val ddlSuperClasses: List<LsiClass>,
) : LsiClass {
    override val simpleName: String? get() = delegate.simpleName
    override val qualifiedName: String? get() = delegate.qualifiedName
    override val comment: String? get() = delegate.comment
    override val fields: List<LsiField> get() = ddlFields
    override val annotations: List<LsiAnnotation> get() = delegate.annotations
    override val isInterface: Boolean get() = delegate.isInterface
    override val isEnum: Boolean get() = delegate.isEnum
    override val isCollectionType: Boolean get() = delegate.isCollectionType
    override val isPojo: Boolean get() = delegate.isPojo
    override val superClasses: List<LsiClass> get() = ddlSuperClasses
    override val interfaces: List<LsiClass> get() = delegate.interfaces
    override val methods: List<LsiMethod> get() = delegate.methods
    override val fileName: String? get() = delegate.fileName
    override val isObject: Boolean get() = delegate.isObject
    override val isCompanionObject: Boolean get() = delegate.isCompanionObject
}

private fun LsiMethod.toMethodBackedField(cache: MutableMap<String, LsiClass>): LsiField? {
    if (isStatic || parameters.isNotEmpty()) {
        return null
    }
    val propertyName = name?.toJimmerPropertyName() ?: return null
    if (propertyName.isBlank() || propertyName == "class") {
        return null
    }
    return MethodBackedLsiField(propertyName, this, cache)
}

private class MethodBackedLsiField(
    private val propertyName: String,
    private val method: LsiMethod,
    private val cache: MutableMap<String, LsiClass>,
) : LsiField {
    override val name: String get() = propertyName
    override val type: LsiType? get() = method.returnType
    override val typeName: String? get() = method.returnTypeName
    override val comment: String? get() = method.comment
    override val annotations: List<LsiAnnotation> get() = method.annotations
    override val isStatic: Boolean get() = method.isStatic
    override val isConstant: Boolean get() = false
    override val isEnum: Boolean get() = method.returnType?.lsiClass?.isEnum ?: false
    override val isVar: Boolean get() = false
    override val isLateInit: Boolean get() = false
    override val isCollectionType: Boolean get() = method.returnType?.isCollectionType ?: false
    override val defaultValue: String? get() = null
    override val columnName: String? get() = annotations.columnNameFromAnnotations() ?: propertyName.toSnakeCase()
    override val declaringClass: LsiClass? get() = method.declaringClass
    override val fieldTypeClass: LsiClass? get() = method.returnType?.lsiClass?.toJimmerDdlLsiClass(cache)
    override val isNestedObject: Boolean get() = !isCollectionType && fieldTypeClass?.isPojo == true
    override val children: List<LsiField> get() = if (isNestedObject) fieldTypeClass?.fields.orEmpty() else emptyList()
}

private fun String.toJimmerPropertyName(): String {
    if (startsWith("get") && length > 3) {
        return substring(3).replaceFirstChar(Char::lowercase)
    }
    if (startsWith("is") && length > 2) {
        return substring(2).replaceFirstChar(Char::lowercase)
    }
    return this
}

private fun List<LsiAnnotation>.columnNameFromAnnotations(): String? {
    return firstNotNullOfOrNull { annotation ->
        if (!annotation.simpleName.equals("Column", ignoreCase = true)) {
            return@firstNotNullOfOrNull null
        }
        annotation.getAttribute("name")
            ?.toString()
            ?.takeIf { it.isNotBlank() }
    }
}

private fun String.toSnakeCase(): String {
    return replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
        .replace('-', '_')
        .lowercase()
}
