package site.addzero.ddlgenerator.dialect.oracle

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateSequence
import site.addzero.ddlgenerator.core.model.AutoDdlSequence

class OracleAutoDdlDialectTest {

    @Test
    fun `renders oracle sequence`() {
        val dialect = OracleAutoDdlDialect()
        val statements = dialect.render(listOf(CreateSequence(AutoDdlSequence("book_seq", startWith = 100, incrementBy = 5))))
        assertEquals(listOf("""CREATE SEQUENCE "book_seq" START WITH 100 INCREMENT BY 5;"""), statements)
    }
}
