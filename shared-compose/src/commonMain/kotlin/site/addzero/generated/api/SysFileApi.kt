package site.addzero.generated.api

import de.jensklingenberg.ktorfit.http.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import site.addzero.entity.FileUploadResponse
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Ktorfit接口 - 由KSP自动生成
 * 原始Controller: site.addzero.web.modules.controller.SysFileController
 * 基础路径: /sys/file
 */
interface SysFileApi {

/**
 * upload
 * HTTP方法: POST
 * 路径: /sys/file/upload
 * 参数:
 *   - file: io.ktor.client.request.forms.MultiPartFormDataContent (RequestPart)
 * 返回类型: kotlin.String
 */
    @POST("/sys/file/upload")    suspend fun upload(
        @Body file: io.ktor.client.request.forms.MultiPartFormDataContent
    ): kotlin.String

/**
 * download
 * HTTP方法: POST
 * 路径: /sys/file/download
 * 参数:
 *   - fileId: kotlin.String (Query)
 * 返回类型: kotlin.String
 */
    @POST("/sys/file/download")    suspend fun download(
        @Query("fileId") fileId: kotlin.String
    ): kotlin.String

/**
 * queryProgress
 * HTTP方法: GET
 * 路径: /sys/filequeryProgress
 * 参数:
 *   - redisKey: kotlin.String (Query)
 * 返回类型: site.addzero.entity.FileUploadResponse
 */
    @GET("/sys/filequeryProgress")    suspend fun queryProgress(
        @Query("redisKey") redisKey: kotlin.String
    ): site.addzero.entity.FileUploadResponse

}