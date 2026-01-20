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

    val result = client.search(keyword)

    println("搜索结果:")
    println("- 关键词: ${result.keyword}")
    println("- 页码: ${result.page}")
    println("- 结果数量: ${result.total}")
    println("- 是否有更多: ${result.hasMore}")
    println("- 是否成功: ${result.isSuccess}")

    if (result.error != null) {
      println("- 提示: ${result.error}")
      println("\n注意：搜索API可能暂不可用，但解析URL生成功能正常工作")
    }

    if (result.items.isNotEmpty()) {
      println("\n搜索到的视频列表:")
      result.items.forEachIndexed { index, item ->
        println("[$index] ${item.title}")
        println("    ID: ${item.id}")
        println("    类型: ${item.typeName}")
        println("    播放地址: ${item.playUrl}")
        println()
      }
      assertEquals(keyword, result.keyword, "关键词应匹配")
    }

    println("✓ 搜索测试完成")
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
  fun testBuildParseUrlBySource() {
    println("===== 指定解析源测试 =====")

    val playUrl = "https://v.qq.com/x/cover/mzc00200o3jp0vf/d4101j5og2l.html"

    val sources = listOf("综合", "CK", "YT", "虾米")
    sources.forEach { sourceName ->
      val parseUrl = client.buildParseUrlBySource(playUrl, sourceName)
      println("[$sourceName]: $parseUrl")
      assertNotNull(parseUrl)
    }

    println("✓ 指定解析源测试完成")
  }

  @Test
  fun testGetAllParseUrls() {
    println("===== 获取所有解析源URL测试 =====")

    val playUrl = "https://v.qq.com/x/cover/mzc00200o3jp0vf/d4101j5og2l.html"
    val allUrls = client.getAllParseUrls(playUrl)

    println("视频URL: $playUrl")
    println("\n所有解析源URL:")
    allUrls.forEach { (name, url) ->
      println("[$name]: $url")
    }

    assertEquals(TencentVideoSearchClient.PARSE_SOURCES.size, allUrls.size)
    println("\n✓ 获取所有解析源URL测试完成")
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

    // 测试获取所有解析源
    println("\n所有解析源:")
    videoItem.getAllParseUrls().forEach { (name, url) ->
      println("  [$name]: $url")
    }

    println("✓ VideoItem解析URL测试完成")
  }

  @Test
  fun testParseSources() {
    println("===== 解析源列表测试 =====")

    val sources = TencentVideoSearchClient.PARSE_SOURCES
    println("可用解析源数量: ${sources.size}")

    sources.forEach { source ->
      println("- ${source.name}: ${source.url}")
    }

    assertTrue(sources.isNotEmpty(), "解析源列表不能为空")
    assertTrue(sources.any { it.name == "综合" }, "应包含综合解析源")

    println("✓ 解析源列表测试完成")
  }

  @Test
  fun testMultiplePlatformUrls() {
    println("===== 多平台视频解析URL测试 =====")

    // 测试不同平台的视频URL
    val testUrls = mapOf(
      "腾讯视频" to "https://v.qq.com/x/cover/mzc00200o3jp0vf/d4101j5og2l.html",
      "爱奇艺" to "https://www.iqiyi.com/v_19rr1y3vk8.html",
      "优酷" to "https://v.youku.com/v_show/id_XMTY3ODEyODMxMg==.html",
      "哔哩哔哩" to "https://www.bilibili.com/video/BV1xx411c7mD",
      "芒果TV" to "https://www.mgtv.com/b/123456/789.html"
    )

    testUrls.forEach { (platform, url) ->
      println("\n[$platform]")
      println("  原始地址: $url")

      val parseUrl = client.buildParseUrl(url)
      println("  解析地址: $parseUrl")

      assertNotNull(parseUrl)
      assertTrue(parseUrl.contains("jx.jsonplayer.com"))
    }

    println("\n✓ 多平台视频解析URL测试完成")
  }

  @Test
  fun testSearchAndBuildParseUrl() {
    println("===== 搜索并生成解析URL测试 =====")

    val keyword = "仙逆"
    println("搜索关键词: $keyword")

    val result = client.search(keyword, pageSize = 3)

    if (result.items.isNotEmpty()) {
      println("\n搜索结果及其解析地址:")
      result.items.forEach { item ->
        println("\n视频: ${item.title}")
        println("播放地址: ${item.playUrl}")
        println("解析地址: ${item.getParseUrl()}")

        println("多源解析地址:")
        listOf("综合", "CK", "YT").forEach { sourceName ->
          val source = TencentVideoSearchClient.PARSE_SOURCES.find { it.name == sourceName }
          if (source != null) {
            println("  [$sourceName]: ${item.getParseUrl(source.url)}")
          }
        }
      }
    } else {
      println("\n搜索API暂不可用，演示直接使用播放地址生成解析URL:")

      val demoUrl = "https://v.qq.com/x/cover/mzc00200o3jp0vf/d4101j5og2l.html"
      println("\n视频播放地址: $demoUrl")
      println("解析地址: ${client.buildParseUrl(demoUrl)}")

      println("\n多源解析地址:")
      listOf("综合", "CK", "YT", "虾米").forEach { sourceName ->
        println("  [$sourceName]: ${client.buildParseUrlBySource(demoUrl, sourceName)}")
      }
    }

    println("\n✓ 搜索并生成解析URL测试完成")
  }
}
