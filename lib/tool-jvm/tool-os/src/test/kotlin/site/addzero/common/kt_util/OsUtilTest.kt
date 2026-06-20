package site.addzero.common.kt_util

import kotlin.test.Test
import kotlin.test.assertTrue

class OsUtilTest {

    @Test
    fun `getPlatformType 返回当前平台`() {
        val platformType = OsUtil.getPlatformType()

        assertTrue(platformType in PlatformType.entries)
    }
}
