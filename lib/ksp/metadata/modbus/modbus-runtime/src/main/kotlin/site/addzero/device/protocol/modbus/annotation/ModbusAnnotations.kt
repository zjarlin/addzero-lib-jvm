package site.addzero.device.protocol.modbus.annotation

import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GenerateModbusRtuServer

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GenerateModbusTcpServer

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ModbusDeviceApi(
    val serviceId: String = "",
    val summary: String = "",
    val basePath: String = "",
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
/**
 * 可选覆盖注解。
 *
 * 不写 @ModbusOperation 时，处理器会直接按接口方法签名生成操作；
 * 只有需要覆盖默认 operationId / functionCode / address / quantity / capabilityKey 时才需要写。
 */
annotation class ModbusOperation(
    val operationId: String = "",
    /**
     * 默认使用 AUTO，根据签名自动推导。
     *
     * 只有需要 READ_HOLDING_REGISTERS / READ_COILS / READ_DISCRETE_INPUTS
     * 或其他非默认语义时，
     * 才建议显式写出 functionCode。
     */
    val functionCode: ModbusFunctionCode = ModbusFunctionCode.AUTO,
    /**
     * 标准 Modbus 零基起始地址。
     *
     * 例如：
     * - Coil 00001 对应 address = 0
     * - Input Register 30001 对应 address = 0
     * - Holding Register 40001 对应 address = 0
     */
    val address: Int = -1,
    /**
     * 标准 Modbus 数量字段。
     *
     * 表示本次操作涉及的 coil / discrete input / register 数量；
     * 传 -1 时由处理器按参数或返回 DTO 自动推导。
     */
    val quantity: Int = -1,
    val capabilityKey: String = "",
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class ModbusParam(
    val order: Int,
    val codec: ModbusCodec,
    /**
     * 对寄存器功能码表示寄存器偏移；
     * 对 coil/discrete 功能码表示线圈位序号偏移。
     */
    val registerOffset: Int = -1,
    /**
     * 仅寄存器位字段使用，表示 0..15 的 bit 位偏移。
     */
    val bitOffset: Int = -1,
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class ModbusField(
    val codec: ModbusCodec,
    /**
     * 对寄存器功能码表示寄存器偏移；
     * 对 coil/discrete 功能码表示线圈位序号偏移。
     */
    val registerOffset: Int = -1,
    /**
     * 仅寄存器位字段使用，表示 0..15 的 bit 位偏移。
     */
    val bitOffset: Int = -1,
    val length: Int = 1,
)
