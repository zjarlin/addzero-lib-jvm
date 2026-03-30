package site.addzero.device.protocol.modbus.model

enum class ModbusFunctionCode {
    /**
     * 自动推导功能码。
     *
     * 默认规则：
     * - 无参数且返回 DTO / Int / Boolean 时，推导为 READ_INPUT_REGISTERS
     * - 单个 BOOL_COIL Boolean 参数时，推导为 WRITE_SINGLE_COIL
     * - 多个 BOOL_COIL Boolean 参数时，推导为 WRITE_MULTIPLE_COILS
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

    /** 读取 coils，标准功能码 0x01。 */
    READ_COILS,

    /** 读取 discrete inputs，标准功能码 0x02。 */
    READ_DISCRETE_INPUTS,

    /** 写单线圈，通常用于单个布尔动作。 */
    WRITE_SINGLE_COIL,

    /** 写多个线圈，标准功能码 0x0F。 */
    WRITE_MULTIPLE_COILS,

    /** 写单寄存器，通常用于单个 U16 标量。 */
    WRITE_SINGLE_REGISTER,

    /** 写多个寄存器，通常用于多参数或宽字段写入。 */
    WRITE_MULTIPLE_REGISTERS,
}

enum class ModbusCodec {
    /**
     * 自动推导编解码方式。
     *
     * 默认规则：
     * - coil / discrete 场景下的 Boolean -> BOOL_COIL
     * - 寄存器场景下的 Boolean -> BIT_FLAG
     * - Int -> U16
     *
     * 无法安全推导为 U32_BE 时，不会自动升级，仍需显式声明。
     */
    AUTO,

    /**
     * Kotlin `Boolean` <-> Modbus coil/discrete 单个位。
     */
    BOOL_COIL,

    /**
     * Kotlin `Boolean` <-> 寄存器内某一 bit 位。
     */
    BIT_FLAG,

    /**
     * Kotlin `Int` <-> 单个 16 位无符号寄存器。
     */
    U16,

    /**
     * Kotlin `Int` <-> 两个 16 位寄存器拼出的 32 位大端值。
     */
    U32_BE,
}
