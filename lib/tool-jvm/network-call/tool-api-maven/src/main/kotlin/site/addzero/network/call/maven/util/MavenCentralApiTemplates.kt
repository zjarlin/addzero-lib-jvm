package site.addzero.network.call.maven.util

/**
 * Maven Central REST API 的 curl 命令模板常量
 * 参考: https://central.sonatype.org/search/rest-api-guide/
 *
 * 所有模板使用 {{占位符}} 表示需要替换的变量
 */
object MavenCentralApiTemplates {

    // Maven Central API 基础 URL

    private const val SEARCH_API_BASE = "https://central.sonatype.com/solrsearch/select"
    private const val REMOTE_CONTENT_BASE = "https://search.maven.org/remotecontent"

    /**
     * 基础搜索 API curl 模板
     * 占位符: {{query}}, {{rows}}, {{wt}}
     */
    const val CURL_SEARCH_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q={{query}}&rows={{rows}}&wt={{wt}}"
    """

    /**
     * 带 core 参数的搜索 API curl 模板 (用于获取所有版本)
     * 占位符: {{query}}, {{rows}}, {{wt}}, {{core}}
     */
    const val CURL_SEARCH_WITH_CORE_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q={{query}}&rows={{rows}}&wt={{wt}}&core={{core}}"
    """

    /**
     * 文件下载 API curl 模板
     * 占位符: {{filepath}}
     */
    const val CURL_DOWNLOAD_TEMPLATE = """
        curl -X GET \
          "$REMOTE_CONTENT_BASE?filepath={{filepath}}"
    """

    /**
     * 按 groupId 搜索的 curl 模板
     * 占位符: {{groupId}}, {{rows}}
     */
    const val CURL_SEARCH_BY_GROUP_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q=g:{{groupId}}&rows={{rows}}&wt=json"
    """

    /**
     * 按 artifactId 搜索的 curl 模板
     * 占位符: {{artifactId}}, {{rows}}
     */
    const val CURL_SEARCH_BY_ARTIFACT_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q=a:{{artifactId}}&rows={{rows}}&wt=json"
    """

    /**
     * 按坐标搜索的 curl 模板
     * 占位符: {{groupId}}, {{artifactId}}, {{rows}}
     */
    const val CURL_SEARCH_BY_COORDINATES_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q=g:{{groupId}}+AND+a:{{artifactId}}&rows={{rows}}&wt=json"
    """

    /**
     * 搜索所有版本的 curl 模板 (使用 GAV core)
     * 占位符: {{groupId}}, {{artifactId}}, {{rows}}
     */
    const val CURL_SEARCH_ALL_VERSIONS_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q=g:{{groupId}}+AND+a:{{artifactId}}&rows={{rows}}&wt=json&core=gav"
    """

    /**
     * 按类名搜索的 curl 模板
     * 占位符: {{className}}, {{rows}}
     */
    const val CURL_SEARCH_BY_CLASS_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q=c:{{className}}&rows={{rows}}&wt=json"
    """

    /**
     * 按完全限定类名搜索的 curl 模板
     * 占位符: {{fullyQualifiedClassName}}, {{rows}}
     */
    const val CURL_SEARCH_BY_FC_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q=fc:{{fullyQualifiedClassName}}&rows={{rows}}&wt=json"
    """

    /**
     * 按 SHA-1 搜索的 curl 模板
     * 占位符: {{sha1}}, {{rows}}
     */
    const val CURL_SEARCH_BY_SHA1_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q=1:{{sha1}}&rows={{rows}}&wt=json"
    """

    /**
     * 按标签搜索的 curl 模板
     * 占位符: {{tag}}, {{rows}}
     */
    const val CURL_SEARCH_BY_TAG_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q=tags:{{tag}}&rows={{rows}}&wt=json"
    """

    /**
     * 关键词搜索的 curl 模板
     * 占位符: {{keyword}}, {{rows}}
     */
    const val CURL_SEARCH_BY_KEYWORD_TEMPLATE = """
        curl -X GET \
          -H "Accept: application/json" \
          "$SEARCH_API_BASE?q={{keyword}}&rows={{rows}}&wt=json"
    """
}
