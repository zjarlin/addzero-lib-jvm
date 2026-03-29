package site.addzero.core.network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.koin.core.annotation.Single
import site.addzero.core.network.json.json
import site.addzero.ktor2curl.CurlLogger
import site.addzero.ktor2curl.KtorToCurl
import site.addzero.util.KoinInjector
import kotlin.time.Duration.Companion.minutes

const val DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE = "default"

data class AddZeroHttpClientRequestContribution(
    val headers: Map<String, String> = emptyMap(),
    val bearerToken: String? = null,
) {
    fun isEmpty(): Boolean {
        return headers.isEmpty() && bearerToken.isNullOrBlank()
    }
}

data class AddZeroHttpClientFeaturePolicy(
    val enableSse: Boolean? = null,
    val enableWebSocket: Boolean? = null,
) {
    fun merge(other: AddZeroHttpClientFeaturePolicy): AddZeroHttpClientFeaturePolicy {
        return AddZeroHttpClientFeaturePolicy(
            enableSse = other.enableSse ?: enableSse,
            enableWebSocket = other.enableWebSocket ?: enableWebSocket,
        )
    }

    fun resolve(): AddZeroHttpClientResolvedFeatures {
        return AddZeroHttpClientResolvedFeatures(
            enableSse = enableSse == true,
            enableWebSocket = enableWebSocket == true,
        )
    }
}

data class AddZeroHttpClientResolvedFeatures(
    val enableSse: Boolean = false,
    val enableWebSocket: Boolean = false,
)

interface AddZeroHttpClientProfileSpi {
    val profile: String
        get() = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE

    val order: Int
        get() = 0

    fun requestContribution(): AddZeroHttpClientRequestContribution {
        return AddZeroHttpClientRequestContribution()
    }

    fun featurePolicy(): AddZeroHttpClientFeaturePolicy {
        return AddZeroHttpClientFeaturePolicy()
    }

    fun configure(config: HttpClientConfig<*>) = Unit
}

private data class AddZeroHttpClientRuntimeSettings(
    val requestContribution: AddZeroHttpClientRequestContribution = AddZeroHttpClientRequestContribution(),
    val featurePolicy: AddZeroHttpClientFeaturePolicy = AddZeroHttpClientFeaturePolicy(),
) {
    fun isEmpty(): Boolean {
        return requestContribution.isEmpty() &&
            featurePolicy == AddZeroHttpClientFeaturePolicy()
    }
}

@Single
class AddZeroHttpClientFactory {
    companion object {
        fun shared(): AddZeroHttpClientFactory {
            return KoinInjector.inject()
        }
    }

    private data class CacheKey(
        val profile: String,
        val features: AddZeroHttpClientResolvedFeatures,
    )

    private val settingsByProfile = mutableMapOf<String, AddZeroHttpClientRuntimeSettings>()
    private val clients = linkedMapOf<CacheKey, HttpClient>()

    fun get(
        profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE,
        overrides: AddZeroHttpClientFeaturePolicy = AddZeroHttpClientFeaturePolicy(),
    ): HttpClient {
        val normalizedProfile = profile.normalizeHttpClientProfile()
        val contributors = collectContributors(normalizedProfile)
        val resolvedFeatures = contributors.fold(AddZeroHttpClientFeaturePolicy()) { acc, contributor ->
            acc.merge(contributor.featurePolicy())
        }.merge(snapshotSettings(normalizedProfile).featurePolicy)
            .merge(overrides)
            .resolve()
        val key = CacheKey(
            profile = normalizedProfile,
            features = resolvedFeatures,
        )
        return synchronized(this) {
            clients[key] ?: buildClient(
                profile = normalizedProfile,
                contributors = contributors,
                features = resolvedFeatures,
            ).also { created ->
                clients[key] = created
            }
        }
    }

    fun putHeader(
        profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE,
        name: String,
        value: String?,
    ) {
        val normalizedValue = value?.trim()?.ifBlank { null }
        updateRuntimeSettings(profile) { settings ->
            val headers = settings.requestContribution.headers.toMutableMap()
            if (normalizedValue == null) {
                headers.remove(name)
            } else {
                headers[name] = normalizedValue
            }
            settings.copy(
                requestContribution = settings.requestContribution.copy(
                    headers = headers.toMap(),
                ),
            )
        }
    }

    fun removeHeader(
        profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE,
        name: String,
    ) {
        putHeader(profile = profile, name = name, value = null)
    }

    fun clearHeaders(profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE) {
        updateRuntimeSettings(profile) { settings ->
            settings.copy(
                requestContribution = settings.requestContribution.copy(
                    headers = emptyMap(),
                ),
            )
        }
    }

    fun setBearerToken(
        profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE,
        token: String?,
    ) {
        updateRuntimeSettings(profile) { settings ->
            settings.copy(
                requestContribution = settings.requestContribution.copy(
                    bearerToken = token?.trim()?.ifBlank { null },
                ),
            )
        }
    }

    fun setFeaturePolicy(
        profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE,
        featurePolicy: AddZeroHttpClientFeaturePolicy,
    ) {
        updateFeaturePolicy(profile) {
            featurePolicy
        }
    }

    fun setSseEnabled(
        profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE,
        enabled: Boolean?,
    ) {
        updateFeaturePolicy(profile) { featurePolicy ->
            featurePolicy.copy(enableSse = enabled)
        }
    }

    fun setWebSocketEnabled(
        profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE,
        enabled: Boolean?,
    ) {
        updateFeaturePolicy(profile) { featurePolicy ->
            featurePolicy.copy(enableWebSocket = enabled)
        }
    }

