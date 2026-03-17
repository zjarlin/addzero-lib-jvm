package site.addzero.biz.spec.iot.protocol.modbus

import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.requireText

/**
 * Single Modbus write target.
 */
class ModbusWritePoint(
    pointId: String?,
    val slaveId: Int,
    val pointAddress: Int,
    registerType: ModbusRegisterType?,
    valueType: IotValueType?,
    val writeValue: String?,
) {

    val pointId: String = requireText(pointId, "pointId")
    val registerType: ModbusRegisterType = registerType ?: throw IllegalArgumentException(
        "registerType and valueType must not be null",
    )
    val valueType: IotValueType = valueType ?: throw IllegalArgumentException(
        "registerType and valueType must not be null",
    )
}
