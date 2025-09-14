package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.web.modules.controller.SysJdbcController
 * 基础路径: /sysJdbc
 */
interface SysJdbcApi {

/**
 * getJdbcMetaData
 * HTTP方法: GET
 * 路径: /sysJdbc/getJdbcMetaData
 * 返回类型: kotlin.Unit
 */
    @GET("/sysJdbc/getJdbcMetaData")    suspend fun getJdbcMetaData(): kotlin.Unit

}