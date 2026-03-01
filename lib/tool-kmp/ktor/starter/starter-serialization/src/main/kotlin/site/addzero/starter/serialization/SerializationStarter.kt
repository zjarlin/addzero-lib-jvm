package site.addzero.starter.serialization

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

@Single
class SerializationStarter : AppStarter {

    override fun Application.onInstall() {
        install(ContentNegotiation) {
            json(site.addzero.core.network.json.json)
        }
    }
}
