package site.addzero.starter.statuspages

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

@Single
class StatusPagesStarter : AppStarter {

    override fun Application.onInstall() {
        install(StatusPages) {
            exception<IllegalArgumentException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, cause.message ?: "Bad Request"))
                call.application.log.warn("Bad Request: ${cause.message}", cause)
            }
            exception<SerializationException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "Malformed JSON: ${cause.message}"))
                call.application.log.error("Serialization error: ${cause.message}", cause)
            }
            exception<Throwable> { call, cause ->
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(500, cause.message ?: "Internal Server Error")
                )
                call.application.log.error("Unhandled exception: ${cause.message}", cause)
            }
        }
    }
}
