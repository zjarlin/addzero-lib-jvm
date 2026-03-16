package site.addzero.biz.spec.iot.tdengine;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TdSchemaPlannerTest {

    @Test
    void shouldPlanAddDropModifyAndRecreateColumns() {
        TdSchemaPlanner planner = new TdSchemaPlanner();
        TdSchemaDiff diff = planner.plan(
                Arrays.asList(
                        TdengineSchemaDefaults.tsColumn(),
                        TdengineSchemaDefaults.reportTimeColumn(),
                        TdengineSchemaDefaults.deviceIdTagColumn(),
                        new TdColumnSpec("temperature", TdColumnSpec.TYPE_INT, null, null),
                        new TdColumnSpec("legacy", TdColumnSpec.TYPE_FLOAT, null, null),
                        new TdColumnSpec("name", TdColumnSpec.TYPE_VARCHAR, 64, null),
                        new TdColumnSpec("mode", TdColumnSpec.TYPE_VARCHAR, 32, null)
                ),
                Arrays.asList(
                        new TdColumnSpec("temperature", TdColumnSpec.TYPE_FLOAT, null, null),
                        new TdColumnSpec("status", TdColumnSpec.TYPE_BOOL, null, null),
                        new TdColumnSpec("name", TdColumnSpec.TYPE_VARCHAR, 128, null),
                        new TdColumnSpec("mode", TdColumnSpec.TYPE_VARCHAR, 16, null)
                )
        );

        assertEquals(Collections.singletonList(new TdColumnSpec("status", TdColumnSpec.TYPE_BOOL, null, null)), diff.getAddedColumns());
        assertEquals(Collections.singletonList(new TdColumnSpec("legacy", TdColumnSpec.TYPE_FLOAT, null, null)), diff.getDroppedColumns());
        assertEquals(Collections.singletonList(new TdColumnSpec("name", TdColumnSpec.TYPE_VARCHAR, 128, null)), diff.getModifiedColumns());
        assertEquals(
                Arrays.asList(
                        new TdColumnSpec("temperature", TdColumnSpec.TYPE_FLOAT, null, null),
                        new TdColumnSpec("mode", TdColumnSpec.TYPE_VARCHAR, 16, null)
                ),
                diff.getRecreatedColumns()
        );
    }

    @Test
    void shouldReturnEmptyDiffForEmptySchema() {
        TdSchemaPlanner planner = new TdSchemaPlanner();
        TdSchemaDiff diff = planner.plan(Collections.<TdColumnSpec>emptyList(), Collections.<TdColumnSpec>emptyList());
        assertTrue(diff.isEmpty());
    }
}
