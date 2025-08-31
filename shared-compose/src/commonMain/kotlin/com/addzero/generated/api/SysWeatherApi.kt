package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysWeatherController
 * 基础路径: /sysWeather
 */
interface SysWeatherApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /sysWeather/page
 * 返回类型: kotlin.Unit
 */
    @GET("/sysWeather/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /sysWeather/save
 * 返回类型: kotlin.Unit
 */
    @POST("/sysWeather/save")    suspend fun save(): kotlin.Unit

}