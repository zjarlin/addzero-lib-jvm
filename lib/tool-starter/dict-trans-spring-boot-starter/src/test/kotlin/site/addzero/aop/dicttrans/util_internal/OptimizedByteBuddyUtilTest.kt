package site.addzero.aop.dicttrans.util_internal

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import java.math.BigDecimal
import java.util.*

/**
 * 优化字节码工具测试
 */
class OptimizedByteBuddyUtilTest {

    @BeforeEach
    fun setUp() {
        OptimizedByteBuddyUtil.clearCache()
    }

    @Test
    fun `test direct bytecode generation`() {
        // 直接测试字节码生成，绕过RefUtil.isT检查
        val entity = SimpleTestEntity()
        entity.name = "test"
        entity.age = 25
        
        // 创建字段需求
        val fieldRequirements = setOf(
            NeedAddInfo(entity, "nameText", null, null, null, String::class.java),
            NeedAddInfo(entity, "ageText", null, null, null, String::class.java)
        )
        
        // 直接生成增强类
        val originalClass = entity.javaClass
        var subclass = net.bytebuddy.ByteBuddy().subclass(originalClass)
        
        fieldRequirements.forEach { needAddInfo ->
            subclass = subclass.defineProperty(needAddInfo.fieldName, needAddInfo.type)
        }
        
        val enhancedClass = subclass.make().load(originalClass.classLoader).loaded
        val enhancedObj = enhancedClass.newInstance()
        
        // 拷贝属性
        site.addzero.aop.dicttrans.util.BeanUtil.copyProperties(entity, enhancedObj)
        
        // 验证结果
        println("原始对象类型: ${entity.javaClass.name}")
        println("增强对象类型: ${enhancedObj.javaClass.name}")
        println("增强对象字段: ${enhancedObj.javaClass.declaredFields.map { it.name }}")
        
        assertTrue(hasField(enhancedObj, "nameText"), "缺少 nameText 字段")
        assertTrue(hasField(enhancedObj, "ageText"), "缺少 ageText 字段")
        
        // 验证原始字段也存在（通过反射访问，包括继承的字段）
        assertTrue(hasFieldIncludingInherited(enhancedObj, "name"), "缺少原始 name 字段")
        assertTrue(hasFieldIncludingInherited(enhancedObj, "age"), "缺少原始 age 字段")
    }

    @Test
    fun `test batch processing with nested objects`() {
        // 创建测试数据
        val entities = listOf(
            createComplexNestedEntity("entity1"),
            createComplexNestedEntity("entity2"),
            createComplexNestedEntity("entity3")
        )

        // 模拟字段需求函数
        val getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo> = { obj ->
            when (obj.javaClass.simpleName) {
                "ComplexNestedEntity" -> mutableListOf(
                    NeedAddInfo(obj, "genderText", null, null, null, String::class.java),
                    NeedAddInfo(obj, "deviceStatusText", null, null, null, String::class.java),
                    NeedAddInfo(obj, "productNameTest1", null, null, null, String::class.java)
                )
                "DeviceInfo" -> mutableListOf(
                    NeedAddInfo(obj, "devicecodeTest1", null, null, null, String::class.java),
                    NeedAddInfo(obj, "deviceNametest1", null, null, null, String::class.java)
                )
                "Location" -> mutableListOf(
                    NeedAddInfo(obj, "testvar1Text", null, null, null, String::class.java),
                    NeedAddInfo(obj, "testvar2Text", null, null, null, String::class.java),
                    NeedAddInfo(obj, "devicecodeTest1", null, null, null, String::class.java)
                )
                "SensorInfo" -> mutableListOf(
                    NeedAddInfo(obj, "testvar1Text", null, null, null, String::class.java),
                    NeedAddInfo(obj, "testvar2Text", null, null, null, String::class.java),
                    NeedAddInfo(obj, "devicecodeTest1", null, null, null, String::class.java)
                )
                else -> mutableListOf()
            }
        }

        // 执行批量处理
        val startTime = System.currentTimeMillis()
        val processedEntities = OptimizedByteBuddyUtil.genChildObjectsBatch(entities, getNeedAddInfoFun)
        val endTime = System.currentTimeMillis()

        println("批量处理耗时: ${endTime - startTime}ms")
        println("缓存统计: ${OptimizedByteBuddyUtil.getCacheStats()}")

        // 验证结果
        assertEquals(3, processedEntities.size)
        
        processedEntities.forEachIndexed { index, entity ->
            assertNotNull(entity)
            println("Entity $index class: ${entity!!.javaClass.name}")
            println("Entity $index fields: ${entity.javaClass.declaredFields.map { it.name }}")
            
            // 验证增强字段存在
            assertTrue(hasField(entity, "genderText"), "Entity $index missing genderText field")
            assertTrue(hasField(entity, "deviceStatusText"), "Entity $index missing deviceStatusText field")
            assertTrue(hasField(entity, "productNameTest1"), "Entity $index missing productNameTest1 field")
        }
    }

