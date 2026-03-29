package site.addzero.kcp.i18n.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCall
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
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import java.io.File

/**
 * IR生成扩展，用于处理字符串字面量的国际化替换
 */
@OptIn(DeprecatedForRemovalCompilerApi::class, UnsafeDuringIrConstructionAPI::class, ObsoleteDescriptorBasedAPI::class)
class I18NIrGenerationExtension(
    private val resourceBasePath: String = "i18n",
    private val generatedCatalogFile: String? = null,
    scanScope: String = I18NPluginKeys.scanScopeAll,
) : IrGenerationExtension {

    // 缓存t函数符号
    private var tFunctionSymbol: IrSimpleFunctionSymbol? = null
    private val generatedEntries = linkedMapOf<String, String>()
    private val effectiveScanScope = ScanScope.from(scanScope)

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.files.forEach { file ->
            // 处理文件中的字符串字面量
            processStringLiterals(file, pluginContext)
        }
        writeGeneratedCatalog()
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
                        if (isAnnotationArgument(currentCall) || !shouldScanCurrentFunction(currentFunction)) {
                            return super.visitConst(expression)
                        }
                        if (shouldIgnoreStringLiteral(stringValue, functionName, componentName)) {
                            return super.visitConst(expression)
                        }

                        // 生成资源键，按照要求的格式：文件名_函数名_组件名_参数名
                        val resourceKey = generateResourceKey(
                            fileName,
                            functionName,
                            componentName,
                            stringValue
                        )
                        generatedEntries.putIfAbsent(resourceKey, stringValue)

                        println("[I18N] Generated resource key: '$resourceKey' for string: '$stringValue'")

                        // 创建t函数调用
                        return createTFunctionCall(pluginContext, resourceKey, stringValue)
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
    private fun createTFunctionCall(
        pluginContext: IrPluginContext,
        resourceKey: String,
        fallbackText: String,
    ): IrExpression {
        val tFunctionSymbol = findRuntimeFunction(pluginContext)
        val builder = DeclarationIrBuilder(pluginContext, tFunctionSymbol)
        return builder.irCall(tFunctionSymbol).apply {
            putValueArgument(0, builder.irString(resourceKey))
            putValueArgument(1, builder.irString(fallbackText))
            putValueArgument(2, builder.irString(resourceBasePath))
        }
    }

    private fun findRuntimeFunction(pluginContext: IrPluginContext): IrSimpleFunctionSymbol {
        if (tFunctionSymbol != null) {
            return tFunctionSymbol!!
        }
        val tFunction = pluginContext.referenceFunctions(I18NPluginKeys.runtimeFunctionCallableId)
            .singleOrNull()
            ?: error(
                "无法找到 site.addzero.util.i18nT 函数，请确认业务项目已引入 kcp-i18n-runtime " +
                    "或直接使用 site.addzero.kcp.i18n Gradle 插件",
            )
        tFunctionSymbol = tFunction
        return tFunction
    }

    private fun writeGeneratedCatalog() {
        val outputPath = generatedCatalogFile?.takeIf(String::isNotBlank) ?: return
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(
            buildString {
                generatedEntries
                    .toSortedMap()
                    .forEach { (key, value) ->
                    append(escapeKey(key))
                    append('=')
                    append(escapeValue(value))
                    append('\n')
                }
            },
        )
    }

    private fun escapeKey(text: String): String {
        return buildString {
            text.forEachIndexed { index, char ->
                when {
                    char == '\\' -> append("\\\\")
                    char == '=' -> append("\\=")
                    char == ':' -> append("\\:")
                    char == '\n' -> append("\\n")
                    char == '\r' -> append("\\r")
                    char == '\t' -> append("\\t")
                    char == '#' && index == 0 -> append("\\#")
                    char == '!' && index == 0 -> append("\\!")
                    else -> append(char)
                }
            }
        }
    }

    private fun escapeValue(text: String): String {
        return buildString {
            text.forEachIndexed { index, char ->
                when {
                    char == '\\' -> append("\\\\")
                    char == '\n' -> append("\\n")
                    char == '\r' -> append("\\r")
                    char == '\t' -> append("\\t")
                    char == ' ' && index == 0 -> append("\\ ")
                    else -> append(char)
                }
            }
        }
    }

    companion object {
        private val ignoredCallNames = setOf(
            "mutableStateOf",
            "getUri",
            "readBytes",
            "sourceInformation",
            "sourceInformationMarkerStart",
            "sourceInformationMarkerEnd",
            "traceEventStart",
            "traceEventEnd",
        )
    }

    private enum class ScanScope {
        ALL,
        COMPOSABLE_ONLY,
        ;

        companion object {
            fun from(value: String): ScanScope {
                return when (value.trim()) {
                    I18NPluginKeys.scanScopeComposableOnly -> COMPOSABLE_ONLY
                    "", I18NPluginKeys.scanScopeAll -> ALL
                    else -> error(
                        "Unsupported kcp-i18n scanScope `$value`. " +
                            "Supported values: `${I18NPluginKeys.scanScopeAll}`, `${I18NPluginKeys.scanScopeComposableOnly}`.",
                    )
                }
            }
        }
    }

    private fun shouldIgnoreStringLiteral(
        stringValue: String,
        functionName: String?,
        componentName: String?,
    ): Boolean {
        if (componentName in ignoredCallNames || functionName in ignoredCallNames) {
            return true
        }
        return stringValue.startsWith("composeResources/")
    }

    private fun shouldScanCurrentFunction(function: IrFunction?): Boolean {
        return when (effectiveScanScope) {
            ScanScope.ALL -> true
            ScanScope.COMPOSABLE_ONLY -> function?.hasAnnotation(I18NPluginKeys.composableAnnotationFqName) == true
        }
    }

    private fun IrFunction.hasAnnotation(annotationFqName: FqName): Boolean {
        return annotations.any { annotation ->
            (annotation.symbol.owner.parent as? IrClass)?.fqNameWhenAvailable == annotationFqName
        }
    }

    private fun isAnnotationArgument(call: IrFunctionAccessExpression?): Boolean {
        val constructorCall = call as? IrConstructorCall ?: return false
        val annotationClass = constructorCall.symbol.owner.parent as? IrClass ?: return false
        return annotationClass.kind == ClassKind.ANNOTATION_CLASS
    }
}
