package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import site.addzero.entity.ai.EmbDTO
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.ai.document.DocumentController
 * 基础路径: /document
 */
interface DocumentApi {

/**
 * embedding
 * HTTP方法: POST
 * 路径: /documentembedding
 * 参数:
 *   - file: io.ktor.client.request.forms.MultiPartFormDataContent (RequestPart)
 * 返回类型: kotlin.collections.MutableList<kotlin.String>?
 */
    @POST("/documentembedding")    suspend fun embedding(
        @Body file: io.ktor.client.request.forms.MultiPartFormDataContent
    ): kotlin.collections.MutableList<kotlin.String>?

/**
 * embeddingText
 * HTTP方法: POST
 * 路径: /documentembeddingText
 * 参数:
 *   - embDTO: site.addzero.entity.ai.EmbDTO (RequestBody)
 * 返回类型: kotlin.collections.MutableList<kotlin.String>?
 */
    @POST("/documentembeddingText")    suspend fun embeddingText(
        @Body embDTO: site.addzero.entity.ai.EmbDTO
    ): kotlin.collections.MutableList<kotlin.String>?

/**
 * query
 * HTTP方法: GET
 * 路径: /documentquery
 * 参数:
 *   - query: kotlin.String (RequestParam)
 * 返回类型: kotlin.String
 */
    @GET("/documentquery")    suspend fun query(
        @Query("query") query: kotlin.String
    ): kotlin.String

}