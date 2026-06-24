package site.addzero.lsi.apt.logger

import site.addzero.lsi.logger.LsiLogger
import javax.annotation.processing.Messager

fun Messager.toLsiLogger(): LsiLogger {
    return site.addzero.lsi.apt.logger.AptLsiLogger(this)
}
