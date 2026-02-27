package site.addzero.network.call.tempmail

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TempMailClient(
    private val baseUrl: String = "https://api.mail.tm",
    private val client: OkHttpClient = defaultClient(),
) {
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun getDomains(): List<TempMailDomain> {
        val root = getJson("/domains")
        return root["hydra:member"]
            ?.map { node ->
                TempMailDomain(
                    id = node.path("id").asText(),
                    domain = node.path("domain").asText(),
                    isActive = node.path("isActive").asBoolean(false),
                    isPrivate = node.path("isPrivate").asBoolean(false),
                )
            }
            .orEmpty()
            .filter { it.domain.isNotBlank() }
    }

    fun createMailboxAndLogin(
        prefix: String = "az",
        passwordLength: Int = 12,
    ): TempMailMailbox {
        val domains = getDomains().filter { it.isActive }
        val chosenDomain = domains.firstOrNull()?.domain
            ?: throw IllegalStateException("No active temp-mail domains available")

        val localPart = "${sanitizePrefix(prefix)}${randomAlphaNumeric(8)}"
        val address = "$localPart@$chosenDomain"
        val password = randomAlphaNumeric(passwordLength.coerceAtLeast(8))

        val accountId = createAccount(address, password)
        val token = createToken(address, password)

        return TempMailMailbox(
            address = address,
            password = password,
            accountId = accountId,
            token = token,
        )
    }

    fun createAccount(address: String, password: String): String {
        val body = objectMapper.writeValueAsString(
            mapOf("address" to address, "password" to password),
        )
        val root = postJson("/accounts", body)
        return root.path("id").asText().ifBlank {
            throw IllegalStateException("Create account failed: id missing for address=$address")
        }
    }

    fun createToken(address: String, password: String): String {
        val body = objectMapper.writeValueAsString(
            mapOf("address" to address, "password" to password),
        )
        val root = postJson("/token", body)
        return root.path("token").asText().ifBlank {
            throw IllegalStateException("Create token failed: token missing for address=$address")
        }
    }

    fun listMessages(token: String, page: Int = 1): List<TempMailMessageSummary> {
        val root = getJson("/messages?page=$page", token)
        return root["hydra:member"]
            ?.map { node ->
                val from = node.path("from")
                TempMailMessageSummary(
                    id = node.path("id").asText(),
                    fromAddress = from.path("address").asText(),
                    fromName = from.path("name").asText(),
                    subject = node.path("subject").asText(),
                    intro = node.path("intro").asText(),
                    seen = node.path("seen").asBoolean(false),
                    createdAt = node.path("createdAt").asText(),
                )
            }
            .orEmpty()
            .filter { it.id.isNotBlank() }
    }

    fun getMessage(token: String, messageId: String): TempMailMessageDetail {
        val root = getJson("/messages/$messageId", token)
        val from = root.path("from")
        val recipients = root.path("to")
            .map { toNode ->
                TempMailRecipient(
                    address = toNode.path("address").asText(),
                    name = toNode.path("name").asText(),
                )
            }

        return TempMailMessageDetail(
            id = root.path("id").asText(),
            fromAddress = from.path("address").asText(),
            fromName = from.path("name").asText(),
            to = recipients,
            subject = root.path("subject").asText(),
            text = root.path("text").asText(),
            html = parseHtmlBody(root.path("html")),
            createdAt = root.path("createdAt").asText(),
        )
    }

    private fun getJson(path: String, token: String? = null): JsonNode {
        val requestBuilder = Request.Builder().url("$baseUrl$path")
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("GET $path failed: HTTP ${response.code}, body=$body")
            }
            return objectMapper.readTree(body)
        }
    }

    private fun postJson(path: String, body: String, token: String? = null): JsonNode {
        val requestBuilder = Request.Builder()
            .url("$baseUrl$path")
            .post(body.toRequestBody(JSON_MEDIA_TYPE))
            .header("Content-Type", "application/json")
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("POST $path failed: HTTP ${response.code}, body=$responseBody")
            }
            return objectMapper.readTree(responseBody)
        }
    }

    private fun parseHtmlBody(node: JsonNode): String {
        if (node.isArray && node.size() > 0) {
            return node.first().asText()
        }
        return node.asText()
    }

    private fun sanitizePrefix(prefix: String): String =
        prefix.filter { it.isLetterOrDigit() }.ifBlank { "az" }

    private fun randomAlphaNumeric(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return buildString(length) {
            repeat(length) {
                append(chars[Random.nextInt(chars.length)])
            }
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
    }
}
