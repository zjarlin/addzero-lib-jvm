package site.addzero.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

/**
 * 改进版反射工具类测试
 */
class ImprovedReflectUtilTest {
    
    class TestClass(val name: String, val age: Int) {
        fun greet(): String = "Hello, I'm $name, $age years old"
    }
    
    @Test
    fun testGetConstructor() {
        val constructor = ImprovedReflectUtil.getConstructor(TestClass::class.java, String::class.java, Int::class.java)
        assertNotNull(constructor)
        
        val instance = constructor?.newInstance("Alice", 25)
        assertNotNull(instance)
        assertEquals("Alice", instance?.name)
        assertEquals(25, instance?.age)
    }
    
    @Test
    fun testGetConstructors() {
        val constructors = ImprovedReflectUtil.getConstructors(TestClass::class.java)
        assertTrue(constructors.isNotEmpty())
        assertEquals(1, constructors.size)
    }
    
    @Test
    fun testGetFields() {
        val fields = ImprovedReflectUtil.getFields(TestClass::class.java)
        assertEquals(2, fields.size)
        assertTrue(fields.any { it.name == "name" })
        assertTrue(fields.any { it.name == "age" })
    }
    
    @Test
    fun testGetMethods() {
        val methods = ImprovedReflectUtil.getMethods(TestClass::class.java)
        assertTrue(methods.isNotEmpty())
        assertTrue(methods.any { it.name == "greet" })
    }
    
    @Test
    fun testClearAllCaches() {
        // 先填充一些缓存
        ImprovedReflectUtil.getConstructors(TestClass::class.java)
        ImprovedReflectUtil.getFields(TestClass::class.java)
        ImprovedReflectUtil.getMethods(TestClass::class.java)
        
        // 清理缓存
        ImprovedReflectUtil.clearAllCaches()
        
        // 再次获取应该也能正常工作
        val constructors = ImprovedReflectUtil.getConstructors(TestClass::class.java)
        assertTrue(constructors.isNotEmpty())
    }
    
    @Test
    fun testCleanupExpiredEntries() {
        // 先填充一些缓存
        ImprovedReflectUtil.getConstructors(TestClass::class.java)
        ImprovedReflectUtil.getFields(TestClass::class.java)
        ImprovedReflectUtil.getMethods(TestClass::class.java)
        
        // 清理过期条目
        ImprovedReflectUtil.cleanupExpiredEntries()
        
        // 再次获取应该也能正常工作
        val fields = ImprovedReflectUtil.getFields(TestClass::class.java)
        assertTrue(fields.isNotEmpty())
    }
}