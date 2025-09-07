package com.addzero.assist

import com.addzero.core.ext.bean2map

object AddFun {
    val <T>T?.getIdExt: Any
        get() = this?.bean2map()["id"].toString()

}
