package site.addzero.device.protocol.modbus.ksp.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ModbusKdocParserTest {
    @Test
    fun parseKeepsSummaryAndParamComments() {
        val doc =
            """
            读取设备信息。

            返回当前板卡的静态元数据。
            @param unitId 当前 Modbus 单元地址。
            """.trimIndent()

        val parsed = ModbusKdocParser.parse(doc, fallbackSummary = "默认摘要。")

        assertEquals("读取设备信息。", parsed.summary)
        assertEquals(listOf("返回当前板卡的静态元数据。"), parsed.descriptionLines)
        assertEquals("当前 Modbus 单元地址。", parsed.parameterDocs["unitId"])
    }
}
