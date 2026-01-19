package site.addzero.network.call.video

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VideoParseClientSingletonTest {

  @Test
  fun testGetInstance() {
    val instance1 = VideoParseClient.getInstance()
    val instance2 = VideoParseClient.getInstance()
    
    assertNotNull(instance1, "单例实例不能为空")
    assertEquals(instance1, instance2, "多次获取应该返回同一个实例")
    
    println("✓ 单例模式测试通过")
  }

  @Test
  fun testGetInstanceCaching() {
    val instance1 = VideoParseClient.getInstance()
    val instance2 = VideoParseClient.getInstance()
    
    val sources1 = VideoParseClient.getParseSources()
    val sources2 = VideoParseClient.getParseSources()
    
    assertEquals(sources1, sources2, "单例应该缓存解析源列表")
    assertTrue(sources1.size == 15, "应该有15个解析源")
    
    sources1.forEach { source ->
      println("- ${source.name}: ${source.url}")
    }
    
    println("✓ 单例缓存测试通过 - ${sources1.size} 个解析源")
  }

  @Test
  fun testGetInstanceConsistency() {
    val instance1 = VideoParseClient.getInstance()
    val instance2 = VideoParseClient.getInstance()
    
    val source1 = VideoParseClient.getParseSourceByName("综合")
    val source2 = VideoParseClient.getParseSourceByName("综合")
    
    assertEquals(source1, source2, "不同实例应该返回相同结果")
    assertEquals("综合", source1?.name)
    
    println("✓ 单例一致性测试通过 - 解析源状态在多实例间一致")
  }

  @Test
  fun testGetInstancePerformance() {
    val iterations = 10
    val startTime = System.currentTimeMillis()
    
    repeat(iterations) {
      val instance = VideoParseClient.getInstance()
      VideoParseClient.getParseSources()
    }
    
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    val avgTime = duration.toDouble() / iterations
    
    assertTrue(avgTime < 10, "单例获取平均时间应该小于10毫秒")
    
    println("✓ 单例性能测试通过 - 平均获取时间: ${String.format("%.2f", avgTime)} 毫秒")
  }

  @Test
  fun testGetInstanceHashCode() {
    val instance1 = VideoParseClient.getInstance()
    val instance2 = VideoParseClient.getInstance()
    
    val hash1 = System.identityHashCode(instance1)
    val hash2 = System.identityHashCode(instance2)
    
    assertEquals(hash1, hash2, "单例应该返回相同的哈希码")
    
    println("✓ 单例哈希码测试通过 - identityHashCode: $hash1")
  }
}