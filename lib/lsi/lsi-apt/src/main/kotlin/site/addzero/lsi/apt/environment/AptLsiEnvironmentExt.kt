package site.addzero.lsi.apt.environment

import site.addzero.lsi.environment.LsiEnvironment
import javax.annotation.processing.ProcessingEnvironment

fun ProcessingEnvironment.toLsiEnvironment(): LsiEnvironment {
 return site.addzero.lsi.apt.environment.AptLsiEnvironment(this)
}
