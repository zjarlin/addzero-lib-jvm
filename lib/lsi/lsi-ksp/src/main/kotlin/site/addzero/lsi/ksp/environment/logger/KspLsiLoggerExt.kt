package site.addzero.lsi.ksp.environment.logger

import com.google.devtools.ksp.processing.KSPLogger
import site.addzero.lsi.logger.LsiLogger

fun KSPLogger.toLsiLogger(): LsiLogger {
    return KspLsiLogger(this)
}
