package site.addzero.jimmer.apt.processor.analyzer

import site.addzero.jimmer.apt.processor.model.*
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField

/**
 * Jimmer注解提取器
 * 
 * 负责从LSI抽象中提取Jimmer特定的注解信息
 */
class JimmerAnnotationExtractor {

    /**
     * 提取实体信息
     */
    fun extractEntityInfo(lsiClass: LsiClass): JimmerEntityInfo {
        val entityAnnotation = findEntityAnnotation(lsiClass)
        
        return JimmerEntityInfo(
            tableName = extractTableName(entityAnnotation),
            microServiceName = extractMicroServiceName(entityAnnotation),
            logicalDeleted = hasLogicalDeletedAnnotation(lsiClass),
            immutable = extractImmutableFlag(lsiClass),
            isAbstract = lsiClass.isInterface || lsiClass.annotations.any { 
                it.qualifiedName?.endsWith("MappedSuperclass") == true 
            }
        )
    }

    /**
     * 提取字段信息
     */
    fun extractFieldInfo(lsiField: LsiField): JimmerFieldInfo {
        return JimmerFieldInfo(
            columnName = extractColumnName(lsiField),
            insertable = extractInsertable(lsiField),
            updatable = extractUpdatable(lsiField),
            formula = extractFormula(lsiField),
            idView = extractIdView(lsiField),
            key = hasKeyAnnotation(lsiField),
            version = hasVersionAnnotation(lsiField),
            logicalDeleted = hasLogicalDeletedAnnotation(lsiField),
            generatedValueStrategy = extractGeneratedValueStrategy(lsiField)
        )
    }

    /**
     * 提取关系信息
     */
    fun extractRelationshipInfo(lsiField: LsiField): RelationshipMetadata? {
        val relationshipType = determineRelationshipType(lsiField) ?: return null
        
        return when (relationshipType) {
            RelationshipType.MANY_TO_ONE -> extractManyToOneRelationship(lsiField)
            RelationshipType.ONE_TO_MANY -> extractOneToManyRelationship(lsiField)
            RelationshipType.MANY_TO_MANY -> extractManyToManyRelationship(lsiField)
            RelationshipType.ONE_TO_ONE -> extractOneToOneRelationship(lsiField)
        }
    }

    /**
     * 判断是否为计算属性
     */
    fun isComputedProperty(lsiField: LsiField): Boolean {
        return hasFormulaAnnotation(lsiField) || 
               hasIdViewAnnotation(lsiField) ||
               isTransientField(lsiField)
    }

    // ========== 私有辅助方法 ==========

