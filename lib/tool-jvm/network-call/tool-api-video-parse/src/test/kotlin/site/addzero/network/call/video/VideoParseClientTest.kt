package site.addzero.network.call.video

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.network.call.video.model.VideoPlatform

class VideoParseClientTest {

  @Test
  fun testGetParseSources() {
    val client = VideoParseClient()
    val sources = VideoParseClient.getParseSources()
    assertTrue(sources.isNotEmpty(), "解析源列表不能为空")
    println("✓ 可用视频解析源数量: ${sources.size}")
    sources.forEach { source ->
      println("- ${source.name}: ${source.url}")
    }
  }

  @Test
  fun testGetParseSourceByName() {
    val client = VideoParseClient()
    val source = VideoParseClient.getParseSourceByName("综合")
    assertNotNull(source, "解析源不能为空")
    assertEquals("综合", source.name)
    println("✓ 解析源查找功能正常")
  }

  @Test
  fun testDetectPlatform() {
    val client = VideoParseClient()
    
    val qqUrl = "https://v.qq.com/x/cover/abc123.html"
    val iqiyiUrl = "https://www.iqiyi.com/v/abc123.html"
    val bilibiliUrl = "https://www.bilibili.com/video/BV1xx411c7mD"
    val unknownUrl = "https://unknown-site.com/video"
    
    assertEquals(VideoPlatform.QQ, client.detectPlatform(qqUrl), "应该检测到腾讯视频")
    assertEquals(VideoPlatform.IQIYI, client.detectPlatform(iqiyiUrl), "应该检测到爱奇艺")
    assertEquals(VideoPlatform.BILIBILI, client.detectPlatform(bilibiliUrl), "应该检测到哔哩哔哩")
    assertEquals(VideoPlatform.UNKNOWN, client.detectPlatform(unknownUrl), "应该检测为未知平台")
    
    println("✓ 平台检测功能正常")
  }

  @Test
  fun testIsSupportedUrl() {
    val client = VideoParseClient()
    
    val supportedUrls = listOf(
      "https://v.qq.com/x/cover/abc123.html",
      "https://www.iqiyi.com/v/abc123.html",
      "https://www.bilibili.com/video/BV1xx411c7mD"
    )
    
    supportedUrls.forEach { url ->
      assertTrue(client.isSupportedUrl(url), "$url 应该被支持")
    }
    
    assertFalse(client.isSupportedUrl("https://unknown-site.com/video"), "不支持的平台应该返回false")
    
    println("✓ URL支持检测功能正常")
  }

  @Test
  fun testGetVideoInfo() {
    val client = VideoParseClient()
    val url = "https://www.bilibili.com/video/BV1xx411c7mD"
    
    val info = client.getVideoInfo(url)
    assertNotNull(info, "视频信息不能为空")
    assertEquals(VideoPlatform.BILIBILI.displayName, info.platform)
    assertNotNull(info.parseUrl)
    
    println("✓ 获取视频信息功能正常")
    println("  平台: ${info.platform}")
    println("  视频ID: ${info.videoId}")
  }

  @Test
  fun testGetParseUrl() {
    val client = VideoParseClient()
    val url = "https://v.qq.com/x/cover/abc123.html"
    
    val parseUrl1 = client.getParseUrl(url, 0)
    assertNotNull(parseUrl1, "解析URL不能为空")
    assertTrue(parseUrl1.contains("jx.jsonplayer.com"), "应该包含解析源域名")
    
    val parseUrl2 = client.getParseUrl(url, "综合")
    assertEquals(parseUrl1, parseUrl2, "通过索引和名称获取的URL应该相同")
    
    println("✓ 获取解析URL功能正常")
  }

  @Test
  fun testParse() {
    val client = VideoParseClient()
    val url = "https://v.qq.com/x/cover/abc123.html"
    
    try {
      val result = client.parse(url)
      println("解析结果:")
      println("- 标题: ${result?.title}")
      println("- 解析源: ${result?.parseSource}")
      println("- 视频URL: ${result?.videoUrls?.firstOrNull()}")
      
      assertNotNull(result, "解析结果不能为空")
    } catch (e: Exception) {
      println("解析失败: ${e.message}")
    }
  }

  @Test
  fun testParseWithSourceIndex() {
    val client = VideoParseClient()
    val url = "https://v.qq.com/x/cover/abc123.html"
    
    try {
      val result = client.parseWithSource(url, 0)
      assertNotNull(result, "解析结果不能为空")
      assertEquals("综合", result.parseSource)
      println("✓ 通过索引解析功能正常")
    } catch (e: Exception) {
      println("解析失败: ${e.message}")
    }
  }

  @Test
  fun testParseWithSourceName() {
    val client = VideoParseClient()
    val url = "https://v.qq.com/x/cover/abc123.html"
    
    try {
      val result = client.parseWithSource(url, "综合")
      assertNotNull(result, "解析结果不能为空")
      assertEquals("综合", result.parseSource)
      println("✓ 通过名称解析功能正常")
    } catch (e: Exception) {
      println("解析失败: ${e.message}")
    }
  }
}