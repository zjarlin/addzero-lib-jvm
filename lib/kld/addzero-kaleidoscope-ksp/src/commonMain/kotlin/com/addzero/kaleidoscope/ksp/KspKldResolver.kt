package com.addzero.kaleidoscope.ksp

import com.addzero.kaleidoscope.core.KldWriter
import java.io.Writer


/**
 * KSP 平台的 KldWriter 实现
 */
class KspKldWriter(private val writer: Writer) : KldWriter {
    override fun write(text: String) {
        writer.write(text)
    }

    override fun close() {
        writer.close()
    }
}

