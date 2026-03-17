package site.addzero.biz.spec.iot

import java.time.LocalDateTime
import java.util.LinkedHashMap

/**
 * Standardized telemetry envelope for protocol decoders and SQL builders.
 */
class TelemetryReport private constructor(
    val schemaRef: IotThingRef,
    val sourceRef: IotThingRef,
    val reportTime: LocalDateTime,
    val values: Map<String, TelemetryValue>,
) {

    class Builder {

        private var schemaRef: IotThingRef? = null
        private var sourceRef: IotThingRef? = null
        private var reportTime: LocalDateTime? = null
        private val values = linkedMapOf<String, TelemetryValue>()

        fun schemaRef(schemaRef: IotThingRef?) = apply {
            this.schemaRef = schemaRef
        }

        fun sourceRef(sourceRef: IotThingRef?) = apply {
            this.sourceRef = sourceRef
        }

        fun reportTime(reportTime: LocalDateTime?) = apply {
            this.reportTime = reportTime
        }

        fun addValue(value: TelemetryValue?) = apply {
            require(value != null) { "value must not be null" }
            values[value.identifier] = value
        }

        fun build(): TelemetryReport {
            return TelemetryReport(
                schemaRef = schemaRef ?: throw IllegalArgumentException("schemaRef must not be null"),
                sourceRef = sourceRef ?: throw IllegalArgumentException("sourceRef must not be null"),
                reportTime = reportTime ?: throw IllegalArgumentException("reportTime must not be null"),
                values = LinkedHashMap(values).toMap().also {
                    require(it.isNotEmpty()) { "telemetry values must not be empty" }
                },
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
