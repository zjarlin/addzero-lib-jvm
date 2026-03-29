package site.addzero.device.protocol.modbus.model

enum class ModbusFunctionCode {
    /**
     * 自动推导功能码。
     *
     * 默认规则：
     * - 无参数且返回 DTO / Int / Boolean 时，推导为 READ_INPUT_REGISTERS
     * - 单个 BOOL_COIL Boolean 参数时，推导为 WRITE_SINGLE_COIL
     * - 单个 1 寄存器标量参数时，推导为 WRITE_SINGLE_REGISTER
     * - 其他写入场景推导为 WRITE_MULTIPLE_REGISTERS
     *
     * 注意：
     * - READ_HOLDING_REGISTERS 不会被默认选中，需要显式声明
     * - 无法从签名安全推导时，应显式声明 functionCode
     */
    AUTO,

    /** 读取 holding register，常用于可读配置或运行态寄存器。 */
    READ_HOLDING_REGISTERS,

    /** 读取 input register，纯读 DTO 最常见的默认值。 */
    READ_INPUT_REGISTERS,

    /** 写单线圈，通常用于单个布尔动作。 */
    WRITE_SINGLE_COIL,

    /** 写单寄存器，通常用于单个 U16 标量。 */
    WRITE_SINGLE_REGISTER,

    /** 写多个寄存器，通常用于多参数或宽字段写入。 */
    WRITE_MULTIPLE_REGISTERS,
}

enum class ModbusCodec {
    BOOL_COIL,
    BIT_FLAG,
    U16,
    U32_BE,
}
