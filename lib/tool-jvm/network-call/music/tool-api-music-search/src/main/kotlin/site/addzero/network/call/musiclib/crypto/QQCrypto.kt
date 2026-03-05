package site.addzero.network.call.musiclib.crypto

/**
 * QQ音乐解密算法
 * 用于解密 mflac, mgg, qmc 等格式
 */
object QQCrypto {

    // 默认掩码表 (58字节)
    private val DEFAULT_QQ_MASK_58 = byteArrayOf(
        74, -42, -54, -112, 103, -9, 82,
        94, -107, 35, -97, 19, 17, 126,
        71, 116, 61, -112, -86, 63, 81,
        -58, 9, -43, -97, -6, 102, -7,
        -13, -42, -95, -112, -96, -9, -16,
        29, -107, -34, -97, -124, 17, -12,
        14, 116, -69, -112, -68, 63, -110,
        0, 9, 91, -97, 98, 102, -95
    )

    private const val DEFAULT_QQ_SUPER_58_A: Byte = -61  // 195
    private const val DEFAULT_QQ_SUPER_58_B: Byte = -40  // 216

    /**
     * 解密QQ音乐加密文件
     * @param encrypted 加密数据
     * @param ext 文件扩展名 (mflac, mgg, qmcflac 等)
     * @return 解密后的数据和真实扩展名
     */
    fun decryptQQ(encrypted: ByteArray, ext: String): Pair<ByteArray, String> {
        if (encrypted.isEmpty()) {
            throw IllegalArgumentException("Empty input")
        }

        val mask = if (ext == "mflac") {
            detectQQMaskFromEncrypted(encrypted) ?: QQMask(DEFAULT_QQ_MASK_58, DEFAULT_QQ_SUPER_58_A, DEFAULT_QQ_SUPER_58_B)
        } else {
            QQMask(DEFAULT_QQ_MASK_58, DEFAULT_QQ_SUPER_58_A, DEFAULT_QQ_SUPER_58_B)
        }

        val plain = mask.decrypt(encrypted)

        val realExt = when (ext) {
            "mflac", "qmcflac", "bkcflac" -> "flac"
            "mgg", "qmcogg" -> "ogg"
            "tkm" -> "m4a"
            else -> detectAudioExt(plain)
        }

        return Pair(plain, realExt)
    }

    /**
     * 检测音频格式
     */
    private fun detectAudioExt(data: ByteArray): String {
        if (data.size >= 4) {
            when {
                data.copyOfRange(0, 4).contentEquals("fLaC".toByteArray()) -> return "flac"
                data.copyOfRange(0, 3).contentEquals("ID3".toByteArray()) -> return "mp3"
                data.copyOfRange(0, 4).contentEquals("OggS".toByteArray()) -> return "ogg"
                data.copyOfRange(4, 8).contentEquals("ftyp".toByteArray()) -> return "m4a"
            }
        }
        return "mp3"
    }

    /**
     * 从加密数据中检测掩码
     */
    private fun detectQQMaskFromEncrypted(encrypted: ByteArray): QQMask? {
        val max = minOf(encrypted.size, 32768)

        for (i in 0 until max step 128) {
            if (i + 128 > encrypted.size) break

            try {
                val mask = QQMask.from128(encrypted.copyOfRange(i, i + 128))
                val head = mask.decrypt(encrypted.copyOfRange(0, 4))
                if (head.contentEquals("fLaC".toByteArray())) {
                    return mask
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    /**
     * QQ掩码类
     */
    private class QQMask(val matrix58: ByteArray, val superA: Byte, val superB: Byte) {
        private val matrix128: ByteArray = generateMask128From58()

        companion object {
            fun from128(matrix128: ByteArray): QQMask {
                if (matrix128.size != 128) {
                    throw IllegalArgumentException("Incorrect mask128 length")
                }

                val e = matrix128[0]
                val b = matrix128[8]
                val out = mutableListOf<Byte>()

                for (n in 0 until 8) {
                    val i = 16 * n
                    val o = 120 - i

                    if (matrix128[i] != e || matrix128[i + 8] != b) {
                        throw IllegalArgumentException("Decode mask-128 to mask-58 failed")
                    }

                    val a = matrix128.copyOfRange(i + 1, i + 8)
                    val c = ByteArray(7) { j ->
                        matrix128[o + 7 - j]
                    }

                    if (!a.contentEquals(c)) {
                        throw IllegalArgumentException("Decode mask-128 to mask-58 failed")
                    }

                    out.addAll(a.toList())
                }

                return QQMask(out.toByteArray(), e, b)
            }
        }

        private fun generateMask128From58(): ByteArray {
            val out = ByteArray(128)
            var idx = 0

            for (i in 0 until 8) {
                out[idx++] = superA
                val chunk = matrix58.copyOfRange(7 * i, 7 * i + 7)
                chunk.copyInto(out, idx)
                idx += 7
                out[idx++] = superB

                val revChunk = matrix58.copyOfRange(49 - 7 * i, 56 - 7 * i)
                for (j in revChunk.size - 1 downTo 0) {
                    out[idx++] = revChunk[j]
                }
            }

            return out
        }

        fun decrypt(encrypted: ByteArray): ByteArray {
            val out = encrypted.copyOf()
            var r = -1
            var n = -1

            for (i in out.indices) {
                r++
                n++
                if (r == 32768 || (r > 32768 && (r + 1) % 32768 == 0)) {
                    r++
                    n++
                }
                if (n >= 128) {
                    n -= 128
                }
                out[i] = (out[i].toInt() xor matrix128[n].toInt()).toByte()
            }

            return out
        }
    }
}
