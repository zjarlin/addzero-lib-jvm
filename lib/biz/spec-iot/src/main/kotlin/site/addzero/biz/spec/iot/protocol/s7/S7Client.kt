package site.addzero.biz.spec.iot.protocol.s7

import site.addzero.biz.spec.iot.IotValueType
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Thin S7 client wrapper with connection caching.
 */
class S7Client {

    fun disconnect(options: S7ConnectionOptions) {
        val existing = CONNECTIONS.remove(options.cacheKey())
        if (existing != null) {
            try {
                existing.javaClass.getMethod("close").invoke(existing)
            } catch (ex: Exception) {
                throw IllegalStateException("Failed to close S7 client", ex)
            }
        }
    }

    fun verifyConnection(options: S7ConnectionOptions): Boolean {
        return try {
            val connection = getConnection(options)
            val readByte = connection.javaClass.getMethod("readByte", String::class.java)
            readByte.invoke(connection, S7DataArea.M.code + 0 + "." + 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun batchRead(options: S7ConnectionOptions, blocks: List<S7ReadBlock>): List<ByteArray> {
        try {
            val multiReadClass = loadClass(MULTI_READ_CLASS_NAME)
            val addressRead = multiReadClass.getConstructor().newInstance()
            val addData = multiReadClass.getMethod("addData", String::class.java, Int::class.javaPrimitiveType)
            blocks.forEach { block ->
                addData.invoke(
                    addressRead,
                    block.dataArea.code + block.dataAreaNumber + "." + block.startAddress,
                    block.dataSize,
                )
            }
            val readMultiByte = getConnection(options).javaClass.getMethod("readMultiByte", multiReadClass)
            @Suppress("UNCHECKED_CAST")
            return readMultiByte.invoke(getConnection(options), addressRead) as List<ByteArray>
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to batch read S7 blocks", ex)
        }
    }

    fun singleWrite(options: S7ConnectionOptions, writePoint: S7WritePoint) {
        try {
            val plc = getConnection(options)
            val address = writePoint.dataArea.code +
                writePoint.dataAreaNumber +
                "." +
                writePoint.byteOffset +
                "." +
                writePoint.bitOffset
            when (writePoint.valueType) {
                IotValueType.BOOLEAN -> {
                    plc.javaClass.getMethod("writeBoolean", String::class.java, Boolean::class.javaPrimitiveType)
                        .invoke(plc, address, java.lang.Boolean.parseBoolean(writePoint.writeValue))
                }

                IotValueType.FLOAT32 -> {
                    plc.javaClass.getMethod("writeFloat32", String::class.java, Float::class.javaPrimitiveType)
                        .invoke(plc, address, java.lang.Float.parseFloat(requireWriteValue(writePoint.writeValue)))
                }

                IotValueType.INT32 -> {
                    plc.javaClass.getMethod("writeInt32", String::class.java, Int::class.javaPrimitiveType)
                        .invoke(plc, address, Integer.parseInt(requireWriteValue(writePoint.writeValue)))
                }
            }
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to write S7 point ${writePoint.propertyIdentifier}", ex)
        }
    }

    private fun getConnection(options: S7ConnectionOptions): Any {
        val existing = CONNECTIONS[options.cacheKey()]
        if (existing != null) {
            return existing
        }
        return synchronized(CONNECTIONS) {
            CONNECTIONS[options.cacheKey()] ?: run {
                try {
                    newS7Plc(options).also { CONNECTIONS[options.cacheKey()] = it }
                } catch (ex: Exception) {
                    throw IllegalStateException("Failed to initialize S7 client runtime", ex)
                }
            }
        }
    }

    private fun newS7Plc(options: S7ConnectionOptions): Any {
        val plcTypeClass = loadClass(PLC_TYPE_CLASS_NAME)
        val plcType = plcTypeClass.enumConstants.first { (it as Enum<*>).name == "S1200" }
        val plcClass = loadClass(PLC_CLASS_NAME)
        val constructor = plcClass.getConstructor(plcTypeClass, String::class.java, Int::class.javaPrimitiveType)
        return constructor.newInstance(plcType, options.host, options.port)
    }

    private fun loadClass(className: String): Class<*> {
        try {
            return Class.forName(className)
        } catch (ex: ClassNotFoundException) {
            throw IllegalStateException("Missing runtime dependency for S7 support: $className", ex)
        }
    }

    private fun requireWriteValue(value: String?): String {
        return value ?: throw IllegalArgumentException("writeValue must not be null")
    }

    companion object {
        private const val PLC_CLASS_NAME = "com.github.xingshuangs.iot.protocol.s7.service.S7PLC"
        private const val PLC_TYPE_CLASS_NAME = "com.github.xingshuangs.iot.protocol.s7.enums.EPlcType"
        private const val MULTI_READ_CLASS_NAME = "com.github.xingshuangs.iot.protocol.s7.service.MultiAddressRead"
        private val CONNECTIONS = ConcurrentHashMap<String, Any>()
    }
}
