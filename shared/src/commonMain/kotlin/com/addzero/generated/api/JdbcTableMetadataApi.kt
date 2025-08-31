package com.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: com.addzero.web.modules.controller.JdbcTableMetadataController
 * 基础路径: /jdbcTableMetadata
 * 输出目录: /Users/zjarlin/IdeaProjects/addzero/shared/src/commonMain/kotlin/com/addzero/generated/api
 */
 
interface JdbcTableMetadataApi {

/**
 * getTableMetadata
 * HTTP方法: GET
 * 路径: /jdbcTableMetadata/getTableMetadata
 * 参数:
 *   - tablename: kotlin.String (Query)
 * 返回类型: kotlin.collections.List<com.addzero.model.entity.JdbcTableMetadata>
 */
    @GET("/jdbcTableMetadata/getTableMetadata")    suspend fun getTableMetadata(
        @Query("tablename") tablename: kotlin.String
    ): kotlin.collections.List<com.addzero.model.entity.JdbcTableMetadata>

}