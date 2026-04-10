# tool-serial

JVM 串口工具模块，基于 `jSerialComm` 提供可复用的 Kotlin API，用于枚举串口、打开连接、同步读写和按分隔符收包。

现在还额外提供：
- 串口日志按“行”轮询输出 `Flow<String>`
- 已编码好的 SSE 文本帧 `Flow<String>`，可直接给 controller 写回浏览器

## Maven 坐标

`site.addzero:tool-serial`

## 本地模块路径

`lib/tool-jvm/tool-serial`

## 快速使用

```kotlin
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialSseStreamConfig
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

## SSE 推日志

```kotlin
import io.ktor.http.ContentType
import io.ktor.server.response.respondTextWriter
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialPortTool

val serialConfig =
    SerialPortConfig(
        portName = "/dev/cu.usbserial-2140",
        baudRate = 115200,
        readTimeoutMs = 200,
    )

val sseFlow = SerialPortTool.openSseLogFlow(serialConfig)

call.respondTextWriter(contentType = ContentType.Text.EventStream) {
    sseFlow.collect { frame ->
        write(frame)
        flush()
    }
}
```

## 提供的能力

- 枚举本机串口和基础描述信息
- 配置波特率、数据位、停止位、校验位、流控、读写超时
- 同步写入字节或文本
- 按最大长度读取、精确长度读取、按分隔符读取、立即读取可用缓冲区
- 轮询日志行 `Flow<String>`
- 直接输出 SSE 帧 `Flow<String>`
- 超时与打开失败显式抛错，不做静默降级

## 运行约束

- 仅支持 JVM
- 底层依赖 `jSerialComm`，实际串口能力取决于宿主系统驱动与设备状态
- `readTimeoutMs` / `writeTimeoutMs` 在 Linux 与 macOS 上通常按 100ms 粒度取整
