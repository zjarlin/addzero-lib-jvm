package site.addzero.biz.spec.iot

/**
 * Single telemetry value in a report.
 */
class TelemetryValue(
    identifier: String?,
    valueType: IotValueType?,
    val value: Any?,
) {

    val identifier: String = requireText(identifier, "identifier")
    val valueType: IotValueType = valueType ?: throw IllegalArgumentException("valueType must not be null")
}
