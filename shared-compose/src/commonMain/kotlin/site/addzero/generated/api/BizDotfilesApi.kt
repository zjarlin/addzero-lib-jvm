package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.web.modules.controller.BizDotfilesController
 * 基础路径: /bizDotfiles
 */
interface BizDotfilesApi {

/**
 * page
 * HTTP方法: GET
 * 路径: /bizDotfiles/page
 * 返回类型: kotlin.Unit
 */
    @GET("/bizDotfiles/page")    suspend fun page(): kotlin.Unit

/**
 * save
 * HTTP方法: POST
 * 路径: /bizDotfiles/save
 * 返回类型: kotlin.Unit
 */
    @POST("/bizDotfiles/save")    suspend fun save(): kotlin.Unit

/**
 * tree
 * HTTP方法: GET
 * 路径: /bizDotfiles/tree
 * 参数:
 *   - keyword: kotlin.String (RequestParam)
 * 返回类型: kotlin.collections.List<site.addzero.generated.isomorphic.BizDotfilesIso>
 */
    @GET("/bizDotfiles/tree")    suspend fun tree(
        @Query("keyword") keyword: kotlin.String
    ): kotlin.collections.List<site.addzero.generated.isomorphic.BizDotfilesIso>

}