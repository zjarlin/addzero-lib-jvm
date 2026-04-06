package site.addzero.device.protocol.modbus.model

enum class ModbusFunctionCode {
    AUTO,
    READ_HOLDING_REGISTERS,
    READ_INPUT_REGISTERS,
    READ_COILS,
    READ_DISCRETE_INPUTS,
    WRITE_SINGLE_COIL,
    WRITE_MULTIPLE_COILS,
    WRITE_SINGLE_REGISTER,
    WRITE_MULTIPLE_REGISTERS,
}

enum class ModbusCodec {
    AUTO,
    BOOL_COIL,
    BIT_FLAG,
    U16,
    U32_BE,
    STRING_ASCII,
    STRING_UTF8,
}
