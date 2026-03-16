package site.addzero.biz.spec.iot.protocol.modbus;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.msg.WriteCoilRequest;
import com.serotonin.modbus4j.msg.WriteCoilResponse;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thin Modbus TCP client wrapper with connection caching.
 */
public final class ModbusClient {

    private static final ModbusFactory FACTORY = new ModbusFactory();
    private static final Map<String, ModbusMaster> MASTERS = new ConcurrentHashMap<String, ModbusMaster>();

    public ModbusMaster getMaster(ModbusConnectionOptions options) {
        ModbusMaster existing = MASTERS.get(options.cacheKey());
        if (existing != null) {
            return existing;
        }
        synchronized (ModbusClient.class) {
            ModbusMaster current = MASTERS.get(options.cacheKey());
            if (current != null) {
                return current;
            }
            try {
                IpParameters parameters = new IpParameters();
                parameters.setHost(options.getHost());
                parameters.setPort(options.getPort());
                ModbusMaster created = FACTORY.createTcpMaster(parameters, false);
                created.init();
                MASTERS.put(options.cacheKey(), created);
                return created;
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to initialize Modbus master", ex);
            }
        }
    }

    public void close(ModbusConnectionOptions options) {
        ModbusMaster existing = MASTERS.remove(options.cacheKey());
        if (existing != null) {
            try {
                existing.destroy();
            } catch (Exception ignored) {
                // Ignore close failures.
            }
        }
    }

    public boolean verifyConnection(ModbusConnectionOptions options) {
        try {
            BatchRead<String> testBatch = new BatchRead<String>();
            testBatch.addLocator("health_check", BaseLocator.inputRegister(1, 0, DataType.TWO_BYTE_INT_UNSIGNED));
            testBatch.setContiguousRequests(true);
            getMaster(options).send(testBatch);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public Map<String, Object> batchReadRaw(ModbusConnectionOptions options, List<ModbusPointBinding> points) {
        BatchRead<String> batch = new BatchRead<String>();
        for (ModbusPointBinding point : points) {
            if (point.getRegisterType() == ModbusRegisterType.COIL_STATUS) {
                batch.addLocator(point.getPointId(), BaseLocator.coilStatus(point.getSlaveId(), point.getPointAddress()));
            } else if (point.getRegisterType() == ModbusRegisterType.INPUT_STATUS) {
                batch.addLocator(point.getPointId(), BaseLocator.inputStatus(point.getSlaveId(), point.getPointAddress()));
            } else if (point.getRegisterType() == ModbusRegisterType.HOLDING_REGISTER) {
                batch.addLocator(point.getPointId(), BaseLocator.holdingRegister(point.getSlaveId(), point.getPointAddress(), DataType.FOUR_BYTE_FLOAT_SWAPPED));
            } else if (point.getRegisterType() == ModbusRegisterType.INPUT_REGISTER) {
                batch.addLocator(point.getPointId(), BaseLocator.inputRegister(point.getSlaveId(), point.getPointAddress(), DataType.FOUR_BYTE_FLOAT_SWAPPED));
            }
        }

        try {
            batch.setContiguousRequests(true);
            BatchResults<String> results = getMaster(options).send(batch);
            Map<String, Object> rawValues = new LinkedHashMap<String, Object>();
            for (ModbusPointBinding point : points) {
                rawValues.put(point.getPointId(), results.getValue(point.getPointId()));
            }
            return rawValues;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to batch read Modbus points", ex);
        }
    }

    public boolean singleWritePoint(ModbusConnectionOptions options, ModbusWritePoint point) {
        try {
            ModbusMaster master = getMaster(options);
            if (point.getRegisterType() == ModbusRegisterType.COIL_STATUS) {
                WriteCoilResponse response = (WriteCoilResponse) master.send(
                        new WriteCoilRequest(point.getSlaveId(), point.getPointAddress(), Boolean.parseBoolean(point.getWriteValue()))
                );
                return !response.isException();
            }
            if (point.getRegisterType() == ModbusRegisterType.HOLDING_REGISTER) {
                String writeValue = point.getWriteValue();
                if (point.getValueType() == site.addzero.biz.spec.iot.IotValueType.BOOLEAN) {
                    writeValue = Boolean.parseBoolean(writeValue) ? "1" : "0";
                }
                WriteRegisterResponse response = (WriteRegisterResponse) master.send(
                        new WriteRegisterRequest(point.getSlaveId(), point.getPointAddress(), Integer.parseInt(writeValue))
                );
                return !response.isException();
            }
            throw new IllegalArgumentException("Unsupported Modbus write register type: " + point.getRegisterType());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to write Modbus point " + point.getPointId(), ex);
        }
    }
}
