package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.ai.agent.dbdesign.DbDesignController
 * 基础路径: 
 */
interface DbDesignApi {

/**
 * dbdesign
 * HTTP方法: GET
 * 路径: dbdesign
 * 参数:
 *   - modelName: kotlin.String (RequestParam)
 *   - ques: kotlin.String (RequestParam)
 * 返回类型: com.addzero.entity.ai.FormDTO?
 */
    @GET("dbdesign")    suspend fun dbdesign(
        @Query("modelName") modelName: kotlin.String,
        @Query("ques") ques: kotlin.String
    ): com.addzero.entity.ai.FormDTO?

}