package site.addzero.vibepocket.api.music

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.SampleBuffer
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * 不依赖 ffmpeg 的简单倍速处理工具。
 *
 * 当前实现支持输入：
 * - WAV
 * - MP3
 *
 * 当前统一输出：
 * - WAV
 * - PCM
 * - 16-bit little-endian
 *
 * 这里的倍速是最小可用实现。
 * 它通过重采样改变播放速度，输出会更短或更长；
 * 不做高阶的“变速不变调”时间伸缩处理。
 */
object MusicPlaybackRateUtil {

    fun changePlaybackRate(input: ByteArray, playbackRate: Double): ByteArray {
        requirePlaybackRate(playbackRate)

        val wav = decodeToPcm16Wave(input)
        val outputData = resamplePcm16(
            pcmData = wav.data,
            channels = wav.channels,
            blockAlign = wav.blockAlign,
            playbackRate = playbackRate,
        )
        return buildWav(
            channels = wav.channels,
            sampleRate = wav.sampleRate,
            bitsPerSample = wav.bitsPerSample,
            pcmData = outputData,
        )
    }

    fun changePlaybackRate(inputPathOrUrl: String, playbackRate: Double): ByteArray {
        return changePlaybackRate(
            input = readInputBytes(inputPathOrUrl),
            playbackRate = playbackRate,
        )
    }

    fun changePlaybackRateToFile(
        input: ByteArray,
        playbackRate: Double,
        outputPath: String,
    ): String {
        val output = changePlaybackRate(input, playbackRate)
        writeBytes(outputPath, output)
        return outputPath
    }

    fun changePlaybackRateToFile(
        inputPathOrUrl: String,
        playbackRate: Double,
        outputPath: String? = null,
    ): String {
        val resolvedOutputPath = outputPath ?: buildDefaultOutputPath(inputPathOrUrl, playbackRate)
        val output = changePlaybackRate(inputPathOrUrl, playbackRate)
        writeBytes(resolvedOutputPath, output)
        return resolvedOutputPath
    }

    private fun requirePlaybackRate(playbackRate: Double) {
        require(playbackRate.isFinite()) {
            "playbackRate must be finite."
        }
        require(playbackRate > 0.0) {
            "playbackRate must be greater than 0."
        }
    }

    private fun readInputBytes(inputPathOrUrl: String): ByteArray {
        return if (isHttpUrl(inputPathOrUrl)) {
            URL(inputPathOrUrl).openStream().use { it.readBytes() }
        } else {
            Files.readAllBytes(Paths.get(inputPathOrUrl))
        }
    }

    private fun writeBytes(outputPath: String, bytes: ByteArray) {
        val path = Paths.get(outputPath)
        val parent = path.parent
        if (parent != null) {
            Files.createDirectories(parent)
        }
        Files.write(path, bytes)
    }

    private fun buildDefaultOutputPath(inputPathOrUrl: String, playbackRate: Double): String {
        require(!isHttpUrl(inputPathOrUrl)) {
            "outputPath is required when input is a URL."
        }

        val path = Paths.get(inputPathOrUrl)
        val fileName = path.fileName.toString()
        val dotIndex = fileName.lastIndexOf('.')
        val baseName = if (dotIndex >= 0) fileName.substring(0, dotIndex) else fileName
        val rateSuffix = playbackRate.toString().replace('.', '_')
        val outputFileName = "${baseName}_${rateSuffix}x.wav"
        val parent = path.parent ?: Paths.get(".")
        return parent.resolve(outputFileName).toString()
    }

    private fun isHttpUrl(value: String): Boolean {
        return value.startsWith("http://") || value.startsWith("https://")
    }

    private fun decodeToPcm16Wave(bytes: ByteArray): ParsedAudio {
        return when (detectInputFormat(bytes)) {
            InputAudioFormat.WAV -> parseWav(bytes)
            InputAudioFormat.MP3 -> decodeMp3(bytes)
        }
    }

    private fun detectInputFormat(bytes: ByteArray): InputAudioFormat {
        require(bytes.isNotEmpty()) {
            "Input audio is empty."
        }
        if (looksLikeWav(bytes)) {
            return InputAudioFormat.WAV
        }
        if (looksLikeMp3(bytes)) {
            return InputAudioFormat.MP3
        }
        error("Unsupported audio format. Only WAV and MP3 are supported.")
    }

