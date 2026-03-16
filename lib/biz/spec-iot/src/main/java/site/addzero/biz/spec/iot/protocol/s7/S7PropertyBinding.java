package site.addzero.biz.spec.iot.protocol.s7;

import site.addzero.biz.spec.iot.IotValueType;

/**
 * Property-to-byte binding for S7 payload decoding.
 */
public final class S7PropertyBinding {

    private final String identifier;
    private final IotValueType valueType;
    private final int byteOffset;
    private final int bitOffset;

    public S7PropertyBinding(String identifier, IotValueType valueType, int byteOffset, int bitOffset) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier must not be blank");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("valueType must not be null");
        }
        this.identifier = identifier.trim();
        this.valueType = valueType;
        this.byteOffset = byteOffset;
        this.bitOffset = bitOffset;
    }

    public String getIdentifier() {
        return identifier;
    }

    public IotValueType getValueType() {
        return valueType;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public int getBitOffset() {
        return bitOffset;
    }
}
