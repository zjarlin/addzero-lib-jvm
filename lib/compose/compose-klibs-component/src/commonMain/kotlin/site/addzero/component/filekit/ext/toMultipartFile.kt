package site.addzero.component.filekit.ext

import io.github.vinceglb.filekit.core.PlatformFile
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

suspend fun PlatformFile.toMultipartFile(): MultiPartFormDataContent {
    val fileBytes = readBytes()
    return MultiPartFormDataContent(
        formData {
            append("fileName", name)
            append(
                "file",
                fileBytes,
                Headers.build {
                    append(HttpHeaders.ContentType, guessContentType(name))
                    append(HttpHeaders.ContentDisposition, "filename=$name")
                },
            )
        },
    )
}

private fun guessContentType(fileName: String): String {
    return when (fileName.substringAfterLast('.', "").lowercase()) {
        "png" -> ContentType.Image.PNG.toString()
        "jpg", "jpeg" -> ContentType.Image.JPEG.toString()
        "gif" -> ContentType.Image.GIF.toString()
        "webp" -> "image/webp"
        "svg" -> "image/svg+xml"
        "pdf" -> ContentType.Application.Pdf.toString()
        "json" -> ContentType.Application.Json.toString()
        "txt", "log" -> ContentType.Text.Plain.toString()
        "csv" -> "text/csv"
        "md" -> "text/markdown"
        "mp3" -> "audio/mpeg"
        "mp4" -> "video/mp4"
        else -> ContentType.Application.OctetStream.toString()
    }
}
