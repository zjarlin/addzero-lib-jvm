package site.addzero.configcenter.ktor

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import site.addzero.configcenter.runtime.ConfigCenterRuntimeKoinModule

@Module(includes = [ConfigCenterRuntimeKoinModule::class])
@ComponentScan("site.addzero.configcenter.ktor")
class ConfigCenterKtorKoinModule

