package site.addzero.common.util.data_structure.spo.util

import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class SpoFileReaderTest {

    @Test
    fun `readTab 解析表头并过滤空行`() {
        val tempFile = Files.createTempFile("spo-tab", ".txt")
        tempFile.writeText(
            """
            subject	predicate	object
            飞机1	宽	175

            坦克	高	123
            """.trimIndent()
        )

        val rows = SpoFileReader.readTab(tempFile.toString())

        assertEquals(
            listOf(
                mapOf("subject" to "飞机1", "predicate" to "宽", "object" to "175"),
                mapOf("subject" to "坦克", "predicate" to "高", "object" to "123")
            ),
            rows
        )

        tempFile.deleteIfExists()
    }
}
