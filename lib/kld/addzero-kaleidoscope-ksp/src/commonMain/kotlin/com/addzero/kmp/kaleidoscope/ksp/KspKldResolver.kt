package com.addzero.kmp.kaleidoscope.ksp

import com.addzero.kmp.kaleidoscope.core.KldWriter


/**
 * KSP 平台的 KldWriter 实现
 */
class KspKldWriter(private val writer: java.io.Writer) : KldWriter {
    override fun write(text: String) {
        writer.write(text)
    }

    override fun close() {
        writer.close()
    }
}

