package site.addzero.network.call.lsky

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LskyUploadClientSingletonTest {

  @Test
  fun testGetInstance() {
    val instance1 = LskyUploadClient.getInstance()
    val instance2 = LskyUploadClient.getInstance()
    
    assertNotNull(instance1, "单例实例不能为空")
    assertEquals(instance1, instance2, "多次获取应该返回同一个实例")
    
    println("✓ 单例模式测试通过")
  }

  @Test
  fun testGetInstancePerformance() {
    val iterations = 10
    val startTime = System.currentTimeMillis()
    
    repeat(iterations) {
      val instance = LskyUploadClient.getInstance()
      assertNotNull(instance)
    }
    
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    val avgTime = duration.toDouble() / iterations
    
    assertTrue(avgTime < 10, "单例获取平均时间应该小于10毫秒")
    
    println("✓ 单例性能测试通过 - 平均获取时间: ${String.format("%.2f", avgTime)} 毫秒")
  }

  @Test
  fun testGetInstanceHashCode() {
    val instance1 = LskyUploadClient.getInstance()
    val instance2 = LskyUploadClient.getInstance()
    
    val hash1 = System.identityHashCode(instance1)
    val hash2 = System.identityHashCode(instance2)
    
    assertEquals(hash1, hash2, "单例应该返回相同的哈希码")
    
    println("✓ 单例哈希码测试通过 - identityHashCode: $hash1")
  }

  @Test
  fun testGetInstanceWithConfig() {
    val config = site.addzero.network.call.lsky.model.LskyUploadConfig(
      baseUrl = "https://test.example.com",
      strategyId = 2
    )
    
    val instance = LskyUploadClient.getInstance(config)
    assertNotNull(instance, "带配置的单例实例不能为空")
    
    println("✓ 带配置的单例模式测试通过")
  }
}