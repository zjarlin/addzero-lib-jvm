package site.addzero.network.call.musiclib.utils

import kotlin.random.Random

/**
 * 中国大陆IP段前缀 (电信/联通/移动)
 */
private val CHINA_IP_PREFIXES = listOf(
    listOf(116, 255, 0, 0),
    listOf(116, 228, 0, 0),
    listOf(218, 192, 0, 0),
    listOf(124, 0, 0, 0),
    listOf(14, 132, 0, 0),
    listOf(183, 14, 0, 0),
    listOf(58, 14, 0, 0),
    listOf(113, 116, 0, 0),
    listOf(120, 230, 0, 0)
)

/**
 * 生成一个随机的中国大陆IP地址
 */
fun RandomChinaIP(): String {
    val prefix = CHINA_IP_PREFIXES.random()
    return "${prefix[0]}.${prefix[1]}.${Random.nextInt(1, 255)}.${Random.nextInt(1, 255)}"
}

/**
 * 生成随机GUID
 */
fun generateGuid(): String {
    return (1000000000..9999999999).random().toString()
}

/**
 * 生成随机字符串
 */
fun randomString(length: Int): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

/**
 * 生成时间戳
 */
fun timestamp(): String = System.currentTimeMillis().toString()

/**
 * 生成秒级时间戳
 */
fun timestampSeconds(): String = (System.currentTimeMillis() / 1000).toString()
