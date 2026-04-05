package site.addzero.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import site.addzero.component.toast.ToastManager

/**
 * HttpResponse 事件总线消费
 */
@Composable
fun EventBusConsumer() {
    LaunchedEffect(Unit) {
        EventBus.consumer<HttpResponse> {
            val feedback = resolveHttpFeedback(this)
            val message = feedback.message
            when (feedback.status) {
                OK -> {
                    if (message.isNotBlank()) {
                        ToastManager.info(message)
                    }
                }

                Unauthorized -> {
                    show(message)
                }

                Forbidden -> {
                    ToastManager.error("无权限访问")
                }

                BadRequest -> {
                    show(message)
                }

                HttpStatusCode(499, "Need More Info") -> {
                    ToastManager.info("请补充资料后再操作")
                }

                else -> {
                    show(message)
                }
            }
        }
    }
}

private data class HttpFeedback(
    val status: HttpStatusCode,
    val message: String,
)

private suspend fun resolveHttpFeedback(response: HttpResponse): HttpFeedback {
    val bodyText = runCatching {
        response.bodyAsText()
    }.getOrDefault("")
    val statusCode = extractStatusCode(bodyText) ?: response.status.value
    val message = extractMessage(bodyText).ifBlank {
        response.status.description.ifBlank {
            "请求失败"
        }
    }
    return HttpFeedback(
        status = HttpStatusCode.fromValue(statusCode),
        message = message,
    )
}

private fun extractStatusCode(bodyText: String): Int? {
    val matched = STATUS_CODE_REGEX.find(bodyText)?.groupValues?.getOrNull(1)
    return matched?.toIntOrNull()
}

private fun extractMessage(bodyText: String): String {
    val matched = MESSAGE_REGEX.find(bodyText)?.groupValues?.getOrNull(1).orEmpty()
    return matched
        .replace("\\\\n", "\n")
        .replace("\\\\\"", "\"")
        .trim()
}

private suspend fun show(message: String) {
    if (message.isNotBlank()) {
        ToastManager.warning(message)
    }
}

private val STATUS_CODE_REGEX = Regex("\"(?:code|status)\"\\s*:\\s*(\\d+)")
private val MESSAGE_REGEX = Regex("\"(?:message|detail|title)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")
