package site.addzero.util.lsi_impl.impl.ksp.environment.logger

import com.google.devtools.ksp.processing.KSPLogger
import site.addzero.util.lsi.logger.LsiLogger

class KspLsiLogger(private val logger: KSPLogger) : LsiLogger {
    override fun info(msg: String) {
        logger.info(msg)
    }

    override fun warn(msg: String) {
        logger.warn(msg)
    }

    override fun error(msg: String) {
        logger.error(msg)
    }
}