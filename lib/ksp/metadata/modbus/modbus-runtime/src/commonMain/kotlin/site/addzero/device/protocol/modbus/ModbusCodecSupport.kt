package site.addzero.device.protocol.modbus

import site.addzero.device.protocol.modbus.model.ModbusCodec

object ModbusCodecSupport {
    fun decodeInt(codec: ModbusCodec, registers: List<Int>, registerOffset: Int): Int =
        when (codec) {
            ModbusCodec.U8 -> registers.getOrElse(registerOffset) { 0 } and 0xFF
            ModbusCodec.U16 -> registers.getOrElse(registerOffset) { 0 }
            ModbusCodec.U32_BE -> {
                val hi = registers.getOrElse(registerOffset) { 0 }
                val lo = registers.getOrElse(registerOffset + 1) { 0 }
                (hi shl 16) or (lo and 0xFFFF)
            }
            else -> error("Current codec does not support Int decode: $codec")
        }

    fun decodeBoolean(codec: ModbusCodec, registers: List<Int>, registerOffset: Int, bitOffset: Int): Boolean =
        when (codec) {
            ModbusCodec.BOOL_COIL -> registers.getOrElse(registerOffset) { 0 } != 0
            ModbusCodec.BIT_FLAG -> {
                val value = registers.getOrElse(registerOffset) { 0 }
                ((value shr bitOffset) and 0x1) == 1
            }
            else -> error("Current codec does not support Boolean decode: $codec")
        }

    fun decodeByteArray(
        codec: ModbusCodec,
        registers: List<Int>,
        registerOffset: Int,
        byteLength: Int,
    ): ByteArray {
        require(byteLength > 0) { "ByteArray length must be > 0" }
        require(codec == ModbusCodec.BYTE_ARRAY) { "Current codec does not support ByteArray decode: $codec" }
        val registerWidth = (byteLength + 1) / 2
        val bytes = ByteArray(registerWidth * 2)
        repeat(registerWidth) { index ->
            val value = registers.getOrElse(registerOffset + index) { 0 }
            bytes[index * 2] = ((value shr 8) and 0xFF).toByte()
            bytes[index * 2 + 1] = (value and 0xFF).toByte()
        }
        return bytes.copyOf(byteLength)
    }

    fun decodeString(
        codec: ModbusCodec,
        registers: List<Int>,
        registerOffset: Int,
        registerWidth: Int,
    ): String {
        require(registerWidth > 0) { "String register width must be > 0" }
        val bytes = ByteArray(registerWidth * 2)
        repeat(registerWidth) { index ->
            val value = registers.getOrElse(registerOffset + index) { 0 }
            bytes[index * 2] = ((value shr 8) and 0xFF).toByte()
            bytes[index * 2 + 1] = (value and 0xFF).toByte()
        }
        val effectiveLength = bytes.indexOf(0).let { index -> if (index >= 0) index else bytes.size }
        val payload = bytes.copyOf(effectiveLength)
        return when (codec) {
            ModbusCodec.STRING_ASCII -> {
                require(payload.all { byte -> byte.toInt() and 0x80 == 0 }) {
                    "ASCII register payload contains non-ASCII bytes"
                }
                payload.decodeToString()
            }
            ModbusCodec.STRING_UTF8 -> payload.decodeToString()
            else -> error("Current codec does not support String decode: $codec")
        }
    }

    fun encodeString(
        codec: ModbusCodec,
        value: String,
        registerWidth: Int,
    ): List<Int> {
        require(registerWidth > 0) { "String register width must be > 0" }
        val bytes =
            when (codec) {
                ModbusCodec.STRING_ASCII -> encodeAscii(value)
                ModbusCodec.STRING_UTF8 -> value.encodeToByteArray()
                else -> error("Current codec does not support String encode: $codec")
            }
        val byteCapacity = registerWidth * 2
        require(bytes.size < byteCapacity) {
            "Encoded string length ${bytes.size} exceeds register capacity ${byteCapacity - 1}, codec=$codec, value=$value"
        }
        val padded = ByteArray(byteCapacity)
        bytes.copyInto(padded, endIndex = bytes.size)
        return List(registerWidth) { index ->
            val high = padded[index * 2].toInt() and 0xFF
            val low = padded[index * 2 + 1].toInt() and 0xFF
            (high shl 8) or low
        }
    }

    fun encodeByteArray(
        codec: ModbusCodec,
        value: ByteArray,
        byteLength: Int,
    ): List<Int> {
        require(byteLength > 0) { "ByteArray length must be > 0" }
        require(codec == ModbusCodec.BYTE_ARRAY) { "Current codec does not support ByteArray encode: $codec" }
        require(value.size == byteLength) {
            "Encoded byte array length ${value.size} does not match expected length $byteLength"
        }
        val registerWidth = (byteLength + 1) / 2
        val padded = ByteArray(registerWidth * 2)
        value.copyInto(padded, endIndex = value.size)
        return List(registerWidth) { index ->
            val high = padded[index * 2].toInt() and 0xFF
            val low = padded[index * 2 + 1].toInt() and 0xFF
            (high shl 8) or low
        }
    }

    fun encodeValue(codec: ModbusCodec, value: String): List<Int> =
        when (codec) {
            ModbusCodec.AUTO ->
                error("AUTO must be resolved during code generation, not at runtime")
            ModbusCodec.BOOL_COIL -> listOf(if (value.toBooleanStrictOrNull() == true) 1 else 0)
            ModbusCodec.U8 -> {
                val number = value.toInt()
                require(number in 0..0xFF) {
                    "U8 codec expects value in 0..255, actual=$number"
                }
                listOf(number)
            }
            ModbusCodec.U16 -> listOf(value.toInt())
            ModbusCodec.U32_BE -> {
                val number = value.toLong()
                listOf(((number shr 16) and 0xFFFF).toInt(), (number and 0xFFFF).toInt())
            }
            ModbusCodec.BIT_FLAG ->
                listOf(
                    value.toBooleanStrictOrNull()?.let { booleanValue -> if (booleanValue) 1 else 0 }
                        ?: value.toInt(),
                )
            ModbusCodec.BYTE_ARRAY -> error("Use encodeByteArray(codec, value, byteLength) for byte array codecs")
            ModbusCodec.STRING_ASCII,
            ModbusCodec.STRING_UTF8 -> error("Use encodeString(codec, value, registerWidth) for string codecs")
        }

    private fun encodeAscii(value: String): ByteArray {
        require(value.all { char -> char.code in 0x00..0x7F }) {
            "ASCII codec does not support non-ASCII characters: $value"
        }
        return value.encodeToByteArray()
    }
}
