package site.addzero.biz.spec.iot.protocol.modbus

/**
 * Common Modbus register types.
 */
enum class ModbusRegisterType(
    val code: String,
) {
    COIL_STATUS("01"),
    INPUT_STATUS("02"),
    HOLDING_REGISTER("03"),
    INPUT_REGISTER("04"),
}
