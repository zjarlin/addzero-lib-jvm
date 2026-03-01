package site.addzero.lsi.apt.logger

import site.addzero.lsi.logger.LsiLogger
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

class AptLsiLogger (val logger: Messager): LsiLogger {
    override fun info(msg: String) {
        logger.printMessage(Diagnostic.Kind.NOTE, msg)
    }

    override fun warn(msg: String) {
        logger.printMessage(Diagnostic.Kind.WARNING, msg)
    }

    override fun error(msg: String) {
        logger.printMessage(Diagnostic.Kind.ERROR, msg)
    }
}
