package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import com.addzero.model.entity.SysDept
import com.addzero.generated.isomorphic.SysDeptIso

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysDeptController
 * 基础路径: /sysDept
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface SysDeptApi {

/**
 * save
 * HTTP方法: POST
 * 路径: /sysDept/save
 * 参数:
 *   - dept: com.addzero.generated.isomorphic.SysDeptIso (RequestBody)
 * 返回类型: com.addzero.model.entity.SysDept
 */
    @POST("/sysDept/save")    suspend fun save(
        @Body dept: com.addzero.generated.isomorphic.SysDeptIso
    ): com.addzero.model.entity.SysDept

/**
 * get
 * HTTP方法: GET
 * 路径: /sysDept/get/{id}
 * 参数:
 *   - id: kotlin.Long (Query)
 * 返回类型: com.addzero.generated.isomorphic.SysDeptIso
 */
    @GET("/sysDept/get/{id}")    suspend fun get(
        @Query("id") id: kotlin.Long
    ): com.addzero.generated.isomorphic.SysDeptIso

/**
 * delete
 * HTTP方法: DELETE
 * 路径: /sysDept/delete
 * 参数:
 *   - id: kotlin.Long (Query)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/sysDept/delete")    suspend fun delete(
        @Query("id") id: kotlin.Long
    ): kotlin.Unit

/**
 * tree
 * HTTP方法: GET
 * 路径: /sysDept/tree
 * 参数:
 *   - keyword: kotlin.String (RequestParam)
 * 返回类型: kotlin.collections.List<com.addzero.model.entity.SysDept>
 */
    @GET("/sysDept/tree")    suspend fun tree(
        @Query("keyword") keyword: kotlin.String
    ): kotlin.collections.List<com.addzero.model.entity.SysDept>

}