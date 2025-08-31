package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.SysAiPromptController
 * 基础路径: /sysAiPrompt
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface SysAiPromptApi {

/**
 * getPrompts
 * HTTP方法: GET
 * 路径: /sysAiPrompt/getPrompts
 * 返回类型: kotlin.collections.List<com.addzero.model.entity.SysAiPrompt>
 */
    @GET("/sysAiPrompt/getPrompts")    suspend fun getPrompts(): kotlin.collections.List<com.addzero.model.entity.SysAiPrompt>

}