package site.addzero.device.contract

import site.addzero.device.protocol.modbus.annotation.GenerateModbusRtuServer
import site.addzero.device.protocol.modbus.annotation.ModbusOperation
import site.addzero.device.protocol.modbus.annotation.ModbusParam
import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.device.protocol.modbus.model.ModbusCommandResult
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

/**
 * 固件烧录与复位接口。
 */
@GenerateModbusRtuServer
interface FlashApi {
    /**
     * 触发设备复位。
     *
     * @param trigger 写入 true 时触发一次设备复位。
     */
    @ModbusOperation(
        address = 64,
        functionCode = ModbusFunctionCode.WRITE_SINGLE_COIL,
    )
    suspend fun resetDevice(
        @ModbusParam(order = 0, codec = ModbusCodec.BOOL_COIL)
        trigger: Boolean,
    ): ModbusCommandResult

    /**
     * 初始化一次烧录会话。
     *
     * @param totalBytes 本次固件总字节数。
     * @param crc32 本次固件镜像的 CRC32。
     */
    @ModbusOperation(
        address = 512,
        functionCode = ModbusFunctionCode.WRITE_MULTIPLE_REGISTERS,
    )
    suspend fun firmwareStart(
        @ModbusParam(order = 0, codec = ModbusCodec.U32_BE)
        totalBytes: Int,
        @ModbusParam(order = 1, codec = ModbusCodec.U32_BE)
        crc32: Int,
    ): ModbusCommandResult

    /**
     * 写入一帧固件数据。
     *
     * @param sequence 分片序号，从 0 开始递增。
     * @param usedBytes 当前分片实际有效字节数。
     * @param word0 数据字 0。
     * @param word1 数据字 1。
     * @param word2 数据字 2。
     * @param word3 数据字 3。
     */
    @ModbusOperation(
        address = 520,
        functionCode = ModbusFunctionCode.WRITE_MULTIPLE_REGISTERS,
    )
    suspend fun firmwareChunk(
        @ModbusParam(order = 0, codec = ModbusCodec.U16)
        sequence: Int,
        @ModbusParam(order = 1, codec = ModbusCodec.U16)
        usedBytes: Int,
        @ModbusParam(order = 2, codec = ModbusCodec.U32_BE)
        word0: Int,
        @ModbusParam(order = 3, codec = ModbusCodec.U32_BE)
        word1: Int,
        @ModbusParam(order = 4, codec = ModbusCodec.U32_BE)
        word2: Int,
        @ModbusParam(order = 5, codec = ModbusCodec.U32_BE)
        word3: Int,
    ): ModbusCommandResult

    /**
     * 提交烧录结果。
     *
     * @param totalChunks 本次烧录总分片数。
     */
    @ModbusOperation(
        address = 530,
        functionCode = ModbusFunctionCode.WRITE_SINGLE_REGISTER,
    )
    suspend fun firmwareCommit(
        @ModbusParam(order = 0, codec = ModbusCodec.U16)
        totalChunks: Int,
    ): ModbusCommandResult
}
