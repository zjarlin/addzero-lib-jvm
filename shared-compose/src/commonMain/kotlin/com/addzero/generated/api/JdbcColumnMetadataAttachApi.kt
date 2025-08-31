package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.JdbcColumnMetadataAttachController
 * 基础路径: /jdbcColumnMetadataAttach
 */
interface JdbcColumnMetadataAttachApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /jdbcColumnMetadataAttach/page
 * 返回类型: kotlin.Unit
 */
    @GET("/jdbcColumnMetadataAttach/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /jdbcColumnMetadataAttach/save
 * 返回类型: kotlin.Unit
 */
    @POST("/jdbcColumnMetadataAttach/save")    suspend fun save(): kotlin.Unit

}