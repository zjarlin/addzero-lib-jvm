package site.addzero.biz.spec.iot.tdengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of TDengine schema comparison.
 */
public final class TdSchemaDiff {

    private final List<TdColumnSpec> addedColumns;
    private final List<TdColumnSpec> droppedColumns;
    private final List<TdColumnSpec> modifiedColumns;
    private final List<TdColumnSpec> recreatedColumns;

    public TdSchemaDiff(
            List<TdColumnSpec> addedColumns,
            List<TdColumnSpec> droppedColumns,
            List<TdColumnSpec> modifiedColumns,
            List<TdColumnSpec> recreatedColumns
    ) {
        this.addedColumns = unmodifiableCopy(addedColumns);
        this.droppedColumns = unmodifiableCopy(droppedColumns);
        this.modifiedColumns = unmodifiableCopy(modifiedColumns);
        this.recreatedColumns = unmodifiableCopy(recreatedColumns);
    }

    public List<TdColumnSpec> getAddedColumns() {
        return addedColumns;
    }

    public List<TdColumnSpec> getDroppedColumns() {
        return droppedColumns;
    }

    public List<TdColumnSpec> getModifiedColumns() {
        return modifiedColumns;
    }

    public List<TdColumnSpec> getRecreatedColumns() {
        return recreatedColumns;
    }

    public boolean isEmpty() {
        return addedColumns.isEmpty()
                && droppedColumns.isEmpty()
                && modifiedColumns.isEmpty()
                && recreatedColumns.isEmpty();
    }

    private static List<TdColumnSpec> unmodifiableCopy(List<TdColumnSpec> columns) {
        return Collections.unmodifiableList(new ArrayList<TdColumnSpec>(columns));
    }
}
