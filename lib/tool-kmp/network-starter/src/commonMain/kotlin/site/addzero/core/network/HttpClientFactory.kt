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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

const val DEFAULT_HTTP_CLIENT_PROFILE = "default"

data class HttpClientRequestContribution(
    val headers: Map<String, String> = emptyMap(),
    val bearerToken: String? = null,
) {
    fun isEmpty(): Boolean {
        return headers.isEmpty() && bearerToken.isNullOrBlank()
    }
}

data class HttpClientFeaturePolicy(
    val enableSse: Boolean? = null,
    val enableWebSocket: Boolean? = null,
) {
    fun merge(other: HttpClientFeaturePolicy): HttpClientFeaturePolicy {
        return HttpClientFeaturePolicy(
            enableSse = other.enableSse ?: enableSse,
            enableWebSocket = other.enableWebSocket ?: enableWebSocket,
        )
    }

    fun resolve(): HttpClientFeatures {
        return HttpClientFeatures(
            enableSse = enableSse ?: false,
            enableWebSocket = enableWebSocket ?: false,
        )
    }
}

data class HttpClientFeatures(
    val enableSse: Boolean = false,
    val enableWebSocket: Boolean = false,
)

interface HttpClientProfileSpi {
    val profile: String
        get() = DEFAULT_HTTP_CLIENT_PROFILE

    val order: Int
        get() = 0

    fun requestContribution(): HttpClientRequestContribution {
        return HttpClientRequestContribution()
    }

    fun featurePolicy(): HttpClientFeaturePolicy {
        return HttpClientFeaturePolicy()
    }

    fun configure(config: HttpClientConfig<*>) = Unit
}

private data class HttpClientRuntimeSettings(
    val requestContribution: HttpClientRequestContribution = HttpClientRequestContribution(),
    val featurePolicy: HttpClientFeaturePolicy = HttpClientFeaturePolicy(),
) {
    fun isEmpty(): Boolean {
        return requestContribution.isEmpty() &&
            featurePolicy == HttpClientFeaturePolicy()
    }
}

@Single
class HttpClientFactory {
    companion object {
        fun shared(): HttpClientFactory {
            return KoinInjector.inject()
        }
    }

    private data class CacheKey(
        val profile: String,
        val features: HttpClientFeatures,
    )

    private val curlLogDeduplicator = CurlLogDeduplicator(
        window = 5.seconds,
    )
    private val settingsByProfile = mutableMapOf<String, HttpClientRuntimeSettings>()
    private val clients = linkedMapOf<CacheKey, HttpClient>()

