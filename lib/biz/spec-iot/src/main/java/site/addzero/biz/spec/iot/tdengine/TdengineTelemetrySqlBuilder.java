package site.addzero.biz.spec.iot.tdengine;

import site.addzero.biz.spec.iot.IotPropertySpec;
import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.TelemetryReport;
import site.addzero.biz.spec.iot.TelemetryValue;
import site.addzero.biz.spec.iot.spi.IotPropertySpecProvider;
import site.addzero.biz.spec.iot.spi.IotPropertySpecProviders;
import site.addzero.biz.spec.iot.spi.TdengineTypeMappings;
import site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategies;
import site.addzero.biz.spec.iot.spi.TelemetryTableNamingStrategy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds TDengine DDL/DML/query SQL without binding to JDBC or MyBatis.
 */
public final class TdengineTelemetrySqlBuilder {

    private final TdSchemaPlanner schemaPlanner;

    public TdengineTelemetrySqlBuilder() {
        this(new TdSchemaPlanner());
    }

    public TdengineTelemetrySqlBuilder(TdSchemaPlanner schemaPlanner) {
        this.schemaPlanner = schemaPlanner;
    }

    public List<TdColumnSpec> buildDesiredColumns(IotThingRef schemaRef) {
        IotPropertySpecProvider provider = IotPropertySpecProviders.load(schemaRef);
        List<IotPropertySpec> specs = provider.getPropertySpecs(schemaRef);
        List<TdColumnSpec> columns = new ArrayList<TdColumnSpec>();
        Set<String> identifiers = new LinkedHashSet<String>();
        for (IotPropertySpec spec : specs) {
            String normalizedIdentifier = normalizeIdentifier(spec.getIdentifier());
            if (!identifiers.add(normalizedIdentifier)) {
                throw new IllegalStateException("Duplicate property identifier detected: " + normalizedIdentifier);
            }
            columns.add(TdengineTypeMappings.toColumnSpec(spec));
        }
        return Collections.unmodifiableList(columns);
    }

    public TdSchemaDiff planSchemaDiff(IotThingRef schemaRef, List<TdColumnSpec> existingColumns) {
        return schemaPlanner.plan(existingColumns, buildDesiredColumns(schemaRef));
    }

    public SqlStatement buildCreateStable(IotThingRef schemaRef) {
        return buildCreateStable(schemaRef, buildDesiredColumns(schemaRef));
    }

