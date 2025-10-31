package site.addzero.util

import okhttp3.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach

class CurlExecutorTest {


    @Test
    fun testExecuteWithHeaders() {
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
        """.trimIndent()

        val response = CurlExecutor.execute(curlCommand)
        val string = response.body?.string()
        println(string)


    }

}
