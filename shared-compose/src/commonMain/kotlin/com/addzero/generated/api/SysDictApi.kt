package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import com.addzero.generated.isomorphic.SysDictIso
import com.addzero.generated.isomorphic.SysDictItemIso
import com.addzero.entity.PageResult
import com.addzero.entity.low_table.CommonTableDaTaInputDTO

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

/**
 * page
 * HTTP方法: POST
 * 路径: /sysDict/page
 * 参数:
 *   - commonTableDaTaInputDTO: com.addzero.entity.low_table.CommonTableDaTaInputDTO (RequestBody)
 * 返回类型: com.addzero.entity.PageResult<com.addzero.generated.isomorphic.SysDictIso>
 */
    @POST("/sysDict/page")    suspend fun page(
        @Body commonTableDaTaInputDTO: com.addzero.entity.low_table.CommonTableDaTaInputDTO
    ): com.addzero.entity.PageResult<com.addzero.generated.isomorphic.SysDictIso>

/**
 * save
 * HTTP方法: POST
 * 路径: /sysDict/save
 * 参数:
 *   - input: com.addzero.generated.isomorphic.SysDictIso (RequestBody)
 * 返回类型: kotlin.Int
 */
    @POST("/sysDict/save")    suspend fun save(
        @Body input: com.addzero.generated.isomorphic.SysDictIso
    ): kotlin.Int

/**
 * edit
 * HTTP方法: PUT
 * 路径: /sysDict/update
 * 参数:
 *   - e: com.addzero.generated.isomorphic.SysDictIso (RequestBody)
 * 返回类型: kotlin.Int
 */
    @PUT("/sysDict/update")    suspend fun edit(
        @Body e: com.addzero.generated.isomorphic.SysDictIso
    ): kotlin.Int

/**
 * deleteByIds
 * HTTP方法: DELETE
 * 路径: /sysDict/delete
 * 参数:
 *   - ids: kotlin.String (RequestParam)
 * 返回类型: kotlin.Int
 */
    @DELETE("/sysDict/delete")    suspend fun deleteByIds(
        @Query("ids") ids: kotlin.String
    ): kotlin.Int

/**
 * saveBatch
 * HTTP方法: POST
 * 路径: /sysDict/saveBatch
 * 参数:
 *   - input: kotlin.collections.List<com.addzero.generated.isomorphic.SysDictIso> (RequestBody)
 * 返回类型: kotlin.Int
 */
    @POST("/sysDict/saveBatch")    suspend fun saveBatch(
        @Body input: kotlin.collections.List<com.addzero.generated.isomorphic.SysDictIso>
    ): kotlin.Int

/**
 * findById
 * HTTP方法: GET
 * 路径: /sysDict/findById
 * 参数:
 *   - id: kotlin.String (Query)
 * 返回类型: com.addzero.generated.isomorphic.SysDictIso
 */
    @GET("/sysDict/findById")    suspend fun findById(
        @Query("id") id: kotlin.String
    ): com.addzero.generated.isomorphic.SysDictIso

/**
 * loadTableConfig
 * HTTP方法: GET
 * 路径: /sysDict/loadTableConfig
 * 返回类型: kotlin.Unit
 */
    @GET("/sysDict/loadTableConfig")    suspend fun loadTableConfig(): kotlin.Unit

}