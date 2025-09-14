package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.web.modules.controller.SysColumnConfigController
 * 基础路径: /sysColumnConfig
 */
interface SysColumnConfigApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /sysColumnConfig/page
 * 返回类型: kotlin.Unit
 */
    @GET("/sysColumnConfig/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /sysColumnConfig/save
 * 返回类型: kotlin.Unit
 */
    @POST("/sysColumnConfig/save")    suspend fun save(): kotlin.Unit

}