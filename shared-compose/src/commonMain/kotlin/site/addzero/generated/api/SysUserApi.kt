package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.entity.PageResult

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.web.modules.controller.SysUserController
 * 基础路径: /sysUser
 */
interface SysUserApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /sysUser/page
 * 返回类型: site.addzero.entity.PageResult<site.addzero.generated.isomorphic.SysUserIso>
 */
    @GET("/sysUser/page")    suspend fun page(): site.addzero.entity.PageResult<site.addzero.generated.isomorphic.SysUserIso>

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
 * 返回类型: kotlin.collections.List<site.addzero.generated.isomorphic.SysUserIso>
 */
    @GET("/sysUser/tree")    suspend fun tree(
        @Query("keyword") keyword: kotlin.String
    ): kotlin.collections.List<site.addzero.generated.isomorphic.SysUserIso>

}