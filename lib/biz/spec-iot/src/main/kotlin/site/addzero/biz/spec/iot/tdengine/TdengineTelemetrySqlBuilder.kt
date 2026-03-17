package site.addzero.biz.spec.iot.tdengine

import site.addzero.biz.spec.iot.IotPropertySpec
import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.TelemetryReport
import site.addzero.biz.spec.iot.TelemetryValue
import site.addzero.biz.spec.iot.spi.IotPropertySpecProviders
import site.addzero.biz.spec.iot.spi.TdengineTypeMappings
import site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategies
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.Locale

/**
 * Builds TDengine DDL/DML/query SQL without binding to JDBC or MyBatis.
 */
class TdengineTelemetrySqlBuilder(
    private val schemaPlanner: TdSchemaPlanner = TdSchemaPlanner(),
) {

    fun buildDesiredColumns(schemaRef: IotThingRef): List<TdColumnSpec> {
        val specs = IotPropertySpecProviders.load(schemaRef).getPropertySpecs(schemaRef)
        val columns = mutableListOf<TdColumnSpec>()
        val identifiers = linkedSetOf<String>()
        specs.forEach { spec ->
            val normalizedIdentifier = normalizeIdentifier(spec.identifier)
            if (!identifiers.add(normalizedIdentifier)) {
                throw IllegalStateException("Duplicate property identifier detected: $normalizedIdentifier")
            }
            columns += TdengineTypeMappings.toColumnSpec(spec)
        }
        return columns.toList()
    }

    fun planSchemaDiff(schemaRef: IotThingRef, existingColumns: List<TdColumnSpec>): TdSchemaDiff {
        return schemaPlanner.plan(existingColumns, buildDesiredColumns(schemaRef))
    }

    fun buildCreateStable(schemaRef: IotThingRef): SqlStatement {
        return buildCreateStable(schemaRef, buildDesiredColumns(schemaRef))
    }

    fun buildCreateStable(schemaRef: IotThingRef, columns: List<TdColumnSpec>): SqlStatement {
        val namingStrategy = TelemetryTableNamingStrategies.load(schemaRef)
        val sql = StringBuilder()
        sql.append("CREATE STABLE ")
            .append(namingStrategy.stableTableName(schemaRef))
            .append(" (")
            .append(TdengineSchemaDefaults.tsColumn().field)
            .append(" ")
            .append(TdengineSchemaDefaults.tsColumn().type)
            .append(", ")
            .append(TdengineSchemaDefaults.reportTimeColumn().field)
            .append(" ")
            .append(TdengineSchemaDefaults.reportTimeColumn().type)

        columns.forEach { column ->
            sql.append(", ").append(renderColumnDefinition(column))
        }

        sql.append(") TAGS (")
            .append(renderColumnDefinition(TdengineSchemaDefaults.deviceIdTagColumn()))
            .append(")")
        return SqlStatement(sql.toString(), emptyList())
    }

    fun buildAlterStatements(schemaRef: IotThingRef, diff: TdSchemaDiff): List<SqlStatement> {
        val namingStrategy = TelemetryTableNamingStrategies.load(schemaRef)
        val stableTable = namingStrategy.stableTableName(schemaRef)
        val statements = mutableListOf<SqlStatement>()

        diff.addedColumns.forEach { column ->
            statements += SqlStatement(
                "ALTER STABLE $stableTable ADD COLUMN ${renderColumnDefinition(column)}",
                emptyList(),
            )
        }
        diff.droppedColumns.forEach { column ->
            statements += SqlStatement(
                "ALTER STABLE $stableTable DROP COLUMN ${column.field}",
                emptyList(),
            )
        }
        diff.modifiedColumns.forEach { column ->
            statements += SqlStatement(
                "ALTER STABLE $stableTable MODIFY COLUMN ${renderColumnDefinition(column)}",
                emptyList(),
            )
        }
        diff.recreatedColumns.forEach { column ->
            statements += SqlStatement(
                "ALTER STABLE $stableTable DROP COLUMN ${column.field}",
                emptyList(),
            )
            statements += SqlStatement(
                "ALTER STABLE $stableTable ADD COLUMN ${renderColumnDefinition(column)}",
                emptyList(),
            )
        }

        return statements.toList()
    }

    fun buildSchemaMigration(schemaRef: IotThingRef, existingColumns: List<TdColumnSpec>): List<SqlStatement> {
        val desiredColumns = buildDesiredColumns(schemaRef)
        if (desiredColumns.isEmpty() && stripReserved(existingColumns).isEmpty()) {
            return emptyList()
        }
        if (stripReserved(existingColumns).isEmpty()) {
            return listOf(buildCreateStable(schemaRef, desiredColumns))
        }
        return buildAlterStatements(schemaRef, schemaPlanner.plan(existingColumns, desiredColumns))
    }

    fun buildInsert(report: TelemetryReport): SqlStatement {
        val namingStrategy = TelemetryTableNamingStrategies.load(report.schemaRef)
        val subTable = namingStrategy.subTableName(report.schemaRef, report.sourceRef)
        val stableTable = namingStrategy.stableTableName(report.schemaRef)

        val sql = StringBuilder()
        val parameters = mutableListOf<Any?>()
        sql.append("INSERT INTO ")
            .append(subTable)
            .append(" USING ")
            .append(stableTable)
            .append(" TAGS (?) (")
            .append(TdengineSchemaDefaults.tsColumn().field)
            .append(", ")
            .append(TdengineSchemaDefaults.reportTimeColumn().field)
        parameters += report.sourceRef.id

        val legalValues = filterKnownValues(report)
        if (legalValues.isEmpty()) {
            throw IllegalArgumentException("Telemetry report contains no known property values")
        }

        legalValues.keys.forEach { identifier ->
            sql.append(", ").append(normalizeIdentifier(identifier))
        }
        sql.append(") VALUES (NOW, ?")
        parameters += toEpochMillis(report.reportTime)

        legalValues.values.forEach { value ->
            sql.append(", ?")
            parameters += value.value
        }
        sql.append(")")

        return SqlStatement(sql.toString(), parameters)
    }

    fun buildHistoryQuery(query: TelemetryHistoryQuery): SqlStatement {
        val namingStrategy = TelemetryTableNamingStrategies.load(query.schemaRef)
        val identifier = normalizeIdentifier(query.identifier)
        val tableName = namingStrategy.subTableName(query.schemaRef, query.sourceRef)
        val sql = "SELECT $identifier AS value, " +
            "${TdengineSchemaDefaults.tsColumn().field} AS update_time " +
            "FROM $tableName " +
            "WHERE $identifier IS NOT NULL " +
            "AND ${TdengineSchemaDefaults.tsColumn().field} BETWEEN ? AND ? " +
            "ORDER BY ${TdengineSchemaDefaults.tsColumn().field} DESC"
        return SqlStatement(
            sql,
            listOf(
                toEpochMillis(query.fromTime),
                toEpochMillis(query.toTime),
            ),
        )
    }

    fun buildLatestQuery(schemaRef: IotThingRef, identifier: String?): SqlStatement {
        val namingStrategy = TelemetryTableNamingStrategies.load(schemaRef)
        val normalizedIdentifier = normalizeIdentifier(identifier)
        val sql = "SELECT " +
            TdengineSchemaDefaults.deviceIdTagColumn().field +
            ", " +
            TdengineSchemaDefaults.tsColumn().field +
            " AS update_time, LAST(" +
            normalizedIdentifier +
            ") AS value FROM " +
            namingStrategy.stableTableName(schemaRef) +
            " GROUP BY " +
            TdengineSchemaDefaults.deviceIdTagColumn().field
        return SqlStatement(sql, emptyList())
    }

    private fun filterKnownValues(report: TelemetryReport): Map<String, TelemetryValue> {
        val legalValues = LinkedHashMap<String, TelemetryValue>()
        val propertySpecs = IotPropertySpecProviders.load(report.schemaRef).getPropertySpecs(report.schemaRef)
        val legalIdentifiers = LinkedHashSet<String>()
        propertySpecs.forEach { spec ->
            legalIdentifiers += normalizeIdentifier(spec.identifier)
        }
        report.values.forEach { (identifier, value) ->
            val normalizedIdentifier = normalizeIdentifier(identifier)
            if (legalIdentifiers.contains(normalizedIdentifier)) {
                legalValues[normalizedIdentifier] = value
            }
        }
        return legalValues
    }

    private fun stripReserved(columns: List<TdColumnSpec>): List<TdColumnSpec> {
        return columns.filterNot { TdengineSchemaDefaults.getReservedFieldNames().contains(it.field) }
    }

    private fun renderColumnDefinition(column: TdColumnSpec): String {
        val builder = StringBuilder()
        builder.append(column.field).append(" ").append(column.type)
        if (column.length != null && column.length > 0) {
            builder.append("(").append(column.length).append(")")
        }
        return builder.toString()
    }

    private fun normalizeIdentifier(identifier: String?): String {
        val cleanIdentifier = identifier?.trim()
        require(!cleanIdentifier.isNullOrEmpty()) { "identifier must not be blank" }
        val lower = cleanIdentifier.lowercase(Locale.ROOT)
        val builder = StringBuilder()
        lower.forEach { current ->
            if ((current in 'a'..'z') || (current in '0'..'9') || current == '_') {
                builder.append(current)
            } else {
                builder.append('_')
            }
        }
        if (builder.isEmpty() || builder[0].isDigit()) {
            throw IllegalArgumentException(
                "identifier must start with a letter or underscore after normalization: $identifier",
            )
        }
        return builder.toString()
    }

    private fun toEpochMillis(time: LocalDateTime): Long {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
