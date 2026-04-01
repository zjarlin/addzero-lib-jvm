package site.addzero.stm32.bootloader

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Stm32UartBootloaderTest {
    @Test
    fun `connect parses protocol version chip id and commands`() {
        val transport =
            FakeBootloaderTransport(
                reads =
                    mutableListOf(
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x07),
                        byteArrayOf(0x31, 0x00, 0x01, 0x02, 0x11, 0x21, 0x31, 0x44),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x31, 0x00, 0x00),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x01),
                        byteArrayOf(0x04, 0x67),
                        byteArrayOf(0x79),
                    ),
            )
        val bootloader = newBootloader(transport)

        val info = bootloader.connect()

        assertEquals(0x31, info.protocolVersion)
        assertEquals("v3.1", info.protocolVersionText)
        assertEquals(0x31, info.bootloaderVersion)
        assertEquals(0x0467, info.chipId)
        assertTrue(Stm32BootloaderCommand.EXTENDED_ERASE_MEMORY in info.supportedCommands)
        assertTrue(Stm32BootloaderCommand.READ_MEMORY in info.supportedCommands)
    }

    @Test
    fun `enter bootloader toggles boot0 and reset lines`() {
        val transport = FakeBootloaderTransport()
        val sleeper = RecordingSleeper()
        val bootloader =
            newBootloader(
                transport = transport,
                sleeper = sleeper,
                config =
                    defaultConfig(
                        lineControl =
                            Stm32BootloaderLineControl(
                                boot0 = Stm32ControlSignal(Stm32ControlLine.RTS, assertedOutput = true),
                                reset = Stm32ControlSignal(Stm32ControlLine.DTR, assertedOutput = false),
                                boot0SetupDelayMs = 12,
                                resetPulseMs = 34,
                                bootloaderReadyDelayMs = 56,
                            ),
                    ),
            )

        bootloader.enterBootloaderMode()

        assertEquals(
            listOf(
                SignalEvent(Stm32ControlLine.RTS, true),
                SignalEvent(Stm32ControlLine.DTR, false),
                SignalEvent(Stm32ControlLine.DTR, true),
            ),
            transport.signalEvents,
        )
        assertEquals(listOf(12L, 34L, 56L), sleeper.calls)
        assertTrue(transport.clearBuffersCalled)
    }

    @Test
    fun `program uses extended erase pads tail and verifies`() {
        val transport =
            FakeBootloaderTransport(
                reads =
                    mutableListOf(
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x07),
                        byteArrayOf(0x31, 0x00, 0x01, 0x02, 0x11, 0x21, 0x31, 0x44),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x31, 0x00, 0x00),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x01),
                        byteArrayOf(0x04, 0x67),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x01, 0x02, 0x03),
                    ),
            )
        val bootloader =
            newBootloader(
                transport = transport,
                config =
                    defaultConfig(
                        writeChunkSize = 256,
                        readChunkSize = 256,
                    ),
            )

        val report =
            bootloader.program(
                Stm32FlashRequest(
                    startAddress = 0x0800_0000,
                    firmware = byteArrayOf(0x01, 0x02, 0x03),
                    startApplicationAfterWrite = false,
                ),
            )

        assertEquals(3, report.bytesWritten)
        assertEquals(Stm32BootloaderCommand.EXTENDED_ERASE_MEMORY, report.eraseCommand)
        assertTrue(report.verified)
        assertFalse(report.startedApplication)

        val writePayload = transport.writes.single { write -> write.size == 6 && write[0] == 0x03.toByte() }
        assertContentEquals(
            byteArrayOf(
                0x03,
                0x01,
                0x02,
                0x03,
                0xFF.toByte(),
                0xFC.toByte(),
            ),
            writePayload,
        )
        assertTrue(
            transport.writes.any { write ->
                write.contentEquals(byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0x00))
            },
        )
    }

    @Test
    fun `read memory sends complement length frame`() {
        val transport =
            FakeBootloaderTransport(
                reads =
                    mutableListOf(
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x07),
                        byteArrayOf(0x31, 0x00, 0x01, 0x02, 0x11, 0x21, 0x31, 0x44),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x31, 0x00, 0x00),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x01),
                        byteArrayOf(0x04, 0x67),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x11, 0x22),
                    ),
            )
        val bootloader = newBootloader(transport)

        val bytes = bootloader.readMemory(0x0800_0000, 2)

        assertContentEquals(byteArrayOf(0x11, 0x22), bytes)
        assertTrue(
            transport.writes.any { write ->
                write.contentEquals(byteArrayOf(0x01, 0xFE.toByte()))
            },
        )
    }

    @Test
    fun `program emits monotonic progress for ui binding`() {
        val transport =
            FakeBootloaderTransport(
                reads =
                    mutableListOf(
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x07),
                        byteArrayOf(0x31, 0x00, 0x01, 0x02, 0x11, 0x21, 0x31, 0x44),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x31, 0x00, 0x00),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x01),
                        byteArrayOf(0x04, 0x67),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x79),
                        byteArrayOf(0x01, 0x02, 0x03, 0x04),
                    ),
            )
        val bootloader = newBootloader(transport)
        val progressEvents = mutableListOf<Stm32FlashProgress>()

        bootloader.program(
            request =
                Stm32FlashRequest(
                    startAddress = 0x0800_0000,
                    firmware = byteArrayOf(0x01, 0x02, 0x03, 0x04),
                    startApplicationAfterWrite = false,
                ),
            progressListener = Stm32FlashProgressListener { progress ->
                progressEvents += progress
            },
        )

        assertTrue(progressEvents.isNotEmpty())
        assertEquals(Stm32FlashStage.CONNECTING, progressEvents.first().stage)
        assertEquals(Stm32FlashStage.COMPLETED, progressEvents.last().stage)
        assertEquals(100.0, progressEvents.last().overallPercent)
        assertTrue(progressEvents.any { it.stage == Stm32FlashStage.WRITING && it.stageCompletedBytes == 4L })
        assertTrue(progressEvents.any { it.stage == Stm32FlashStage.VERIFYING && it.stageCompletedBytes == 4L })
        assertTrue(progressEvents.any { it.stage == Stm32FlashStage.STARTING_APPLICATION && it.message.contains("跳过") })
        assertTrue(
            progressEvents.zipWithNext().all { (left, right) ->
                right.overallPercent >= left.overallPercent
            },
        )
    }

    private fun newBootloader(
        transport: FakeBootloaderTransport,
        sleeper: RecordingSleeper = RecordingSleeper(),
        config: Stm32BootloaderConfig = defaultConfig(),
    ): Stm32UartBootloader {
        return Stm32UartBootloader(
            config = config,
            transport = transport,
            sleeper = sleeper,
            marker = true,
        )
    }

    private fun defaultConfig(
        lineControl: Stm32BootloaderLineControl? = null,
        writeChunkSize: Int = 256,
        readChunkSize: Int = 256,
    ): Stm32BootloaderConfig {
        return Stm32BootloaderConfig(
            serialConfig = stm32BootloaderSerialConfig("COM9"),
            lineControl = lineControl,
            writeChunkSize = writeChunkSize,
            readChunkSize = readChunkSize,
        )
    }
}

private class FakeBootloaderTransport(
    val reads: MutableList<ByteArray> = mutableListOf(),
) : Stm32BootloaderTransport {
    val writes = mutableListOf<ByteArray>()
    val signalEvents = mutableListOf<SignalEvent>()
    var clearBuffersCalled: Boolean = false

    override fun write(bytes: ByteArray) {
        writes += bytes.copyOf()
    }

    override fun readExact(size: Int): ByteArray {
        require(reads.isNotEmpty()) {
            "没有为 size=$size 预置响应数据"
        }
        val next = reads.removeAt(0)
        require(next.size == size) {
            "预置响应长度错误：expected=$size actual=${next.size}"
        }
        return next
    }

    override fun clearBuffers() {
        clearBuffersCalled = true
    }

    override fun setLine(
        line: Stm32ControlLine,
        output: Boolean,
    ) {
        signalEvents += SignalEvent(line, output)
    }

    override fun close() {
    }
}

private data class SignalEvent(
    val line: Stm32ControlLine,
    val output: Boolean,
)

private class RecordingSleeper : Stm32BootloaderSleeper {
    val calls = mutableListOf<Long>()

    override fun sleep(durationMs: Long) {
        calls += durationMs
    }
}
