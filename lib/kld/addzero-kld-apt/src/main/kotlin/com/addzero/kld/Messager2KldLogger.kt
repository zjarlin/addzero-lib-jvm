package com.addzero.kld

import com.addzero.kld.processing.KSPLogger
import com.addzero.kld.symbol.KLNode
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

 fun Messager.toKld(): KSPLogger {
    val messager = this
    return object : KSPLogger {
        override fun logging(message: String, symbol: KLNode?) {
            messager.printMessage(Diagnostic.Kind.NOTE, message)
        }

        override fun info(message: String, symbol: KLNode?) {
            messager.printMessage(Diagnostic.Kind.NOTE, message)
        }

        override fun warn(message: String, symbol: KLNode?) {
            messager.printMessage(Diagnostic.Kind.WARNING, message)
        }

        override fun error(message: String, symbol: KLNode?) {
            messager.printMessage(Diagnostic.Kind.ERROR, message)
        }

        override fun exception(e: Throwable) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.message)
        }

    }
}

