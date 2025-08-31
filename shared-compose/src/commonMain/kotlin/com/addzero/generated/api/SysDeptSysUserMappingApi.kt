package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysDeptSysUserMappingController
 * 基础路径: /sysDeptSysUserMapping
 */
interface SysDeptSysUserMappingApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /sysDeptSysUserMapping/page
 * 返回类型: kotlin.Unit
 */
    @GET("/sysDeptSysUserMapping/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /sysDeptSysUserMapping/save
 * 返回类型: kotlin.Unit
 */
    @POST("/sysDeptSysUserMapping/save")    suspend fun save(): kotlin.Unit

}