package site.addzero.ksp.metadata.jimmer.entity.spi

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XAnnotated
import androidx.room.compiler.processing.XFieldElement
import androidx.room.compiler.processing.XMethodElement
import androidx.room.compiler.processing.XNullability
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.XTypeElement
import androidx.room.compiler.processing.isArray
import androidx.room.compiler.processing.isEnum
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

const val JIMMER_ENTITY_ANNOTATION = "org.babyfish.jimmer.sql.Entity"

data class JimmerEntityCollectionResult(
    val deferred: List<KSAnnotated>,
    val entitiesByQualifiedName: Map<String, JimmerEntityMeta>
) {
    val entities: Set<JimmerEntityMeta>
        get() = LinkedHashSet(entitiesByQualifiedName.values)
}

object JimmerEntityCollector {
    @OptIn(ExperimentalProcessingApi::class)
    fun collect(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver
    ): JimmerEntityCollectionResult {
        val xProcessingEnv = XProcessingEnv.create(environment, resolver)
        val logger = environment.logger
        val symbols = resolver.getSymbolsWithAnnotation(JIMMER_ENTITY_ANNOTATION).toList()
        val deferred = mutableListOf<KSAnnotated>()
        val entitiesByQualifiedName = linkedMapOf<String, JimmerEntityMeta>()

        symbols.forEach { symbol ->
            if (!symbol.validate()) {
                deferred += symbol
                return@forEach
            }

            val declaration = symbol as? KSClassDeclaration ?: return@forEach
            val qualifiedName = declaration.qualifiedName?.asString() ?: return@forEach
            val typeElement = xProcessingEnv.findTypeElement(qualifiedName)
            if (typeElement == null) {
                logger.error("收集 Jimmer 实体失败: 无法解析 XTypeElement: $qualifiedName")
                return@forEach
            }
            entitiesByQualifiedName[qualifiedName] = typeElement.toJimmerEntityMeta()
        }

        return JimmerEntityCollectionResult(
            deferred = deferred,
            entitiesByQualifiedName = entitiesByQualifiedName
        )
    }
}

private fun XTypeElement.toJimmerEntityMeta(): JimmerEntityMeta {
    return JimmerEntityMeta(
        qualifiedName = qualifiedName,
        packageName = packageName,
        simpleName = name,
        docComment = docComment.orEmpty(),
        properties = collectJimmerProperties()
    )
}

private fun XTypeElement.collectJimmerProperties(): List<JimmerPropertyMeta> {
    val fieldsByName = getAllFieldsIncludingPrivateSupers().associateBy { it.name }
    val propertiesByName = linkedMapOf<String, JimmerPropertyMeta>()

    getAllMethods()
        .asSequence()
        .filter { it.isKotlinPropertyGetter() }
        .filter { it.parameters.isEmpty() }
        .filter { !it.isStatic() }
        .forEach { method ->
            val propertyName = method.propertyName ?: normalizeMethodPropertyName(method)
            if (propertyName.isBlank()) {
                return@forEach
            }
            val relatedField = fieldsByName[propertyName]
            propertiesByName[propertyName] = JimmerPropertyMeta(
                name = propertyName,
                type = method.returnType.toJimmerTypeRef(),
                docComment = method.docComment.orEmpty().ifBlank { relatedField?.docComment.orEmpty() },
                formIgnored = method.hasFormIgnore() || (relatedField?.hasFormIgnore() == true)
            )
        }

    if (propertiesByName.isNotEmpty()) {
        return propertiesByName.values.toList()
    }

    fieldsByName.values.forEach { field ->
        propertiesByName[field.name] = JimmerPropertyMeta(
            name = field.name,
            type = field.type.toJimmerTypeRef(),
            docComment = field.docComment.orEmpty(),
            formIgnored = field.hasFormIgnore()
        )
    }

    return propertiesByName.values.toList()
}

