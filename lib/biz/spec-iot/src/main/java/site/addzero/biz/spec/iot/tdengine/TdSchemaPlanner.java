package site.addzero.biz.spec.iot.tdengine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TDengine schema diff planner extracted from MyBatis mapper logic.
 */
public final class TdSchemaPlanner {

    private final Set<String> reservedFieldNames;

    public TdSchemaPlanner() {
        this(TdengineSchemaDefaults.getReservedFieldNames());
    }

    public TdSchemaPlanner(Set<String> reservedFieldNames) {
        this.reservedFieldNames = new LinkedHashSet<String>(reservedFieldNames);
    }

    public TdSchemaDiff plan(List<TdColumnSpec> existingColumns, List<TdColumnSpec> desiredColumns) {
        Map<String, TdColumnSpec> currentByField = indexByField(filterReserved(existingColumns));
        Map<String, TdColumnSpec> desiredByField = indexByField(desiredColumns);

        List<TdColumnSpec> addedColumns = new ArrayList<TdColumnSpec>();
        List<TdColumnSpec> droppedColumns = new ArrayList<TdColumnSpec>();
        List<TdColumnSpec> modifiedColumns = new ArrayList<TdColumnSpec>();
        List<TdColumnSpec> recreatedColumns = new ArrayList<TdColumnSpec>();

        for (TdColumnSpec desired : desiredColumns) {
            TdColumnSpec existing = currentByField.get(desired.getField());
            if (existing == null) {
                addedColumns.add(desired);
                continue;
            }
            if (!existing.getType().equals(desired.getType())) {
                recreatedColumns.add(desired);
                continue;
            }
            if (desired.getLength() != null) {
                Integer existingLength = existing.getLength();
                if (existingLength == null || desired.getLength().intValue() > existingLength.intValue()) {
                    modifiedColumns.add(desired);
                } else if (desired.getLength().intValue() < existingLength.intValue()) {
                    recreatedColumns.add(desired);
                }
            }
        }

        for (TdColumnSpec existing : currentByField.values()) {
            if (!desiredByField.containsKey(existing.getField())) {
                droppedColumns.add(existing);
            }
        }

        return new TdSchemaDiff(addedColumns, droppedColumns, modifiedColumns, recreatedColumns);
    }

    private List<TdColumnSpec> filterReserved(List<TdColumnSpec> columns) {
        List<TdColumnSpec> filtered = new ArrayList<TdColumnSpec>();
        for (TdColumnSpec column : columns) {
            if (!reservedFieldNames.contains(column.getField())) {
                filtered.add(column);
            }
        }
        return filtered;
    }

    private Map<String, TdColumnSpec> indexByField(List<TdColumnSpec> columns) {
        Map<String, TdColumnSpec> indexed = new LinkedHashMap<String, TdColumnSpec>();
        for (TdColumnSpec column : columns) {
            indexed.put(column.getField(), column);
        }
        return indexed;
    }
}
