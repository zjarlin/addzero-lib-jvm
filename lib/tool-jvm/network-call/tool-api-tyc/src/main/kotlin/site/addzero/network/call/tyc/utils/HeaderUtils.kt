package site.addzero.network.call.tyc.utils

object HeaderUtils {
    private const val X_AUTH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODIxMTI0NDg2NiIsImlhdCI6MTU1Nzc0Mzc2NCwiZXhwIjoxNTYwMzM1NzY0fQ.BACR-0C0uRCbINF6Lv-aZ4Tz16TYhxrUyhWZ7D2rm9F4kIkZtwIjqePPj_WXVIhtYDQhkXbtEBaldpq9fnAT-A"

    fun getHeaders(authorization: String, tycAuthToken: String = X_AUTH_TOKEN): Map<String, String> {
        // 设置请求参数 0###oo34J0ZRgatN5UBO8UQRwap6Ew_A###1565664617903###24ed6f7b1512aee63869b97552a2bd8f

        return mapOf(
            "Authorization" to authorization,
            "host" to "api9.tianyancha.com",
            "Content-Type" to "application/json",
            "X-AUTH-TOKEN" to tycAuthToken,
            "Accept-Encoding" to "gzip, deflate",
            "Accept" to "*/*",
            "version" to "TYC-XCX-WX",
            "User-Agent" to "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 MicroMessenger/7.0.5(0x17000523) NetType/WIFI Language/zh_CN",
            "Accept-Language" to "zh-cn"
        )
    }
}
