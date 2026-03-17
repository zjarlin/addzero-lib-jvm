package site.addzero.biz.spec.iot.protocol.modbus

import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.TelemetryReport
import site.addzero.biz.spec.iot.TelemetryValue
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime

/**
 * Decodes raw Modbus locator values into typed telemetry.
 */
class ModbusTelemetryDecoder {

    fun decode(rawValues: Map<String, Any?>, bindings: List<ModbusPointBinding>): Map<String, TelemetryValue> {
        val decoded = linkedMapOf<String, TelemetryValue>()
        bindings.forEach { binding ->
            val rawValue = rawValues[binding.pointId] ?: return@forEach
            val value = decodeValue(binding, rawValue)
            if (value != null) {
                decoded[binding.identifier] = TelemetryValue(binding.identifier, binding.valueType, value)
            }
        }
        return decoded.toMap()
    }

    fun decodeReport(
        schemaRef: IotThingRef,
        sourceRef: IotThingRef,
        reportTime: LocalDateTime,
        rawValues: Map<String, Any?>,
        bindings: List<ModbusPointBinding>,
    ): TelemetryReport {
        val decoded = decode(rawValues, bindings)
        val builder = TelemetryReport.builder()
            .schemaRef(schemaRef)
            .sourceRef(sourceRef)
            .reportTime(reportTime)
        decoded.values.forEach { value ->
            builder.addValue(value)
        }
        return builder.build()
    }

    private fun decodeValue(binding: ModbusPointBinding, rawValue: Any): Any? {
        return when (binding.valueType) {
            IotValueType.FLOAT32 -> {
                if (rawValue is Number) {
                    rawValue.toFloat()
                } else {
                    null
                }
            }

            IotValueType.INT32 -> {
                if (rawValue is Number) {
                    extractIntFromSwappedFloat(rawValue.toFloat())
                } else {
                    null
                }
            }

            IotValueType.BOOLEAN -> {
                when (rawValue) {
                    is Boolean -> rawValue
                    is Number -> extractBooleanFromSwappedFloat(rawValue.toFloat())
                    else -> null
                }
            }
        }
    }

    private fun extractIntFromSwappedFloat(value: Float): Int {
        val buffer = ByteBuffer.allocate(4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putFloat(value)
        val bytes = buffer.array()
        val highByte = bytes[1].toInt() and 0xFF
        val lowByte = bytes[0].toInt() and 0xFF
        return (highByte shl 8) or lowByte
    }

    private fun extractBooleanFromSwappedFloat(value: Float): Boolean {
        val buffer = ByteBuffer.allocate(4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putFloat(value)
        val bytes = buffer.array()
        return (bytes[0].toInt() and 0x01) == 1
    }
}
