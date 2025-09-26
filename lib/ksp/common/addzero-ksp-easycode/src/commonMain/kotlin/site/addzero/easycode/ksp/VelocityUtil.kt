package site.addzero.easycode.ksp

import org.apache.velocity.app.VelocityEngine
import site.addzero.core.ext.bean2map
import kotlin.reflect.KClass

object VelocityUtil {
    fun <T : Any> formatCode(
        templateConent: String, meta: T, kspOption: Map<String, String> = emptyMap(), kclass:
        KClass<T>, velocityEngineCallBack: (VelocityEngine) -> Unit
    ): String {
        val bean2map = meta.bean2map(kclass)

        return TODO("提供返回值")
    }

}
