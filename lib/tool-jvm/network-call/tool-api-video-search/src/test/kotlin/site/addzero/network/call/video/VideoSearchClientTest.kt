package site.addzero.network.call.video

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VideoSearchClientTest {

  @Test
  fun testGetSearchSources() {
    val client = VideoSearchClient()
    val sources = VideoSearchClient.getSearchSources()
    assertTrue(sources.isNotEmpty(), "搜索源列表不能为空")
    println("✓ 可用视频搜索源数量: ${sources.size}")
    sources.forEach { source ->
      println("- ${source.name}: ${source.baseUrl} (启用: ${source.enabled})")
    }
  }

  @Test
  fun testGetSearchSourceByName() {
    val client = VideoSearchClient()
    val source = VideoSearchClient.getSearchSourceByName("豆瓣")
    assertNotNull(source, "搜索源不能为空")
    assertEquals("豆瓣", source.name)
    println("✓ 搜索源查找功能正常")
  }

  @Test
  fun testSearch() {
    val client = VideoSearchClient()
    
    try {
      val results = client.search("阿凡达", page = 1)
      println("搜索关键词: 阿凡达")
      println("结果数量: ${results.size}")
      
      results.take(3).forEach { result ->
        println("\n标题: ${result.title}")
        println("- 年份: ${result.year}")
        println("- 评分: ${result.rating}")
        println("- 来源: ${result.source}")
      }
      
      assertTrue(results.isNotEmpty(), "搜索结果不能为空")
    } catch (e: Exception) {
      println("搜索失败: ${e.message}")
    }
  }

  @Test
  fun testSearchWithSourceIndex() {
    val client = VideoSearchClient()
    
    try {
      val results = client.searchWithSource("三体", 0, page = 1)
      assertNotNull(results, "搜索结果不能为空")
      assertTrue(results.isNotEmpty(), "搜索结果不能为空")
      println("✓ 通过索引搜索功能正常")
    } catch (e: Exception) {
      println("搜索失败: ${e.message}")
    }
  }

  @Test
  fun testSearchWithSourceName() {
    val client = VideoSearchClient()
    
    try {
      val results = client.searchWithSource("三体", "豆瓣", page = 1)
      assertNotNull(results, "搜索结果不能为空")
      println("✓ 通过名称搜索功能正常 (结果数: ${results.size})")
    } catch (e: Exception) {
      println("搜索失败: ${e.message}")
    }
  }

  @Test
  fun testSearchAllSources() {
    val client = VideoSearchClient()
    val keyword = "三体"
    
    try {
      val results = client.searchAllSources(keyword)
      println("使用所有源搜索: $keyword")
      
      results.forEach { (sourceName, searchResults) ->
        println("\n$sourceName: 找到 ${searchResults.size} 个结果")
        searchResults.take(2).forEach { result ->
          println("  - ${result.title}")
        }
      }
      
      assertTrue(results.isNotEmpty(), "搜索结果不能为空")
    } catch (e: Exception) {
      println("全源搜索失败: ${e.message}")
    }
  }

  @Test
  fun testGetVideoDetail() {
    val client = VideoSearchClient()
    val videoId = "test123"
    
    val detail = client.getVideoDetail(videoId, "豆瓣")
    assertNotNull(detail, "视频详情不能为空")
    assertEquals(videoId, detail.videoId)
    assertEquals("豆瓣", detail.platform)
    
    println("✓ 获取视频详情功能正常")
    println("  标题: ${detail.title}")
    println("  年份: ${detail.year}")
  }

  @Test
  fun testGetPlayList() {
    val client = VideoSearchClient()
    val keyword = "测试关键词"
    
    val playList = client.getPlayList(keyword)
    assertNotNull(playList, "播放列表不能为空")
    assertEquals(keyword, playList.title)
    
    println("✓ 获取播放列表功能正常")
    println("  标题: ${playList.title}")
    println("  剧集数: ${playList.total}")
  }
}