package site.addzero.common.kt_util

import kotlin.test.Test
import kotlin.test.assertEquals

class CmdUtilTest {

    @Test
    fun `runCmd 执行当前系统命令`() {
        val output = CmdUtil.runCmd("printf cmd-ok").trim()

        assertEquals("cmd-ok", output)
    }
}
