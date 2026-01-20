package site.addzero.network.call.video.tencent

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 视频搜索 + 解析集成测试
 *
 * 完整流程：搜索视频 -> 获取播放地址 -> 生成解析地址
 */
class VideoSearchAndParseIntegrationTest {

  private val searchClient = TencentVideoSearchClient.getInstance()

  // 解析源列表
  private val parseSources = listOf(
    "综合" to "https://jx.jsonplayer.com/player/?url=",
    "CK" to "https://www.ckplayer.vip/jiexi/?url=",
    "YT" to "https://jx.yangtu.top/?url=",
    "Player-JY" to "https://jx.playerjy.com/?url=",
    "yparse" to "https://jx.yparse.com/index.php?url=",
    "8090" to "https://www.8090g.cn/?url=",
    "剖元" to "https://www.pouyun.com/?url=",
    "虾米" to "https://jx.xmflv.com/?url=",
    "OK" to "https://api.okjx.com/?url=",
    "BL" to "https://api.bljiex.com/?url=",
    "m3u8" to "https://jx.m3u8.tv/?url="
  )

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
      val isQQVideo = firstVideo.playUrl.contains("v.qq.com") || firstVideo.playUrl.contains("qq.com")
      println("  是腾讯视频: $isQQVideo")
      println()

      // 4. 获取所有可用解析源
      println("步骤4: 获取可用解析源")
      println("  可用解析源数量: ${parseSources.size}")
      parseSources.take(5).forEach { (name, _) ->
        println("  - $name")
      }
      println()

      // 5. 生成解析URL
      println("步骤5: 生成解析URL")
      parseSources.take(5).forEach { (name, baseUrl) ->
        val parseUrl = firstVideo.getParseUrl(baseUrl)
        println("  [$name]: $parseUrl")
      }
      println()

      println("✓ 完整流程测试完成")

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

      // 使用默认解析源生成解析URL
      val (sourceName, sourceUrl) = parseSources.first()
      val parseUrl = searchClient.buildParseUrl(url, sourceUrl)
      println("  解析URL [$sourceName]: $parseUrl")

      assertTrue(parseUrl.contains("jsonplayer"))

      println()
    }

    println("✓ 直接URL解析测试完成")
  }

  @Test
  fun testMultiSourceParse() {
    println("===== 多解析源测试 =====\n")

    val videoUrl = "https://v.qq.com/x/cover/mzc00200o3jp0vf/d4101j5og2l.html"
    println("视频URL: $videoUrl\n")

    println("尝试使用不同解析源生成解析地址:\n")

    parseSources.forEach { (name, baseUrl) ->
      val parseUrl = searchClient.buildParseUrl(videoUrl, baseUrl)
      println("[$name]")
      println("  $parseUrl")
      println()
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

          // 使用searchClient生成
          val parseUrl2 = searchClient.buildParseUrl(item.playUrl, parseSources.first().second)
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
