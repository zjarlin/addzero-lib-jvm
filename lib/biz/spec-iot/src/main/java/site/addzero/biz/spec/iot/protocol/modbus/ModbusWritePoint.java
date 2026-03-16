package site.addzero.biz.spec.iot.protocol.modbus;

import site.addzero.biz.spec.iot.IotValueType;

/**
 * Single Modbus write target.
 */
public final class ModbusWritePoint {

    private final String pointId;
    private final int slaveId;
    private final int pointAddress;
    private final ModbusRegisterType registerType;
    private final IotValueType valueType;
    private final String writeValue;

    public ModbusWritePoint(
            String pointId,
            int slaveId,
            int pointAddress,
            ModbusRegisterType registerType,
            IotValueType valueType,
            String writeValue
    ) {
        if (pointId == null || pointId.trim().isEmpty()) {
            throw new IllegalArgumentException("pointId must not be blank");
        }
        if (registerType == null || valueType == null) {
            throw new IllegalArgumentException("registerType and valueType must not be null");
        }
        this.pointId = pointId.trim();
        this.slaveId = slaveId;
        this.pointAddress = pointAddress;
        this.registerType = registerType;
        this.valueType = valueType;
        this.writeValue = writeValue;
    }

    public String getPointId() {
        return pointId;
    }

    public int getSlaveId() {
        return slaveId;
    }

    public int getPointAddress() {
        return pointAddress;
    }

    public ModbusRegisterType getRegisterType() {
        return registerType;
    }

    public IotValueType getValueType() {
        return valueType;
    }

    public String getWriteValue() {
        return writeValue;
    }
}
