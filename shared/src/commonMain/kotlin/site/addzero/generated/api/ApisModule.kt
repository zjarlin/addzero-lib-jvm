package site.addzero.kcloud.plugins.codegencontext.api.external

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.codegencontext.api.external.*

/**
 * 为 controller2api 生成的接口补充可自注册的 Koin 注入入口。
 */
@Module
@Configuration
class ApisModule {
    @Single
    fun codegenContextApi(): CodegenContextApi {
        return Apis.codegenContextApi
    }

    @Single
    fun codegenTemplateApi(): CodegenTemplateApi {
        return Apis.codegenTemplateApi
    }
}