    private fun findEntityAnnotation(lsiClass: LsiClass): LsiAnnotation? {
        return lsiClass.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.Entity" ||
            annotation.simpleName == "Entity"
        }
    }

    private fun extractTableName(entityAnnotation: LsiAnnotation?): String? {
        return entityAnnotation?.getAttribute("name") as? String
    }

    private fun extractMicroServiceName(entityAnnotation: LsiAnnotation?): String? {
        return entityAnnotation?.getAttribute("microServiceName") as? String
    }

    private fun hasLogicalDeletedAnnotation(lsiClass: LsiClass): Boolean {
        return lsiClass.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.LogicalDeleted" ||
            annotation.simpleName == "LogicalDeleted"
        }
    }

    private fun hasLogicalDeletedAnnotation(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.LogicalDeleted" ||
            annotation.simpleName == "LogicalDeleted"
        }
    }

    private fun extractImmutableFlag(lsiClass: LsiClass): Boolean {
        // Jimmer实体默认是不可变的，除非明确标记为可变
        val mutableAnnotation = lsiClass.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.Mutable" ||
            annotation.simpleName == "Mutable"
        }
        return mutableAnnotation == null
    }

    private fun extractColumnName(lsiField: LsiField): String? {
        val columnAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.Column" ||
            annotation.simpleName == "Column"
        }
        return columnAnnotation?.getAttribute("name") as? String
    }

    private fun extractInsertable(lsiField: LsiField): Boolean {
        val columnAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.Column" ||
            annotation.simpleName == "Column"
        }
        return columnAnnotation?.getAttribute("insertable") as? Boolean ?: true
    }

    private fun extractUpdatable(lsiField: LsiField): Boolean {
        val columnAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.Column" ||
            annotation.simpleName == "Column"
        }
        return columnAnnotation?.getAttribute("updatable") as? Boolean ?: true
    }

    private fun extractFormula(lsiField: LsiField): String? {
        val formulaAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.Formula" ||
            annotation.simpleName == "Formula"
        }
        return formulaAnnotation?.getAttribute("sql") as? String
    }

    private fun extractIdView(lsiField: LsiField): String? {
        val idViewAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.IdView" ||
            annotation.simpleName == "IdView"
        }
        return idViewAnnotation?.getAttribute("value") as? String
    }

    private fun hasKeyAnnotation(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.Key" ||
            annotation.simpleName == "Key"
        }
    }

    private fun hasVersionAnnotation(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.Version" ||
            annotation.simpleName == "Version"
        }
    }

    private fun extractGeneratedValueStrategy(lsiField: LsiField): GeneratedValueStrategy? {
        val generatedValueAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.GeneratedValue" ||
            annotation.simpleName == "GeneratedValue"
        }
        
        val strategyValue = generatedValueAnnotation?.getAttribute("strategy") as? String
        return when (strategyValue) {
            "IDENTITY" -> GeneratedValueStrategy.IDENTITY
            "SEQUENCE" -> GeneratedValueStrategy.SEQUENCE
            "AUTO" -> GeneratedValueStrategy.AUTO
            "UUID" -> GeneratedValueStrategy.UUID
            else -> null
        }
    }

    private fun hasFormulaAnnotation(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.Formula" ||
            annotation.simpleName == "Formula"
        }
    }

    private fun hasIdViewAnnotation(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.IdView" ||
            annotation.simpleName == "IdView"
        }
    }

    private fun isTransientField(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "java.beans.Transient" ||
            annotation.qualifiedName == "javax.persistence.Transient" ||
            annotation.simpleName == "Transient"
        }
    }

    private fun determineRelationshipType(lsiField: LsiField): RelationshipType? {
        return when {
            hasAnnotation(lsiField, "ManyToOne") -> RelationshipType.MANY_TO_ONE
            hasAnnotation(lsiField, "OneToMany") -> RelationshipType.ONE_TO_MANY
            hasAnnotation(lsiField, "ManyToMany") -> RelationshipType.MANY_TO_MANY
            hasAnnotation(lsiField, "OneToOne") -> RelationshipType.ONE_TO_ONE
            else -> null
        }
    }

    private fun hasAnnotation(lsiField: LsiField, annotationName: String): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName?.endsWith(".$annotationName") == true ||
            annotation.simpleName == annotationName
        }
    }

    private fun extractManyToOneRelationship(lsiField: LsiField): RelationshipMetadata {
        // TODO: 实现ManyToOne关系提取逻辑
        return RelationshipMetadata(
            type = RelationshipType.MANY_TO_ONE,
            sourceTable = "", // 需要从上下文获取
            targetTable = "", // 需要从字段类型推断
            sourceColumn = lsiField.name ?: "unknown",
            targetColumn = "id" // 默认目标主键
        )
    }

    private fun extractOneToManyRelationship(lsiField: LsiField): RelationshipMetadata {
        // TODO: 实现OneToMany关系提取逻辑
        return RelationshipMetadata(
            type = RelationshipType.ONE_TO_MANY,
            sourceTable = "", // 需要从上下文获取
            targetTable = "", // 需要从字段类型推断
            sourceColumn = "id", // 默认源主键
            targetColumn = "" // 需要从mappedBy获取
        )
    }

    private fun extractManyToManyRelationship(lsiField: LsiField): RelationshipMetadata {
        // TODO: 实现ManyToMany关系提取逻辑
        return RelationshipMetadata(
            type = RelationshipType.MANY_TO_MANY,
            sourceTable = "", // 需要从上下文获取
            targetTable = "", // 需要从字段类型推断
            sourceColumn = "id", // 默认源主键
            targetColumn = "id", // 默认目标主键
            joinTable = "" // 需要从JoinTable注解获取
        )
    }

    private fun extractOneToOneRelationship(lsiField: LsiField): RelationshipMetadata {
        // TODO: 实现OneToOne关系提取逻辑
        return RelationshipMetadata(
            type = RelationshipType.ONE_TO_ONE,
            sourceTable = "", // 需要从上下文获取
            targetTable = "", // 需要从字段类型推断
            sourceColumn = lsiField.name ?: "unknown",
            targetColumn = "id" // 默认目标主键
        )
    }
}