package site.addzero.device.protocol.modbus.annotation

import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GenerateModbusRtuServer

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GenerateModbusTcpServer

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class ModbusOperation(
    val address: Int = -1,
    val functionCode: ModbusFunctionCode = ModbusFunctionCode.AUTO,
    val returnCodec: ModbusCodec = ModbusCodec.AUTO,
    val returnLength: Int = 1,
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class ModbusParam(
    val order: Int,
    val codec: ModbusCodec = ModbusCodec.AUTO,
    val registerOffset: Int = -1,
    val bitOffset: Int = -1,
    val length: Int = 1,
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class ModbusField(
    val codec: ModbusCodec = ModbusCodec.AUTO,
    val registerOffset: Int = -1,
    val bitOffset: Int = -1,
    val length: Int = 1,
)
