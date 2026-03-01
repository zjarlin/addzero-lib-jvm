package site.addzero.starter.banner

import io.ktor.server.application.*
import org.koin.core.annotation.Single
import site.addzero.ktor.banner.Banner
import site.addzero.starter.AppStarter

@Single
class BannerStarter : AppStarter {
    override val order: Int get() = 30

    override fun Application.onInstall() {
        val config = environment.config
        val bannerConfig = config.config("banner")
        val text = bannerConfig?.propertyOrNull("text")?.getString() ?: "APP"
        val subtitle = bannerConfig?.propertyOrNull("subtitle")?.getString() ?: ""
        install(Banner) {
            this.text = text
            this.subtitle = subtitle
        }
    }
}