    @Test
    fun `test performance comparison`() {
        val entities = (1..100).map { createComplexNestedEntity("entity$it") }
        
        val getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo> = { obj ->
            TransInternalUtil.getNeedAddFields(obj).toMutableList()
        }

        // 测试原始方法
        val startTime1 = System.currentTimeMillis()
        val originalResults = entities.map { 
            ByteBuddyUtil.genChildObjectRecursion(it, java.util.function.Function { obj ->
                getNeedAddInfoFun(obj)
            })
        }
        val endTime1 = System.currentTimeMillis()
        val originalTime = endTime1 - startTime1

        // 清理缓存
        OptimizedByteBuddyUtil.clearCache()

        // 测试优化方法
        val startTime2 = System.currentTimeMillis()
        val optimizedResults = OptimizedByteBuddyUtil.genChildObjectsBatch(entities, getNeedAddInfoFun)
        val endTime2 = System.currentTimeMillis()
        val optimizedTime = endTime2 - startTime2

        println("原始方法耗时: ${originalTime}ms")
        println("优化方法耗时: ${optimizedTime}ms")
        println("性能提升: ${((originalTime - optimizedTime).toDouble() / originalTime * 100).toInt()}%")
        println("缓存统计: ${OptimizedByteBuddyUtil.getCacheStats()}")

        // 验证结果一致性
        assertEquals(originalResults.size, optimizedResults.size)
    }

    private fun createComplexNestedEntity(prefix: String): ComplexNestedEntity {
        val entity = ComplexNestedEntity()
        entity.gender = "1"
        entity.deviceStatus = "1"
        entity.productKey = "PROD001"
        
        val deviceInfo = DeviceInfo()
        deviceInfo.deviceId = "49"
        deviceInfo.deviceId1 = "55"
        deviceInfo.temperature = BigDecimal("25.5")
        deviceInfo.createTime = Date()
        
        val location = Location()
        location.testvar1 = "1"
        location.testvar2 = "2"
        location.testTableVar = "55"
        deviceInfo.location = location
        
        val sensor1 = SensorInfo()
        sensor1.testvar1 = "1"
        sensor1.testvar2 = "2"
        sensor1.testTableVar = "55"
        
        val sensor2 = SensorInfo()
        sensor2.testvar1 = "2"
        sensor2.testvar2 = "3"
        sensor2.testTableVar = "66"
        
        val sensors = listOf(sensor1, sensor2)
        deviceInfo.sensors = sensors
        
        entity.deviceInfo = deviceInfo
        entity.deviceInfo1 = deviceInfo
        
        return entity
    }

    private fun hasField(obj: Any, fieldName: String): Boolean {
        return try {
            obj.javaClass.getDeclaredField(fieldName)
            true
        } catch (e: NoSuchFieldException) {
            false
        }
    }

    private fun hasFieldIncludingInherited(obj: Any, fieldName: String): Boolean {
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            try {
                clazz.getDeclaredField(fieldName)
                return true
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        return false
    }

}

// 测试用的数据类 - 移到外部避免被RefUtil.isT排除
open class ComplexNestedEntity {
    var gender: String = "0"
    var deviceStatus: String = ""
    var deviceStatus1: String? = null
    var productKey: String = "PROD001"
    var deviceInfo: DeviceInfo? = null
    var deviceInfo1: DeviceInfo? = null
}

open class DeviceInfo {
    var deviceId: String = "49"
    var deviceId1: String = "55"
    var temperature: BigDecimal? = null
    var createTime: Date? = null
    var location: Location? = null
    var sensors: List<SensorInfo>? = null
}

open class Location {
    var testvar1: String = "1"
    var testvar2: String = "2"
    var testTableVar: String = "55"
}

open class SensorInfo {
    var testvar1: String = "1"
    var testvar2: String = "2"
    var testTableVar: String = "55"
}