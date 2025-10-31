package site.addzero.util

import okhttp3.*
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CurlParserTest {

    @Test
    fun testParseComplexCurlCommand() {
        val curlCommand = """
            curl 'https://demo.jetlinks.cn/api/device-product/_query' \
              -H 'accept: application/json, text/plain, */*' \
              -H 'accept-language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6' \
              -H 'content-type: application/json' \
              -H 'origin: https://demo.jetlinks.cn' \
              -H 'priority: u=1, i' \
              -H 'referer: https://demo.jetlinks.cn/' \
              -H 'sec-ch-ua: "Not/A)Brand";v="8", "Chromium";v="126", "Microsoft Edge";v="126"' \
              -H 'sec-ch-ua-mobile: ?0' \
              -H 'sec-ch-ua-platform: "Windows"' \
              -H 'sec-fetch-dest: empty' \
              -H 'sec-fetch-mode: cors' \
              -H 'sec-fetch-site: same-origin' \
              -H 'user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0' \
              -H 'x-access-token: 90a6e10cc7528db9a723096a98a09398' \
              --data-raw '{"pageIndex":0,"pageSize":96,"sorts":[{"name":"createTime","order":"desc"}],"terms":[]}'
        """.trimIndent().replace("\\\n", " ")

        val parsedCurl = CurlParser.parseCurl(curlCommand)

        // 验证方法
        assertEquals("POST", parsedCurl.method)

        // 验证URL
        assertEquals("https://demo.jetlinks.cn/api/device-product/_query", parsedCurl.url)

        // 验证headers
        val headers = parsedCurl.headers
        assertNotNull(headers)
        assertEquals("application/json, text/plain, */*", headers["accept"])
        assertEquals("zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6", headers["accept-language"])
        assertEquals("application/json", headers["content-type"])
        assertEquals("https://demo.jetlinks.cn", headers["origin"])
        assertEquals("90a6e10cc7528db9a723096a98a09398", headers["x-access-token"])

        // 验证body
        val body = parsedCurl.body
        assertNotNull(body)
        assertTrue(body.contains("\"pageIndex\":0"))
        assertTrue(body.contains("\"pageSize\":96"))
        assertTrue(body.contains("\"name\":\"createTime\""))
        assertTrue(body.contains("\"order\":\"desc\""))

        // 验证content-type
        assertEquals("application/json", parsedCurl.contentType)
    }

    @Test
    fun testParseSimpleGetCurlCommand() {
        val curlCommand = "curl https://example.com/api"
        val parsedCurl = CurlParser.parseCurl(curlCommand)

        assertEquals("GET", parsedCurl.method)
        assertEquals("https://example.com/api", parsedCurl.url)
    }

    @Test
    fun testParseCurlWithMethod() {
        val curlCommand = "curl -X PUT https://example.com/api"
        val parsedCurl = CurlParser.parseCurl(curlCommand)

        assertEquals("PUT", parsedCurl.method)
        assertEquals("https://example.com/api", parsedCurl.url)
    }

    // 测试CurlExecutor
    // 注意：这个测试需要网络连接，且依赖外部服务，仅作演示用途
    // @Test
    fun testCurlExecutor() {
        val curlCommand = "curl https://httpbin.org/get"
        val parsedCurl = CurlParser.parseCurl(curlCommand)
        
        try {
            val response = CurlExecutor.execute(parsedCurl)
            assertEquals(200, response.code)
            response.close()
        } catch (e: IOException) {
            // 网络请求可能失败，这在单元测试中是正常的
        }
    }
}