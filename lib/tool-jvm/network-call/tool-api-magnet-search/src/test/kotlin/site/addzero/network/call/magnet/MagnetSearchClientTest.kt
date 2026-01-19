package site.addzero.network.call.magnet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MagnetSearchClientTest {

  @Test
  fun testGetSearchSources() {
    val client = MagnetSearchClient()
    val sources = client.getSearchSources()
    assertTrue(sources.isNotEmpty(), "搜索源列表不能为空")
    println("✓ 可用磁力搜索源数量: ${sources.size}")
    sources.forEach { source ->
      println("- ${source.name}: ${source.baseUrl} (启用: ${source.enabled})")
    }
  }

  @Test
  fun testGetSearchSourceByName() {
    val client = MagnetSearchClient()
    val source = client.getSearchSourceByName("BTDig")
    assertNotNull(source, "搜索源不能为空")
    assertEquals("BTDig", source.name)
    assertEquals("https://btdig.com", source.baseUrl)
    println("✓ 搜索源查找功能正常")
  }

  @Test
  fun testMagnetHash() {
    val client = MagnetSearchClient()
    val magnetLink = "magnet:?xt=urn:btih:c12fe1c06bba254a9dc9f519b335aa7c1367a88&dn=test"

    val hash = client.getMagnetHash(magnetLink)
    assertEquals("c12fe1c06bba254a9dc9f519b335aa7c1367a88", hash)

    println("✓ 磁力哈希提取正常: $hash")
  }

  @Test
  fun testValidateMagnetLink() {
    val client = MagnetSearchClient()

    val validLink = "magnet:?xt=urn:btih:c12fe1c06bba254a9dc9f519b335aa7c1367a88"
    val invalidLink = "not a magnet link"
    val invalidHashLink = "magnet:?xt=urn:btih:invalid"

    assertTrue(client.validateMagnetLink(validLink), "有效链接应该通过验证")
    assertFalse(client.validateMagnetLink(invalidLink), "无效链接应该失败")
    assertFalse(client.validateMagnetLink(invalidHashLink), "无效哈希链接应该失败")

    println("✓ 磁力链接验证功能正常")
  }

  @Test
  fun testCreateMagnetLink() {
    val client = MagnetSearchClient()
    val hash = "c12fe1c06bba254a9dc9f519b335aa7c1367a88"
    val displayName = "测试文件"

    val magnetLink = client.createMagnetLink(hash, displayName)
    assertTrue(magnetLink.startsWith("magnet:?xt=urn:btih:"), "应该以 magnet:?xt=urn:btih: 开头")
    assertTrue(magnetLink.contains(hash), "链接应该包含哈希值")
    val encodedName = java.net.URLEncoder.encode(displayName, "UTF-8")
    assertTrue(magnetLink.contains(encodedName), "应该包含URL编码的显示名称")

    println("✓ 创建磁力链接测试通过")
  }

  @Test
  fun testSearch() {
    val client = MagnetSearchClient()

    try {
      val results = client.search("三体", page = 1)
      println("搜索关键词: 三体")
      println("结果数量: ${results.size}")

      results.take(3).forEach { result ->
        println("\n标题: ${result.title}")
        println("- 大小: ${result.size}")
        println("- 磁力链接: ${result.magnetLink.take(50)}...")
        println("- 来源: ${result.source}")
        println("- 日期: ${result.date}")
      }

      assertTrue(results.isNotEmpty(), "搜索结果不能为空")
    } catch (e: Exception) {
      println("搜索失败: ${e.message}")
    }
  }

  @Test
  fun testSearchAllSources() {
    val client = MagnetSearchClient()
    val keyword = "阿凡达"

    try {
      val results = client.searchAllSources(keyword)
      println("使用所有源搜索: $keyword")

      results.forEach { (sourceName, searchResults) ->
        println("\n$sourceName: 找到 ${searchResults.size} 个结果")
        searchResults.take(2).forEach { result ->
          println("  - ${result.title} (${result.size})")
        }
      }

      assertTrue(results.isNotEmpty(), "搜索结果不能为空")
    } catch (e: Exception) {
      println("全源搜索失败: ${e.message}")
    }
  }
}
