package site.addzero.network.call.maven.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import site.addzero.util.CurlExecutor

/**
 * Maven Central 分页搜索工具
 * 
 * 支持滚动加载下一页的分页查询
 * 参考: https://central.sonatype.org/search/rest-api-guide/
 */
object MavenCentralPaginatedSearchUtil {

    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private const val SEARCH_API_BASE = "https://central.sonatype.com/solrsearch/select"
    private const val DEFAULT_PAGE_SIZE = 20

    /**
     * 按 groupId 分页搜索
     */
    fun searchByGroupIdPaginated(
        groupId: String,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedSearchSession = PaginatedSearchSession(
        query = "g:$groupId",
        pageSize = pageSize
    )

    /**
     * 按 artifactId 分页搜索
     */
    fun searchByArtifactIdPaginated(
        artifactId: String,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedSearchSession = PaginatedSearchSession(
        query = "a:$artifactId",
        pageSize = pageSize
    )

    /**
     * 按坐标分页搜索
     */
    fun searchByCoordinatesPaginated(
        groupId: String,
        artifactId: String,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedSearchSession = PaginatedSearchSession(
        query = "g:$groupId+AND+a:$artifactId",
        pageSize = pageSize
    )

    /**
     * 按所有版本分页搜索
     */
    fun searchAllVersionsPaginated(
        groupId: String,
        artifactId: String,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedSearchSession = PaginatedSearchSession(
        query = "g:$groupId+AND+a:$artifactId",
        pageSize = pageSize,
        core = "gav"
    )

    /**
     * 关键词分页搜索
     */
    fun searchByKeywordPaginated(
        keyword: String,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedSearchSession = PaginatedSearchSession(
        query = keyword,
        pageSize = pageSize
    )

    /**
     * 按类名分页搜索
     */
    fun searchByClassNamePaginated(
        className: String,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedSearchSession = PaginatedSearchSession(
        query = "c:$className",
        pageSize = pageSize
    )

    /**
     * 分页搜索会话
     * 维护当前搜索状态,支持滚动加载下一页
     */
    class PaginatedSearchSession(
        private val query: String,
        private val pageSize: Int = DEFAULT_PAGE_SIZE,
        private val core: String? = null,
        private val sortBy: String = "timestamp desc"
    ) {
        private var currentStart: Int = 0
        private var totalResults: Long = 0L
        private var hasLoadedFirstPage = false

        /**
         * 加载下一页数据
         * @return 分页结果,包含当前页数据和分页元信息
         */
        fun loadNextPage(): PaginatedSearchResult {
            val result = executeSearch(currentStart, pageSize)
            
            if (!hasLoadedFirstPage) {
                totalResults = result.totalResults
                hasLoadedFirstPage = true
            }
            
            currentStart += pageSize
            
            return result
        }

        /**
         * 重置会话,从第一页重新开始
         */
        fun reset() {
            currentStart = 0
            totalResults = 0L
            hasLoadedFirstPage = false
        }

        /**
         * 是否还有更多数据
         * 注意：第一次调用前返回 true，让调用者可以加载第一页
         */
        fun hasMore(): Boolean = !hasLoadedFirstPage || currentStart < totalResults

        /**
         * 获取当前页码 (从1开始)
         */
        fun getCurrentPage(): Int = (currentStart / pageSize)

        /**
         * 获取总结果数
         */
        fun getTotalResults(): Long = totalResults

        /**
         * 流式加载所有结果
         * 使用 Sequence 惰性求值,适合处理大量数据
         */
        fun loadAllAsSequence(): Sequence<MavenArtifact> = sequence {
            reset()
            while (true) {
                val result = loadNextPage()
                yieldAll(result.artifacts)
                if (!result.hasMore) break
            }
        }

        /**
         * 加载所有结果到 List (谨慎使用,可能占用大量内存)
         */
        fun loadAll(maxResults: Int = 1000): List<MavenArtifact> {
            return loadAllAsSequence().take(maxResults).toList()
        }

        private fun executeSearch(start: Int, rows: Int): PaginatedSearchResult {
            val url = buildSearchUrl(start, rows)
            
            return runCatching {
                val curl = """curl -X GET -H "Accept: application/json" "$url""""
                val response = CurlExecutor.execute(curl)
                
                when {
                    response.isSuccessful -> {
                        response.body?.string()
                            ?.let { parseSearchResponse(it, start, rows) }
                            ?: PaginatedSearchResult.empty()
                    }
                    else -> PaginatedSearchResult.empty()
                }
            }.getOrElse {
                PaginatedSearchResult.empty()
            }
        }

        private fun buildSearchUrl(start: Int, rows: Int): String {
            // Maven Central API 使用简单的参数格式，不支持 sort 参数
            val params = buildList {
                add("q=$query")
                add("start=$start")
                add("rows=$rows")
                add("wt=json")
                core?.let { add("core=$it") }
            }
            
            return "$SEARCH_API_BASE?${params.joinToString("&")}"
        }

        private fun parseSearchResponse(
            jsonContent: String,
            start: Int,
            rows: Int
        ): PaginatedSearchResult {
            return try {
                val response: SearchResponse = objectMapper.readValue(jsonContent)
                val wrapper = response.response ?: return PaginatedSearchResult.empty()
                
                val artifacts = wrapper.docs?.map { doc ->
                    MavenArtifact(
                        id = doc.id ?: "",
                        groupId = doc.g ?: "",
                        artifactId = doc.a ?: "",
                        version = doc.v ?: "",
                        latestVersion = doc.latestVersion ?: doc.v ?: "",
                        packaging = doc.p ?: "jar",
                        timestamp = doc.timestamp ?: 0L,
                        repositoryId = doc.repositoryId ?: "central",
                        classifier = doc.l,
                        text = doc.text
                    )
                } ?: emptyList()
                
                PaginatedSearchResult(
                    artifacts = artifacts,
                    totalResults = wrapper.numFound ?: 0L,
                    start = start,
                    pageSize = rows,
                    currentPage = start / rows + 1,
                    totalPages = ((wrapper.numFound ?: 0L) + rows - 1) / rows
                )
            } catch (e: Exception) {
                println("解析分页响应失败: ${e.message}")
                e.printStackTrace()
                PaginatedSearchResult.empty()
            }
        }
    }
}

/**
 * 分页搜索结果
 */
data class PaginatedSearchResult(
    val artifacts: List<MavenArtifact>,
    val totalResults: Long,
    val start: Int,
    val pageSize: Int,
    val currentPage: Int,
    val totalPages: Long
) {
    val hasMore: Boolean
        get() = start + artifacts.size < totalResults

    val hasPrevious: Boolean
        get() = start > 0

    companion object {
        fun empty() = PaginatedSearchResult(
            artifacts = emptyList(),
            totalResults = 0L,
            start = 0,
            pageSize = 0,
            currentPage = 0,
            totalPages = 0L
        )
    }
}
