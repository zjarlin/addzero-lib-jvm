package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import com.addzero.model.entity.SysDict
import com.addzero.model.entity.SysDictItem

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysDictController
 * 基础路径: /sysDict
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface SysDictApi {

/**
 * querydict
 * HTTP方法: GET
 * 路径: /sysDict/querydict
 * 参数:
 *   - keyword: kotlin.String (RequestParam)
 * 返回类型: kotlin.collections.List<com.addzero.model.entity.SysDict>
 */
    @GET("/sysDict/querydict")    suspend fun querydict(
        @Query("keyword") keyword: kotlin.String
    ): kotlin.collections.List<com.addzero.model.entity.SysDict>

/**
 * saveDict
 * HTTP方法: POST
 * 路径: /sysDict/saveDict
 * 参数:
 *   - vO: com.addzero.model.entity.SysDict (RequestBody)
 * 返回类型: com.addzero.model.entity.SysDict
 */
    @POST("/sysDict/saveDict")    suspend fun saveDict(
        @Body vO: com.addzero.model.entity.SysDict
    ): com.addzero.model.entity.SysDict

/**
 * saveDictItem
 * HTTP方法: POST
 * 路径: /sysDict/saveDictItem
 * 参数:
 *   - impl: com.addzero.model.entity.SysDictItem (RequestBody)
 * 返回类型: com.addzero.model.entity.SysDictItem
 */
    @POST("/sysDict/saveDictItem")    suspend fun saveDictItem(
        @Body impl: com.addzero.model.entity.SysDictItem
    ): com.addzero.model.entity.SysDictItem

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
 * 返回类型: kotlin.collections.List<com.addzero.model.entity.SysDict>
 */
    @GET("/sysDict/tree")    suspend fun tree(
        @Query("keyword") keyword: kotlin.String
    ): kotlin.collections.List<com.addzero.model.entity.SysDict>

}