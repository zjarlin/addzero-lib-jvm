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
 * 只有需要覆盖默认 address 时才需要写。
 */
annotation class ModbusOperation(
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
    /**
     * 显式指定功能码。
     *
     * 传 AUTO 时按方法签名自动推导。
     * 对语义 DTO 读取尤其建议显式写出，避免把 Boolean DTO 默认推导到 input register。
     */
    val functionCode: ModbusFunctionCode = ModbusFunctionCode.AUTO,
    /**
     * 标量返回值的编解码方式。
     *
     * 仅在返回 `Boolean` / `Int` / `String` 时生效；
     * 返回 DTO 时请继续在 DTO 字段上声明 `@ModbusField`。
     */
    val returnCodec: ModbusCodec = ModbusCodec.AUTO,
    /**
     * 标量返回值长度。
     *
     * - 对 `U16` / `BOOL_COIL` / `BIT_FLAG` 通常保持 1
     * - 对 `U32_BE` 表示值个数，实际寄存器宽度为 `2 * returnLength`
     * - 对 `STRING_*` 表示占用的寄存器数，实际最大字节数为 `returnLength * 2 - 1`
     */
    val returnLength: Int = 1,
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
     * - String -> STRING_UTF8
     *
     * 只有需要 U32_BE / STRING_ASCII 或强制覆盖默认推导时才建议显式写。
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
    /**
     * 字段长度。
     *
     * - 对 U16 / BOOL_COIL / BIT_FLAG 通常保持 1
     * - 对 U32_BE 表示值个数，实际寄存器宽度为 `2 * length`
     * - 对 STRING_* 表示占用的寄存器数，实际最大字节数为 `length * 2 - 1`
     */
    val length: Int = 1,
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
     * - String -> STRING_UTF8
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
    /**
     * 字段长度。
     *
     * - 对 U16 / BOOL_COIL / BIT_FLAG 通常保持 1
     * - 对 U32_BE 表示值个数，实际寄存器宽度为 `2 * length`
     * - 对 STRING_* 表示占用的寄存器数，实际最大字节数为 `length * 2 - 1`
     */
    val length: Int = 1,
)
