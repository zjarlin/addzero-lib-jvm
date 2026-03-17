package site.addzero.biz.spec.iot.protocol.modbus

import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.requireText

/**
 * Modbus point binding used for reading and telemetry decoding.
 */
class ModbusPointBinding(
    pointId: String?,
    schemaRef: IotThingRef?,
    sourceRef: IotThingRef?,
    val slaveId: Int,
    val pointAddress: Int,
    registerType: ModbusRegisterType?,
    identifier: String?,
    valueType: IotValueType?,
) {

    val pointId: String = requireText(pointId, "pointId")
    val schemaRef: IotThingRef = schemaRef ?: throw IllegalArgumentException(
        "schemaRef, sourceRef, registerType and valueType must not be null",
    )
    val sourceRef: IotThingRef = sourceRef ?: throw IllegalArgumentException(
        "schemaRef, sourceRef, registerType and valueType must not be null",
    )
    val registerType: ModbusRegisterType = registerType ?: throw IllegalArgumentException(
        "schemaRef, sourceRef, registerType and valueType must not be null",
    )
    val identifier: String = requireText(identifier, "identifier")
    val valueType: IotValueType = valueType ?: throw IllegalArgumentException(
        "schemaRef, sourceRef, registerType and valueType must not be null",
    )
}
