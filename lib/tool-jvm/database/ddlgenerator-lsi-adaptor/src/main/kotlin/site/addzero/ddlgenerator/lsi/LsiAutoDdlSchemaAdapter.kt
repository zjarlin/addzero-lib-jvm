package site.addzero.ddlgenerator.lsi

import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlForeignKey
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlJunction
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlSchema
import site.addzero.ddlgenerator.core.model.AutoDdlSequence
import site.addzero.ddlgenerator.core.model.AutoDdlTable
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.clazz.guessTableName
import site.addzero.lsi.field.LsiField

object LsiAutoDdlSchemaAdapter {

    fun from(classes: List<LsiClass>): AutoDdlSchema {
        val entities = classes.filter { it.isPersistedEntity() }
        val tables = entities.map { entity ->
            entity.toAutoDdlTable(entities)
        }
        val sequences = entities
            .mapNotNull { entity ->
                entity.allFields()
                    .firstOrNull { it.isIdField() && !it.sequenceName().isNullOrBlank() }
                    ?.sequenceName()
            }
            .distinct()
            .map { AutoDdlSequence(name = it) }
        return AutoDdlSchema(tables = tables, sequences = sequences)
    }

    private fun LsiClass.toAutoDdlTable(allEntities: List<LsiClass>): AutoDdlTable {
        val scalarColumns = mutableListOf<AutoDdlColumn>()
        val foreignKeys = mutableListOf<AutoDdlForeignKey>()

        allFields().forEach { field ->
            when {
                field.shouldSkipField() -> Unit
                field.isOwningAssociation() -> {
                    val referencedClass = field.fieldTypeClass ?: return@forEach
                    val referencedId = referencedClass.allFields().firstOrNull { it.isIdField() }
                    val columnName = field.joinColumnName() ?: "${field.name.orEmpty()}_id"
                    val referenceColumnName = field.referencedColumnName() ?: referencedId?.columnName ?: referencedId?.name ?: "id"
                    val column = AutoDdlColumn(
                        name = columnName,
                        logicalType = referencedId?.toLogicalType() ?: AutoDdlLogicalType.INT64,
                        nullable = field.isNullable,
                        comment = field.comment,
                    )
                    scalarColumns += column
                    foreignKeys += AutoDdlForeignKey(
                        name = "fk_${guessTableName}_$columnName",
                        columnNames = listOf(columnName),
                        referencedTableName = referencedClass.guessTableName,
                        referencedColumnNames = listOf(referenceColumnName),
                    )
                }
                field.isOwningManyToMany() -> Unit
                else -> scalarColumns += field.toColumn()
            }
        }

        val indexes = buildIndexes(this, scalarColumns)
        return AutoDdlTable(
            name = guessTableName,
            comment = comment,
            columns = scalarColumns.distinctBy { it.name.lowercase() },
            indexes = indexes,
            foreignKeys = foreignKeys,
        )
    }

    private fun buildIndexes(
        clazz: LsiClass,
        columns: List<AutoDdlColumn>,
    ): List<AutoDdlIndex> {
        val fields = clazz.allFields()
            .filter { field -> columns.any { it.name.equals(field.columnName ?: field.name, ignoreCase = true) } }

        val groupedKeys = fields
            .filter { it.hasAnnotationSimple("Key") && !it.isIdField() }
            .groupBy { it.annotationValue("Key", "group")?.takeIf(String::isNotBlank) ?: "" }

        return buildList {
            groupedKeys.forEach { (groupName, groupedFields) ->
                val columnNames = groupedFields.mapNotNull { it.columnName ?: it.name }
                if (columnNames.isEmpty()) {
                    return@forEach
                }
                val normalizedTableName = clazz.guessTableName
                val indexName = if (groupName.isBlank()) {
                    "uk_${normalizedTableName}_${columnNames.joinToString("_")}"
                } else {
                    "uk_${normalizedTableName}_$groupName"
                }
                add(AutoDdlIndex(name = indexName, columnNames = columnNames, type = AutoDdlIndexType.UNIQUE))
            }

            fields.filter { it.isUniqueField() && !it.hasAnnotationSimple("Key") }
                .forEach { field ->
                    val columnName = field.columnName ?: field.name ?: return@forEach
                    add(
                        AutoDdlIndex(
                            name = "uk_${clazz.guessTableName}_$columnName",
                            columnNames = listOf(columnName),
                            type = AutoDdlIndexType.UNIQUE,
                        )
                    )
                }
        }.distinctBy { it.name.lowercase() }
    }

