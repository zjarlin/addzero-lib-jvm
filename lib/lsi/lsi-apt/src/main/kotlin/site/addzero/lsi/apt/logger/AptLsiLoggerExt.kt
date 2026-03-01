package site.addzero.lsi.apt.logger

import site.addzero.lsi.logger.LsiLogger
import javax.annotation.processing.Messager

fun Messager.toLsiLogger(): LsiLogger {
    return _root_ide_package_.site.addzero.lsi.apt.logger.AptLsiLogger(this)
}
