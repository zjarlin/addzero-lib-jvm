package site.addzero.easycode.ksp

import site.addzero.core.ext.bean2map
import kotlin.reflect.KClass

object VelocityUtil {
     fun <T : Any> formatCode(templateConent: String, meta: T, kclass: KClass<T>): String {
         val bean2map = meta.bean2map(kclass)

         return TODO("提供返回值")
    }

}
