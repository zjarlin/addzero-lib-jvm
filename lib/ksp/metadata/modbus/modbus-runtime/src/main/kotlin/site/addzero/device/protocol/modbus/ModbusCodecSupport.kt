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
        }
}
