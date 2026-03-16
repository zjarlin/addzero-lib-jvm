package site.addzero.biz.spec.iot.protocol.s7;

import site.addzero.biz.spec.iot.IotValueType;

/**
 * Single S7 write target.
 */
public final class S7WritePoint {

    private final String propertyIdentifier;
    private final IotValueType valueType;
    private final S7DataArea dataArea;
    private final int dataAreaNumber;
    private final int byteOffset;
    private final int bitOffset;
    private final String writeValue;

    public S7WritePoint(
            String propertyIdentifier,
            IotValueType valueType,
            S7DataArea dataArea,
            int dataAreaNumber,
            int byteOffset,
            int bitOffset,
            String writeValue
    ) {
        if (propertyIdentifier == null || propertyIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("propertyIdentifier must not be blank");
        }
        if (valueType == null || dataArea == null) {
            throw new IllegalArgumentException("valueType and dataArea must not be null");
        }
        this.propertyIdentifier = propertyIdentifier.trim();
        this.valueType = valueType;
        this.dataArea = dataArea;
        this.dataAreaNumber = dataAreaNumber;
        this.byteOffset = byteOffset;
        this.bitOffset = bitOffset;
        this.writeValue = writeValue;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public IotValueType getValueType() {
        return valueType;
    }

    public S7DataArea getDataArea() {
        return dataArea;
    }

    public int getDataAreaNumber() {
        return dataAreaNumber;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public int getBitOffset() {
        return bitOffset;
    }

    public String getWriteValue() {
        return writeValue;
    }
}
