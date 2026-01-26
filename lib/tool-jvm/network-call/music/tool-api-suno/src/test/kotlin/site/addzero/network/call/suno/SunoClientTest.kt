package site.addzero.network.call.suno

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import site.addzero.network.call.suno.model.SunoMusicRequest

/**
 * Suno 客户端测试
 */
@DisplayName("Suno 客户端测试")
class SunoClientTest {

    private lateinit var client: SunoClient
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        // API Token is still required by the client constructor, but its value won't be used for calls to mockWebServer
        client = SunoClient("test-token", mockWebServer.url("/").toString())
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    @DisplayName("测试生成音乐")
    @Tag("unit")
    fun testGenerateMusic_success() {
        // Given
        val expectedTaskId = "task_test_id_123"
        val mockResponseJson = """
            {
              "code": 200,
              "message": "success",
              "data": "$expectedTaskId"
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(mockResponseJson))

        val request = SunoMusicRequest(
            prompt = "A happy song about a dog playing in the park",
            makeInstrumental = false,
            mv = "chirp-v5",
            tags = "pop, upbeat"
        )

        // When
        val taskId = client.generateMusic(request)

        // Then
        assertNotNull(taskId)
        assertEquals(expectedTaskId, taskId)

        // Verify the request sent to the mock server
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/suno/submit/music", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        assertEquals("Bearer test-token", recordedRequest.getHeader("Authorization"))
        assertEquals("application/json", recordedRequest.getHeader("Accept"))
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"))

        val expectedRequestBody = """
            {"prompt":"A happy song about a dog playing in the park","tags":"pop, upbeat","makeInstrumental":false,"model":"chirp-v5"}
        """.trimIndent()
        assertEquals(expectedRequestBody, recordedRequest.body.readUtf8())

        println("✓ 生成音乐任务成功，任务 ID: $taskId")
    }
}
