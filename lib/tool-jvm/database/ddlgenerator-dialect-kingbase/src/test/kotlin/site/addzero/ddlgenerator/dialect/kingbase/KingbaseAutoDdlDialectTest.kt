package site.addzero.ddlgenerator.dialect.kingbase

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.ddlgenerator.core.diff.CreateSequence
import site.addzero.ddlgenerator.core.model.AutoDdlSequence

class KingbaseAutoDdlDialectTest {

    @Test
    fun `inherits postgres style sequence`() {
        val dialect = KingbaseAutoDdlDialect()
        val statements = dialect.render(listOf(CreateSequence(AutoDdlSequence("seq_book"))))
        assertEquals(listOf("""CREATE SEQUENCE IF NOT EXISTS "seq_book" START WITH 1 INCREMENT BY 1;"""), statements)
    }
}
