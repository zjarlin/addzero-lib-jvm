package site.addzero.biz.spec.iot.tdengine

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TdSchemaPlannerTest {

    @Test
    fun shouldPlanAddDropModifyAndRecreateColumns() {
        val planner = TdSchemaPlanner()
        val diff = planner.plan(
            listOf(
                TdengineSchemaDefaults.tsColumn(),
                TdengineSchemaDefaults.reportTimeColumn(),
                TdengineSchemaDefaults.deviceIdTagColumn(),
                TdColumnSpec("temperature", TdColumnSpec.TYPE_INT, null, null),
                TdColumnSpec("legacy", TdColumnSpec.TYPE_FLOAT, null, null),
                TdColumnSpec("name", TdColumnSpec.TYPE_VARCHAR, 64, null),
                TdColumnSpec("mode", TdColumnSpec.TYPE_VARCHAR, 32, null),
            ),
            listOf(
                TdColumnSpec("temperature", TdColumnSpec.TYPE_FLOAT, null, null),
                TdColumnSpec("status", TdColumnSpec.TYPE_BOOL, null, null),
                TdColumnSpec("name", TdColumnSpec.TYPE_VARCHAR, 128, null),
                TdColumnSpec("mode", TdColumnSpec.TYPE_VARCHAR, 16, null),
            ),
        )

        assertEquals(
            listOf(TdColumnSpec("status", TdColumnSpec.TYPE_BOOL, null, null)),
            diff.addedColumns,
        )
        assertEquals(
            listOf(TdColumnSpec("legacy", TdColumnSpec.TYPE_FLOAT, null, null)),
            diff.droppedColumns,
        )
        assertEquals(
            listOf(TdColumnSpec("name", TdColumnSpec.TYPE_VARCHAR, 128, null)),
            diff.modifiedColumns,
        )
        assertEquals(
            listOf(
                TdColumnSpec("temperature", TdColumnSpec.TYPE_FLOAT, null, null),
                TdColumnSpec("mode", TdColumnSpec.TYPE_VARCHAR, 16, null),
            ),
            diff.recreatedColumns,
        )
    }

    @Test
    fun shouldReturnEmptyDiffForEmptySchema() {
        val planner = TdSchemaPlanner()
        val diff = planner.plan(emptyList(), emptyList())
        assertTrue(diff.isEmpty())
    }
}
