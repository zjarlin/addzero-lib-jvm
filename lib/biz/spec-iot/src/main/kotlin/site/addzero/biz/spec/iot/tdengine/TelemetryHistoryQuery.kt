package site.addzero.biz.spec.iot.tdengine

import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.requireText
import java.time.LocalDateTime

/**
 * History query input DTO.
 */
class TelemetryHistoryQuery(
    schemaRef: IotThingRef?,
    sourceRef: IotThingRef?,
    identifier: String?,
    fromTime: LocalDateTime?,
    toTime: LocalDateTime?,
) {

    val schemaRef: IotThingRef = schemaRef ?: throw IllegalArgumentException("schemaRef and sourceRef must not be null")
    val sourceRef: IotThingRef = sourceRef ?: throw IllegalArgumentException("schemaRef and sourceRef must not be null")
    val identifier: String = requireText(identifier, "identifier")
    val fromTime: LocalDateTime = fromTime ?: throw IllegalArgumentException("fromTime and toTime must not be null")
    val toTime: LocalDateTime = toTime ?: throw IllegalArgumentException("fromTime and toTime must not be null")
}
