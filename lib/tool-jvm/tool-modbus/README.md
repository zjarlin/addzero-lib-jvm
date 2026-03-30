# tool-modbus

JVM Modbus 工具模块，当前提供面向 Kotlin 的 Modbus TCP 客户端与服务端封装，底层基于仓库已在使用的 `j2mod`。

包结构约定：

- `site.addzero.modbus.tcp.client`：TCP 客户端实现
- `site.addzero.modbus.tcp.server`：TCP 服务端实现
- `site.addzero.modbus.rtu.client`：RTU 客户端实现
- `site.addzero.modbus.rtu.server`：RTU 服务端实现
- `site.addzero.modbus`：仅保留协议无关的公共类型

## Maven 坐标

`site.addzero:tool-modbus`

## 本地模块路径

`lib/tool-jvm/tool-modbus`

## 快速使用

```kotlin
import site.addzero.modbus.tcp.client.ModbusTcpClient
import site.addzero.modbus.tcp.client.ModbusTcpClientConfig
import site.addzero.modbus.tcp.server.ModbusTcpServer
import site.addzero.modbus.tcp.server.ModbusTcpServerConfig

val server = ModbusTcpServer(
    ModbusTcpServerConfig(
        host = "127.0.0.1",
        port = 15020,
        defaultUnitId = 1,
    ),
)
server.image().setHoldingRegister(0, 123)
server.start()

ModbusTcpClient(
    ModbusTcpClientConfig(
        host = "127.0.0.1",
        port = 15020,
        unitId = 1,
    ),
).use { client ->
    val value = client.readHoldingRegister(0)
    println(value)
    client.writeSingleRegister(1, 456)
}

server.close()
```

## RTU 快速使用

```kotlin
import site.addzero.modbus.rtu.client.ModbusRtuClient
import site.addzero.modbus.rtu.client.ModbusRtuClientConfig
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialParity

val client = ModbusRtuClient(
    ModbusRtuClientConfig(
        serialConfig = SerialPortConfig(
            portName = "/dev/ttyUSB0",
            baudRate = 9600,
            parity = SerialParity.EVEN,
        ),
        unitId = 1,
        retries = 1,
    ),
)

client.use { rtu ->
    val value = rtu.readHoldingRegister(0)
    println(value)
}
```

## 提供的能力

- Modbus TCP 客户端：
  - 读线圈、离散输入、保持寄存器、输入寄存器
  - 写单个/批量线圈
  - 写单个/批量保持寄存器
  - mask write register
- Modbus TCP 服务端：
  - 启动/关闭从站监听
  - 按 unit id 管理独立 process image
  - 通过 Kotlin API 预置和读取 coil / discrete input / register 值
- Modbus RTU 客户端：
  - 复用 `tool-serial` 的串口参数模型
  - 支持基础读写、重试和按请求重连
- Modbus RTU 服务端：
  - 复用同一套 process image API
  - 底层通过串口从站监听对外提供数据

## 运行约束

- 仅支持 JVM
- 服务端依赖进程内监听端口，测试或运行时请确保端口未被占用
- RTU 运行时依赖真实串口设备与正确的串口参数；单元测试本身不访问真实硬件
