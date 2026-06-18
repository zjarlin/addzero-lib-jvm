package site.addzero.web.infra.jackson

import com.fasterxml.jackson.databind.Module

fun interface AddzeroJacksonModuleProvider {
    fun modules(): List<Module>
}
