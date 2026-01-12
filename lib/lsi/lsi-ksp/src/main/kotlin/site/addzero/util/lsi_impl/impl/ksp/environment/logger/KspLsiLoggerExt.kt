package site.addzero.util.lsi_impl.impl.ksp.environment.logger

import com.google.devtools.ksp.processing.KSPLogger
import site.addzero.util.lsi.logger.LsiLogger

fun KSPLogger.toLsiLogger(): LsiLogger {
    return KspLsiLogger(this)
}