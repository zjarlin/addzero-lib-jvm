package site.addzero.jimmer.apt.processor.analyzer

import site.addzero.jimmer.apt.processor.model.*
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass

/**
 * Jimmer关系分析器
 * 
 * 专门负责分析Jimmer实体之间的关系注解
 */
class JimmerRelationshipAnalyzer {

    /**
     * 分析字段的关系信息
     */
    fun analyzeRelationship(
        lsiField: LsiField, 
        sourceEntity: LsiClass,
        sourceTableName: String
    ): RelationshipMetadata? {
        val relationshipType = determineRelationshipType(lsiField) ?: return null
        
        return when (relationshipType) {
            RelationshipType.MANY_TO_ONE -> analyzeManyToOneRelationship(lsiField, sourceEntity, sourceTableName)
            RelationshipType.ONE_TO_MANY -> analyzeOneToManyRelationship(lsiField, sourceEntity, sourceTableName)
            RelationshipType.MANY_TO_MANY -> analyzeManyToManyRelationship(lsiField, sourceEntity, sourceTableName)
            RelationshipType.ONE_TO_ONE -> analyzeOneToOneRelationship(lsiField, sourceEntity, sourceTableName)
        }
    }

    /**
     * 提取级联操作
     */
    fun extractCascadeActions(relationshipAnnotation: LsiAnnotation): Set<CascadeAction> {
        val cascadeValue = relationshipAnnotation.getAttribute("cascade")
        
        return when (cascadeValue) {
            is Array<*> -> {
                cascadeValue.mapNotNull { value ->
                    when (value.toString()) {
                        "PERSIST" -> CascadeAction.PERSIST
                        "MERGE" -> CascadeAction.MERGE
                        "REMOVE" -> CascadeAction.REMOVE
                        "REFRESH" -> CascadeAction.REFRESH
                        "DETACH" -> CascadeAction.DETACH
                        "ALL" -> CascadeAction.ALL
                        else -> null
                    }
                }.toSet()
            }
            is String -> {
                when (cascadeValue) {
                    "ALL" -> setOf(CascadeAction.ALL)
                    else -> emptySet()
                }
            }
            else -> emptySet()
        }
    }

    /**
     * 提取解除关联操作
     */
    fun extractDissociateAction(lsiField: LsiField): DissociateAction? {
        val onDissociateAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.OnDissociate" ||
            annotation.simpleName == "OnDissociate"
        }
        