    fun clear(profile: String = DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE) {
        val normalizedProfile = profile.normalizeHttpClientProfile()
        val previousFeaturePolicy = snapshotSettings(normalizedProfile).featurePolicy
        synchronized(this) {
            settingsByProfile.remove(normalizedProfile)
        }
        if (previousFeaturePolicy != AddZeroHttpClientFeaturePolicy()) {
            invalidate(normalizedProfile)
        }
    }

    private fun buildClient(
        profile: String,
        contributors: List<AddZeroHttpClientProfileSpi>,
        features: AddZeroHttpClientResolvedFeatures,
    ): HttpClient {
        return HttpClient(addZeroHttpClientEngineFactory) {
            configClient().invoke(this)
            if (features.enableSse) {
                install(SSE) {
                    showCommentEvents()
                    showRetryEvents()
                }
            }
            if (features.enableWebSocket) {
                install(WebSockets)
            }
            defaultRequest {
                headers.applyProfileContributions(
                    contributors = contributors,
                    runtimeContribution = snapshotSettings(profile).requestContribution,
                )
            }
            contributors.forEach { contributor ->
                contributor.configure(this)
            }
        }
    }

    private fun invalidate(profile: String) {
        val removed = synchronized(this) {
            val keys = clients.keys.filter { key -> key.profile == profile }
            keys.mapNotNull(clients::remove)
        }
        removed.forEach(HttpClient::close)
    }

    private fun collectContributors(profile: String): List<AddZeroHttpClientProfileSpi> {
        return KoinInjector.injectList<AddZeroHttpClientProfileSpi>()
            .filter { contributor -> contributor.profile.normalizeHttpClientProfile() == profile }
            .sortedBy(AddZeroHttpClientProfileSpi::order)
    }

    private fun snapshotSettings(profile: String): AddZeroHttpClientRuntimeSettings {
        return synchronized(this) {
            settingsByProfile[profile] ?: AddZeroHttpClientRuntimeSettings()
        }
    }

    private fun updateRuntimeSettings(
        profile: String,
        transform: (AddZeroHttpClientRuntimeSettings) -> AddZeroHttpClientRuntimeSettings,
    ): AddZeroHttpClientRuntimeSettings {
        val normalizedProfile = profile.normalizeHttpClientProfile()
        return synchronized(this) {
            val updated = transform(settingsByProfile[normalizedProfile] ?: AddZeroHttpClientRuntimeSettings())
            if (updated.isEmpty()) {
                settingsByProfile.remove(normalizedProfile)
                AddZeroHttpClientRuntimeSettings()
            } else {
                settingsByProfile[normalizedProfile] = updated
                updated
            }
        }
    }

    private fun updateFeaturePolicy(
        profile: String,
        transform: (AddZeroHttpClientFeaturePolicy) -> AddZeroHttpClientFeaturePolicy,
    ) {
        val normalizedProfile = profile.normalizeHttpClientProfile()
        val previousFeaturePolicy = snapshotSettings(normalizedProfile).featurePolicy
        val updatedFeaturePolicy = updateRuntimeSettings(normalizedProfile) { settings ->
            settings.copy(featurePolicy = transform(settings.featurePolicy))
        }.featurePolicy
        if (previousFeaturePolicy != updatedFeaturePolicy) {
            invalidate(normalizedProfile)
        }
    }
}

private fun String.normalizeHttpClientProfile(): String {
    return trim().ifBlank { DEFAULT_ADDZERO_HTTP_CLIENT_PROFILE }
}

private fun HeadersBuilder.applyProfileContributions(
    contributors: List<AddZeroHttpClientProfileSpi>,
    runtimeContribution: AddZeroHttpClientRequestContribution,
) {
    contributors.forEach { contributor ->
        applyContribution(contributor.requestContribution())
    }
    applyContribution(runtimeContribution)
}

private fun HeadersBuilder.applyContribution(
    contribution: AddZeroHttpClientRequestContribution,
) {
    contribution.headers.forEach { (name, value) ->
        remove(name)
        append(name, value)
    }
    contribution.bearerToken?.trim()?.ifBlank { null }?.let { token ->
        remove(HttpHeaders.Authorization)
        append(HttpHeaders.Authorization, "Bearer $token")
    }
}

internal expect val addZeroHttpClientEngineFactory: HttpClientEngineFactory<*>

private fun configClient(): HttpClientConfig<*>.() -> Unit = {
    configTimeout()
    install(createClientPlugin("HttpResponseInterceptor") {
        onResponse { response ->
            val error = response.status.value != HttpStatusCode.OK.value
            if (error) {
                val body = runCatching {
                    response.bodyAsText()
                }.getOrNull()
                println("异常body: $body")
                dispatchHttpResponseEvent(response)
            }
        }
    })
    configLog()
    configJson()
    configCurl()
}

private fun HttpClientConfig<*>.configCurl() {
    install(KtorToCurl) {
        converter = object : CurlLogger {
            override fun log(curl: String) {
                println(curl)
            }
        }
    }
}

private fun HttpClientConfig<*>.configLog() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
}

private fun HttpClientConfig<*>.configJson() {
    install(ContentNegotiation) {
        json(json)
    }
}

private fun HttpClientConfig<*>.configTimeout() {
    install(HttpTimeout) {
        requestTimeoutMillis = 5.minutes.inWholeMilliseconds
        connectTimeoutMillis = 30_000
        socketTimeoutMillis = 5.minutes.inWholeMilliseconds
    }
}
