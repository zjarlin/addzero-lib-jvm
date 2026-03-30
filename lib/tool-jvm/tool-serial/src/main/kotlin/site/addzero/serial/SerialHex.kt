package site.addzero.serial

/**
 * 把字节数组转成十六进制字符串，方便做串口日志排查。
 */
fun ByteArray.toHexString(separator: String = " "): String =
    joinToString(separator = separator) { byte -> "%02X".format(byte.toInt() and 0xFF) }

/**
 * 把十六进制文本解析成字节数组。
 *
 * 输入允许包含空格、换行和 `0x` 前缀。
 */
fun String.hexToByteArray(): ByteArray {
    val normalized =
        replace("0x", "", ignoreCase = true)
            .replace("\\s+".toRegex(), "")
    require(normalized.isNotEmpty()) {
        "十六进制字符串不能为空"
    }
    require(normalized.length % 2 == 0) {
        "十六进制字符串长度必须为偶数"
    }
    return normalized
        .chunked(2)
        .map { chunk -> chunk.toInt(16).toByte() }
        .toByteArray()
}
