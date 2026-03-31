package site.addzero.device.contract

import site.addzero.device.protocol.modbus.annotation.GenerateModbusRtuServer
import site.addzero.device.protocol.modbus.annotation.ModbusField
import site.addzero.device.protocol.modbus.annotation.ModbusOperation
import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

/**
 * 最小真实契约样例。
 *
 * 这份文件就是 smoke fixture 的真实输入；
 * KSP 会直接扫描这个包并生成 Kotlin gateway、C bridge/dispatch、Markdown 协议文档。
 */
@GenerateModbusRtuServer
interface DeviceApi {
    /**
     * 读取 24 路输出通道状态。
     */
    @ModbusOperation(
        address = 0,
        functionCode = ModbusFunctionCode.READ_COILS,
    )
    suspend fun getDeviceInfo(): DeviceInfo24

    /**
     * 读取设备运行信息。
     */
    @ModbusOperation(
        address = 100,
        functionCode = ModbusFunctionCode.READ_INPUT_REGISTERS,
    )
    suspend fun getDeviceRuntimeInfo(): DeviceRuntimeInfo
}

/**
 * 24 路数字输出快照。
 */
data class DeviceInfo24(
    /** 通道 1。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 0)
    val ch1: Boolean,
    /** 通道 2。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 1)
    val ch2: Boolean,
    /** 通道 3。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 2)
    val ch3: Boolean,
    /** 通道 4。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 3)
    val ch4: Boolean,
    /** 通道 5。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 4)
    val ch5: Boolean,
    /** 通道 6。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 5)
    val ch6: Boolean,
    /** 通道 7。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 6)
    val ch7: Boolean,
    /** 通道 8。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 7)
    val ch8: Boolean,
    /** 通道 9。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 8)
    val ch9: Boolean,
    /** 通道 10。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 9)
    val ch10: Boolean,
    /** 通道 11。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 10)
    val ch11: Boolean,
    /** 通道 12。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 11)
    val ch12: Boolean,
    /** 通道 13。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 12)
    val ch13: Boolean,
    /** 通道 14。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 13)
    val ch14: Boolean,
    /** 通道 15。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 14)
    val ch15: Boolean,
    /** 通道 16。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 15)
    val ch16: Boolean,
    /** 通道 17。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 16)
    val ch17: Boolean,
    /** 通道 18。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 17)
    val ch18: Boolean,
    /** 通道 19。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 18)
    val ch19: Boolean,
    /** 通道 20。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 19)
    val ch20: Boolean,
    /** 通道 21。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 20)
    val ch21: Boolean,
    /** 通道 22。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 21)
    val ch22: Boolean,
    /** 通道 23。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 22)
    val ch23: Boolean,
    /** 通道 24。 */
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 23)
    val ch24: Boolean,
)

/**
 * 设备运行信息。
 */
data class DeviceRuntimeInfo(
    /** 协议版本。 */
    @ModbusField(codec = ModbusCodec.U16, registerOffset = 0)
    val protocolVersion: Int,
    /** 通道总数。 */
    @ModbusField(codec = ModbusCodec.U16, registerOffset = 1)
    val channelCount: Int,
    /** 从站地址。 */
    @ModbusField(codec = ModbusCodec.U16, registerOffset = 2)
    val unitId: Int,
    /** 波特率编码。 */
    @ModbusField(codec = ModbusCodec.U16, registerOffset = 3)
    val baudRateCode: Int,
)
