package site.addzero.lsi.apt.environment

import site.addzero.lsi.environment.LsiEnvironment
import site.addzero.lsi.logger.LsiLogger
import site.addzero.lsi.apt.logger.toLsiLogger
import javax.annotation.processing.ProcessingEnvironment

class AptLsiEnvironment(val processingEnvironment: ProcessingEnvironment) :
    LsiEnvironment {
    override val options: MutableMap<String, String>
        get() {
            val options1 = processingEnvironment.options
            return options1
        }
    override val logger: LsiLogger
        get() {
            val messager = processingEnvironment.messager
            return messager.toLsiLogger()
        }
}

