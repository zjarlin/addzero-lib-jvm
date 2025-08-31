package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import com.addzero.entity.SignInStatus
import com.addzero.generated.isomorphic.SysUserIso
import com.addzero.entity.SecondLoginResponse
import com.addzero.entity.SecondLoginDTO

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.LoginController
 * 基础路径: /sys/login
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface LoginApi {

/**
 * hasPermition
 * HTTP方法: GET
 * 路径: /sys/login/hasPermition
 * 参数:
 *   - code: kotlin.String (RequestParam)
 * 返回类型: kotlin.Boolean
 */
    @GET("/sys/login/hasPermition")    suspend fun hasPermition(
        @Query("code") code: kotlin.String
    ): kotlin.Boolean

/**
 * signin
 * HTTP方法: POST
 * 路径: /sys/login/signin
 * 参数:
 *   - loginRe: kotlin.String (RequestBody)
 * 返回类型: com.addzero.entity.SignInStatus
 */
    @POST("/sys/login/signin")    suspend fun signin(
        @Body loginRe: kotlin.String
    ): com.addzero.entity.SignInStatus

/**
 * signup
 * HTTP方法: POST
 * 路径: /sys/login/signup
 * 参数:
 *   - userRegFormState: com.addzero.generated.isomorphic.SysUserIso (RequestBody)
 * 返回类型: kotlin.Boolean
 */
    @POST("/sys/login/signup")    suspend fun signup(
        @Body userRegFormState: com.addzero.generated.isomorphic.SysUserIso
    ): kotlin.Boolean

/**
 * signinSecond
 * HTTP方法: POST
 * 路径: /sys/login/signinSecond
 * 参数:
 *   - secondLoginDTO: com.addzero.entity.SecondLoginDTO (RequestBody)
 * 返回类型: com.addzero.entity.SecondLoginResponse
 */
    @POST("/sys/login/signinSecond")    suspend fun signinSecond(
        @Body secondLoginDTO: com.addzero.entity.SecondLoginDTO
    ): com.addzero.entity.SecondLoginResponse

}