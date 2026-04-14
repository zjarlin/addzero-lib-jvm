package site.addzero.serial

/**
 * 把字节数组转成十六进制字符串，方便做串口日志排查。
 *
 * 示例：
 * - `byteArrayOf(0x0A, 0x1B).toHexString()` -> `0A 1B`
 */
fun ByteArray.toHexString(separator: String = " "): String =
    joinToString(separator = separator) { byte -> "%02X".format(byte.toInt() and 0xFF) }

/**
 * 把十六进制文本解析成字节数组。
 *
 * 输入允许包含空格、换行和 `0x` 前缀。
 * 这样做是为了方便直接粘贴日志、文档或抓包工具里的内容。
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
        /**
         * 每两位十六进制文本对应一个字节。
         */
        .chunked(2)
        .map { chunk -> chunk.toInt(16).toByte() }
        .toByteArray()
}
