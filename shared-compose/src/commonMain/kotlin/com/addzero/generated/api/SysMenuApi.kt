package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysMenuController
 * 基础路径: /sysMenu
 */
interface SysMenuApi {

/**
 * getAllMenu
 * HTTP方法: POST
 * 路径: /sysMenu/getSysMenu
 * 参数:
 *   - params: kotlin.collections.Set<kotlin.String> (RequestBody)
 * 返回类型: kotlin.collections.Map<kotlin.String, com.addzero.entity.sys.menu.SysMenuVO>
 */
    @POST("/sysMenu/getSysMenu")    suspend fun getAllMenu(
        @Body params: kotlin.collections.Set<kotlin.String>
    ): kotlin.collections.Map<kotlin.String, com.addzero.entity.sys.menu.SysMenuVO>

}