    private fun looksLikeWav(bytes: ByteArray): Boolean {
        return bytes.size >= 12 &&
            readAscii(bytes, 0, 4) == "RIFF" &&
            readAscii(bytes, 8, 4) == "WAVE"
    }

    private fun looksLikeMp3(bytes: ByteArray): Boolean {
        if (bytes.size < 3) {
            return false
        }

        if (readAscii(bytes, 0, 3) == "ID3") {
            return true
        }

        val first = bytes[0].toInt() and 0xFF
        val second = bytes[1].toInt() and 0xFF
        return first == 0xFF && (second and 0xE0) == 0xE0
    }

    private fun parseWav(bytes: ByteArray): ParsedAudio {
        require(bytes.size >= 44) {
            "Input is too small to be a valid WAV file."
        }
        require(readAscii(bytes, 0, 4) == "RIFF") {
            "Only RIFF WAV is supported."
        }
        require(readAscii(bytes, 8, 4) == "WAVE") {
            "Only WAVE format is supported."
        }

        var offset = 12
        var audioFormat = -1
        var channels = -1
        var sampleRate = -1
        var blockAlign = -1
        var bitsPerSample = -1
        var dataOffset = -1
        var dataSize = -1

        while (offset + 8 <= bytes.size) {
            val chunkId = readAscii(bytes, offset, 4)
            val chunkSize = readIntLE(bytes, offset + 4)
            val chunkDataStart = offset + 8
            val chunkDataEnd = chunkDataStart + chunkSize

            require(chunkSize >= 0 && chunkDataEnd <= bytes.size) {
                "Invalid WAV chunk: $chunkId"
            }

            when (chunkId) {
                "fmt " -> {
                    require(chunkSize >= 16) {
                        "Invalid fmt chunk size: $chunkSize"
                    }
                    audioFormat = readShortLE(bytes, chunkDataStart).toInt()
                    channels = readShortLE(bytes, chunkDataStart + 2).toInt()
                    sampleRate = readIntLE(bytes, chunkDataStart + 4)
                    blockAlign = readShortLE(bytes, chunkDataStart + 12).toInt()
                    bitsPerSample = readShortLE(bytes, chunkDataStart + 14).toInt()
                }

                "data" -> {
                    dataOffset = chunkDataStart
                    dataSize = chunkSize
                }
            }

            offset = chunkDataEnd + (chunkSize and 1)
        }

        require(audioFormat == 1) {
            "Only PCM WAV is supported. audioFormat=$audioFormat"
        }
        require(bitsPerSample == 16) {
            "Only 16-bit WAV is supported. bitsPerSample=$bitsPerSample"
        }
        require(channels > 0) {
            "Invalid channel count: $channels"
        }
        require(sampleRate > 0) {
            "Invalid sampleRate: $sampleRate"
        }
        require(blockAlign == channels * 2) {
            "Only PCM16 little-endian WAV is supported. blockAlign=$blockAlign"
        }
        require(dataOffset >= 0 && dataSize >= 0) {
            "WAV data chunk not found."
        }
        require(dataSize % blockAlign == 0) {
            "WAV data size is not aligned to frame size."
        }

        return ParsedAudio(
            channels = channels,
            sampleRate = sampleRate,
            bitsPerSample = bitsPerSample,
            blockAlign = blockAlign,
            data = bytes.copyOfRange(dataOffset, dataOffset + dataSize),
        )
    }

    private fun decodeMp3(bytes: ByteArray): ParsedAudio {
        val output = ByteArrayOutputStream()
        var sampleRate = -1
        var channels = -1

        ByteArrayInputStream(bytes).use { input ->
            val bitstream = Bitstream(input)
            val decoder = Decoder()

            try {
                while (true) {
                    val header = bitstream.readFrame() ?: break

                    try {
                        val sampleBuffer = decoder.decodeFrame(header, bitstream) as SampleBuffer
                        if (sampleRate < 0) {
                            sampleRate = sampleBuffer.sampleFrequency
                        }
                        if (channels < 0) {
                            channels = sampleBuffer.channelCount
                        }

                        val samples = sampleBuffer.buffer
                        val sampleCount = sampleBuffer.bufferLength
                        for (index in 0 until sampleCount) {
                            writeShortLE(output, samples[index].toInt())
                        }
                    } finally {
                        bitstream.closeFrame()
                    }
                }
            } finally {
                bitstream.close()
            }
        }

        require(sampleRate > 0) {
            "Unable to decode MP3 sample rate."
        }
        require(channels > 0) {
            "Unable to decode MP3 channels."
        }

        val pcmData = output.toByteArray()
        val blockAlign = channels * 2
        require(pcmData.isNotEmpty()) {
            "Decoded MP3 PCM data is empty."
        }
        require(pcmData.size % blockAlign == 0) {
            "Decoded MP3 PCM data is not aligned to frame size."
        }

        return ParsedAudio(
            channels = channels,
            sampleRate = sampleRate,
            bitsPerSample = 16,
            blockAlign = blockAlign,
            data = pcmData,
        )
    }

