package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.web.modules.controller.SysAiPromptController
 * 基础路径: /sysAiPrompt
 */
interface SysAiPromptApi {

/**
 * getPrompts
 * HTTP方法: GET
 * 路径: /sysAiPrompt/getPrompts
 * 返回类型: kotlin.collections.List<site.addzero.generated.isomorphic.SysAiPromptIso>
 */
    @GET("/sysAiPrompt/getPrompts")    suspend fun getPrompts(): kotlin.collections.List<site.addzero.generated.isomorphic.SysAiPromptIso>

}