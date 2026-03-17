package site.addzero.biz.spec.iot.tdengine

/**
 * History query output DTO.
 */
data class TelemetryHistoryEntry(
    val identifier: String,
    val value: Any?,
    val updateTimeEpochMillis: Long?,
)
