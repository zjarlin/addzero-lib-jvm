package site.addzero.biz.spec.iot.protocol.s7

import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.TelemetryReport
import site.addzero.biz.spec.iot.TelemetryValue
import java.nio.ByteBuffer
import java.time.LocalDateTime

/**
 * Decodes S7 byte payloads into telemetry values.
 */
class S7TelemetryDecoder {

    fun decode(payload: ByteArray, bindings: List<S7PropertyBinding>): Map<String, TelemetryValue> {
        val buffer = ByteBuffer.wrap(payload)
        val decoded = linkedMapOf<String, TelemetryValue>()
        bindings.forEach { binding ->
            val value = decodeValue(buffer, binding)
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
        payload: ByteArray,
        bindings: List<S7PropertyBinding>,
    ): TelemetryReport {
        val decoded = decode(payload, bindings)
        val builder = TelemetryReport.builder()
            .schemaRef(schemaRef)
            .sourceRef(sourceRef)
            .reportTime(reportTime)
        decoded.values.forEach { value ->
            builder.addValue(value)
        }
        return builder.build()
    }

    private fun decodeValue(buffer: ByteBuffer, binding: S7PropertyBinding): Any? {
        return when (binding.valueType) {
            IotValueType.INT32 -> buffer.getInt(binding.byteOffset)
            IotValueType.FLOAT32 -> buffer.getFloat(binding.byteOffset)
            IotValueType.BOOLEAN -> (buffer.get(binding.byteOffset).toInt() and (1 shl binding.bitOffset)) != 0
        }
    }
}
