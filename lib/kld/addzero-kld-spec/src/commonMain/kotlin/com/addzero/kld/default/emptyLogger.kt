package com.addzero.kld.default

import com.addzero.kld.processing.KSPLogger
import com.addzero.kld.symbol.KLNode

val emptyLogger=object : KSPLogger {
    override fun logging(message: String, symbol: KLNode?) {
        TODO("Not yet implemented")
    }

    override fun info(message: String, symbol: KLNode?) {
        TODO("Not yet implemented")
    }

    override fun warn(message: String, symbol: KLNode?) {
        TODO("Not yet implemented")
    }

    override fun error(message: String, symbol: KLNode?) {
        TODO("Not yet implemented")
    }

    override fun exception(e: Throwable) {
        TODO("Not yet implemented")
    }

}
