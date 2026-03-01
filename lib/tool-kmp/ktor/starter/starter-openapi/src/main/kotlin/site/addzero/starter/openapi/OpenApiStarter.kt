package site.addzero.starter.openapi

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

@Single
class OpenApiStarter : AppStarter {

    override fun Application.enable(): Boolean {
        val enabled = environment.config.propertyOrNull("openapi.enabled")?.getAs<Boolean>()
        return  enabled ?:true
    }

    override fun Application.onInstall() {
        val config = environment.config.config("openapi")
        val path = config?.propertyOrNull("path")?.getString() ?: "/swagger"
        val spec = config?.propertyOrNull("spec")?.getString() ?: "openapi/documentation.yaml"
        routing {
            swaggerUI(path, spec)
        }
    }
}
