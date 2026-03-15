package sample

import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import sample.generated.springktor.registerGeneratedSpringRoutes

class GeneratedSpringApplicationTest {
    @Test
    fun topLevelAndControllerRoutesWork() = testApplication {
        application {
            installSmokeDependencies()
            this@application.install(ContentNegotiation) {
                json()
            }
            routing {
                registerGeneratedSpringRoutes()
            }
        }

        val topResponse = client.get("/spring/top/42?name=alice") {
            header("X-Trace", "trace-1")
        }
        assertEquals("42:alice:trace-1:GET", topResponse.bodyAsText())

        val controllerResponse = client.get("/controller/9")
        assertEquals("hello-9-7", controllerResponse.bodyAsText())
    }

    @Test
    fun requestBodyAndMultipartBindingsWork() = testApplication {
        application {
            installSmokeDependencies()
            this@application.install(ContentNegotiation) {
                json()
            }
            routing {
                registerGeneratedSpringRoutes()
            }
        }

        val echoResponse = client.post("/spring/echo") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"neo"}""")
        }
        assertEquals("""{"message":"echo:neo"}""", echoResponse.bodyAsText())

        val uploadResponse = client.post("/spring/upload") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", "abc".toByteArray(), headers = io.ktor.http.Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                            append(HttpHeaders.ContentDisposition, """filename="note.txt"""")
                        })
                        append("note", "memo")
                    }
                )
            )
        }
        assertEquals("note.txt:3:memo", uploadResponse.bodyAsText())

        val uploadsResponse = client.post("/spring/uploads") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("files", "abc".toByteArray(), headers = io.ktor.http.Headers.build {
                            append(HttpHeaders.ContentDisposition, """filename="a.txt"""")
                        })
                        append("files", "hello".toByteArray(), headers = io.ktor.http.Headers.build {
                            append(HttpHeaders.ContentDisposition, """filename="b.txt"""")
                        })
                    }
                )
            )
        }
        assertEquals("a.txt:3|b.txt:5", uploadsResponse.bodyAsText())

        val uploadNativeResponse = client.post("/spring/upload-native") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", "xyz".toByteArray(), headers = io.ktor.http.Headers.build {
                            append(HttpHeaders.ContentDisposition, """filename="native.txt"""")
                        })
                        append("note", "plain-ktor")
                    }
                )
            )
        }
        assertEquals("native.txt:3:plain-ktor", uploadNativeResponse.bodyAsText())
    }

    @Test
    fun directReturnAndGlobalExceptionHandlingWork() = testApplication {
        application {
            installSmokeDependencies()
            this@application.install(ContentNegotiation) {
                json()
            }
            this@application.install(StatusPages) {
                exception<NoSuchElementException> { call, _ ->
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        message = StatusPayload(
                            code = 404,
                            message = "missing",
                        )
                    )
                }
            }
            routing {
                registerGeneratedSpringRoutes()
            }
        }

        val notFoundResponse = client.get("/spring/status/not-found")
        assertEquals(HttpStatusCode.NotFound, notFoundResponse.status)
        assertEquals("""{"code":404,"message":"missing"}""", notFoundResponse.bodyAsText())

        val noContentResponse = client.get("/spring/status/no-content")
        assertEquals(HttpStatusCode.NoContent, noContentResponse.status)
        assertEquals("", noContentResponse.bodyAsText())
    }

    @Test
    fun fileDownloadSseAndManualWebSocketWork() = testApplication {
        application {
            installSmokeDependencies()
            this@application.install(ContentNegotiation) {
                json()
            }
            this@application.install(WebSockets)
            routing {
                registerGeneratedSpringRoutes()
                registerSmokeNativeKtorRoutes()
            }
        }

        val downloadResponse = client.get("/spring/download/report")
        assertEquals("download:report", downloadResponse.bodyAsText())
        assertEquals("""attachment; filename="report.txt"""", downloadResponse.headers[HttpHeaders.ContentDisposition])

        val sseResponse = client.get("/spring/sse/messages")
        assertTrue((sseResponse.contentType()?.toString() ?: "").startsWith(ContentType.Text.EventStream.toString()))
        assertEquals("event:message\ndata:hello\n\ndata:world\n\n", sseResponse.bodyAsText())

        val websocketClient = createClient {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }
        websocketClient.webSocket("/ws/echo") {
            send(Frame.Text("neo"))
            val firstFrame = incoming.receive() as Frame.Text
            assertEquals("echo:neo", firstFrame.readText())
        }
    }

    private fun installSmokeDependencies() {
        stopKoin()
        startKoin {
            modules(
                module {
                    single { GreetingService() }
                    single { MagicNumber(7) }
                    single { SmokeController(get(), get()) }
                }
            )
        }
    }
}
