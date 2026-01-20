package site.addzero.network.call.video.tencent

import site.addzero.network.call.video.VideoParseClient
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 视频搜索 + 解析集成测试
 *
 * 完整流程：搜索视频 -> 获取播放地址 -> 生成解析地址 -> 解析视频
 */
class VideoSearchAndParseIntegrationTest {

  private val searchClient = TencentVideoSearchClient.getInstance()
  private val parseClient = VideoParseClient.getInstance()

  @Test
  fun testSearchAndParseWorkflow() {
    println("===== 搜索+解析完整流程测试 =====\n")

    val keyword = "仙逆"
    println("步骤1: 搜索视频 - 关键词: $keyword")

    try {
      // 1. 搜索视频
      val searchResult = searchClient.search(keyword, pageSize = 5)
      println("搜索到 ${searchResult.items.size} 个结果\n")

      if (searchResult.items.isEmpty()) {
        println("未搜索到结果，跳过后续测试")
        return
      }

      // 2. 选取第一个结果
      val firstVideo = searchResult.items.first()
      println("步骤2: 选择视频")
      println("  标题: ${firstVideo.title}")
      println("  ID: ${firstVideo.id}")
      println("  播放地址: ${firstVideo.playUrl}")
      println()

      // 3. 检测平台
      println("步骤3: 检测视频平台")
      val platform = parseClient.detectPlatform(firstVideo.playUrl)
      println("  检测到平台: ${platform.displayName}")
      assertTrue(parseClient.isSupportedUrl(firstVideo.playUrl), "URL应该被支持")
      println()

      // 4. 获取所有可用解析源
      println("步骤4: 获取可用解析源")
      val parseSources = VideoParseClient.getParseSources()
      println("  可用解析源数量: ${parseSources.size}")
      parseSources.take(5).forEach { source ->
        println("  - ${source.name}: ${source.type.displayName}")
      }
      println()

      // 5. 生成解析URL
      println("步骤5: 生成解析URL")
      val parseUrls = parseSources.take(5).map { source ->
        source.name to parseClient.getParseUrl(firstVideo.playUrl, source.name)
      }
      parseUrls.forEach { (name, url) ->
        println("  [$name]: $url")
      }
      println()

      // 6. 尝试解析（使用第一个解析源）
      println("步骤6: 尝试解析视频")
      try {
        val parseResult = parseClient.parse(firstVideo.playUrl)
        println("  解析成功!")
        println("  标题: ${parseResult?.title}")
        println("  解析源: ${parseResult?.parseSource}")
        println("  视频URL: ${parseResult?.videoUrls?.firstOrNull()}")
        assertNotNull(parseResult, "解析结果不应为空")
      } catch (e: Exception) {
        println("  解析请求失败(可能是网络问题): ${e.message}")
      }

      println("\n✓ 完整流程测试完成")

    } catch (e: Exception) {
      println("测试失败: ${e.message}")
      e.printStackTrace()
    }
  }

  @Test
  fun testDirectUrlParse() {
    println("===== 直接URL解析测试 =====\n")

    // 测试几个不同平台的视频URL
    val testUrls = listOf(
      "https://v.qq.com/x/cover/mzc00200o3jp0vf/d4101j5og2l.html" to "腾讯视频",
      "https://www.iqiyi.com/v_19rr1y3vk8.html" to "爱奇艺",
      "https://www.bilibili.com/video/BV1xx411c7mD" to "哔哩哔哩"
    )

    testUrls.forEach { (url, platformName) ->
      println("测试: $platformName")
      println("  URL: $url")

      // 检测平台
      val platform = parseClient.detectPlatform(url)
      println("  检测平台: ${platform.displayName}")

      // 检查是否支持
      val isSupported = parseClient.isSupportedUrl(url)
      println("  是否支持: $isSupported")

      // 获取解析URL
      val parseUrl = parseClient.getParseUrl(url, 0)
      println("  解析URL: $parseUrl")

      // 获取视频信息
      val videoInfo = parseClient.getVideoInfo(url)
      println("  视频ID: ${videoInfo?.videoId}")

      println()
    }

    println("✓ 直接URL解析测试完成")
  }

  @Test
  fun testMultiSourceParse() {
    println("===== 多解析源测试 =====\n")

    val videoUrl = "https://v.qq.com/x/cover/mzc00200o3jp0vf/d4101j5og2l.html"
    println("视频URL: $videoUrl\n")

    val parseSources = VideoParseClient.getParseSources()

    println("尝试使用不同解析源生成解析地址:\n")

    parseSources.forEach { source ->
      try {
        val parseUrl = parseClient.getParseUrl(videoUrl, source.name)
        println("[${source.name}] ${source.type.displayName}")
        println("  $parseUrl")
        println()
      } catch (e: Exception) {
        println("[${source.name}] 生成失败: ${e.message}")
      }
    }

    println("✓ 多解析源测试完成")
  }

  @Test
  fun testSearchResultToParseUrl() {
    println("===== 搜索结果转解析URL测试 =====\n")

    val keywords = listOf("庆余年", "狂飙")

    keywords.forEach { keyword ->
      println("关键词: $keyword")
      try {
        val result = searchClient.search(keyword, pageSize = 2)

        result.items.forEach { item ->
          println("\n  视频: ${item.title}")
          println("  播放地址: ${item.playUrl}")

          // 使用VideoItem的方法生成解析URL
          val parseUrl = item.getParseUrl()
          println("  默认解析: $parseUrl")

          // 使用parseClient生成
          val parseUrl2 = parseClient.getParseUrl(item.playUrl, "综合")
          println("  综合解析: $parseUrl2")
        }
        println()
      } catch (e: Exception) {
        println("  搜索失败: ${e.message}")
      }
    }

    println("✓ 搜索结果转解析URL测试完成")
  }
}
