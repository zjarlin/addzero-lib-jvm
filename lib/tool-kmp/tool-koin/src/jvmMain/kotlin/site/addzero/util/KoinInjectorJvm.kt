package site.addzero.util

import cn.hutool.core.util.ClassUtil
import org.koin.core.module.Module

/**
 * JVM 平台实现：通过反射加载 Koin KSP 生成的默认模块
 */
internal actual fun loadGeneratedKoinModule(): List<Module> {
    val appmodules = getAppmodules()
    val defaultModules = getDefaultModules()
    val module = defaultModules + appmodules
    return module
}

fun getDefaultModules(): List<Module> {
    val scanPackage1 = ClassUtil.scanPackage("org.koin.ksp.generated", { it.name.contains("KoinDefault") })
    if (scanPackage1.isEmpty()) {
        return emptyList()
    }
    val scanPackage = scanPackage1.first()
        ?: return emptyList()
    val declaredMethods = scanPackage.declaredMethods
    val map = declaredMethods.filter { it.name.contains("getDefaultModule") }.first() ?: return emptyList()
    val invoke = map.invoke(null) as Module
    val listOf = listOf(invoke)
    return listOf
}

private fun getAppmodules(): List<Module> {
    val scanPackage1 = ClassUtil.scanPackage("org.koin.ksp.generated", { it.name.contains("ApplicationGensite") })
    if (scanPackage1.isEmpty()) {
        return emptyList()
    }

    val scanPackage =
        scanPackage1.first()
            ?: return emptyList()
    val declaredMethods = scanPackage.declaredMethods
    val map = declaredMethods.filter { it.name.contains("getConfigurationModules") }.first() ?: return emptyList()
    val invoke = map.invoke(null, DefaultKoinApp) as List<Module>
    return invoke
}
