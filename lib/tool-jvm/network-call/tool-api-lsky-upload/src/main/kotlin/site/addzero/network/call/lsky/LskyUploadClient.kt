package site.addzero.network.call.lsky

import com.alibaba.fastjson2.JSON
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import site.addzero.network.call.lsky.model.*
import java.io.File
import java.util.concurrent.TimeUnit

class LskyUploadClient(
  private val config: LskyUploadConfig = LskyUploadConfig()
) {

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  companion object {
    @Volatile
    private var instance: LskyUploadClient? = null

    fun getInstance(config: LskyUploadConfig = LskyUploadConfig()): LskyUploadClient {
      return instance ?: synchronized(this) {
        instance ?: LskyUploadClient(config).also { instance = it }
      }
    }
  }

  fun uploadImage(filePath: String): LskyUploadResult {
    return uploadImage(File(filePath))
  }

  fun uploadImage(file: File): LskyUploadResult {
    val fileName = file.name
    val mimeType = getMimeType(fileName)

    val requestBody = MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("strategy_id", config.strategyId.toString())
      .addFormDataPart(
        "file",
        fileName,
        file.asRequestBody(mimeType.toMediaType())
      )
      .build()

    val request = Request.Builder()
      .url("${config.baseUrl}${config.uploadEndpoint}")
      .post(requestBody)
      .addHeader("accept", "application/json, text/javascript, */*; q=0.01")
      .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
      .addHeader("content-type", "multipart/form-data")
      .addHeader("origin", config.baseUrl)
      .addHeader("priority", "u=1, i")
      .addHeader("referer", "${config.baseUrl}${config.uploadEndpoint}")
      .addHeader("sec-ch-ua", "\"Microsoft Edge\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"")
      .addHeader("sec-ch-ua-mobile", "?0")
      .addHeader("sec-ch-ua-platform", "\"macOS\"")
      .addHeader("sec-fetch-dest", "empty")
      .addHeader("sec-fetch-mode", "cors")
      .addHeader("sec-fetch-site", "same-origin")
      .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0")
      .addHeader("x-requested-with", "XMLHttpRequest")
      .apply {
        config.xsrfToken?.let { addHeader("cookie", "XSRF-TOKEN=$it") }
        config.sessionId?.let { addHeader("cookie", "lsky_pro_session=$it") }
        config.csrfToken?.let { addHeader("x-csrf-token", it) }
      }
      .build()

    return try {
      client.newCall(request).execute().use { response ->
        val responseBody = response.body?.string()
          ?: throw RuntimeException("响应为空")

        if (!response.isSuccessful) {
          throw RuntimeException("上传失败: HTTP ${response.code} - $responseBody")
        }

        parseUploadResult(responseBody)
      }
    } catch (e: Exception) {
      LskyUploadResult(
        success = false,
        message = "上传异常: ${e.message}",
        data = null
      )
    }
  }

  fun uploadImages(filePaths: List<String>): List<LskyUploadResult> {
    return filePaths.map { uploadImage(it) }
  }

  fun uploadImages(files: List<File>): List<LskyUploadResult> {
    return files.map { uploadImage(it) }
  }

  private fun parseUploadResult(json: String): LskyUploadResult {
    return try {
      val jsonObject = JSON.parseObject(json)
      val success = jsonObject.getBoolean("success", false)
      val message = jsonObject.getString("message")
      val dataJson = jsonObject.getJSONObject("data")

      val data = if (dataJson != null) {
        LskyUploadData(
          url = dataJson.getString("url") ?: "",
          filename = dataJson.getString("filename") ?: "",
          extension = dataJson.getString("extension") ?: "",
          size = dataJson.getLong("size", 0L),
          mime = dataJson.getString("mime") ?: "",
          strategy_id = dataJson.getInteger("strategy_id", 1),
          created_at = dataJson.getString("created_at") ?: "",
          updated_at = dataJson.getString("updated_at") ?: ""
        )
      } else {
        null
      }

      LskyUploadResult(success, message, data)
    } catch (e: Exception) {
      LskyUploadResult(
        success = false,
        message = "解析响应失败: ${e.message}",
        data = null
      )
    }
  }

  private fun getMimeType(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
      "jpg", "jpeg" -> "image/jpeg"
      "png" -> "image/png"
      "gif" -> "image/gif"
      "webp" -> "image/webp"
      "bmp" -> "image/bmp"
      "svg" -> "image/svg+xml"
      "ico" -> "image/x-icon"
      else -> "application/octet-stream"
    }
  }

  fun updateConfig(newConfig: LskyUploadConfig) {
    @Suppress("SENSELESS_COMPARISON")
    if (this.config != newConfig) {
      @Suppress("KotlinConstantConditions")
      this.config.baseUrl = newConfig.baseUrl
      @Suppress("KotlinConstantConditions")
      this.config.uploadEndpoint = newConfig.uploadEndpoint
      @Suppress("KotlinConstantConditions")
      this.config.strategyId = newConfig.strategyId
      @Suppress("KotlinConstantConditions")
      this.config.xsrfToken = newConfig.xsrfToken
      @Suppress("KotlinConstantConditions")
      this.config.sessionId = newConfig.sessionId
      @Suppress("KotlinConstantConditions")
      this.config.csrfToken = newConfig.csrfToken
    }
  }
}