private fun XType.toJimmerTypeRef(): JimmerTypeRef {
    val typeElement = typeElement
    val sourceTypeName = asTypeName().toString()
    val qualifiedName = typeElement?.qualifiedName
    val simpleName = qualifiedName?.substringAfterLast('.')
        ?: parseSimpleTypeName(sourceTypeName)
    val typeArguments = typeArguments.map { it.toJimmerTypeRef() }
    val typeKind = when {
        isArray() -> JimmerTypeKind.ARRAY
        isCollectionType(qualifiedName, simpleName) -> JimmerTypeKind.COLLECTION
        mapToKotlinPrimitive(qualifiedName, simpleName) != null -> JimmerTypeKind.BASIC
        simpleName == "String" || qualifiedName == "java.lang.String" || qualifiedName == "kotlin.String" -> JimmerTypeKind.BASIC
        qualifiedName == "java.math.BigDecimal" || simpleName == "BigDecimal" -> JimmerTypeKind.BASIC
        mapToKotlinxDateTime(qualifiedName, simpleName) != null -> JimmerTypeKind.DATE_TIME
        qualifiedName in setOf("java.util.Date", "java.sql.Timestamp") -> JimmerTypeKind.BASIC
        typeElement?.isEnum() == true -> JimmerTypeKind.ENUM
        isJimmerEntity(typeElement) -> JimmerTypeKind.ENTITY
        else -> JimmerTypeKind.OTHER
    }

    return JimmerTypeRef(
        qualifiedName = qualifiedName,
        simpleName = simpleName,
        nullable = nullability == XNullability.NULLABLE,
        kind = typeKind,
        typeArguments = typeArguments,
        sourceTypeName = sourceTypeName
    )
}

private fun XAnnotated.hasFormIgnore(): Boolean {
    return getAllAnnotations().any { annotation ->
        annotation.qualifiedName == "site.addzero.entity2form.annotation.FormIgnore" ||
            annotation.name == "FormIgnore"
    }
}

private fun normalizeMethodPropertyName(method: XMethodElement): String {
    val methodName = method.name
    if (methodName.startsWith("get") && methodName.length > 3) {
        return methodName.substring(3).replaceFirstChar { it.lowercase() }
    }
    if (methodName.startsWith("is") && methodName.length > 2) {
        return methodName.substring(2).replaceFirstChar { it.lowercase() }
    }
    return methodName
}

private fun isCollectionType(qualifiedName: String?, simpleName: String): Boolean {
    if (qualifiedName == null) {
        return simpleName in setOf(
            "List",
            "MutableList",
            "Set",
            "MutableSet",
            "Collection",
            "Map",
            "MutableMap"
        )
    }
    return qualifiedName in setOf(
        "kotlin.collections.List",
        "kotlin.collections.MutableList",
        "kotlin.collections.Set",
        "kotlin.collections.MutableSet",
        "kotlin.collections.Collection",
        "kotlin.collections.Map",
        "kotlin.collections.MutableMap",
        "java.util.List",
        "java.util.Set",
        "java.util.Collection",
        "java.util.Map"
    )
}

private fun isJimmerEntity(typeElement: XTypeElement?): Boolean {
    return typeElement
        ?.getAllAnnotations()
        ?.any { it.qualifiedName == JIMMER_ENTITY_ANNOTATION } == true
}

private fun mapToKotlinPrimitive(qualifiedName: String?, simpleName: String): String? {
    return when ((qualifiedName ?: simpleName).lowercase()) {
        "kotlin.int", "java.lang.integer", "int", "integer" -> "Int"
        "kotlin.long", "java.lang.long", "long" -> "Long"
        "kotlin.short", "java.lang.short", "short" -> "Short"
        "kotlin.byte", "java.lang.byte", "byte" -> "Byte"
        "kotlin.float", "java.lang.float", "float" -> "Float"
        "kotlin.double", "java.lang.double", "double" -> "Double"
        "kotlin.boolean", "java.lang.boolean", "boolean" -> "Boolean"
        "kotlin.char", "java.lang.character", "char", "character" -> "Char"
        else -> null
    }
}

private fun mapToKotlinxDateTime(qualifiedName: String?, simpleName: String): String? {
    val normalized = qualifiedName ?: simpleName
    return when (normalized) {
        "java.time.LocalDate" -> "LocalDate"
        "java.time.LocalDateTime" -> "LocalDateTime"
        "java.time.LocalTime" -> "LocalTime"
        "java.time.Instant" -> "Instant"
        "kotlinx.datetime.LocalDate" -> "LocalDate"
        "kotlinx.datetime.LocalDateTime" -> "LocalDateTime"
        "kotlinx.datetime.LocalTime" -> "LocalTime"
        "kotlinx.datetime.Instant" -> "Instant"
        else -> null
    }
}

private fun parseSimpleTypeName(typeName: String): String {
    return typeName
        .substringBefore('<')
        .substringAfterLast('.')
        .removeSuffix("?")
        .trim()
        .ifBlank { "Any" }
}
