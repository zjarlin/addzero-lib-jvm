package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import com.addzero.entity.PageResult

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysUserController
 * 基础路径: /sysUser
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface SysUserApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /sysUser/page
 * 返回类型: com.addzero.entity.PageResult<com.addzero.model.entity.SysUser>
 */
    @GET("/sysUser/page")    suspend fun page(): com.addzero.entity.PageResult<com.addzero.model.entity.SysUser>

/**
 * save
 * HTTP方法: POST
 * 路径: /sysUser/save
 * 返回类型: kotlin.Unit
 */
    @POST("/sysUser/save")    suspend fun save(): kotlin.Unit

/**
 * tree
 * HTTP方法: GET
 * 路径: /sysUser/tree
 * 参数:
 *   - keyword: kotlin.String (RequestParam)
 * 返回类型: kotlin.collections.List<com.addzero.model.entity.SysUser>
 */
    @GET("/sysUser/tree")    suspend fun tree(
        @Query("keyword") keyword: kotlin.String
    ): kotlin.collections.List<com.addzero.model.entity.SysUser>

}