 package site.addzero.ksp.singletonadapter.kcp

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName

class SingletonAdapterIrGenerationExtension : IrGenerationExtension {

    private val singletonAdapterAnnotation = FqName("site.addzero.ksp.singletonadapter.anno.SingletonAdapter")

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        println("SingletonAdapter KCP Plugin: Processing module with ${moduleFragment.files.size} files")

        // 遍历所有文件，收集需要处理的类
        moduleFragment.files.forEach { file ->
            file.acceptVoid(object : IrVisitorVoid() {
                override fun visitClass(declaration: IrClass) {
                    // 检查类是否有@SingletonAdapter注解
                    val hasAnnotation = declaration.annotations.any { annotation ->
                        annotation.symbol?.owner?.name?.asString() == "SingletonAdapter" ||
                        annotation.type.toString().contains("SingletonAdapter")
                    }

                    if (hasAnnotation) {
                        println("Found @SingletonAdapter class: ${declaration.name}")
                        // KCP IR代码生成非常复杂，需要深入研究Kotlin编译器IR API
                        // 当前版本只做注解检测，完整的代码生成需要更多时间和研究
                        // 可以参考KSP版本的实现逻辑，但IR API完全不同
                    }

                    super.visitClass(declaration)
                }
            })
        }
    }
}
