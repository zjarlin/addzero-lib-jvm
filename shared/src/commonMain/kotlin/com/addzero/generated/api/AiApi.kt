package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import com.addzero.entity.VisionRequest

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.AiController
 * 基础路径: /ai
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface AiApi {

/**
 * getDeepSeekBalance
 * HTTP方法: GET
 * 路径: /aigetDeepSeekBalance
 * 返回类型: kotlin.String
 */
    @GET("/aigetDeepSeekBalance")    suspend fun getDeepSeekBalance(): kotlin.String

/**
 * chatVision
 * HTTP方法: POST
 * 路径: /ai/chatVision
 * 参数:
 *   - visionRequest: com.addzero.entity.VisionRequest (RequestBody)
 * 返回类型: kotlin.String
 */
    @POST("/ai/chatVision")    suspend fun chatVision(
        @Body visionRequest: com.addzero.entity.VisionRequest
    ): kotlin.String

/**
 * genVideo
 * HTTP方法: POST
 * 路径: /ai/genVideo
 * 参数:
 *   - visionRequest: com.addzero.entity.VisionRequest (RequestBody)
 * 返回类型: kotlin.String
 */
    @POST("/ai/genVideo")    suspend fun genVideo(
        @Body visionRequest: com.addzero.entity.VisionRequest
    ): kotlin.String

/**
 * getAiVideoProgres
 * HTTP方法: GET
 * 路径: /ai/getAiVideoProgres
 * 参数:
 *   - taskkId: kotlin.String (Query)
 * 返回类型: kotlin.Unit
 */
    @GET("/ai/getAiVideoProgres")    suspend fun getAiVideoProgres(
        @Query("taskkId") taskkId: kotlin.String
    ): kotlin.Unit

}