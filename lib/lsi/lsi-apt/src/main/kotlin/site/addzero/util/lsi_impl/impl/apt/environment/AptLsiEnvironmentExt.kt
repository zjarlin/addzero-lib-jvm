package site.addzero.util.lsi_impl.impl.apt.environment

import site.addzero.util.lsi.environment.LsiEnvironment
import javax.annotation.processing.ProcessingEnvironment

fun ProcessingEnvironment.toLsiEnvironment(): LsiEnvironment {
 return  AptLsiEnvironment(this)
}
