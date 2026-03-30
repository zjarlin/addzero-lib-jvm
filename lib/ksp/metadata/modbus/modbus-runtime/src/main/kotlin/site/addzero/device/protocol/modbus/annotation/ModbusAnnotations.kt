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
/**
 * 可选覆盖注解。
 *
 * 不写 @ModbusOperation 时，处理器会直接按接口方法签名生成操作；
 * 只有需要覆盖默认 functionCode / address 时才需要写。
 */
annotation class ModbusOperation(
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
     *
     * 传 -1 时由处理器按 serviceId + 方法签名稳定推导。
     * 如果主机端和固件端都基于同一份契约生成，可以省略；
     * 如果需要与既有固件地址表对齐，建议显式写出。
     */
    val address: Int = -1,
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class ModbusParam(
    val order: Int,
    /**
     * 默认 AUTO。
     *
     * 常见场景可以完全省略：
     * - Boolean 线圈 -> BOOL_COIL
     * - Boolean 寄存器位 -> BIT_FLAG
     * - Int -> U16
     *
     * 只有需要 U32_BE 或强制覆盖默认推导时才建议显式写。
     */
    val codec: ModbusCodec = ModbusCodec.AUTO,
    /**
     * 对寄存器功能码表示寄存器偏移；
     * 对 coil/discrete 功能码表示线圈位序号偏移。
     *
     * 传 -1 时按声明顺序自动顺延：
     * - 第一个参数默认 0
     * - 第二个参数默认接在前一个参数后面
     *
     * 只有需要“跳地址”或多个字段复用同一寄存器时才建议显式写。
     */
    val registerOffset: Int = -1,
    /**
     * 仅寄存器位字段使用，表示 0..15 的 bit 位偏移。
     *
     * 对 Boolean + AUTO/BIT_FLAG，默认值为 0。
     */
    val bitOffset: Int = -1,
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class ModbusField(
    /**
     * 默认 AUTO。
     *
     * 常见场景可以完全省略：
     * - Boolean 线圈 -> BOOL_COIL
     * - Boolean 寄存器位 -> BIT_FLAG
     * - Int -> U16
     */
    val codec: ModbusCodec = ModbusCodec.AUTO,
    /**
     * 对寄存器功能码表示寄存器偏移；
     * 对 coil/discrete 功能码表示线圈位序号偏移。
     *
     * 传 -1 时按字段声明顺序自动顺延。
     */
    val registerOffset: Int = -1,
    /**
     * 仅寄存器位字段使用，表示 0..15 的 bit 位偏移。
     *
     * 对 Boolean + AUTO/BIT_FLAG，默认值为 0。
     */
    val bitOffset: Int = -1,
    val length: Int = 1,
)
