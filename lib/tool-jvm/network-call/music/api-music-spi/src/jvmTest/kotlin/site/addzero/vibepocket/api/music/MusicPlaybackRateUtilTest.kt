package site.addzero.vibepocket.api.music

import java.util.Base64
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MusicPlaybackRateUtilTest {

    @Test
    fun `changePlaybackRate should shrink wav data when speed increases`() {
        val input = makeWav()
        val normal = MusicPlaybackRateUtil.changePlaybackRate(input, 1.0)
        val faster = MusicPlaybackRateUtil.changePlaybackRate(input, 2.0)

        assertEquals("RIFF", readAscii(normal, 0, 4))
        assertEquals("WAVE", readAscii(normal, 8, 4))
        assertEquals("RIFF", readAscii(faster, 0, 4))
        assertEquals("WAVE", readAscii(faster, 8, 4))
        assertTrue(readIntLE(faster, 40) < readIntLE(normal, 40))
    }

    @Test
    fun `changePlaybackRate should accept mp3 and return wav`() {
        val input = Base64.getDecoder().decode(TEST_MP3_BASE64)
        val normal = MusicPlaybackRateUtil.changePlaybackRate(input, 1.0)
        val faster = MusicPlaybackRateUtil.changePlaybackRate(input, 1.25)

        assertEquals("RIFF", readAscii(normal, 0, 4))
        assertEquals("WAVE", readAscii(normal, 8, 4))
        assertEquals("RIFF", readAscii(faster, 0, 4))
        assertEquals("WAVE", readAscii(faster, 8, 4))
        assertTrue(readIntLE(faster, 40) < readIntLE(normal, 40))
    }

    private fun makeWav(sampleRate: Int = 8000, frames: Int = 8000): ByteArray {
        val pcm = ByteArray(frames * 2)
        for (index in 0 until frames) {
            val sample = (sin(2.0 * Math.PI * 440.0 * index / sampleRate) * 12000.0).toInt()
            writeShortLE(pcm, index * 2, sample)
        }

        val output = ByteArray(44 + pcm.size)
        writeAscii(output, 0, "RIFF")
        writeIntLE(output, 4, 36 + pcm.size)
        writeAscii(output, 8, "WAVE")
        writeAscii(output, 12, "fmt ")
        writeIntLE(output, 16, 16)
        writeShortLE(output, 20, 1)
        writeShortLE(output, 22, 1)
        writeIntLE(output, 24, sampleRate)
        writeIntLE(output, 28, sampleRate * 2)
        writeShortLE(output, 32, 2)
        writeShortLE(output, 34, 16)
        writeAscii(output, 36, "data")
        writeIntLE(output, 40, pcm.size)
        pcm.copyInto(output, destinationOffset = 44)
        return output
    }

    private fun readAscii(bytes: ByteArray, offset: Int, length: Int): String {
        return buildString(length) {
            for (index in 0 until length) {
                append((bytes[offset + index].toInt() and 0xFF).toChar())
            }
        }
    }

    private fun writeAscii(bytes: ByteArray, offset: Int, value: String) {
        for (index in value.indices) {
            bytes[offset + index] = value[index].code.toByte()
        }
    }

    private fun readIntLE(bytes: ByteArray, offset: Int): Int {
        val b0 = bytes[offset].toInt() and 0xFF
        val b1 = (bytes[offset + 1].toInt() and 0xFF) shl 8
        val b2 = (bytes[offset + 2].toInt() and 0xFF) shl 16
        val b3 = (bytes[offset + 3].toInt() and 0xFF) shl 24
        return b0 or b1 or b2 or b3
    }

    private fun writeShortLE(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    }

    private fun writeIntLE(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    companion object {
        private const val TEST_MP3_BASE64 =
            "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"
    }
}
