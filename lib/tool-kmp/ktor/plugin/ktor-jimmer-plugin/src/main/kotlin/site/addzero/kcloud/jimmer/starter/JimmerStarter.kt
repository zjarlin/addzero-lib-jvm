package site.addzero.kcloud.jimmer.starter

import io.ktor.server.application.*
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter
import site.addzero.kcloud.jimmer.plugin.JimmerPlugin

@Named("jimmerStarter")
@Single
class JimmerStarter : AppStarter {

    override fun Application.onInstall() {
        install(JimmerPlugin)
    }
}
