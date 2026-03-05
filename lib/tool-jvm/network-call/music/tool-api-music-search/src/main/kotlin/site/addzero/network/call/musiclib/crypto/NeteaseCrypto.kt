package site.addzero.network.call.musiclib.crypto

import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 网易云音乐加密算法
 * 包含 Linux API / WeApi / EApi 三种加密方式
 */
object NeteaseCrypto {

    // Linux API Key (Hex)
    private const val LINUX_API_KEY_HEX = "7246674226682325323F5E6544673A51"

    // WeApi Constants
    private const val WEAPI_NONCE = "0CoJUm6Qyw8W8jud"
    private const val WEAPI_IV = "0102030405060708"
    private const val WEAPI_PUB_MODULUS =
        "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7"
    private const val WEAPI_PUB_KEY = "010001"

    // EApi Key
    private val EAPI_KEY = "e82ckenh8dichen8".toByteArray()

    // NCM Decryption Keys
    private val NCM_CORE_KEY = "hzHRAmso5kInbaxW".toByteArray()
    private val NCM_META_KEY = "#14ljk_!\\]&0U<'(".toByteArray()

    /**
     * Linux API 加密 (用于搜索接口)
     */
    fun encryptLinux(data: String): String {
        val key = LINUX_API_KEY_HEX.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return aesEncryptECB(data.toByteArray(), key).toHex().uppercase()
    }

    /**
     * WeApi 加密 (用于下载/详情接口)
     * 返回 (params, encSecKey)
     */
    fun encryptWeApi(text: String): Pair<String, String> {
        val secKey = randomString(16)
        val encText = aesEncryptCBC(text, WEAPI_NONCE, WEAPI_IV)
        val params = aesEncryptCBC(encText, secKey, WEAPI_IV)
        val encSecKey = rsaEncrypt(secKey, WEAPI_PUB_KEY, WEAPI_PUB_MODULUS)
        return Pair(params, encSecKey)
    }

    /**
     * EApi 加密 (用于高音质VIP下载)
     */
    fun encryptEApi(urlPath: String, payload: String): String {
        val path = urlPath.replace("/eapi/", "/api/").substringAfter(".com")
        val text = "nobody${path}use${payload}md5forencrypt"
        val digest = md5(text)
        val data = "$path-36cd479b6b5-$payload-36cd479b6b5-$digest"
        return aesEncryptECB(data.toByteArray(), EAPI_KEY).toHex()
    }

    /**
     * NCM 文件解密
     */
    fun decryptNcm(encrypted: ByteArray): Triple<ByteArray, String, ByteArray?> {
        if (encrypted.size < 16 || !encrypted.slice(0..7).toByteArray()
                .contentEquals("CTENFDAM".toByteArray())
        ) {
            throw IllegalArgumentException("Invalid NCM file")
        }

        var offset = 10
        val keyLen = encrypted.readInt32LE(offset)
        offset += 4
        val keyData = encrypted.copyOfRange(offset, offset + keyLen).map { (it.toInt() xor 0x64).toByte() }.toByteArray()
        offset += keyLen

        val decryptedKey = aesECBDecrypt(NCM_CORE_KEY, keyData).pkcs7Unpad()
        val realKey = decryptedKey.copyOfRange(17, decryptedKey.size)
        val keyBox = buildKeyBox(realKey)

        val metaLen = encrypted.readInt32LE(offset)
        offset += 4
        val metaData = encrypted.copyOfRange(offset, offset + metaLen).map { (it.toInt() xor 0x63).toByte() }.toByteArray()
        offset += metaLen

        val format = parseNcmFormat(metaData)

        offset += 9  // Skip CRC and unknown
        val imageSize = encrypted.readInt32LE(offset)
        offset += 4 + imageSize  // Skip image

        val audioData = encrypted.copyOfRange(offset, encrypted.size)
        val decryptedAudio = ByteArray(audioData.size) { i ->
            val j = ((i + 1) and 0xff).toByte()
            val idx = ((keyBox[j.toInt() and 0xff] + keyBox[(keyBox[j.toInt() and 0xff] + j) and 0xff]) and 0xff)
            (audioData[i].toInt() xor keyBox[idx]).toByte()
        }

        val finalFormat = format.ifEmpty { detectAudioExt(decryptedAudio) }
        return Triple(decryptedAudio, finalFormat, if (imageSize > 0) encrypted.copyOfRange(offset - imageSize, offset) else null)
    }

    // ============ 内部加密方法 ============

    private fun aesEncryptECB(data: ByteArray, key: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
        return cipher.doFinal(data)
    }

    private fun aesEncryptCBC(text: String, key: String, iv: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(iv.toByteArray()))
        return Base64.getEncoder().encodeToString(cipher.doFinal(text.toByteArray()))
    }

    private fun aesECBDecrypt(key: ByteArray, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
        return cipher.doFinal(data)
    }

    private fun rsaEncrypt(text: String, pubKey: String, modulus: String): String {
        val reversedText = text.reversed()
        val hexText = reversedText.toByteArray().toHex()

        val biText = BigInteger(hexText, 16)
        val biPub = BigInteger(pubKey, 16)
        val biMod = BigInteger(modulus, 16)

        val result = biText.modPow(biPub, biMod)
        return "%0256x".format(result)
    }

    private fun randomString(size: Int): String {
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..size).map { letters.random() }.joinToString("")
    }

    private fun md5(input: String): String {
        return MessageDigest.getInstance("MD5").digest(input.toByteArray()).toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun ByteArray.readInt32LE(offset: Int): Int {
        return (this[offset].toInt() and 0xff) or
                ((this[offset + 1].toInt() and 0xff) shl 8) or
                ((this[offset + 2].toInt() and 0xff) shl 16) or
                ((this[offset + 3].toInt() and 0xff) shl 24)
    }

    private fun ByteArray.pkcs7Unpad(): ByteArray {
        if (isEmpty()) return this
        val pad = this[lastIndex].toInt()
        if (pad <= 0 || pad > size) return this
        return copyOf(size - pad)
    }

    private fun buildKeyBox(key: ByteArray): IntArray {
        val box = IntArray(256) { it }
        var c = 0
        var last = 0
        var keyPos = 0

        for (i in 0 until 256) {
            val swap = box[i]
            c = (swap + last + key[keyPos].toInt()) and 0xff
            box[i] = box[c]
            box[c] = swap
            last = c
            keyPos = (keyPos + 1) % key.size
        }
        return box
    }

    private fun parseNcmFormat(metaData: ByteArray): String {
        if (metaData.size <= 22) return ""
        val decoded = try {
            Base64.getDecoder().decode(String(metaData.copyOfRange(22, metaData.size)))
        } catch (e: Exception) {
            return ""
        }
        val decrypted = aesECBDecrypt(NCM_META_KEY, decoded).pkcs7Unpad()
        val jsonStr = String(decrypted).removePrefix("music:")

        // 简单提取 format 字段
        val regex = """"format"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(jsonStr)?.groupValues?.get(1) ?: ""
    }

    private fun detectAudioExt(data: ByteArray): String {
        if (data.size >= 4) {
            val header = data.copyOfRange(0, 4)
            when {
                header.contentEquals("fLaC".toByteArray()) -> return "flac"
                header.contentEquals("OggS".toByteArray()) -> return "ogg"
                header.slice(0..2).toByteArray().contentEquals("ID3".toByteArray()) -> return "mp3"
                header.copyOfRange(4, 8).contentEquals("ftyp".toByteArray()) -> return "m4a"
            }
        }
        return "mp3"
    }
}