    private fun resamplePcm16(
        pcmData: ByteArray,
        channels: Int,
        blockAlign: Int,
        playbackRate: Double,
    ): ByteArray {
        val totalFrames = pcmData.size / blockAlign
        require(totalFrames > 0) {
            "PCM data is empty."
        }

        val outputFrames = (totalFrames / playbackRate).roundToInt().coerceAtLeast(1)
        val output = ByteArray(outputFrames * blockAlign)

        for (frameIndex in 0 until outputFrames) {
            val sourcePosition = frameIndex * playbackRate
            val leftFrame = floor(sourcePosition).toInt().coerceIn(0, totalFrames - 1)
            val rightFrame = (leftFrame + 1).coerceAtMost(totalFrames - 1)
            val fraction = sourcePosition - leftFrame

            for (channel in 0 until channels) {
                val leftOffset = leftFrame * blockAlign + channel * 2
                val rightOffset = rightFrame * blockAlign + channel * 2
                val outputOffset = frameIndex * blockAlign + channel * 2

                val leftSample = readShortLE(pcmData, leftOffset).toInt()
                val rightSample = readShortLE(pcmData, rightOffset).toInt()
                val mixed = (leftSample + (rightSample - leftSample) * fraction)
                    .roundToInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

                writeShortLE(output, outputOffset, mixed)
            }
        }

        return output
    }

    private fun buildWav(
        channels: Int,
        sampleRate: Int,
        bitsPerSample: Int,
        pcmData: ByteArray,
    ): ByteArray {
        val blockAlign = channels * (bitsPerSample / 8)
        val byteRate = sampleRate * blockAlign
        val riffChunkSize = 36 + pcmData.size
        val output = ByteArray(44 + pcmData.size)

        writeAscii(output, 0, "RIFF")
        writeIntLE(output, 4, riffChunkSize)
        writeAscii(output, 8, "WAVE")
        writeAscii(output, 12, "fmt ")
        writeIntLE(output, 16, 16)
        writeShortLE(output, 20, 1)
        writeShortLE(output, 22, channels)
        writeIntLE(output, 24, sampleRate)
        writeIntLE(output, 28, byteRate)
        writeShortLE(output, 32, blockAlign)
        writeShortLE(output, 34, bitsPerSample)
        writeAscii(output, 36, "data")
        writeIntLE(output, 40, pcmData.size)
        pcmData.copyInto(output, destinationOffset = 44)

        return output
    }

    private fun readAscii(bytes: ByteArray, offset: Int, length: Int): String {
        require(offset >= 0 && offset + length <= bytes.size) {
            "Invalid ASCII read range."
        }
        return buildString(length) {
            for (index in 0 until length) {
                append((bytes[offset + index].toInt() and 0xFF).toChar())
            }
        }
    }

    private fun writeAscii(bytes: ByteArray, offset: Int, value: String) {
        require(value.length == 4) {
            "ASCII chunk id must be 4 characters."
        }
        for (index in value.indices) {
            bytes[offset + index] = value[index].code.toByte()
        }
    }

    private fun readShortLE(bytes: ByteArray, offset: Int): Short {
        val low = bytes[offset].toInt() and 0xFF
        val high = bytes[offset + 1].toInt() shl 8
        return (low or high).toShort()
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

    private fun writeShortLE(output: ByteArrayOutputStream, value: Int) {
        output.write(value and 0xFF)
        output.write((value ushr 8) and 0xFF)
    }

    private fun writeIntLE(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    private enum class InputAudioFormat {
        WAV,
        MP3,
    }

    private data class ParsedAudio(
        val channels: Int,
        val sampleRate: Int,
        val bitsPerSample: Int,
        val blockAlign: Int,
        val data: ByteArray,
    )
}
