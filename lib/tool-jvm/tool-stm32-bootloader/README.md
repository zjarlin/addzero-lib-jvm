# tool-stm32-bootloader

基于 STM32 官方 ROM USART Bootloader 与 ST-Link SWD 协议的 JVM 烧录库，纯 Kotlin 实现，不依赖外部 CLI。

## Maven 坐标

`site.addzero:tool-stm32-bootloader`

## 本地模块路径

`lib/tool-jvm/tool-stm32-bootloader`

## 提供的能力

- 通过 STM32 System Memory Bootloader 完成串口握手、读 ID、读版本
- 支持 `Read Memory`、`Write Memory`、`Go`
- 自动识别 `Erase` / `Extended Erase`，支持整片擦除和页码擦除
- 可选通过 `DTR` / `RTS` 控制 `BOOT0` 和 `NRST`
- 完整烧录流程封装：进入 Bootloader、擦除、写入、回读校验、启动应用
- 提供阶段化进度回调，可直接绑定上位机进度条
- 支持 ST-Link `SWD` 探测、目标电压读取、`NRST` 控制、系统复位
- 支持当前已实测目标 `STM32F1xx HD (chipId=0x414)` 的 SWD 页擦除、写入、回读校验

## 快速使用

```kotlin
import site.addzero.stm32.bootloader.Stm32BootloaderConfig
import site.addzero.stm32.bootloader.Stm32BootloaderLineControl
import site.addzero.stm32.bootloader.Stm32ControlLine
import site.addzero.stm32.bootloader.Stm32ControlSignal
import site.addzero.stm32.bootloader.Stm32FlashRequest
import site.addzero.stm32.bootloader.Stm32UartBootloader
import site.addzero.stm32.bootloader.stm32BootloaderSerialConfig

val config = Stm32BootloaderConfig(
    serialConfig = stm32BootloaderSerialConfig(
        portName = "/dev/cu.usbserial-1420",
        baudRate = 115200,
    ),
    autoEnterBootloaderOnConnect = true,
    lineControl = Stm32BootloaderLineControl(
        boot0 = Stm32ControlSignal(Stm32ControlLine.RTS, assertedOutput = true),
        reset = Stm32ControlSignal(Stm32ControlLine.DTR, assertedOutput = false),
    ),
)

val firmware = java.io.File("app.bin").readBytes()

Stm32UartBootloader(config).use { bootloader ->
    val report = bootloader.program(
        request = Stm32FlashRequest(
            startAddress = 0x0800_0000,
            firmware = firmware,
        ),
        progressListener = { progress ->
            println("${progress.stage} ${"%.1f".format(progress.overallPercent)}% ${progress.message}")
        },
    )
    println(report)
}
```

## 运行约束

- 仅支持 JVM
- 目标芯片是否支持某个串口口线进入 System Memory，需要按 `AN2606` 查具体型号
- 大多数 STM32 Bootloader 串口要求偶校验；`STM32WB0` 和 `STM32WL3x` 线按官方说明使用无校验
- 如果尾块不是 4 字节对齐，库会默认补 `0xFF` 后再写入，这是 STM32 `Write Memory` 协议本身的要求
- ST-Link SWD 当前优先覆盖 `STM32F1xx HD (chipId=0x414)`；其他芯片族需要继续补对应 Flash 规程
