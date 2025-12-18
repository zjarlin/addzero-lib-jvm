package site.addzero.ioc.test

import org.junit.Test
import site.addzero.ioc.generated.AutoBeanRegistry
import kotlin.test.assertNotNull

class GeneratedRegistryTest {

    @Test
    fun testGeneratedRegistry() {
        // 确保生成的注册表存在
        assertNotNull(AutoBeanRegistry)

        // 测试注册的组件
        testGeneratedRegistry(AutoBeanRegistry)

        // 测试组件名称
        val componentNames = AutoBeanRegistry.getComponentNames()
        assert(componentNames.contains("testService")) { "Should contain testService" }
        assert(componentNames.contains("customService")) { "Should contain customService" }

        // 测试根据名称获取类型
        val testServiceType = AutoBeanRegistry.getComponentType("testService")
        assert(testServiceType == TestService::class) { "Should return TestService class" }

        println("Generated registry test passed!")
    }
}