import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class MavenArtifact(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val extension: String,
    val packaging: String,
    val classifier: String?
)

class MavenCentralSearcher(
    cacheDir: File = File(System.getProperty("java.io.tmpdir"), "maven-search-cache"),
    cacheSize: Long = 10 * 1024 * 1024, // 10MB
    connectTimeout: Int = 10_000,
    readTimeout: Int = 10_000
) {
    private val client: OkHttpClient
    private val objectMapper = jacksonObjectMapper()

    init {
        val cache = Cache(cacheDir, cacheSize)
        client = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(connectTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout.toLong(), TimeUnit.MILLISECONDS)
            .build()
    }

    /**
     * 通过名称搜索Maven依赖
     */
    fun searchByName(name: String, maxResults: Int = 500): List<MavenArtifact> {
        val encodedName = encode(name)
        val url = "http://search.maven.org/solrsearch/select?rows=$maxResults&wt=json&q=$encodedName"
        return queryMavenCentral(url)
    }

    /**
     * 通过GAV坐标搜索Maven依赖
     */
    fun searchByGav(
        groupId: String? = null,
        artifactId: String? = null,
        version: String? = null,
        maxResults: Int = 500
    ): List<MavenArtifact> {
        if (groupId.isNullOrBlank() && artifactId.isNullOrBlank() && version.isNullOrBlank()) {
            return emptyList()
        }

        val queryParts = mutableListOf<String>()
        groupId?.takeIf { it.isNotBlank() }?.let {
            queryParts.add("g:\"${encode(it)}\"")
        }
        artifactId?.takeIf { it.isNotBlank() }?.let {
            queryParts.add("a:\"${encode(it)}\"")
        }
        version?.takeIf { it.isNotBlank() }?.let {
            queryParts.add("v:\"${encode(it)}\"")
        }

        val url = "http://search.maven.org/solrsearch/select?rows=$maxResults&wt=json&core=gav&q=${queryParts.joinToString("+AND+")}"
        return queryMavenCentral(url)
    }

    /**
     * 通过打包类型搜索Maven依赖
     */
    fun searchByPackaging(packaging: String, maxResults: Int = 500): List<MavenArtifact> {
        val encodedPackaging = encode(packaging)
        val url = "http://search.maven.org/solrsearch/select?rows=$maxResults&wt=json&core=gav&q=p:\"$encodedPackaging\""
        return queryMavenCentral(url)
    }

    private fun queryMavenCentral(url: String): List<MavenArtifact> {
        return try {
            val request = Request.Builder()
                .url(URL(url))
                .cacheControl(
                    CacheControl.Builder()
                        .maxStale(5, TimeUnit.MINUTES)
                        .build()
                )
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()

                val jsonNode: JsonNode = objectMapper.readValue(response.body!!.byteStream())
                val responseNode = jsonNode["response"] ?: return emptyList()
                val docsArray = responseNode["docs"] ?: return emptyList()

                mutableListOf<MavenArtifact>().apply {
                    docsArray.forEach { docNode ->
                        addAll(parseArtifact(docNode))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseArtifact(docNode: JsonNode): List<MavenArtifact> {
        val groupId = docNode["g"]?.asText() ?: return emptyList()
        val artifactId = docNode["a"]?.asText() ?: return emptyList()

        val version = docNode["latestVersion"]?.asText()
            ?: docNode["v"]?.asText()
            ?: return emptyList()

        val packaging = docNode["p"]?.asText() ?: "jar"
        val ecArray = docNode["ec"]

        val artifacts = mutableListOf<MavenArtifact>()
        val pattern = "^(-([^.]+))*\\.(.*)$".toRegex()

        ecArray?.forEach { ecNode ->
            val ec = ecNode.asText()
            val matchResult = pattern.matchEntire(ec)
            if (matchResult != null) {
                val classifier = matchResult.groupValues[2].takeIf { it.isNotBlank() }
                val extension = matchResult.groupValues[3]

                // 跳过源码和文档包
                if (classifier != "javadoc" && classifier != "sources") {
                    artifacts.add(
                        MavenArtifact(
                            groupId = groupId,
                            artifactId = artifactId,
                            version = version,
                            extension = extension,
                            packaging = packaging,
                            classifier = classifier
                        )
                    )
                }
            }
        }

        // 添加默认构件（如果没有有效构件）
        if (artifacts.isEmpty()) {
            artifacts.add(
                MavenArtifact(
                    groupId = groupId,
                    artifactId = artifactId,
                    version = version,
                    extension = "jar",
                    packaging = packaging,
                    classifier = null
                )
            )
        }

        return artifacts
    }

    private fun encode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8")
        } catch (e: Exception) {
            value
        }
    }
}

// 使用示例
fun main() {
    val searcher = MavenCentralSearcher()

    // 按名称搜索
    val nameResults = searcher.searchByName("kotlin-stdlib")
    println("按名称搜索 'kotlin-stdlib' 结果: ${nameResults.size} 条")
    nameResults.take(3).forEach {
        println("${it.groupId}:${it.artifactId}:${it.version} [${it.extension}]")
    }

    // 按GAV搜索
    val gavResults = searcher.searchByGav(groupId = "com.fasterxml.jackson.module", artifactId = "jackson-module-kotlin")
    println("\n按GAV搜索结果: ${gavResults.size} 条")
    gavResults.take(3).forEach {
        println("${it.groupId}:${it.artifactId}:${it.version}")
    }
}
