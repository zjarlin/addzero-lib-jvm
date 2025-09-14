package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.web.modules.controller.SysAreaController
 * 基础路径: /sysArea
 */
interface SysAreaApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /sysArea/page
 * 返回类型: kotlin.Unit
 */
    @GET("/sysArea/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /sysArea/save
 * 返回类型: kotlin.Unit
 */
    @POST("/sysArea/save")    suspend fun save(): kotlin.Unit

}