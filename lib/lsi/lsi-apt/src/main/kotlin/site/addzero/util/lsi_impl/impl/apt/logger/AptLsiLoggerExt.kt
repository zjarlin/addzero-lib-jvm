package site.addzero.util.lsi_impl.impl.apt.logger

import site.addzero.util.lsi.logger.LsiLogger
import javax.annotation.processing.Messager

fun Messager.toLsiLogger(): LsiLogger {
    return AptLsiLogger(this)
}
