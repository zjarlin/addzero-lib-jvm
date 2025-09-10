package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysTableConfigController
 * 基础路径: /sysTableConfig
 */
interface SysTableConfigApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /sysTableConfig/page
 * 返回类型: kotlin.Unit
 */
    @GET("/sysTableConfig/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /sysTableConfig/save
 * 返回类型: kotlin.Unit
 */
    @POST("/sysTableConfig/save")    suspend fun save(): kotlin.Unit

}