package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.entity.PageResult
import site.addzero.entity.low_table.CommonTableDaTaInputDTO
import site.addzero.generated.isomorphic.ProductIso

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.web.modules.controller.ProductController
 * 基础路径: /product
 */
interface ProductApi {

/**
 * page
 * HTTP方法: POST
 * 路径: /product/page
 * 参数:
 *   - commonTableDaTaInputDTO: site.addzero.entity.low_table.CommonTableDaTaInputDTO (RequestBody)
 * 返回类型: site.addzero.entity.PageResult<site.addzero.generated.isomorphic.ProductIso>
 */
    @POST("/product/page")    suspend fun page(
        @Body commonTableDaTaInputDTO: site.addzero.entity.low_table.CommonTableDaTaInputDTO
    ): site.addzero.entity.PageResult<site.addzero.generated.isomorphic.ProductIso>

/**
 * save
 * HTTP方法: POST
 * 路径: /product/save
 * 参数:
 *   - input: site.addzero.generated.isomorphic.ProductIso (RequestBody)
 * 返回类型: kotlin.Int
 */
    @POST("/product/save")    suspend fun save(
        @Body input: site.addzero.generated.isomorphic.ProductIso
    ): kotlin.Int

/**
 * edit
 * HTTP方法: PUT
 * 路径: /product/update
 * 参数:
 *   - e: site.addzero.generated.isomorphic.ProductIso (RequestBody)
 * 返回类型: kotlin.Int
 */
    @PUT("/product/update")    suspend fun edit(
        @Body e: site.addzero.generated.isomorphic.ProductIso
    ): kotlin.Int

/**
 * deleteByIds
 * HTTP方法: DELETE
 * 路径: /product/delete
 * 参数:
 *   - ids: kotlin.String (RequestParam)
 * 返回类型: kotlin.Int
 */
    @DELETE("/product/delete")    suspend fun deleteByIds(
        @Query("ids") ids: kotlin.String
    ): kotlin.Int

/**
 * saveBatch
 * HTTP方法: POST
 * 路径: /product/saveBatch
 * 参数:
 *   - input: kotlin.collections.List<site.addzero.generated.isomorphic.ProductIso> (RequestBody)
 * 返回类型: kotlin.Int
 */
    @POST("/product/saveBatch")    suspend fun saveBatch(
        @Body input: kotlin.collections.List<site.addzero.generated.isomorphic.ProductIso>
    ): kotlin.Int

/**
 * findById
 * HTTP方法: GET
 * 路径: /product/findById
 * 参数:
 *   - id: kotlin.String (Query)
 * 返回类型: site.addzero.generated.isomorphic.ProductIso
 */
    @GET("/product/findById")    suspend fun findById(
        @Query("id") id: kotlin.String
    ): site.addzero.generated.isomorphic.ProductIso

/**
 * loadTableConfig
 * HTTP方法: GET
 * 路径: /product/loadTableConfig
 * 返回类型: kotlin.Unit
 */
    @GET("/product/loadTableConfig")    suspend fun loadTableConfig(): kotlin.Unit

}