        val actionValue = onDissociateAnnotation?.getAttribute("value") as? String
        return when (actionValue) {
            "SET_NULL" -> DissociateAction.SET_NULL
            "DELETE" -> DissociateAction.DELETE
            "CHECK" -> DissociateAction.CHECK
            else -> null
        }
    }

    // ========== 私有辅助方法 ==========

    private fun determineRelationshipType(lsiField: LsiField): RelationshipType? {
        return when {
            hasAnnotation(lsiField, "ManyToOne") -> RelationshipType.MANY_TO_ONE
            hasAnnotation(lsiField, "OneToMany") -> RelationshipType.ONE_TO_MANY
            hasAnnotation(lsiField, "ManyToMany") -> RelationshipType.MANY_TO_MANY
            hasAnnotation(lsiField, "OneToOne") -> RelationshipType.ONE_TO_ONE
            else -> null
        }
    }

    private fun analyzeManyToOneRelationship(
        lsiField: LsiField,
        sourceEntity: LsiClass,
        sourceTableName: String
    ): RelationshipMetadata {
        val manyToOneAnnotation = findRelationshipAnnotation(lsiField, "ManyToOne")
        val targetTableName = extractTargetTableName(lsiField)
        val foreignKeyColumn = extractForeignKeyColumn(lsiField)
        
        return RelationshipMetadata(
            type = RelationshipType.MANY_TO_ONE,
            sourceTable = sourceTableName,
            targetTable = targetTableName,
            sourceColumn = foreignKeyColumn,
            targetColumn = "id", // 默认目标主键
            cascadeActions = manyToOneAnnotation?.let { extractCascadeActions(it) } ?: emptySet(),
            dissociateAction = extractDissociateAction(lsiField)
        )
    }

    private fun analyzeOneToManyRelationship(
        lsiField: LsiField,
        sourceEntity: LsiClass,
        sourceTableName: String
    ): RelationshipMetadata {
        val oneToManyAnnotation = findRelationshipAnnotation(lsiField, "OneToMany")
        val mappedBy = oneToManyAnnotation?.getAttribute("mappedBy") as? String
        val targetTableName = extractTargetTableName(lsiField)
        
        return RelationshipMetadata(
            type = RelationshipType.ONE_TO_MANY,
            sourceTable = sourceTableName,
            targetTable = targetTableName,
            sourceColumn = "id", // 源表主键
            targetColumn = mappedBy?.let { camelToSnakeCase(it) + "_id" } ?: "parent_id",
            cascadeActions = oneToManyAnnotation?.let { extractCascadeActions(it) } ?: emptySet(),
            dissociateAction = extractDissociateAction(lsiField)
        )
    }

    private fun analyzeManyToManyRelationship(
        lsiField: LsiField,
        sourceEntity: LsiClass,
        sourceTableName: String
    ): RelationshipMetadata {
        val manyToManyAnnotation = findRelationshipAnnotation(lsiField, "ManyToMany")
        val joinTableInfo = extractJoinTableInfo(lsiField, sourceTableName)
        val targetTableName = extractTargetTableName(lsiField)
        
        return RelationshipMetadata(
            type = RelationshipType.MANY_TO_MANY,
            sourceTable = sourceTableName,
            targetTable = targetTableName,
            sourceColumn = "id",
            targetColumn = "id",
            joinTable = joinTableInfo.tableName,
            joinSourceColumn = joinTableInfo.sourceColumn,
            joinTargetColumn = joinTableInfo.targetColumn,
            cascadeActions = manyToManyAnnotation?.let { extractCascadeActions(it) } ?: emptySet(),
            dissociateAction = extractDissociateAction(lsiField)
        )
    }

    private fun analyzeOneToOneRelationship(
        lsiField: LsiField,
        sourceEntity: LsiClass,
        sourceTableName: String
    ): RelationshipMetadata {
        val oneToOneAnnotation = findRelationshipAnnotation(lsiField, "OneToOne")
        val targetTableName = extractTargetTableName(lsiField)
        val foreignKeyColumn = extractForeignKeyColumn(lsiField)
        
        return RelationshipMetadata(
            type = RelationshipType.ONE_TO_ONE,
            sourceTable = sourceTableName,
            targetTable = targetTableName,
            sourceColumn = foreignKeyColumn,
            targetColumn = "id", // 默认目标主键
            cascadeActions = oneToOneAnnotation?.let { extractCascadeActions(it) } ?: emptySet(),
            dissociateAction = extractDissociateAction(lsiField)
        )
    }

    private fun findRelationshipAnnotation(lsiField: LsiField, annotationName: String): LsiAnnotation? {
        return lsiField.annotations.find { annotation ->
            annotation.qualifiedName?.endsWith(".$annotationName") == true ||
            annotation.simpleName == annotationName
        }
    }

    private fun extractTargetTableName(lsiField: LsiField): String {
        // 从字段类型推断目标表名
        val fieldType = lsiField.type?.qualifiedName ?: return "unknown_table"
        
        // 处理集合类型
        if (fieldType.contains("List<") || fieldType.contains("Set<")) {
            val genericType = extractGenericType(fieldType)
            return classNameToTableName(genericType)
        }
        
        return classNameToTableName(fieldType)
    }

    private fun extractForeignKeyColumn(lsiField: LsiField): String {
        // 检查@JoinColumn注解
        val joinColumnAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.JoinColumn" ||
            annotation.simpleName == "JoinColumn"
        }
        
        val columnName = joinColumnAnnotation?.getAttribute("name") as? String
        if (columnName != null) {
            return columnName
        }
        
        // 默认外键列名：字段名 + "_id"
        return camelToSnakeCase(lsiField.name ?: "unknown") + "_id"
    }

    private fun extractJoinTableInfo(lsiField: LsiField, sourceTableName: String): JoinTableInfo {
        val joinTableAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.JoinTable" ||
            annotation.simpleName == "JoinTable"
        }
        
        if (joinTableAnnotation != null) {
            val tableName = joinTableAnnotation.getAttribute("name") as? String
            val joinColumnName = joinTableAnnotation.getAttribute("joinColumnName") as? String
            val inverseJoinColumnName = joinTableAnnotation.getAttribute("inverseJoinColumnName") as? String
            
            return JoinTableInfo(
                tableName = tableName ?: generateDefaultJoinTableName(sourceTableName, extractTargetTableName(lsiField)),
                sourceColumn = joinColumnName ?: "${sourceTableName}_id",
                targetColumn = inverseJoinColumnName ?: "${extractTargetTableName(lsiField)}_id"
            )
        }
        
        // 默认连接表信息
        val targetTableName = extractTargetTableName(lsiField)
        return JoinTableInfo(
            tableName = generateDefaultJoinTableName(sourceTableName, targetTableName),
            sourceColumn = "${sourceTableName}_id",
            targetColumn = "${targetTableName}_id"
        )
    }

    private fun generateDefaultJoinTableName(sourceTable: String, targetTable: String): String {
        // 按字母顺序排列表名
        return if (sourceTable <= targetTable) {
            "${sourceTable}_${targetTable}_mapping"
        } else {
            "${targetTable}_${sourceTable}_mapping"
        }
    }

    private fun extractGenericType(fieldType: String): String {
        val startIndex = fieldType.indexOf('<')
        val endIndex = fieldType.lastIndexOf('>')
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return fieldType.substring(startIndex + 1, endIndex)
        }
        
        return fieldType
    }

    private fun classNameToTableName(className: String): String {
        // 提取简单类名
        val simpleName = className.substringAfterLast('.')
        
        // 转换为下划线命名
        return camelToSnakeCase(simpleName)
    }

    private fun hasAnnotation(lsiField: LsiField, annotationName: String): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName?.endsWith(".$annotationName") == true ||
            annotation.simpleName == annotationName
        }
    }

    private fun camelToSnakeCase(camelCase: String): String {
        return camelCase.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
    }

    /**
     * 连接表信息数据类
     */
    private data class JoinTableInfo(
        val tableName: String,
        val sourceColumn: String,
        val targetColumn: String
    )
}