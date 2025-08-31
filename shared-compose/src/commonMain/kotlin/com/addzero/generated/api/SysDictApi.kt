package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import com.addzero.generated.isomorphic.SysDictIso
import com.addzero.generated.isomorphic.SysDictItemIso

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysDictController
 * 基础路径: /sysDict
 */
interface SysDictApi {

/**
 * querydict
 * HTTP方法: GET
 * 路径: /sysDict/querydict
 * 参数:
 *   - keyword: kotlin.String (RequestParam)
 * 返回类型: kotlin.collections.List<com.addzero.generated.isomorphic.SysDictIso>
 */
    @GET("/sysDict/querydict")    suspend fun querydict(
        @Query("keyword") keyword: kotlin.String
    ): kotlin.collections.List<com.addzero.generated.isomorphic.SysDictIso>

/**
 * saveDict
 * HTTP方法: POST
 * 路径: /sysDict/saveDict
 * 参数:
 *   - vO: com.addzero.generated.isomorphic.SysDictIso (RequestBody)
 * 返回类型: com.addzero.generated.isomorphic.SysDictIso
 */
    @POST("/sysDict/saveDict")    suspend fun saveDict(
        @Body vO: com.addzero.generated.isomorphic.SysDictIso
    ): com.addzero.generated.isomorphic.SysDictIso

/**
 * saveDictItem
 * HTTP方法: POST
 * 路径: /sysDict/saveDictItem
 * 参数:
 *   - impl: com.addzero.generated.isomorphic.SysDictItemIso (RequestBody)
 * 返回类型: com.addzero.generated.isomorphic.SysDictItemIso
 */
    @POST("/sysDict/saveDictItem")    suspend fun saveDictItem(
        @Body impl: com.addzero.generated.isomorphic.SysDictItemIso
    ): com.addzero.generated.isomorphic.SysDictItemIso

/**
 * deleteDictItem
 * HTTP方法: GET
 * 路径: /sysDict/deleteDictItem
 * 参数:
 *   - lng: kotlin.Long (RequestParam)
 * 返回类型: kotlin.Unit
 */
    @GET("/sysDict/deleteDictItem")    suspend fun deleteDictItem(
        @Query("lng") lng: kotlin.Long
    ): kotlin.Unit

/**
 * deleteDict
 * HTTP方法: GET
 * 路径: /sysDict/deleteDict
 * 参数:
 *   - lng: kotlin.Long (Query)
 * 返回类型: kotlin.Unit
 */
    @GET("/sysDict/deleteDict")    suspend fun deleteDict(
        @Query("lng") lng: kotlin.Long
    ): kotlin.Unit

/**
 * tree
 * HTTP方法: GET
 * 路径: /sysDict/tree
 * 参数:
 *   - keyword: kotlin.String (RequestParam)
 * 返回类型: kotlin.collections.List<com.addzero.generated.isomorphic.SysDictIso>
 */
    @GET("/sysDict/tree")    suspend fun tree(
        @Query("keyword") keyword: kotlin.String
    ): kotlin.collections.List<com.addzero.generated.isomorphic.SysDictIso>

}