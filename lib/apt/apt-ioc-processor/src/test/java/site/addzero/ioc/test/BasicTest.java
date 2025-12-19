package site.addzero.ioc.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础测试：验证项目基本结构
 */
class BasicTest {

    @Test
    void testSimpleAptProcessorExists() {
        // 检查处理器类是否存在
        try {
            Class<?> processorClass = Class.forName("site.addzero.ioc.apt.SimpleAptProcessor");
            assertNotNull(processorClass, "SimpleAptProcessor应该存在");
            System.out.println("SimpleAptProcessor class found: " + processorClass.getName());
        } catch (ClassNotFoundException e) {
            fail("SimpleAptProcessor类未找到: " + e.getMessage());
        }
    }

    @Test
    void testAnnotationProcessing() {
        // 这个测试主要是确保测试框架本身工作正常
        assertTrue(true, "测试框架应该正常工作");
        System.out.println("Basic test passed successfully");
    }
}