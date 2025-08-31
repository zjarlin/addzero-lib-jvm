package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import com.addzero.model.entity.SysUser

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysUserCenterController
 * 基础路径: /sysUser
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface SysUserCenterApi {

/**
 * getCurrentUser
 * HTTP方法: GET
 * 路径: /sysUser/getCurrentUser
 * 返回类型: com.addzero.model.entity.SysUser
 */
    @GET("/sysUser/getCurrentUser")    suspend fun getCurrentUser(): com.addzero.model.entity.SysUser

/**
 * updatePassword
 * HTTP方法: POST
 * 路径: /sysUser/updatePassword
 * 参数:
 *   - newPassword: kotlin.String (RequestBody)
 * 返回类型: kotlin.Boolean
 */
    @POST("/sysUser/updatePassword")    suspend fun updatePassword(
        @Body newPassword: kotlin.String
    ): kotlin.Boolean

/**
 * logout
 * HTTP方法: POST
 * 路径: /sysUser/logout
 * 返回类型: kotlin.Boolean
 */
    @POST("/sysUser/logout")    suspend fun logout(): kotlin.Boolean

}