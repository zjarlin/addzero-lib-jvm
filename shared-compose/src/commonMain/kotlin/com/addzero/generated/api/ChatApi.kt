package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.ai.chat.ChatController
 * 基础路径: /chat
 */
interface ChatApi {

/**
 * gettools
 * HTTP方法: GET
 * 路径: /chatgettools
 * 返回类型: kotlin.collections.List<kotlin.Any>
 */
    @GET("/chatgettools")    suspend fun gettools(): kotlin.collections.List<kotlin.Any>

/**
 * jdaoisdj
 * HTTP方法: GET
 * 路径: /chat/dbatest
 * 返回类型: com.addzero.entity.ai.FormDTO?
 */
    @GET("/chat/dbatest")    suspend fun jdaoisdj(): com.addzero.entity.ai.FormDTO?

/**
 * chat
 * HTTP方法: POST
 * 路径: /chat/chatClient
 * 参数:
 *   - text: kotlin.String (Query)
 * 返回类型: kotlin.String?
 */
    @POST("/chat/chatClient")    suspend fun chat(
        @Query("text") text: kotlin.String
    ): kotlin.String?

}