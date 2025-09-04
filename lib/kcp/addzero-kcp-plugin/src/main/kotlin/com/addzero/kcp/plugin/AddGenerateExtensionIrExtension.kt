package com.addzero.kcp.plugin

import com.addzero.kcp.annotations.AddGenerateExtension
import com.addzero.kcp.annotations.Receiver
import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities

/**
 * 实现 IR 生成（第一步：单参数 → 扩展函数），多 @Receiver 场景稍后补齐
 */
@OptIn(DeprecatedForRemovalCompilerApi::class, UnsafeDuringIrConstructionAPI::class, ObsoleteDescriptorBasedAPI::class)
class AddGenerateExtensionIrExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val irFactory = pluginContext.irFactory
        moduleFragment.files.forEach { file: IrFile ->
            file.declarations.filterIsInstance<IrFunction>().filter { fn ->
                fn.annotations.any { it.symbol.owner.parentClassOrNull?.fqNameWhenAvailable?.asString() == AddGenerateExtension::class.qualifiedName }
            }.forEach { fn ->
                val params = fn.valueParameters
                val marked = params.filter { p ->
                    p.annotations.any { it.symbol.owner.parentClassOrNull?.fqNameWhenAvailable?.asString() == Receiver::class.qualifiedName }
                }

                when {
                    params.size == 1 -> {
                        val receiverParam = params.first()
                        val newFun = (irFactory as IrFactoryImpl).buildFun {
                            name = Name.identifier(fn.name.asString())
                            returnType = fn.returnType
                        }.apply {
                            parent = file
                            visibility = DescriptorVisibilities.PUBLIC
                            isInline = fn.isInline
                            isExternal = false
                            // 扩展接收者
                            this.extensionReceiverParameter = receiverParam.copyTo(this)
                            // 构建函数体：调用原函数并传入接收者作为唯一实参
                            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                                val call = irCall(fn.symbol).apply {
                                    putValueArgument(0, irGet(extensionReceiverParameter!!))
                                }
                                +irReturn(call)
                            }
                        }
                        file.declarations.add(newFun)
                    }
                    else -> Unit
                }
            }
        }
    }
}