    fun scanManyToManyTables(classes: List<LsiClass>): List<AutoDdlTable> {
        val entities = classes.filter { it.isPersistedEntity() }
        val tables = linkedMapOf<String, AutoDdlTable>()
        entities.forEach { leftEntity ->
            leftEntity.allFields()
                .filter { it.isOwningManyToMany() }
                .forEach { field ->
                    val rightEntity = field.resolveManyToManyTarget(entities) ?: return@forEach
                    val tableName = field.annotationValue("JoinTable", "name")
                        ?.takeIf { it.isNotBlank() }
                        ?: listOf(leftEntity.guessTableName, rightEntity.guessTableName).sorted().joinToString("_")
                    val leftColumnName = field.annotationValue("JoinTable", "joinColumnName")
                        ?.takeIf { it.isNotBlank() }
                        ?: "${leftEntity.guessTableName}_id"
                    val rightColumnName = field.annotationValue("JoinTable", "inverseJoinColumnName")
                        ?.takeIf { it.isNotBlank() }
                        ?: "${rightEntity.guessTableName}_id"

                    val table = AutoDdlTable(
                        name = tableName,
                        columns = listOf(
                            AutoDdlColumn(leftColumnName, AutoDdlLogicalType.INT64, nullable = false, primaryKey = true),
                            AutoDdlColumn(rightColumnName, AutoDdlLogicalType.INT64, nullable = false, primaryKey = true),
                        ),
                        foreignKeys = listOf(
                            AutoDdlForeignKey(
                                name = "fk_${tableName}_$leftColumnName",
                                columnNames = listOf(leftColumnName),
                                referencedTableName = leftEntity.guessTableName,
                                referencedColumnNames = listOf("id"),
                            ),
                            AutoDdlForeignKey(
                                name = "fk_${tableName}_$rightColumnName",
                                columnNames = listOf(rightColumnName),
                                referencedTableName = rightEntity.guessTableName,
                                referencedColumnNames = listOf("id"),
                            )
                        ),
                        junction = AutoDdlJunction(
                            leftTableName = leftEntity.guessTableName,
                            rightTableName = rightEntity.guessTableName,
                            leftColumnName = leftColumnName,
                            rightColumnName = rightColumnName,
                        )
                    )
                    tables.putIfAbsent(table.name.lowercase(), table)
                }
        }
        return tables.values.toList()
    }

    private fun LsiClass.allFields(): List<LsiField> {
        return (superClasses.flatMap { it.allFields() } + fields).distinctBy { it.name }
    }

    private fun LsiClass.isPersistedEntity(): Boolean {
        return annotations.any { annotation ->
            annotation.qualifiedName in ENTITY_ANNOTATIONS
        }
    }

    private fun LsiField.toColumn(): AutoDdlColumn {
        val columnName = columnName ?: name.orEmpty()
        return AutoDdlColumn(
            name = columnName,
            logicalType = toLogicalType(),
            nullable = isNullable,
            length = length(),
            precision = precision(),
            scale = scale(),
            defaultValue = defaultValue?.takeIf { it.isNotBlank() },
            comment = comment,
            primaryKey = isIdField(),
            autoIncrement = isAutoIncrement(),
            sequenceName = sequenceName(),
        )
    }

    private fun LsiField.toLogicalType(): AutoDdlLogicalType {
        val rawType = typeName?.substringAfterLast('.') ?: return AutoDdlLogicalType.UNKNOWN
        return when (rawType) {
            "String" -> if (isTextType()) AutoDdlLogicalType.TEXT else AutoDdlLogicalType.STRING
            "Char", "Character" -> AutoDdlLogicalType.CHAR
            "Boolean", "boolean" -> AutoDdlLogicalType.BOOLEAN
            "Byte", "byte" -> AutoDdlLogicalType.INT8
            "Short", "short" -> AutoDdlLogicalType.INT16
            "Int", "Integer", "int" -> AutoDdlLogicalType.INT32
            "Long", "long" -> AutoDdlLogicalType.INT64
            "Float", "float" -> AutoDdlLogicalType.FLOAT32
            "Double", "double" -> AutoDdlLogicalType.FLOAT64
            "BigDecimal" -> AutoDdlLogicalType.DECIMAL
            "BigInteger" -> AutoDdlLogicalType.BIG_INTEGER
            "LocalDate", "sqlDate", "DateOnly" -> AutoDdlLogicalType.DATE
            "LocalTime", "sqlTime" -> AutoDdlLogicalType.TIME
            "Instant", "OffsetDateTime", "ZonedDateTime" -> AutoDdlLogicalType.DATETIME_TZ
            "LocalDateTime", "Date", "sqlTimestamp", "Timestamp" -> AutoDdlLogicalType.DATETIME
            "Duration" -> AutoDdlLogicalType.DURATION
            "UUID" -> AutoDdlLogicalType.UUID
            "JsonNode" -> AutoDdlLogicalType.JSON
            "ByteArray", "byte[]" -> AutoDdlLogicalType.BINARY
            else -> AutoDdlLogicalType.UNKNOWN
        }
    }

