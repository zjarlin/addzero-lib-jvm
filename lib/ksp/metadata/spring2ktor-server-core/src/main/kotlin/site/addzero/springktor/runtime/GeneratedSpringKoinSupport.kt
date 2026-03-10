package site.addzero.springktor.runtime

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import org.koin.core.module.Module
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.mp.KoinPlatformTools

fun Application.installOrLoadGeneratedSpringModule(module: Module) {
    val existingKoin = runCatching { getKoin() }.getOrNull()
    if (existingKoin == null) {
        install(Koin) {
            slf4jLogger()
            modules(module)
        }
        return
    }

    existingKoin.loadModules(listOf(module))
}

inline fun <reified T : Any> resolveGeneratedSpringGlobalBean(): T {
    return KoinPlatformTools.defaultContext().get().get()
}

inline fun <reified T : Any> ApplicationCall.resolveGeneratedSpringBean(): T {
    val applicationKoin = runCatching { application.getKoin() }.getOrNull()
    if (applicationKoin != null) {
        return applicationKoin.get()
    }

    return resolveGeneratedSpringGlobalBean()
}
