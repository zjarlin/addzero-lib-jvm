package site.addzero.biz.spec.iot.tdengine

import site.addzero.biz.spec.iot.IotThingRef

/**
 * Latest-value query output DTO.
 */
data class TelemetryLatestEntry(
    val sourceRef: IotThingRef,
    val identifier: String,
    val value: Any?,
    val updateTimeEpochMillis: Long?,
)
