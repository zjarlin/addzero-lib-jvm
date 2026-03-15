package sample

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.httpMethod
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondTextWriter
import io.ktor.utils.io.toByteArray
import kotlinx.serialization.Serializable
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import site.addzero.springktor.runtime.SpringRouteResult
import site.addzero.springktor.runtime.springNoContent
import site.addzero.springktor.runtime.springNotFound

@Serializable
data class EchoRequest(
    val name: String,
)

@Serializable
data class EchoResponse(
    val message: String,
)

@Serializable
data class StatusPayload(
    val code: Int,
    val message: String,
)

data class MagicNumber(
    val value: Int,
)

@Service
class GreetingService {
    fun prefix(): String {
        return "hello"
    }
}

@Configuration
open class SmokeConfig {
    @Bean
    open fun magicNumber(): MagicNumber {
        return MagicNumber(7)
    }
}

@RestController
@RequestMapping("/controller")
class SmokeController(
    private val greetingService: GreetingService,
    private val magicNumber: MagicNumber,
) {
    @GetMapping("/{id}")
    fun controllerHello(@PathVariable id: Int): String {
        return "${greetingService.prefix()}-$id-${magicNumber.value}"
    }
}

@GetMapping("/top/{id}")
suspend fun topHello(
    @PathVariable id: Int,
    name: String,
    @RequestHeader("X-Trace") trace: String,
    call: ApplicationCall,
): String {
    return "$id:$name:$trace:${call.request.httpMethod.value}"
}

@PostMapping("/echo")
suspend fun echo(@RequestBody body: EchoRequest): EchoResponse {
    return EchoResponse(message = "echo:${body.name}")
}

@GetMapping("/status/not-found")
fun notFound(): SpringRouteResult<StatusPayload> {
    return springNotFound(
        StatusPayload(
            code = 404,
            message = "missing",
        )
    )
}

@GetMapping("/status/no-content")
fun noContent(): SpringRouteResult<Nothing> {
    return springNoContent()
}

@PostMapping("/upload")
suspend fun upload(
    @RequestPart("file") file: MultipartFile,
    @RequestPart("note") note: String,
): String {
    return "${file.originalFilename}:${file.bytes.size}:$note"
}

@PostMapping("/uploads")
suspend fun uploads(@RequestPart("files") files: List<MultipartFile>): String {
    return files.joinToString("|") { file ->
        "${file.originalFilename}:${file.bytes.size}"
    }
}

@PostMapping("/upload-native")
suspend fun uploadNative(call: ApplicationCall): String {
    val multipart = call.receiveMultipart()
    var fileName = "missing"
    var fileSize = 0
    var note = ""
    while (true) {
        val part = multipart.readPart() ?: break
        try {
            when (part) {
                is PartData.FileItem -> if (part.name == "file") {
                    fileName = part.originalFileName ?: "unnamed"
                    fileSize = part.provider().toByteArray().size
                }

                is PartData.FormItem -> if (part.name == "note") {
                    note = part.value
                }

                else -> {
                }
            }
        } finally {
            part.dispose()
        }
    }
    return "$fileName:$fileSize:$note"
}

@GetMapping("/download/{name}")
suspend fun download(
    @PathVariable name: String,
    call: ApplicationCall,
) {
    call.response.header(HttpHeaders.ContentDisposition, """attachment; filename="$name.txt"""")
    call.respondBytes(
        bytes = "download:$name".encodeToByteArray(),
        contentType = ContentType.Text.Plain,
    )
}

@GetMapping("/sse/messages")
suspend fun sseMessages(call: ApplicationCall) {
    call.respondTextWriter(contentType = ContentType.Text.EventStream) {
        write("event:message\n")
        write("data:hello\n\n")
        flush()
        write("data:world\n\n")
    }
}