    fun get(
        profile: String = DEFAULT_HTTP_CLIENT_PROFILE,
        overrides: HttpClientFeaturePolicy = HttpClientFeaturePolicy(),
    ): HttpClient {
        val normalizedProfile = profile.normalizeHttpClientProfile()
        val contributors = collectProfiles(normalizedProfile)
        val resolvedFeatures = contributors.fold(HttpClientFeaturePolicy()) { acc, contributor ->
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
        profile: String = DEFAULT_HTTP_CLIENT_PROFILE,
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
        profile: String = DEFAULT_HTTP_CLIENT_PROFILE,
        name: String,
    ) {
        putHeader(profile = profile, name = name, value = null)
    }

    fun clearHeaders(profile: String = DEFAULT_HTTP_CLIENT_PROFILE) {
        updateRuntimeSettings(profile) { settings ->
            settings.copy(
                requestContribution = settings.requestContribution.copy(
                    headers = emptyMap(),
                ),
            )
        }
    }

    fun setBearerToken(
        profile: String = DEFAULT_HTTP_CLIENT_PROFILE,
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
        profile: String = DEFAULT_HTTP_CLIENT_PROFILE,
        featurePolicy: HttpClientFeaturePolicy,
    ) {
        updateFeaturePolicy(profile) {
            featurePolicy
        }
    }

    fun setSseEnabled(
        profile: String = DEFAULT_HTTP_CLIENT_PROFILE,
        enabled: Boolean?,
    ) {
        updateFeaturePolicy(profile) { featurePolicy ->
            featurePolicy.copy(enableSse = enabled)
        }
    }

    fun setWebSocketEnabled(
        profile: String = DEFAULT_HTTP_CLIENT_PROFILE,
        enabled: Boolean?,
    ) {
        updateFeaturePolicy(profile) { featurePolicy ->
            featurePolicy.copy(enableWebSocket = enabled)
        }
    }

    fun clear(profile: String = DEFAULT_HTTP_CLIENT_PROFILE) {
        val normalizedProfile = profile.normalizeHttpClientProfile()
        val previousFeaturePolicy = snapshotSettings(normalizedProfile).featurePolicy
        synchronized(this) {
            settingsByProfile.remove(normalizedProfile)
        }
        if (previousFeaturePolicy != HttpClientFeaturePolicy()) {
            invalidate(normalizedProfile)
        }
    }

    private fun buildClient(
        profile: String,
        contributors: List<HttpClientProfileSpi>,
        features: HttpClientFeatures,
    ): HttpClient {
        return HttpClient(httpClientEngineFactory) {
            configClient(
                curlLogDeduplicator = curlLogDeduplicator,
            ).invoke(this)
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

    private fun collectProfiles(profile: String): List<HttpClientProfileSpi> {
        return KoinInjector.injectList<HttpClientProfileSpi>()
            .filter { contributor -> contributor.profile.normalizeHttpClientProfile() == profile }
            .sortedBy(HttpClientProfileSpi::order)
    }

    private fun snapshotSettings(profile: String): HttpClientRuntimeSettings {
        return synchronized(this) {
            settingsByProfile[profile] ?: HttpClientRuntimeSettings()
        }
    }

    private fun updateRuntimeSettings(
        profile: String,
        transform: (HttpClientRuntimeSettings) -> HttpClientRuntimeSettings,
    ): HttpClientRuntimeSettings {
        val normalizedProfile = profile.normalizeHttpClientProfile()
        return synchronized(this) {
            val updated = transform(settingsByProfile[normalizedProfile] ?: HttpClientRuntimeSettings())
            if (updated.isEmpty()) {
                settingsByProfile.remove(normalizedProfile)
                HttpClientRuntimeSettings()
            } else {
                settingsByProfile[normalizedProfile] = updated
                updated
            }
        }
    }

    private fun updateFeaturePolicy(
        profile: String,
        transform: (HttpClientFeaturePolicy) -> HttpClientFeaturePolicy,
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

internal fun String.normalizeHttpClientProfile(): String {
    return trim().ifBlank { DEFAULT_HTTP_CLIENT_PROFILE }
}

private fun HeadersBuilder.applyProfileContributions(
    contributors: List<HttpClientProfileSpi>,
    runtimeContribution: HttpClientRequestContribution,
) {
    contributors.forEach { contributor ->
        applyContribution(contributor.requestContribution())
    }
    applyContribution(runtimeContribution)
}

private fun HeadersBuilder.applyContribution(
    contribution: HttpClientRequestContribution,
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

internal expect val httpClientEngineFactory: HttpClientEngineFactory<*>

private fun configClient(
    curlLogDeduplicator: CurlLogDeduplicator,
): HttpClientConfig<*>.() -> Unit = {
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
    configCurl(curlLogDeduplicator)
}

private fun HttpClientConfig<*>.configCurl(
    curlLogDeduplicator: CurlLogDeduplicator,
) {
    install(KtorToCurl) {
        converter = object : CurlLogger {
            override fun log(curl: String) {
                if (curlLogDeduplicator.shouldLog(curl)) {
                    println(curl)
                }
            }
        }
    }
}

private class CurlLogDeduplicator(
    private val window: kotlin.time.Duration,
    private val maxEntries: Int = 128,
) {
    private val recentMarks = linkedMapOf<String, TimeMark>()

    fun shouldLog(curl: String): Boolean {
        return synchronized(this) {
            pruneExpired()
            val lastSeen = recentMarks[curl]
            if (lastSeen != null && lastSeen.elapsedNow() < window) {
                false
            } else {
                recentMarks.remove(curl)
                recentMarks[curl] = TimeSource.Monotonic.markNow()
                trimToMaxEntries()
                true
            }
        }
    }

    private fun pruneExpired() {
        val iterator = recentMarks.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.elapsedNow() >= window) {
                iterator.remove()
            }
        }
    }

    private fun trimToMaxEntries() {
        while (recentMarks.size > maxEntries) {
            val iterator = recentMarks.entries.iterator()
            if (!iterator.hasNext()) {
                return
            }
            iterator.next()
            iterator.remove()
        }
    }
}

private fun HttpClientConfig<*>.configLog() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.NONE
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
