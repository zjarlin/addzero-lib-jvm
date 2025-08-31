package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.JdbcTableMetadataAttachController
 * 基础路径: /jdbcTableMetadataAttach
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface JdbcTableMetadataAttachApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /jdbcTableMetadataAttach/page
 * 返回类型: kotlin.Unit
 */
    @GET("/jdbcTableMetadataAttach/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /jdbcTableMetadataAttach/save
 * 返回类型: kotlin.Unit
 */
    @POST("/jdbcTableMetadataAttach/save")    suspend fun save(): kotlin.Unit

}