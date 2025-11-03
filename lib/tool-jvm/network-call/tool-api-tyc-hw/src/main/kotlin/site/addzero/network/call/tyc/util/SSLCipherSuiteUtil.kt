/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.util

import com.sun.org.slf4j.internal.LoggerFactory
import okhttp3.OkHttpClient
import org.apache.http.client.HttpClient
import site.addzero.network.call.tyc.util.Constant.GM_PROTOCOL
import site.addzero.network.call.tyc.util.Constant.HTTPS
import site.addzero.network.call.tyc.util.Constant.INTERNATIONAL_PROTOCOL
import site.addzero.network.call.tyc.util.Constant.SECURE_RANDOM_ALGORITHM_NATIVE_PRNG_NON_BLOCKING
import site.addzero.network.call.tyc.util.Constant.SUPPORTED_CIPHER_SUITES
import site.addzero.network.call.tyc.util.Constant.TRUST_MANAGER_FACTORY
import java.net.HttpURLConnection
import java.net.URL
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object SSLCipherSuiteUtil {
    private val LOGGER: Logger = LoggerFactory.getLogger(SSLCipherSuiteUtil::class.java)
    private var httpClient: CloseableHttpClient? = null
    private var okHttpClient: OkHttpClient? = null

    private const val CIPHER_LEN = 256

    private const val ENTROPY_BITS_REQUIRED = 384

    @Throws(Exception::class)
    fun createHttpClient(protocol: String?): HttpClient? {
        val sslContext = getSslContext(protocol)
        // create factory
        val sslConnectionSocketFactory: SSLConnectionSocketFactory = SSLConnectionSocketFactory(
            sslContext,
            arrayOf<String?>(protocol),
            SUPPORTED_CIPHER_SUITES,
            TrustAllHostnameVerifier()
        )

        httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build()
        return httpClient
    }

    @Throws(Exception::class)
    fun createHttpClientWithVerify(protocol: String?): HttpClient? {
        val sslContext = getSslContextWithVerify(protocol)
        // create factory
        val sslConnectionSocketFactory: SSLConnectionSocketFactory = SSLConnectionSocketFactory(
            sslContext,
            arrayOf<String?>(protocol),
            SUPPORTED_CIPHER_SUITES,
            TheRealHostnameVerifier()
        )

        httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build()
        return httpClient
    }

    @Throws(Exception::class)
    fun createOkHttpClient(protocol: String?): OkHttpClient {
        val sslContext = getSslContext(protocol)
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.getSocketFactory()
        val builder = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, TrustAllManager())
            .hostnameVerifier(TrustAllHostnameVerifier())
        okHttpClient = builder.connectTimeout(10, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
        return okHttpClient!!
    }

    @Throws(Exception::class)
    fun createOkHttpClientWithVerify(protocol: String?): OkHttpClient {
        val sslContext = getSslContextWithVerify(protocol)
        val sslSocketFactory = sslContext.getSocketFactory()

        val tmf = TrustManagerFactory.getInstance(TRUST_MANAGER_FACTORY)
        tmf.init(null as KeyStore?)
        val verify = tmf.getTrustManagers()
        val builder = OkHttpClient.Builder().sslSocketFactory(
            sslSocketFactory,
            (verify[0] as javax.net.ssl.X509TrustManager?)!!
        ).hostnameVerifier(TheRealHostnameVerifier())

        okHttpClient = builder.connectTimeout(10, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
        return okHttpClient!!
    }

    @Throws(Exception::class)
    fun createHttpsOrHttpURLConnection(uUrl: URL, protocol: String?): HttpURLConnection? {
        // initial connection
        if (uUrl.getProtocol().uppercase(Locale.getDefault()) == HTTPS) {
            val sslContext = getSslContext(protocol)
            HttpsURLConnection.setDefaultHostnameVerifier(TrustAllHostnameVerifier())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory())
            return uUrl.openConnection() as HttpsURLConnection?
        }
        return uUrl.openConnection() as HttpURLConnection?
    }

    @Throws(Exception::class)
    fun createHttpsOrHttpURLConnectionWithVerify(uUrl: URL, protocol: String?): HttpURLConnection? {
        // initial connection
        if (uUrl.getProtocol().uppercase(Locale.getDefault()) == HTTPS) {
            val sslContext = getSslContextWithVerify(protocol)
            HttpsURLConnection.setDefaultHostnameVerifier(TheRealHostnameVerifier())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory())
            return uUrl.openConnection() as HttpsURLConnection?
        }
        return uUrl.openConnection() as HttpURLConnection?
    }

    @Throws(
        UnsupportProtocolException::class,
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        KeyManagementException::class
    )
    private fun getSslContext(protocol: String?): SSLContext {
        if (!GM_PROTOCOL.equals(protocol) && !INTERNATIONAL_PROTOCOL.equals(
                protocol
            )
        ) {
            LOGGER.info("Unsupport protocol: {}, Only support GMTLS TLSv1.2", protocol)
            throw UnsupportProtocolException("Unsupport protocol, Only support GMTLS TLSv1.2")
        }
        // Create a trust manager that does not validate certificate chains
        val trust = arrayOf<TrustAllManager?>(TrustAllManager())
        val kms: Array<KeyManager?>? = null
        var sslContext: SSLContext

        sslContext = SSLContext.getInstance(INTERNATIONAL_PROTOCOL, "SunJSSE")

        if (GM_PROTOCOL.equals(protocol)) {
            Security.insertProviderAt(BGMProvider(), 1)
            sslContext = SSLContext.getInstance(GM_PROTOCOL, "BGMProvider")
        }
        val secureRandom: SecureRandom = secureRandom
        sslContext.init(kms, trust, secureRandom)
        sslContext.getServerSessionContext().setSessionCacheSize(8192)
        sslContext.getServerSessionContext().setSessionTimeout(3600)
        return sslContext
    }

    @Throws(
        UnsupportProtocolException::class,
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        KeyManagementException::class,
        KeyStoreException::class
    )
    private fun getSslContextWithVerify(protocol: String?): SSLContext {
        if (!GM_PROTOCOL.equals(protocol) && !INTERNATIONAL_PROTOCOL.equals(
                protocol
            )
        ) {
            LOGGER.info("Unsupport protocol: {}, Only support GMTLS TLSv1.2", protocol)
            throw UnsupportProtocolException("Unsupport protocol, Only support GMTLS TLSv1.2")
        }
        val kms: Array<KeyManager?>? = null
        var sslContext = SSLContext.getInstance(INTERNATIONAL_PROTOCOL, "SunJSSE")
        val secureRandom: SecureRandom = secureRandom

        if (GM_PROTOCOL.equals(protocol)) {
            Security.insertProviderAt(BGMProvider(), 1)
            sslContext = SSLContext.getInstance(GM_PROTOCOL, "BGMProvider")
        }

        val tmf = TrustManagerFactory.getInstance(TRUST_MANAGER_FACTORY)
        tmf.init(null as KeyStore?)
        val verify = tmf.getTrustManagers()
        sslContext.init(kms, verify, secureRandom)

        sslContext.getServerSessionContext().setSessionCacheSize(8192)
        sslContext.getServerSessionContext().setSessionTimeout(3600)
        return sslContext
    }

    private val secureRandom: SecureRandom
        get() {
            var source: SecureRandom?
            try {
                source =
                    SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM_NATIVE_PRNG_NON_BLOCKING)
            } catch (e: NoSuchAlgorithmException) {
                try {
                    source = SecureRandom.getInstanceStrong()
                } catch (ex: NoSuchAlgorithmException) {
                    LOGGER.error("get SecureRandom failed", e)
                    throw RuntimeException("get SecureRandom failed")
                }
            }
            val predictionResistant = true
            val cipher: BlockCipher = AESEngine()
            val reSeed = false
            return SP800SecureRandomBuilder(source, predictionResistant).setEntropyBitsRequired(
                ENTROPY_BITS_REQUIRED
            ).buildCTR(cipher, CIPHER_LEN, null, reSeed)
        }

    // 不校验域名
    private class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return true
        }
    }

    // 校验域名
    private class TheRealHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            if (com.huawei.apig.sdk.util.HostName.checkHostName(hostname)) {
                return true
            } else {
                val hv = HttpsURLConnection.getDefaultHostnameVerifier()
                return hv.verify(hostname, session)
            }
        }
    }

    // 不校验服务端证书
    private class TrustAllManager : X509TrustManager {
        private val issuers: Array<X509Certificate?>?

        init {
            this.issuers = arrayOfNulls<X509Certificate>(0)
        }

        override fun getAcceptedIssuers(): Array<X509Certificate?>? {
            return issuers
        }

        override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        }

        override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        }
    }
}
