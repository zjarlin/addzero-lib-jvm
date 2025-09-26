package site.addzero.assist

import site.addzero.core.ext.bean2map

object AddFun {
    val <T>T?.getIdExt: Any
        get() = {
            val comparable = try {
                val bean2map = this?.bean2map()
                val any = bean2map?.get("id")
                any.toString()
            } catch (e: Exception) {
                this.hashCode()
            }
            comparable
        }

}
