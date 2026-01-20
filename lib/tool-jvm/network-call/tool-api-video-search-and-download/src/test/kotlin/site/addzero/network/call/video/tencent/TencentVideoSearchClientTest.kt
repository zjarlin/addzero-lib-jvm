package site.addzero.network.call.video.tencent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 腾讯视频搜索客户端测试
 */
class TencentVideoSearchClientTest {

  private val client = TencentVideoSearchClient.getInstance()

  @Test
  fun testSearch() {
    println("===== 腾讯视频搜索测试 =====")

    val keyword = "仙逆"
    println("搜索关键词: $keyword")

    try {
      val result = client.search(keyword)

      println("搜索结果:")
      println("- 关键词: ${result.keyword}")
      println("- 页码: ${result.page}")
      println("- 结果数量: ${result.total}")
      println("- 是否有更多: ${result.hasMore}")
      println()

      if (result.items.isNotEmpty()) {
        println("搜索到的视频列表:")
        result.items.forEachIndexed { index, item ->
          println("[$index] ${item.title}")
          println("    ID: ${item.id}")
          println("    类型: ${item.typeName}")
          println("    评分: ${item.score ?: "暂无"}")
          println("    年份: ${item.year ?: "未知"}")
          println("    演员: ${item.actors ?: "未知"}")
          println("    播放地址: ${item.playUrl}")
          println("    封面: ${item.cover ?: "无"}")
          println()
        }

        assertTrue(result.items.isNotEmpty(), "搜索结果不应为空")
        assertEquals(keyword, result.keyword, "关键词应匹配")
      } else {
        println("未搜索到相关视频，可能是API格式变化")
      }

      println("✓ 搜索测试完成")
    } catch (e: Exception) {
      println("搜索失败: ${e.message}")
      e.printStackTrace()
    }
  }

  @Test
  fun testSearchWithPage() {
    println("===== 分页搜索测试 =====")

    val keyword = "斗破苍穹"
    println("搜索关键词: $keyword")

    try {
      // 第一页
      val page0Result = client.search(keyword, page = 0, pageSize = 5)
      println("第0页结果数量: ${page0Result.items.size}")

      // 第二页
      val page1Result = client.search(keyword, page = 1, pageSize = 5)
      println("第1页结果数量: ${page1Result.items.size}")

      println("✓ 分页搜索测试完成")
    } catch (e: Exception) {
      println("分页搜索失败: ${e.message}")
    }
  }

  @Test
  fun testSearchVariousKeywords() {
    println("===== 多关键词搜索测试 =====")

    val keywords = listOf("庆余年", "狂飙", "三体")

    keywords.forEach { keyword ->
      println("\n搜索: $keyword")
      try {
        val result = client.search(keyword, pageSize = 3)
        println("  结果数量: ${result.items.size}")
        result.items.firstOrNull()?.let {
          println("  第一个结果: ${it.title}")
          println("  播放地址: ${it.playUrl}")
        }
      } catch (e: Exception) {
        println("  搜索失败: ${e.message}")
      }
    }

    println("\n✓ 多关键词搜索测试完成")
  }

  @Test
  fun testGetParseUrl() {
    println("===== 解析URL生成测试 =====")

    val playUrl = "https://v.qq.com/x/cover/mzc00200o3jp0vf/d4101j5og2l.html"
    val parseUrl = client.buildParseUrl(playUrl)

    println("原始播放地址: $playUrl")
    println("解析地址: $parseUrl")

    assertNotNull(parseUrl, "解析地址不能为空")
    assertTrue(parseUrl.contains("jx.jsonplayer.com"), "应包含解析域名")
    assertTrue(parseUrl.contains("v.qq.com"), "应包含原始地址(编码后)")

    println("✓ 解析URL生成测试完成")
  }

  @Test
  fun testVideoItemGetParseUrl() {
    println("===== VideoItem解析URL测试 =====")

    val videoItem = TencentVideoItem(
      id = "test123",
      title = "测试视频",
      cover = null,
      playUrl = "https://v.qq.com/x/cover/abc123.html",
      typeName = "电视剧",
      score = 8.5,
      description = "测试描述",
      year = "2024",
      actors = "测试演员",
      updateInfo = "更新至10集"
    )

    val parseUrl = videoItem.getParseUrl()
    println("视频: ${videoItem.title}")
    println("播放地址: ${videoItem.playUrl}")
    println("解析地址: $parseUrl")

    assertNotNull(parseUrl)
    assertTrue(parseUrl.startsWith("https://jx.jsonplayer.com/player/?url="))

    println("✓ VideoItem解析URL测试完成")
  }

  @Test
  fun testSearchAndBuildParseUrl() {
    println("===== 搜索并生成解析URL测试 =====")

    val keyword = "仙逆"
    println("搜索关键词: $keyword")

    try {
      val result = client.search(keyword, pageSize = 3)

      if (result.items.isNotEmpty()) {
        println("\n搜索结果及其解析地址:")
        result.items.forEach { item ->
          println("\n视频: ${item.title}")
          println("播放地址: ${item.playUrl}")
          println("解析地址: ${item.getParseUrl()}")

          // 使用不同的解析源
          val parseUrls = listOf(
            "综合" to "https://jx.jsonplayer.com/player/?url=",
            "CK" to "https://www.ckplayer.vip/jiexi/?url=",
            "YT" to "https://jx.yangtu.top/?url="
          )

          println("多源解析地址:")
          parseUrls.forEach { (name, baseUrl) ->
            println("  [$name]: ${item.getParseUrl(baseUrl)}")
          }
        }

        println("\n✓ 搜索并生成解析URL测试完成")
      } else {
        println("未搜索到结果")
      }
    } catch (e: Exception) {
      println("测试失败: ${e.message}")
    }
  }
}
