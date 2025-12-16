package site.addzero.network.call.maven.util

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * Maven Central 分页搜索测试
 */
class MavenCentralPaginatedSearchTest {

    @Test
    fun `测试基本分页搜索 - Spring Boot工件`() {
        println("\n========== 测试分页搜索: org.springframework.boot ==========")

        val session = MavenCentralPaginatedSearchUtil.searchByGroupIdPaginated(
            groupId = "org.springframework.boot",
            pageSize = 10
        )

        // 加载第一页
        val page1 = session.loadNextPage()
        println("第 ${page1.currentPage} 页, 共 ${page1.totalPages} 页")
        println("总结果数: ${page1.totalResults}")
        println("当前页数据量: ${page1.artifacts.size}")
        
        page1.artifacts.take(5).forEachIndexed { index, artifact ->
            println("  ${index + 1}. ${artifact.artifactId}:${artifact.version}")
        }

        assertTrue(page1.artifacts.isNotEmpty(), "第一页应该有数据")
        assertTrue(page1.totalResults > 0, "总结果数应该大于0")
        
        // 加载第二页
        if (session.hasMore()) {
            println("\n加载第二页...")
            val page2 = session.loadNextPage()
            println("第 ${page2.currentPage} 页数据量: ${page2.artifacts.size}")
            
            page2.artifacts.take(3).forEachIndexed { index, artifact ->
                println("  ${index + 1}. ${artifact.artifactId}:${artifact.version}")
            }
            
            assertTrue(page2.artifacts.isNotEmpty(), "第二页应该有数据")
        }
    }

    @Test
    fun `测试滚动加载 - Guice所有版本`() {
        println("\n========== 测试滚动加载: com.google.inject:guice 所有版本 ==========")

        val session = MavenCentralPaginatedSearchUtil.searchAllVersionsPaginated(
            groupId = "com.google.inject",
            artifactId = "guice",
            pageSize = 5
        )

        println("开始滚动加载...")
        var pageCount = 0
        
        while (session.hasMore() && pageCount < 3) {
            val page = session.loadNextPage()
            pageCount++
            
            println("\n第 $pageCount 页 (${page.start} - ${page.start + page.artifacts.size}):")
            page.artifacts.forEach { artifact ->
                println("  - v${artifact.version} | timestamp=${artifact.timestamp}")
            }
            
            println("  hasMore=${page.hasMore}, totalResults=${page.totalResults}")
        }

        assertTrue(pageCount > 0, "至少应该加载一页数据")
    }

    @Test
    fun `测试分页元信息`() {
        println("\n========== 测试分页元信息 ==========")

        val session = MavenCentralPaginatedSearchUtil.searchByKeywordPaginated(
            keyword = "jackson",
            pageSize = 20
        )

        val page1 = session.loadNextPage()
        
        println("分页信息:")
        println("  总结果数: ${page1.totalResults}")
        println("  每页大小: ${page1.pageSize}")
        println("  当前页码: ${page1.currentPage}")
        println("  总页数: ${page1.totalPages}")
        println("  起始位置: ${page1.start}")
        println("  是否有下一页: ${page1.hasMore}")
        println("  是否有上一页: ${page1.hasPrevious}")

        assertTrue(page1.totalResults >= page1.artifacts.size, "总结果数应该大于等于当前页数据量")
        assertTrue(page1.currentPage == 1, "第一页页码应该是1")
        assertTrue(!page1.hasPrevious, "第一页不应该有上一页")
    }

    @Test
    fun `测试流式加载所有结果`() {
        println("\n========== 测试流式加载: com.google.guava 工件 ==========")

        val session = MavenCentralPaginatedSearchUtil.searchByGroupIdPaginated(
            groupId = "com.google.guava",
            pageSize = 10
        )

        // 使用 Sequence 惰性加载
        val first15 = session.loadAllAsSequence()
            .take(15)
            .toList()

        println("流式加载前15个结果:")
        first15.forEachIndexed { index, artifact ->
            println("  ${index + 1}. ${artifact.artifactId}:${artifact.version}")
        }

        assertTrue(first15.isNotEmpty(), "应该加载到数据")
    }

