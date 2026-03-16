package site.addzero.biz.spec.iot.protocol.s7;

import com.github.xingshuangs.iot.protocol.s7.enums.EPlcType;
import com.github.xingshuangs.iot.protocol.s7.service.MultiAddressRead;
import com.github.xingshuangs.iot.protocol.s7.service.S7PLC;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thin S7 client wrapper with connection caching.
 */
public final class S7Client {

    private static final Map<String, S7PLC> CONNECTIONS = new ConcurrentHashMap<String, S7PLC>();

    public S7PLC getConnection(S7ConnectionOptions options) {
        S7PLC existing = CONNECTIONS.get(options.cacheKey());
        if (existing != null) {
            return existing;
        }
        synchronized (S7Client.class) {
            S7PLC current = CONNECTIONS.get(options.cacheKey());
            if (current != null) {
                return current;
            }
            S7PLC created = new S7PLC(EPlcType.S1200, options.getHost(), options.getPort());
            CONNECTIONS.put(options.cacheKey(), created);
            return created;
        }
    }

    public void disconnect(S7ConnectionOptions options) {
        S7PLC existing = CONNECTIONS.remove(options.cacheKey());
        if (existing != null) {
            existing.close();
        }
    }

    public boolean verifyConnection(S7ConnectionOptions options) {
        try {
            getConnection(options).readByte(S7DataArea.M.getCode() + 0 + "." + 0);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public List<byte[]> batchRead(S7ConnectionOptions options, List<S7ReadBlock> blocks) {
        try {
            MultiAddressRead addressRead = new MultiAddressRead();
            for (S7ReadBlock block : blocks) {
                addressRead.addData(
                        block.getDataArea().getCode() + block.getDataAreaNumber() + "." + block.getStartAddress(),
                        block.getDataSize()
                );
            }
            return getConnection(options).readMultiByte(addressRead);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to batch read S7 blocks", ex);
        }
    }

    public void singleWrite(S7ConnectionOptions options, S7WritePoint writePoint) {
        try {
            S7PLC plc = getConnection(options);
            String address = writePoint.getDataArea().getCode()
                    + writePoint.getDataAreaNumber()
                    + "."
                    + writePoint.getByteOffset()
                    + "."
                    + writePoint.getBitOffset();
            if (writePoint.getValueType() == site.addzero.biz.spec.iot.IotValueType.BOOLEAN) {
                plc.writeBoolean(address, Boolean.parseBoolean(writePoint.getWriteValue()));
                return;
            }
            if (writePoint.getValueType() == site.addzero.biz.spec.iot.IotValueType.FLOAT32) {
                plc.writeFloat32(address, Float.parseFloat(writePoint.getWriteValue()));
                return;
            }
            if (writePoint.getValueType() == site.addzero.biz.spec.iot.IotValueType.INT32) {
                plc.writeInt32(address, Integer.parseInt(writePoint.getWriteValue()));
                return;
            }
            throw new IllegalArgumentException("Unsupported S7 write value type: " + writePoint.getValueType());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to write S7 point " + writePoint.getPropertyIdentifier(), ex);
        }
    }
}