    public SqlStatement buildCreateStable(IotThingRef schemaRef, List<TdColumnSpec> columns) {
        TelemetryTableNamingStrategy namingStrategy = TelemetryTableNamingStrategies.load(schemaRef);
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE STABLE ").append(namingStrategy.stableTableName(schemaRef)).append(" (");
        sql.append(TdengineSchemaDefaults.tsColumn().getField()).append(" ").append(TdengineSchemaDefaults.tsColumn().getType());
        sql.append(", ").append(TdengineSchemaDefaults.reportTimeColumn().getField()).append(" ").append(TdengineSchemaDefaults.reportTimeColumn().getType());
        for (TdColumnSpec column : columns) {
            sql.append(", ").append(renderColumnDefinition(column));
        }
        sql.append(") TAGS (").append(renderColumnDefinition(TdengineSchemaDefaults.deviceIdTagColumn())).append(")");
        return new SqlStatement(sql.toString(), Collections.<Object>emptyList());
    }

    public List<SqlStatement> buildAlterStatements(IotThingRef schemaRef, TdSchemaDiff diff) {
        TelemetryTableNamingStrategy namingStrategy = TelemetryTableNamingStrategies.load(schemaRef);
        String stableTable = namingStrategy.stableTableName(schemaRef);
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        for (TdColumnSpec column : diff.getAddedColumns()) {
            statements.add(new SqlStatement(
                    "ALTER STABLE " + stableTable + " ADD COLUMN " + renderColumnDefinition(column),
                    Collections.<Object>emptyList()
            ));
        }
        for (TdColumnSpec column : diff.getDroppedColumns()) {
            statements.add(new SqlStatement(
                    "ALTER STABLE " + stableTable + " DROP COLUMN " + column.getField(),
                    Collections.<Object>emptyList()
            ));
        }
        for (TdColumnSpec column : diff.getModifiedColumns()) {
            statements.add(new SqlStatement(
                    "ALTER STABLE " + stableTable + " MODIFY COLUMN " + renderColumnDefinition(column),
                    Collections.<Object>emptyList()
            ));
        }
        for (TdColumnSpec column : diff.getRecreatedColumns()) {
            statements.add(new SqlStatement(
                    "ALTER STABLE " + stableTable + " DROP COLUMN " + column.getField(),
                    Collections.<Object>emptyList()
            ));
            statements.add(new SqlStatement(
                    "ALTER STABLE " + stableTable + " ADD COLUMN " + renderColumnDefinition(column),
                    Collections.<Object>emptyList()
            ));
        }
        return Collections.unmodifiableList(statements);
    }

    public List<SqlStatement> buildSchemaMigration(IotThingRef schemaRef, List<TdColumnSpec> existingColumns) {
        List<TdColumnSpec> desiredColumns = buildDesiredColumns(schemaRef);
        if (desiredColumns.isEmpty() && stripReserved(existingColumns).isEmpty()) {
            return Collections.emptyList();
        }
        if (stripReserved(existingColumns).isEmpty()) {
            return Collections.singletonList(buildCreateStable(schemaRef, desiredColumns));
        }
        return buildAlterStatements(schemaRef, schemaPlanner.plan(existingColumns, desiredColumns));
    }

    public SqlStatement buildInsert(TelemetryReport report) {
        TelemetryTableNamingStrategy namingStrategy = TelemetryTableNamingStrategies.load(report.getSchemaRef());
        String subTable = namingStrategy.subTableName(report.getSchemaRef(), report.getSourceRef());
        String stableTable = namingStrategy.stableTableName(report.getSchemaRef());

        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<Object>();
        sql.append("INSERT INTO ").append(subTable)
                .append(" USING ").append(stableTable)
                .append(" TAGS (?) (")
                .append(TdengineSchemaDefaults.tsColumn().getField())
                .append(", ")
                .append(TdengineSchemaDefaults.reportTimeColumn().getField());
        parameters.add(report.getSourceRef().getId());

        Map<String, TelemetryValue> legalValues = filterKnownValues(report);
        if (legalValues.isEmpty()) {
            throw new IllegalArgumentException("Telemetry report contains no known property values");
        }

        for (String identifier : legalValues.keySet()) {
            sql.append(", ").append(normalizeIdentifier(identifier));
        }
        sql.append(") VALUES (NOW, ?");
        parameters.add(toEpochMillis(report.getReportTime()));

        for (TelemetryValue value : legalValues.values()) {
            sql.append(", ?");
            parameters.add(value.getValue());
        }
        sql.append(")");

        return new SqlStatement(sql.toString(), parameters);
    }

    public SqlStatement buildHistoryQuery(TelemetryHistoryQuery query) {
        TelemetryTableNamingStrategy namingStrategy = TelemetryTableNamingStrategies.load(query.getSchemaRef());
        String identifier = normalizeIdentifier(query.getIdentifier());
        String tableName = namingStrategy.subTableName(query.getSchemaRef(), query.getSourceRef());
        String sql = "SELECT " + identifier + " AS value, "
                + TdengineSchemaDefaults.tsColumn().getField() + " AS update_time "
                + "FROM " + tableName + " "
                + "WHERE " + identifier + " IS NOT NULL "
                + "AND " + TdengineSchemaDefaults.tsColumn().getField() + " BETWEEN ? AND ? "
                + "ORDER BY " + TdengineSchemaDefaults.tsColumn().getField() + " DESC";
        List<Object> parameters = new ArrayList<Object>();
        parameters.add(toEpochMillis(query.getFromTime()));
        parameters.add(toEpochMillis(query.getToTime()));
        return new SqlStatement(sql, parameters);
    }

    public SqlStatement buildLatestQuery(IotThingRef schemaRef, String identifier) {
        TelemetryTableNamingStrategy namingStrategy = TelemetryTableNamingStrategies.load(schemaRef);
        String normalizedIdentifier = normalizeIdentifier(identifier);
        String sql = "SELECT "
                + TdengineSchemaDefaults.deviceIdTagColumn().getField()
                + ", "
                + TdengineSchemaDefaults.tsColumn().getField()
                + " AS update_time, LAST("
                + normalizedIdentifier
                + ") AS value "
                + "FROM "
                + namingStrategy.stableTableName(schemaRef)
                + " GROUP BY "
                + TdengineSchemaDefaults.deviceIdTagColumn().getField();
        return new SqlStatement(sql, Collections.<Object>emptyList());
    }

    private Map<String, TelemetryValue> filterKnownValues(TelemetryReport report) {
        Map<String, TelemetryValue> legalValues = new LinkedHashMap<String, TelemetryValue>();
        List<IotPropertySpec> propertySpecs = IotPropertySpecProviders.load(report.getSchemaRef()).getPropertySpecs(report.getSchemaRef());
        Set<String> legalIdentifiers = new LinkedHashSet<String>();
        for (IotPropertySpec spec : propertySpecs) {
            legalIdentifiers.add(normalizeIdentifier(spec.getIdentifier()));
        }
        for (Map.Entry<String, TelemetryValue> entry : report.getValues().entrySet()) {
            String identifier = normalizeIdentifier(entry.getKey());
            if (legalIdentifiers.contains(identifier)) {
                legalValues.put(identifier, entry.getValue());
            }
        }
        return legalValues;
    }

    private List<TdColumnSpec> stripReserved(List<TdColumnSpec> columns) {
        List<TdColumnSpec> filtered = new ArrayList<TdColumnSpec>();
        for (TdColumnSpec column : columns) {
            if (!TdengineSchemaDefaults.getReservedFieldNames().contains(column.getField())) {
                filtered.add(column);
            }
        }
        return filtered;
    }

    private String renderColumnDefinition(TdColumnSpec column) {
        StringBuilder builder = new StringBuilder();
        builder.append(column.getField()).append(" ").append(column.getType());
        if (column.getLength() != null && column.getLength().intValue() > 0) {
            builder.append("(").append(column.getLength()).append(")");
        }
        return builder.toString();
    }

    private String normalizeIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier must not be blank");
        }
        String lower = identifier.trim().toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lower.length(); i++) {
            char current = lower.charAt(i);
            if ((current >= 'a' && current <= 'z') || (current >= '0' && current <= '9') || current == '_') {
                builder.append(current);
            } else {
                builder.append('_');
            }
        }
        if (builder.length() == 0 || Character.isDigit(builder.charAt(0))) {
            throw new IllegalArgumentException("identifier must start with a letter or underscore after normalization: " + identifier);
        }
        return builder.toString();
    }

    private long toEpochMillis(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