    @Test
    fun `测试会话重置`() {
        println("\n========== 测试会话重置 ==========")

        val session = MavenCentralPaginatedSearchUtil.searchByArtifactIdPaginated(
            artifactId = "junit",
            pageSize = 5
        )

        // 第一次加载
        val page1_1 = session.loadNextPage()
        println("第一次加载 - 当前页: ${session.getCurrentPage()}, 数据量: ${page1_1.artifacts.size}")
        
        val page1_2 = session.loadNextPage()
        println("第二次加载 - 当前页: ${session.getCurrentPage()}, 数据量: ${page1_2.artifacts.size}")

        // 重置会话
        session.reset()
        println("\n重置会话后...")
        
        // 重新加载
        val page2_1 = session.loadNextPage()
        println("重新加载 - 当前页: ${session.getCurrentPage()}, 数据量: ${page2_1.artifacts.size}")

        assertTrue(session.getCurrentPage() == 1, "重置后应该回到第1页")
        assertTrue(page1_1.artifacts.size == page2_1.artifacts.size, "重置后第一页数据量应该相同")
    }

    @Test
    fun `测试按类名分页搜索`() {
        println("\n========== 测试按类名分页搜索: Logger ==========")

        val session = MavenCentralPaginatedSearchUtil.searchByClassNamePaginated(
            className = "Logger",
            pageSize = 10
        )

        val page = session.loadNextPage()
        
        println("找到 ${page.totalResults} 个包含 Logger 类的工件")
        println("第一页 ${page.artifacts.size} 个结果:")
        
        page.artifacts.take(5).forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.version}")
        }

        assertTrue(page.artifacts.isNotEmpty(), "应该找到包含Logger类的工件")
    }

    @Test
    fun `测试空结果的分页搜索`() {
        println("\n========== 测试空结果 ==========")

        val session = MavenCentralPaginatedSearchUtil.searchByGroupIdPaginated(
            groupId = "com.nonexistent.group.that.does.not.exist",
            pageSize = 10
        )

        val page = session.loadNextPage()
        
        println("总结果数: ${page.totalResults}")
        println("当前页数据量: ${page.artifacts.size}")
        println("是否有更多: ${session.hasMore()}")

        assertTrue(page.artifacts.isEmpty(), "不存在的 groupId 应该返回空结果")
        assertTrue(page.totalResults == 0L, "总结果数应该是0")
        assertTrue(!session.hasMore(), "不应该有更多数据")
    }

    @Test
    fun `测试加载限量所有结果`() {
        println("\n========== 测试加载限量所有结果: Spring Boot (最多50个) ==========")

        val session = MavenCentralPaginatedSearchUtil.searchByGroupIdPaginated(
            groupId = "org.springframework.boot",
            pageSize = 10
        )

        val all = session.loadAll(maxResults = 50)
        
        println("加载了 ${all.size} 个结果:")
        all.take(10).forEachIndexed { index, artifact ->
            println("  ${index + 1}. ${artifact.artifactId}:${artifact.version}")
        }
        if (all.size > 10) {
            println("  ... 还有 ${all.size - 10} 个结果")
        }

        assertTrue(all.size <= 50, "不应该超过最大限制")
    }

    @Test
    fun `性能测试 - 多页滚动加载`() {
        println("\n========== 性能测试: 滚动加载多页 ==========")

        val session = MavenCentralPaginatedSearchUtil.searchByKeywordPaginated(
            keyword = "spring",
            pageSize = 20
        )

        val startTime = System.currentTimeMillis()
        var totalLoaded = 0
        val maxPages = 3

        repeat(maxPages) { pageIndex ->
            if (session.hasMore()) {
                val page = session.loadNextPage()
                totalLoaded += page.artifacts.size
                println("第 ${pageIndex + 1} 页加载完成, 本页 ${page.artifacts.size} 个, 累计 $totalLoaded 个")
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        println("\n总耗时: ${elapsed}ms")
        println("平均每页: ${elapsed / maxPages}ms")
        println("总加载: $totalLoaded 个工件")

        assertTrue(totalLoaded > 0, "应该加载到数据")
    }
}
