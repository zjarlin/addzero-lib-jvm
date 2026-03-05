package site.addzero.network.call.musiclib

import site.addzero.network.call.musiclib.api.bilibili.BilibiliAPI
import site.addzero.network.call.musiclib.api.fivesing.FivesingAPI
import site.addzero.network.call.musiclib.api.jamendo.JamendoAPI
import site.addzero.network.call.musiclib.api.joox.JooxAPI
import site.addzero.network.call.musiclib.api.kugou.KugouAPI
import site.addzero.network.call.musiclib.api.kuwo.KuwoAPI
import site.addzero.network.call.musiclib.api.migu.MiguAPI
import site.addzero.network.call.musiclib.api.netease.NeteaseAPI
import site.addzero.network.call.musiclib.api.qianqian.QianqianAPI
import site.addzero.network.call.musiclib.api.qq.QQAPI
import site.addzero.network.call.musiclib.api.soda.SodaAPI
import site.addzero.network.call.musiclib.provider.CookieMusicProvider
import site.addzero.network.call.musiclib.provider.MusicProvider

/**
 * 音乐 API 工厂类
 * 用于创建和管理各平台的 API 实例
 */
object MusicAPIFactory {

    /**
     * 创建指定平台的 API 实例
     */
    fun create(source: String, cookie: String = ""): MusicProvider {
        return when (source.lowercase()) {
            "netease", "网易云", "163" -> NeteaseAPI(cookie)
            "qq", "qq音乐", "tencent" -> QQAPI(cookie)
            "kugou", "酷狗", "kg" -> KugouAPI(cookie)
            "kuwo", "酷我", "kw" -> KuwoAPI(cookie)
            "migu", "咪咕", "mg" -> MiguAPI(cookie)
            "bilibili", "b站", "哔哩哔哩" -> BilibiliAPI(cookie)
            "joox" -> JooxAPI(cookie)
            "fivesing", "5sing" -> FivesingAPI(cookie)
            "qianqian", "千千", "91q" -> QianqianAPI(cookie)
            "soda", "汽水", "qishui" -> SodaAPI(cookie)
            "jamendo" -> JamendoAPI(cookie)
            else -> throw IllegalArgumentException("Unknown music source: $source")
        }
    }

    /**
     * 创建带 Cookie 的 API 实例
     */
    fun createWithCookie(source: String, cookie: String): CookieMusicProvider {
        val api = create(source, cookie)
        if (api is CookieMusicProvider) {
            return api
        }
        throw IllegalArgumentException("Source $source does not support cookie")
    }

    /**
     * 获取所有支持的音源
     */
    fun getAllSources(): List<String> = listOf(
        "netease", "qq", "kugou", "kuwo", "migu",
        "bilibili", "joox", "fivesing", "qianqian", "soda", "jamendo"
    )

    /**
     * 获取需要 Cookie 的音源
     */
    fun getCookieSources(): List<String> = listOf(
        "netease", "qq", "kugou", "kuwo", "migu",
        "bilibili", "joox", "fivesing", "qianqian", "soda", "jamendo"
    )
}
