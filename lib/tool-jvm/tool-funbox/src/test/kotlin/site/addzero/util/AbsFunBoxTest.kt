package site.addzero.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class AbsFunBoxTest {

    @Test
    fun `test AbsFunBox is object`() {
        // 验证AbsFunBox是一个单例对象
        assertNotNull(AbsFunBox)
    }

    @Test
    fun `test get all fun with null package name`() {
        // 测试使用null包名调用getAllFun方法
        val result = AbsFunBox.getAllFun(null)
        assertNotNull(result)
        assertTrue(result is List<*>)
    }
}