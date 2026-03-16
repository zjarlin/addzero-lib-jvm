package site.addzero.biz.spec.iot.protocol.s7;

import site.addzero.biz.spec.iot.IotThingRef;

/**
 * One S7 block read range associated with a telemetry source.
 */
public final class S7ReadBlock {

    private final IotThingRef schemaRef;
    private final IotThingRef sourceRef;
    private final S7DataArea dataArea;
    private final int dataAreaNumber;
    private final int startAddress;
    private final int dataSize;

    public S7ReadBlock(IotThingRef schemaRef, IotThingRef sourceRef, S7DataArea dataArea, int dataAreaNumber, int startAddress, int dataSize) {
        if (schemaRef == null || sourceRef == null || dataArea == null) {
            throw new IllegalArgumentException("schemaRef, sourceRef and dataArea must not be null");
        }
        this.schemaRef = schemaRef;
        this.sourceRef = sourceRef;
        this.dataArea = dataArea;
        this.dataAreaNumber = dataAreaNumber;
        this.startAddress = startAddress;
        this.dataSize = dataSize;
    }

    public IotThingRef getSchemaRef() {
        return schemaRef;
    }

    public IotThingRef getSourceRef() {
        return sourceRef;
    }

    public S7DataArea getDataArea() {
        return dataArea;
    }

    public int getDataAreaNumber() {
        return dataAreaNumber;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getDataSize() {
        return dataSize;
    }
}
