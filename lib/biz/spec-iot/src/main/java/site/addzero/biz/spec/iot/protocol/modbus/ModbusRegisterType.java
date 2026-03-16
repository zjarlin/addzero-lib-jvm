package site.addzero.biz.spec.iot.protocol.modbus;

/**
 * Common Modbus register types.
 */
public enum ModbusRegisterType {

    COIL_STATUS("01"),
    INPUT_STATUS("02"),
    HOLDING_REGISTER("03"),
    INPUT_REGISTER("04");

    private final String code;

    ModbusRegisterType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
