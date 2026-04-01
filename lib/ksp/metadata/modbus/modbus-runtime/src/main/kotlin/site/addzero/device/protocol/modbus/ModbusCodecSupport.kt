package site.addzero.device.protocol.modbus

import site.addzero.device.protocol.modbus.model.ModbusCodec

object ModbusCodecSupport {
    fun decodeInt(codec: ModbusCodec, registers: List<Int>, registerOffset: Int): Int =
        when (codec) {
            ModbusCodec.U16 -> registers.getOrElse(registerOffset) { 0 }
            ModbusCodec.U32_BE -> {
                val hi = registers.getOrElse(registerOffset) { 0 }
                val lo = registers.getOrElse(registerOffset + 1) { 0 }
                (hi shl 16) or (lo and 0xFFFF)
            }
            else -> error("当前编解码器不支持整型解码：$codec")
        }

    fun decodeBoolean(codec: ModbusCodec, registers: List<Int>, registerOffset: Int, bitOffset: Int): Boolean =
        when (codec) {
            ModbusCodec.BOOL_COIL -> registers.getOrElse(registerOffset) { 0 } != 0
            ModbusCodec.BIT_FLAG -> {
                val value = registers.getOrElse(registerOffset) { 0 }
                ((value shr bitOffset) and 0x1) == 1
            }
            else -> error("当前编解码器不支持布尔解码：$codec")
        }

    fun decodeString(
        codec: ModbusCodec,
        registers: List<Int>,
        registerOffset: Int,
        registerWidth: Int,
    ): String {
        require(registerWidth > 0) { "字符串寄存器宽度必须大于 0" }
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
                    "ASCII 字符串寄存器中存在非 ASCII 字节"
                }
                payload.toString(Charsets.US_ASCII)
            }

            ModbusCodec.STRING_UTF8 -> payload.toString(Charsets.UTF_8)
            else -> error("当前编解码器不支持字符串解码：$codec")
        }
    }

    fun encodeString(
        codec: ModbusCodec,
        value: String,
        registerWidth: Int,
    ): List<Int> {
        require(registerWidth > 0) { "字符串寄存器宽度必须大于 0" }
        val bytes =
            when (codec) {
                ModbusCodec.STRING_ASCII -> encodeAscii(value)
                ModbusCodec.STRING_UTF8 -> value.toByteArray(Charsets.UTF_8)
                else -> error("当前编解码器不支持字符串编码：$codec")
            }
        val byteCapacity = registerWidth * 2
        require(bytes.size < byteCapacity) {
            "字符串编码后长度 ${bytes.size} 超出寄存器容量 ${byteCapacity - 1}，codec=$codec，value=$value"
        }
        val padded = ByteArray(byteCapacity)
        bytes.copyInto(padded, endIndex = bytes.size)
        return List(registerWidth) { index ->
            val high = padded[index * 2].toInt() and 0xFF
            val low = padded[index * 2 + 1].toInt() and 0xFF
            (high shl 8) or low
        }
    }

    fun encodeValue(codec: ModbusCodec, value: String): List<Int> =
        when (codec) {
            ModbusCodec.AUTO ->
                error("AUTO 编解码器必须在代码生成阶段完成推导，运行时不应直接参与编码")
            ModbusCodec.BOOL_COIL -> listOf(if (value.toBooleanStrictOrNull() == true) 1 else 0)
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
            ModbusCodec.STRING_ASCII,
            ModbusCodec.STRING_UTF8 -> error("字符串编码请使用 encodeString(codec, value, registerWidth)")
        }

    private fun encodeAscii(value: String): ByteArray {
        require(value.all { char -> char.code in 0x00..0x7F }) {
            "ASCII 编码不支持非 ASCII 字符：$value"
        }
        return value.toByteArray(Charsets.US_ASCII)
    }
}
