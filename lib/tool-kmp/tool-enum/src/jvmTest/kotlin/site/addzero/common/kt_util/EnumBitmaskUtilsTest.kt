package site.addzero.common.kt_util

import kotlin.test.Test
import kotlin.test.assertEquals

class EnumBitmaskUtilsTest {

    @Test
    fun `toBitmask 将枚举集合转换为二进制掩码`() {
        val mask = listOf(EnumBitmaskSample.READ, EnumBitmaskSample.EXECUTE).toBitmask()

        assertEquals(5, mask)
    }

    @Test
    fun `toEnumList 将二进制掩码还原为枚举集合`() {
        val mask = listOf(EnumBitmaskSample.READ, EnumBitmaskSample.EXECUTE).toBitmask()

        val byReified = EnumBitmaskUtils.run {
            mask.toEnumList<EnumBitmaskSample>()
        }
        val byKClass = EnumBitmaskUtils.run {
            mask.toEnumList(EnumBitmaskSample::class)
        }
        val byClass = EnumBitmaskUtils.run {
            mask.toEnumList(EnumBitmaskSample::class.java)
        }

        val expected = listOf(EnumBitmaskSample.READ, EnumBitmaskSample.EXECUTE)
        assertEquals(expected, byReified)
        assertEquals(expected, byKClass)
        assertEquals(expected, byClass)
    }
}

private enum class EnumBitmaskSample {
    READ,
    WRITE,
    EXECUTE,
}
