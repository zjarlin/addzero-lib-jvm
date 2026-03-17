package site.addzero.ddlgenerator.runtime.support

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.method.LsiParameter
import site.addzero.lsi.type.LsiType

data class TestAnnotation(
    override val qualifiedName: String?,
    override val simpleName: String?,
    override val attributes: Map<String, Any?> = emptyMap(),
) : LsiAnnotation {
    override fun getAttribute(name: String): Any? = attributes[name]
    override fun hasAttribute(name: String): Boolean = attributes.containsKey(name)
}

data class TestType(
    override val simpleName: String?,
    override val qualifiedName: String? = simpleName,
    override val presentableText: String? = simpleName,
    override val annotations: List<LsiAnnotation> = emptyList(),
    override val isCollectionType: Boolean = false,
    override val typeParameters: List<LsiType> = emptyList(),
    override val isPrimitive: Boolean = false,
    override val componentType: LsiType? = null,
    override val isArray: Boolean = false,
    override val lsiClass: LsiClass? = null,
) : LsiType

data class TestField(
    override val name: String?,
    override val type: LsiType? = null,
    override val typeName: String? = type?.simpleName,
    override val comment: String? = null,
    override val annotations: List<LsiAnnotation> = emptyList(),
    override val isStatic: Boolean = false,
    override val isConstant: Boolean = false,
    override val isEnum: Boolean = false,
    override val isVar: Boolean = false,
    override val isLateInit: Boolean = false,
    override val isCollectionType: Boolean = false,
    override val defaultValue: String? = null,
    override val columnName: String? = null,
    override val declaringClass: LsiClass? = null,
    override val fieldTypeClass: LsiClass? = null,
    override val isNestedObject: Boolean = false,
    override val children: List<LsiField> = emptyList(),
) : LsiField

data class TestParameter(
    override val name: String?,
    override val type: LsiType?,
    override val typeName: String? = type?.simpleName,
    override val annotations: List<LsiAnnotation> = emptyList(),
    override val hasDefault: Boolean = false,
) : LsiParameter

data class TestMethod(
    override val name: String?,
    override val returnType: LsiType? = null,
    override val returnTypeName: String? = returnType?.simpleName,
    override val comment: String? = null,
    override val annotations: List<LsiAnnotation> = emptyList(),
    override val isStatic: Boolean = false,
    override val isAbstract: Boolean = false,
    override val parameters: List<LsiParameter> = emptyList(),
    override val declaringClass: LsiClass? = null,
) : LsiMethod

data class TestClass(
    override val simpleName: String?,
    override val qualifiedName: String? = simpleName,
    override val comment: String? = null,
    override val fields: List<LsiField> = emptyList(),
    override val annotations: List<LsiAnnotation> = emptyList(),
    override val isInterface: Boolean = false,
    override val isEnum: Boolean = false,
    override val isCollectionType: Boolean = false,
    override val isPojo: Boolean = true,
    override val superClasses: List<LsiClass> = emptyList(),
    override val interfaces: List<LsiClass> = emptyList(),
    override val methods: List<LsiMethod> = emptyList(),
) : LsiClass
