package site.addzero.configcenter

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import site.addzero.core.network.json.json as sharedJson

/**
 * Kotlin Multiplatform client for Config Center.
 *
 * Create a client, login, checkout one namespace, then read or write typed configs.
 */
class ConfigCenter(
  baseUrl: String,
  private val httpClient: HttpClient = defaultHttpClient(),
  private val json: Json = sharedJson,
  private val token: String? = null,
  val username: String? = null,
  val namespace: String? = null,
  val commonNamespace: String? = null,
) {
  private val baseUrl = baseUrl.trimEnd('/')

  suspend fun login(username: String, password: String): ConfigCenter {
    require(username.isNotBlank()) { "username must not be blank" }
    require(password.isNotBlank()) { "password must not be blank" }
    val response = post<LoginPayload, LoginRequest>(
      path = "/api/v1/auth/login",
      body = LoginRequest(username = username, password = password),
      token = null,
    )
    return ConfigCenter(
      baseUrl = baseUrl,
      httpClient = httpClient,
      json = json,
      token = response.token,
      username = response.username,
      namespace = namespace,
      commonNamespace = commonNamespace,
    )
  }

  fun checkoutNamespace(
    namespace: String,
    commonNamespace: String? = inferCommonNamespace(namespace),
  ): ConfigCenter {
    require(namespace.isNotBlank()) { "namespace must not be blank" }
    commonNamespace?.let { require(it.isNotBlank()) { "commonNamespace must not be blank" } }
    return ConfigCenter(
      baseUrl = baseUrl,
      httpClient = httpClient,
      json = json,
      token = requireToken(),
      username = username,
      namespace = namespace.trim(),
      commonNamespace = commonNamespace?.trim(),
    )
  }

  suspend inline fun <reified T> get(key: String): T? {
    return get(key, serializer<T>())
  }

  suspend fun <T> get(key: String, serializer: KSerializer<T>): T? {
    val item = getItem(key) ?: return null
    return when (serializer) {
      String.serializer() -> serializer.cast(item.configValue)
      Int.serializer() -> serializer.cast(item.configValue.toIntOrNull())
      Long.serializer() -> serializer.cast(item.configValue.toLongOrNull())
      Double.serializer() -> serializer.cast(item.configValue.toDoubleOrNull())
      Float.serializer() -> serializer.cast(item.configValue.toFloatOrNull())
      Boolean.serializer() -> serializer.cast(item.configValue.toBooleanStrictOrNull())
      else -> json.decodeFromString(serializer, item.configValue)
    }
  }

  suspend fun getItem(key: String): ConfigItem? {
    val normalizedKey = requireKey(key)
    val item = getItemFromNamespace(requireNamespace(), normalizedKey)
    if (item != null) return item
    val fallbackNamespace = commonNamespace?.takeIf { it != requireNamespace() } ?: return null
    return getItemFromNamespace(fallbackNamespace, normalizedKey)
  }

  private suspend fun getItemFromNamespace(namespace: String, key: String): ConfigItem? {
    val response = getEnvelope<JsonElement>(
      buildUrl("/api/v1/config/detail") {
        parameters.append("namespace", namespace)
        parameters.append("key", key)
      },
    )
    if (!response.success) {
      if (response.message.contains("配置不存在")) return null
      throw ConfigCenterException(response.message)
    }
    val data = response.data ?: return null
    return json.decodeFromJsonElement(ConfigItem.serializer(), data)
  }

  suspend inline fun <reified T> set(
    key: String,
    value: T,
    description: String = "",
  ): ConfigItem {
    return set(
      key = key,
      value = value,
      serializer = serializer<T>(),
      description = description,
    )
  }

  suspend fun <T> set(
    key: String,
    value: T,
    serializer: KSerializer<T>,
    description: String = "",
  ): ConfigItem {
    val encoded = encodeValue(value, serializer)
    val payload = UpsertRequest(
      namespace = requireNamespace(),
      key = requireKey(key),
      value = encoded.value,
      valueType = encoded.valueType,
      description = description,
      enabled = true,
      updatedBy = username.orEmpty(),
    )
    return post<ConfigItem, UpsertRequest>(
      path = "/api/v1/config/upsert",
      body = payload,
      token = requireToken(),
    )
  }

  suspend fun setString(key: String, value: String, description: String = ""): ConfigItem {
    return set(key, value, String.serializer(), description)
  }

  suspend fun setNumber(key: String, value: Number, description: String = ""): ConfigItem {
    val payload = UpsertRequest(
      namespace = requireNamespace(),
      key = requireKey(key),
      value = value.toString(),
      valueType = "number",
      description = description,
      enabled = true,
      updatedBy = username.orEmpty(),
    )
    return post<ConfigItem, UpsertRequest>(
      path = "/api/v1/config/upsert",
      body = payload,
      token = requireToken(),
    )
  }

  suspend fun setBoolean(key: String, value: Boolean, description: String = ""): ConfigItem {
    return set(key, value, Boolean.serializer(), description)
  }

  private suspend inline fun <reified Response, reified Request : Any> post(
    path: String,
    body: Request,
    token: String?,
  ): Response {
    val response = httpClient.post(buildUrl(path)) {
      contentType(ContentType.Application.Json)
      if (!token.isNullOrBlank()) bearerAuth(token)
      setBody(body)
    }.body<ApiEnvelope<Response>>()
    if (!response.success) throw ConfigCenterException(response.message)
    return response.data ?: throw ConfigCenterException("Config Center returned empty data")
  }

  private suspend inline fun <reified T> getEnvelope(url: String): ApiEnvelope<T> {
    return httpClient.get(url) {
      bearerAuth(requireToken())
    }.body()
  }

  private fun buildUrl(path: String, block: URLBuilder.() -> Unit = {}): String {
    return URLBuilder().apply {
      takeFrom(baseUrl)
      appendPathSegments(path.trimStart('/').split('/'))
      block()
    }.buildString()
  }

  private fun requireToken(): String {
    return token?.takeIf { it.isNotBlank() }
      ?: throw ConfigCenterException("Config Center client is not logged in")
  }

  private fun requireNamespace(): String {
    return namespace?.takeIf { it.isNotBlank() }
      ?: throw ConfigCenterException("Config Center namespace is not selected")
  }

  private fun requireKey(key: String): String {
    require(key.isNotBlank()) { "key must not be blank" }
    return key.trim()
  }

  private fun <T> encodeValue(value: T, serializer: KSerializer<T>): EncodedValue {
    return when (serializer) {
      String.serializer() -> EncodedValue(value.toString(), "text")
      Int.serializer(),
      Long.serializer(),
      Double.serializer(),
      Float.serializer(),
      -> EncodedValue(value.toString(), "number")
      Boolean.serializer() -> EncodedValue(value.toString(), "boolean")
      else -> EncodedValue(json.encodeToString(serializer, value), "json")
    }
  }

  private fun <T> KSerializer<T>.cast(value: Any?): T? {
    @Suppress("UNCHECKED_CAST")
    return value as? T
  }

  companion object {
    fun defaultHttpClient(): HttpClient {
      return HttpClient {
        install(ContentNegotiation) {
          json(sharedJson)
        }
      }
    }

    fun defaultJson(): Json {
      return sharedJson
    }

    fun inferCommonNamespace(namespace: String): String? {
      val normalized = namespace.trim()
      if (normalized.isEmpty() || normalized.endsWith(".common")) return null
      val separator = normalized.lastIndexOf('.')
      if (separator <= 0) return null
      return normalized.substring(0, separator) + ".common"
    }
  }
}

class ConfigCenterException(message: String) : RuntimeException(message)

private data class EncodedValue(
  val value: String,
  val valueType: String,
)

@Serializable
private data class ApiEnvelope<T>(
  val success: Boolean,
  val message: String,
  val data: T? = null,
)

@Serializable
private data class LoginRequest(
  val username: String,
  val password: String,
)

@Serializable
private data class LoginPayload(
  val token: String,
  val username: String,
)

@Serializable
internal data class UpsertRequest(
  val namespace: String,
  val key: String,
  val value: String,
  @SerialName("value_type")
  val valueType: String,
  val description: String,
  val enabled: Boolean,
  @SerialName("updated_by")
  val updatedBy: String,
)
