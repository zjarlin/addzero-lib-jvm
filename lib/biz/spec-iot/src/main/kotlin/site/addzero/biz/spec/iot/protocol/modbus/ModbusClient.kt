package site.addzero.biz.spec.iot.protocol.modbus

import site.addzero.biz.spec.iot.IotValueType
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

/**
 * Thin Modbus TCP client wrapper with connection caching.
 */
class ModbusClient {

    fun close(options: ModbusConnectionOptions) {
        val existing = MASTERS.remove(options.cacheKey())
        if (existing != null) {
            try {
                existing.javaClass.getMethod("destroy").invoke(existing)
            } catch (_: Exception) {
                // Ignore close failures.
            }
        }
    }

    fun verifyConnection(options: ModbusConnectionOptions): Boolean {
        return try {
            val batchReadClass = loadClass(BATCH_READ_CLASS_NAME)
            val batch = batchReadClass.getConstructor().newInstance()
            val locator = buildInputRegisterLocator(1, 0, "TWO_BYTE_INT_UNSIGNED")
            batchReadClass.getMethod("addLocator", Any::class.java, Any::class.java).invoke(batch, "health_check", locator)
            batchReadClass.getMethod("setContiguousRequests", Boolean::class.javaPrimitiveType).invoke(batch, true)
            getMaster(options).javaClass.getMethod("send", batchReadClass).invoke(getMaster(options), batch)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun batchReadRaw(options: ModbusConnectionOptions, points: List<ModbusPointBinding>): Map<String, Any?> {
        try {
            val batchReadClass = loadClass(BATCH_READ_CLASS_NAME)
            val batch = batchReadClass.getConstructor().newInstance()
            points.forEach { point ->
                val locator = buildLocator(point)
                batchReadClass.getMethod("addLocator", Any::class.java, Any::class.java)
                    .invoke(batch, point.pointId, locator)
            }
            batchReadClass.getMethod("setContiguousRequests", Boolean::class.javaPrimitiveType).invoke(batch, true)
            val results = getMaster(options).javaClass.getMethod("send", batchReadClass).invoke(getMaster(options), batch)
            val getValue = loadClass(BATCH_RESULTS_CLASS_NAME).getMethod("getValue", Any::class.java)
            val rawValues = linkedMapOf<String, Any?>()
            points.forEach { point ->
                rawValues[point.pointId] = getValue.invoke(results, point.pointId)
            }
            return rawValues.toMap()
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to batch read Modbus points", ex)
        }
    }

    fun singleWritePoint(options: ModbusConnectionOptions, point: ModbusWritePoint): Boolean {
        try {
            val master = getMaster(options)
            if (point.registerType == ModbusRegisterType.COIL_STATUS) {
                val request = loadClass(WRITE_COIL_REQUEST_CLASS_NAME)
                    .getConstructor(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
                    .newInstance(point.slaveId, point.pointAddress, java.lang.Boolean.parseBoolean(point.writeValue))
                val response = master.javaClass
                    .getMethod("send", loadClass("com.serotonin.modbus4j.msg.ModbusRequest"))
                    .invoke(master, request)
                return !(response.javaClass.getMethod("isException").invoke(response) as Boolean)
            }
            if (point.registerType == ModbusRegisterType.HOLDING_REGISTER) {
                var writeValue = requireWriteValue(point.writeValue)
                if (point.valueType == IotValueType.BOOLEAN) {
                    writeValue = if (java.lang.Boolean.parseBoolean(writeValue)) {
                        "1"
                    } else {
                        "0"
                    }
                }
                val request = loadClass(WRITE_REGISTER_REQUEST_CLASS_NAME)
                    .getConstructor(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    .newInstance(point.slaveId, point.pointAddress, Integer.parseInt(writeValue))
                val response = master.javaClass
                    .getMethod("send", loadClass("com.serotonin.modbus4j.msg.ModbusRequest"))
                    .invoke(master, request)
                return !(response.javaClass.getMethod("isException").invoke(response) as Boolean)
            }
            throw IllegalArgumentException("Unsupported Modbus write register type: ${point.registerType}")
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to write Modbus point ${point.pointId}", ex)
        }
    }

    private fun getMaster(options: ModbusConnectionOptions): Any {
        val existing = MASTERS[options.cacheKey()]
        if (existing != null) {
            return existing
        }
        return synchronized(MASTERS) {
            MASTERS[options.cacheKey()] ?: run {
                try {
                    createMaster(options).also { MASTERS[options.cacheKey()] = it }
                } catch (ex: Exception) {
                    throw IllegalStateException("Failed to initialize Modbus master", ex)
                }
            }
        }
    }

    private fun createMaster(options: ModbusConnectionOptions): Any {
        val factoryClass = loadClass(FACTORY_CLASS_NAME)
        val factory = factoryClass.getConstructor().newInstance()
        val ipParametersClass = loadClass(IP_PARAMETERS_CLASS_NAME)
        val parameters = ipParametersClass.getConstructor().newInstance()
        ipParametersClass.getMethod("setHost", String::class.java).invoke(parameters, options.host)
        ipParametersClass.getMethod("setPort", Int::class.javaPrimitiveType).invoke(parameters, options.port)
        val master = factoryClass.getMethod("createTcpMaster", ipParametersClass, Boolean::class.javaPrimitiveType)
            .invoke(factory, parameters, false)
        loadClass(MASTER_CLASS_NAME).getMethod("init").invoke(master)
        return master
    }

    private fun buildLocator(point: ModbusPointBinding): Any {
        if (point.registerType == ModbusRegisterType.COIL_STATUS) {
            return loadClass(BASE_LOCATOR_CLASS_NAME)
                .getMethod("coilStatus", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .invoke(null, point.slaveId, point.pointAddress)
        }
        if (point.registerType == ModbusRegisterType.INPUT_STATUS) {
            return loadClass(BASE_LOCATOR_CLASS_NAME)
                .getMethod("inputStatus", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .invoke(null, point.slaveId, point.pointAddress)
        }
        if (point.registerType == ModbusRegisterType.HOLDING_REGISTER) {
            return loadClass(BASE_LOCATOR_CLASS_NAME)
                .getMethod("holdingRegister", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, loadClass(DATA_TYPE_CLASS_NAME))
                .invoke(null, point.slaveId, point.pointAddress, dataTypeConstant("FOUR_BYTE_FLOAT_SWAPPED"))
        }
        if (point.registerType == ModbusRegisterType.INPUT_REGISTER) {
            return loadClass(BASE_LOCATOR_CLASS_NAME)
                .getMethod("inputRegister", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, loadClass(DATA_TYPE_CLASS_NAME))
                .invoke(null, point.slaveId, point.pointAddress, dataTypeConstant("FOUR_BYTE_FLOAT_SWAPPED"))
        }
        throw IllegalArgumentException("Unsupported Modbus register type: ${point.registerType}")
    }

    private fun buildInputRegisterLocator(slaveId: Int, pointAddress: Int, dataTypeField: String): Any {
        return loadClass(BASE_LOCATOR_CLASS_NAME)
            .getMethod("inputRegister", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, loadClass(DATA_TYPE_CLASS_NAME))
            .invoke(null, slaveId, pointAddress, dataTypeConstant(dataTypeField))
    }

    private fun dataTypeConstant(fieldName: String): Any? {
        val field: Field = loadClass(DATA_TYPE_CLASS_NAME).getField(fieldName)
        return field.get(null)
    }

    private fun loadClass(className: String): Class<*> {
        try {
            return Class.forName(className)
        } catch (ex: ClassNotFoundException) {
            throw IllegalStateException("Missing runtime dependency for Modbus support: $className", ex)
        }
    }

    private fun requireWriteValue(value: String?): String {
        return value ?: throw IllegalArgumentException("writeValue must not be null")
    }

    companion object {
        private const val FACTORY_CLASS_NAME = "com.serotonin.modbus4j.ModbusFactory"
        private const val MASTER_CLASS_NAME = "com.serotonin.modbus4j.ModbusMaster"
        private const val IP_PARAMETERS_CLASS_NAME = "com.serotonin.modbus4j.ip.IpParameters"
        private const val BATCH_READ_CLASS_NAME = "com.serotonin.modbus4j.BatchRead"
        private const val BATCH_RESULTS_CLASS_NAME = "com.serotonin.modbus4j.BatchResults"
        private const val BASE_LOCATOR_CLASS_NAME = "com.serotonin.modbus4j.locator.BaseLocator"
        private const val DATA_TYPE_CLASS_NAME = "com.serotonin.modbus4j.code.DataType"
        private const val WRITE_COIL_REQUEST_CLASS_NAME = "com.serotonin.modbus4j.msg.WriteCoilRequest"
        private const val WRITE_REGISTER_REQUEST_CLASS_NAME = "com.serotonin.modbus4j.msg.WriteRegisterRequest"
        private val MASTERS = ConcurrentHashMap<String, Any>()
    }
}
