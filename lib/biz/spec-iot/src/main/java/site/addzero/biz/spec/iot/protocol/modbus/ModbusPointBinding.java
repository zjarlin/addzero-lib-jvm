package site.addzero.biz.spec.iot.protocol.modbus;

import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.IotValueType;

/**
 * Modbus point binding used for reading and telemetry decoding.
 */
public final class ModbusPointBinding {

    private final String pointId;
    private final IotThingRef schemaRef;
    private final IotThingRef sourceRef;
    private final int slaveId;
    private final int pointAddress;
    private final ModbusRegisterType registerType;
    private final String identifier;
    private final IotValueType valueType;

    public ModbusPointBinding(
            String pointId,
            IotThingRef schemaRef,
            IotThingRef sourceRef,
            int slaveId,
            int pointAddress,
            ModbusRegisterType registerType,
            String identifier,
            IotValueType valueType
    ) {
        if (pointId == null || pointId.trim().isEmpty()) {
            throw new IllegalArgumentException("pointId must not be blank");
        }
        if (schemaRef == null || sourceRef == null || registerType == null || valueType == null) {
            throw new IllegalArgumentException("schemaRef, sourceRef, registerType and valueType must not be null");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier must not be blank");
        }
        this.pointId = pointId.trim();
        this.schemaRef = schemaRef;
        this.sourceRef = sourceRef;
        this.slaveId = slaveId;
        this.pointAddress = pointAddress;
        this.registerType = registerType;
        this.identifier = identifier.trim();
        this.valueType = valueType;
    }

    public String getPointId() {
        return pointId;
    }

    public IotThingRef getSchemaRef() {
        return schemaRef;
    }

    public IotThingRef getSourceRef() {
        return sourceRef;
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

    public String getIdentifier() {
        return identifier;
    }

    public IotValueType getValueType() {
        return valueType;
    }
}
