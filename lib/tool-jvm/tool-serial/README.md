# tool-serial

JVM 串口工具模块，基于 `jSerialComm` 提供可复用的 Kotlin API，用于枚举串口、打开连接、同步读写和按分隔符收包。

## Maven 坐标

`site.addzero:tool-serial`

## 本地模块路径

`lib/tool-jvm/tool-serial`

## 快速使用

```kotlin
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialPortTool

val ports = SerialPortTool.listPorts()
println(ports.joinToString { it.systemPortName })

val config = SerialPortConfig(
    portName = "/dev/ttyUSB0",
    baudRate = 115200,
    readTimeoutMs = 1000
)

SerialPortTool.open(config).use { connection ->
    connection.clearBuffers()
    connection.write("AT\r\n")
    val response = connection.readUntil("\r\n".toByteArray())
    println(response.decodeToString())
}
```

## 提供的能力

- 枚举本机串口和基础描述信息
- 配置波特率、数据位、停止位、校验位、流控、读写超时
- 同步写入字节或文本
- 按最大长度读取、精确长度读取、按分隔符读取、立即读取可用缓冲区
- 超时与打开失败显式抛错，不做静默降级

## 运行约束

- 仅支持 JVM
- 底层依赖 `jSerialComm`，实际串口能力取决于宿主系统驱动与设备状态
- `readTimeoutMs` / `writeTimeoutMs` 在 Linux 与 macOS 上通常按 100ms 粒度取整
