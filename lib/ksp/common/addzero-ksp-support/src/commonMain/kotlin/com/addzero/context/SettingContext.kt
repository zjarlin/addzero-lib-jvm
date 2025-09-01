package com.addzero.context

import com.addzero.core.ext.toBean
import com.addzero.core.ext.toMap
import java.util.concurrent.atomic.AtomicReference

object SettingContext {
    private val settingsRef = AtomicReference<Settings?>()

    val settings: Settings
        get() = settingsRef.get() ?: Settings()

    fun initialize(op: Map<String, String>) {
        val toMap = settings.toMap()
        val map = toMap + op

        val mapToBean = map.toBean<Settings>()
        settingsRef.compareAndSet(null, mapToBean)
    }
}