    private fun LsiField.shouldSkipField(): Boolean {
        return isStatic ||
            hasAnnotationSimple("Transient", "Formula", "ManyToManyView", "IdView") ||
            (isCollectionType && !isOwningManyToMany())
    }

    private fun LsiField.isOwningAssociation(): Boolean {
        if (!hasAnnotationSimple("ManyToOne", "OneToOne")) {
            return false
        }
        return annotationValue("ManyToOne", "mappedBy").isNullOrBlank() &&
            annotationValue("OneToOne", "mappedBy").isNullOrBlank()
    }

    private fun LsiField.isOwningManyToMany(): Boolean {
        if (!hasAnnotationSimple("ManyToMany")) {
            return false
        }
        return annotationValue("ManyToMany", "mappedBy").isNullOrBlank()
    }

    private fun LsiField.resolveManyToManyTarget(allEntities: List<LsiClass>): LsiClass? {
        val typeParameters = type?.typeParameters.orEmpty()
        val targetType = typeParameters.firstOrNull()?.qualifiedName ?: fieldTypeClass?.qualifiedName
        return allEntities.firstOrNull { entity ->
            entity.qualifiedName == targetType || entity.simpleName == targetType
        }
    }

    private fun LsiField.isIdField(): Boolean {
        return hasAnnotationSimple("Id") || name.equals("id", ignoreCase = true)
    }

    private fun LsiField.isAutoIncrement(): Boolean {
        if (!hasAnnotationSimple("GeneratedValue")) {
            return false
        }
        val generatorType = annotationValue("GeneratedValue", "generatorType")
        if (!generatorType.isNullOrBlank()) {
            return false
        }
        val strategy = annotationValue("GeneratedValue", "strategy")
        return strategy.isNullOrBlank() || strategy.contains("IDENTITY", ignoreCase = true) || strategy.contains("AUTO", ignoreCase = true)
    }

    private fun LsiField.sequenceName(): String? {
        val strategy = annotationValue("GeneratedValue", "strategy")
        if (strategy?.contains("SEQUENCE", ignoreCase = true) != true) {
            return null
        }
        return annotationValue("GeneratedValue", "generatorName")
    }

    private fun LsiField.length(): Int? {
        return annotationValue("Length", "value")?.toIntOrNull()
            ?: annotationValue("Length", "max")?.toIntOrNull()
            ?: annotationValue("Column", "length")?.toIntOrNull()
    }

    private fun LsiField.precision(): Int? {
        return annotationValue("Column", "precision")?.toIntOrNull()
            ?: annotationValue("Precision", "value")?.toIntOrNull()
    }

    private fun LsiField.scale(): Int? {
        return annotationValue("Column", "scale")?.toIntOrNull()
            ?: annotationValue("Scale", "value")?.toIntOrNull()
    }

    private fun LsiField.isTextType(): Boolean {
        val explicitLength = length()
        if (explicitLength != null && explicitLength > 1000) {
            return true
        }
        return hasAnnotationSimple("Lob") ||
            annotationValue("Column", "columnDefinition")?.contains("TEXT", ignoreCase = true) == true ||
            annotationValue("Column", "columnDefinition")?.contains("CLOB", ignoreCase = true) == true
    }

    private fun LsiField.isUniqueField(): Boolean {
        return hasAnnotationSimple("Unique") ||
            annotation("Column")?.getAttribute("unique")?.toString()?.toBooleanStrictOrNull() == true
    }

    private fun LsiField.joinColumnName(): String? {
        return annotationValue("JoinColumn", "name")
    }

    private fun LsiField.referencedColumnName(): String? {
        return annotationValue("JoinColumn", "referencedColumnName")
    }

    private fun LsiField.hasAnnotationSimple(vararg simpleNames: String): Boolean {
        return annotations.any { annotation ->
            simpleNames.any { annotation.simpleName.equals(it, ignoreCase = true) }
        }
    }

    private fun LsiField.annotation(simpleName: String): LsiAnnotation? {
        return annotations.firstOrNull { it.simpleName.equals(simpleName, ignoreCase = true) }
    }

    private fun LsiField.annotationValue(simpleName: String, attributeName: String): String? {
        return annotation(simpleName)?.getAttribute(attributeName)?.toString()
    }

    private val ENTITY_ANNOTATIONS = setOf(
        "org.babyfish.jimmer.sql.Entity",
        "jakarta.persistence.Entity",
        "javax.persistence.Entity",
    )
}
