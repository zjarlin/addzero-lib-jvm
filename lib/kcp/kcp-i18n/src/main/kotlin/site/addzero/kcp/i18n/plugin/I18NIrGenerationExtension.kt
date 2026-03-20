package site.addzero.kcp.i18n.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import java.io.File

/**
 * IR生成扩展，用于处理字符串字面量的国际化替换
 */
@OptIn(DeprecatedForRemovalCompilerApi::class, UnsafeDuringIrConstructionAPI::class, ObsoleteDescriptorBasedAPI::class)
class I18NIrGenerationExtension(
    private val targetLocale: String = "en",
    private val resourceBasePath: String = "i18n"
) : IrGenerationExtension {

    // 缓存t函数符号
    private var tFunctionSymbol: IrSimpleFunctionSymbol? = null
    private var i8nutilObjectSymbol: IrClassSymbol? = null

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        resolveRuntimeSymbols(moduleFragment)
        moduleFragment.files.forEach { file ->
            // 处理文件中的字符串字面量
            processStringLiterals(file, pluginContext)
        }
    }

    /**
     * 处理字符串字面量的国际化
     */
    private fun processStringLiterals(file: IrFile, pluginContext: IrPluginContext) {
        // 获取文件名（不含扩展名）
        val fileName = File(file.fileEntry.name).nameWithoutExtension

        file.transform(object : IrElementTransformerVoid() {
            private var currentFunction: IrFunction? = null
            private var currentCall: IrFunctionAccessExpression? = null
            private var currentClass: IrClass? = null

            override fun visitClass(declaration: IrClass): IrStatement {
                currentClass = declaration
                try {
                    return super.visitClass(declaration)
                } finally {
                    currentClass = null
                }
            }

            override fun visitFunction(declaration: IrFunction): IrStatement {
                currentFunction = declaration
                try {
                    return super.visitFunction(declaration)
                } finally {
                    currentFunction = null
                }
            }

            override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
                currentCall = expression
                try {
                    return super.visitFunctionAccess(expression)
                } finally {
                    currentCall = null
                }
            }

            override fun visitConst(expression: IrConst): IrExpression {
                // 检查是否为字符串常量
                if (expression.kind == IrConstKind.String) {
                    val stringValue = expression.value as String

                    // 如果字符串不是空的
                    if (
                        stringValue.isNotEmpty() &&
                        currentClass?.fqNameWhenAvailable?.asString() != "site.addzero.util.I8nutil"
                    ) {
                        // 获取组件名（如果在函数调用中）
                        val componentName = try {
                            currentCall?.symbol?.owner?.name?.asString()
                        } catch (e: Exception) {
                            null
                        }

                        // 获取函数名
                        val functionName = try {
                            currentFunction?.name?.asString()
                        } catch (e: Exception) {
                            null
                        }

                        // 生成资源键，按照要求的格式：文件名_函数名_组件名_参数名
                        val resourceKey = generateResourceKey(
                            fileName,
                            functionName,
                            componentName,
                            stringValue
                        )

                        println("[I18N] Generated resource key: '$resourceKey' for string: '$stringValue'")

                        // 创建t函数调用
                        return createTFunctionCall(pluginContext, expression, resourceKey)
                    }
                }

                return super.visitConst(expression)
            }
        }, null)
    }

    /**
     * 生成符合要求格式的资源键
     * 格式: 文件名_函数名_组件名_参数名
     */
    private fun generateResourceKey(fileName: String, functionName: String?, componentName: String?, stringValue: String): String {
        return buildString {
            append(fileName)
            if (functionName != null) {
                append("_")
                append(functionName)
            }
            if (componentName != null) {
                append("_")
                append(componentName)
            }
            append("_")
            // 对于参数名，我们使用固定的"text"，因为IR层面难以准确获取参数名
            append("text")

            // 如果有重复的键，添加数字后缀
            // 这里简化处理，实际实现中可能需要更复杂的去重逻辑
            append("_")
            append(stringValue)
        }
    }

    /**
     * 创建t函数调用
     */
    private fun createTFunctionCall(pluginContext: IrPluginContext, originalExpression: IrConst, resourceKey: String): IrExpression {
        // 直接创建对 site.addzero.util.I8nutil.t(string) 的调用
        val tFunctionSymbol = tFunctionSymbol ?: error("无法找到 site.addzero.util.I8nutil.t 函数")
        val i8nutilObjectSymbol = i8nutilObjectSymbol ?: error("无法找到 site.addzero.util.I8nutil 对象")
        val builder = DeclarationIrBuilder(pluginContext, tFunctionSymbol)
        return builder.irCall(tFunctionSymbol).apply {
            dispatchReceiver = builder.irGetObject(i8nutilObjectSymbol)
            // 创建字符串参数
            putValueArgument(0, builder.irString(resourceKey))
        }
    }

    private fun resolveRuntimeSymbols(moduleFragment: IrModuleFragment) {
        if (tFunctionSymbol != null && i8nutilObjectSymbol != null) {
            return
        }

        val i8nutilClass = moduleFragment.files
            .asSequence()
            .flatMap { file -> file.declarations.asSequence() }
            .filterIsInstance<IrClass>()
            .firstOrNull { irClass ->
                irClass.fqNameWhenAvailable?.asString() == "site.addzero.util.I8nutil"
            }
            ?: error("无法找到 site.addzero.util.I8nutil 类")

        i8nutilObjectSymbol = i8nutilClass.symbol
        val tFunction = i8nutilClass.declarations
            .filterIsInstance<IrFunction>()
            .firstOrNull { function ->
                function.name.asString() == "t" && function.valueParameters.size == 1
            }
            ?: error("无法找到 site.addzero.util.I8nutil.t 函数")
        tFunctionSymbol = tFunction.symbol as IrSimpleFunctionSymbol
    }
}
