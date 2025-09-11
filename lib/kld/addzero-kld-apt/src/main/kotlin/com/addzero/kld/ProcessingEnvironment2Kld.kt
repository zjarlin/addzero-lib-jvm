package com.addzero.kld

import com.addzero.kld.abs.KLOutputStream
import com.addzero.kld.default.createDefaultCodeGenerator
import com.addzero.kld.default.emptyLogger
import com.addzero.kld.processing.CodeGenerator
import com.addzero.kld.processing.Dependencies
import com.addzero.kld.processing.KLSymbolProcessorEnvironment
import com.addzero.kld.symbol.KLClassDeclaration
import javax.annotation.processing.ProcessingEnvironment


fun ProcessingEnvironment.toKld(): KLSymbolProcessorEnvironment {
    val messager = this.messager

    val klSymbolProcessorEnvironment = KLSymbolProcessorEnvironment(
        options = this.options,
        kotlinVersion = KotlinVersion.CURRENT,
        codeGenerator = createDefaultCodeGenerator(),
        logger = messager.toKld(),
        apiVersion = KotlinVersion.CURRENT,
        compilerVersion = KotlinVersion.CURRENT,
        platforms = emptyList(),
        kspVersion = KotlinVersion.CURRENT
    )
    return klSymbolProcessorEnvironment
}



fun main() {
    val klSymbolProcessorEnvironment = KLSymbolProcessorEnvironment(
        options = emptyMap<String, String>(),
        kotlinVersion = KotlinVersion.CURRENT, // 使用当前Kotlin版本
        codeGenerator = createDefaultCodeGenerator(),
        logger = emptyLogger,
        apiVersion = KotlinVersion.CURRENT, // 或者从其他地方获取
        compilerVersion = KotlinVersion.CURRENT,
        platforms = emptyList(), // 根据实际情况提供空列表或默认值
        kspVersion = KotlinVersion.CURRENT
    )
    val message = klSymbolProcessorEnvironment.options
    println(message)

}
