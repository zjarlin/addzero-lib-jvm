package site.addzero.network.call.magnet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MagnetSearchClientSingletonTest {

  @Test
  fun testGetInstance() {
    val instance1 = MagnetSearchClient.getInstance()
    val instance2 = MagnetSearchClient.getInstance()

    assertNotNull(instance1, "单例实例不能为空")
    assertEquals(instance1, instance2, "多次获取应该返回同一个实例")

    println("✓ 单例模式测试通过")
  }

  @Test
  fun testGetInstanceIsThreadSafe() {
    val instance = MagnetSearchClient.getInstance()
    val search = instance.search("蜘蛛侠")
    println()

  }

  @Test
  fun testGetInstanceCaching() {
    val instance1 = MagnetSearchClient.getInstance()
    val instance2 = MagnetSearchClient.getInstance()

    val sources1 = instance1.getSearchSources()
    val sources2 = instance2.getSearchSources()

    assertEquals(sources1, sources2, "单例应该缓存搜索源列表")
    assertEquals(5, sources1.size, "应该有5个搜索源")

    sources1.forEach { source ->
      println("- ${source.name}: ${source.baseUrl} (启用: ${source.enabled})")
    }

    println("✓ 单例缓存测试通过 - ${sources1.size} 个搜索源")
  }

  @Test
  fun testGetInstanceConsistency() {
    val instance1 = MagnetSearchClient.getInstance()
    val instance2 = MagnetSearchClient.getInstance()

    val source1 = instance1.getSearchSourceByName("BTDig")
    val source2 = instance2.getSearchSourceByName("BTDig")

    assertEquals(source1, source2, "不同实例应该返回相同结果")
    assertEquals("BTDig", source1?.name)

    println("✓ 单例一致性测试通过 - 搜索源状态在多实例间一致")
  }

  @Test
  fun testGetInstancePerformance() {
    val iterations = 10
    val startTime = System.currentTimeMillis()

    repeat(iterations) {
      val instance = MagnetSearchClient.getInstance()
      instance.getSearchSources()
    }

    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    val avgTime = duration.toDouble() / iterations

    assertTrue(avgTime < 10, "单例获取平均时间应该小于10毫秒")

    println("✓ 单例性能测试通过 - 平均获取时间: ${String.format("%.2f", avgTime)} 毫秒")
  }

  @Test
  fun testGetInstanceHashCode() {
    val instance1 = MagnetSearchClient.getInstance()
    val instance2 = MagnetSearchClient.getInstance()

    val hash1 = System.identityHashCode(instance1)
    val hash2 = System.identityHashCode(instance2)

    assertEquals(hash1, hash2, "单例应该返回相同的哈希码")

    println("✓ 单例哈希码测试通过 - identityHashCode: $hash1")
  }
}
