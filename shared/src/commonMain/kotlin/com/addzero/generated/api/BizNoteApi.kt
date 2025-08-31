package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.BizNoteController
 * 基础路径: /bizNote
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface BizNoteApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /bizNote/page
 * 返回类型: kotlin.Unit
 */
    @GET("/bizNote/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /bizNote/save
 * 返回类型: kotlin.Unit
 */
    @POST("/bizNote/save")    suspend fun save(): kotlin.Unit

}