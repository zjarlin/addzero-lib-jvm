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
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import java.io.File

/**
 * IR生成扩展，用于处理字符串字面量的国际化替换
 */
@OptIn(DeprecatedForRemovalCompilerApi::class, UnsafeDuringIrConstructionAPI::class, ObsoleteDescriptorBasedAPI::class)
class I18NIrGenerationExtension(
    private val targetLocale: String = "en",
    private val resourceBasePath: String = "i18n",
    private val generatedResourceFile: String? = null,
) : IrGenerationExtension {

    // 缓存t函数符号
    private var tFunctionSymbol: IrSimpleFunctionSymbol? = null
    private val generatedEntries = linkedMapOf<String, String>()

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.files.forEach { file ->
            // 处理文件中的字符串字面量
            processStringLiterals(file, pluginContext)
        }
        writeGeneratedResources()
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
                        if (componentName in ignoredCallNames || functionName in ignoredCallNames) {
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
        val tFunctionSymbol = findRuntimeFunction(pluginContext)
        val builder = DeclarationIrBuilder(pluginContext, tFunctionSymbol)
        return builder.irCall(tFunctionSymbol).apply {
            putValueArgument(0, builder.irString(resourceKey))
            putValueArgument(1, builder.irString(targetLocale))
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

    private fun writeGeneratedResources() {
        val outputPath = generatedResourceFile?.takeIf(String::isNotBlank) ?: return
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        val mergedEntries = linkedMapOf<String, String>()
        if (outputFile.isFile) {
            mergedEntries.putAll(readProperties(outputFile))
        }
        generatedEntries.forEach { (key, value) ->
            mergedEntries.putIfAbsent(key, value)
        }
        outputFile.writeText(
            buildString {
                mergedEntries.forEach { (key, value) ->
                    append(escapeKey(key))
                    append('=')
                    append(escapeValue(value))
                    append('\n')
                }
            },
        )
    }

    private fun readProperties(file: File): Map<String, String> {
        val entries = linkedMapOf<String, String>()
        file.readLines().forEach { rawLine ->
            val trimmedStart = rawLine.trimStart()
            if (trimmedStart.isBlank() || trimmedStart.startsWith("#") || trimmedStart.startsWith("!")) {
                return@forEach
            }
            val separatorIndex = findSeparatorIndex(rawLine)
            val rawKey = if (separatorIndex >= 0) {
                rawLine.substring(0, separatorIndex)
            } else {
                rawLine
            }
            val rawValue = if (separatorIndex >= 0) {
                rawLine.substring(separatorIndex + 1).trimStart()
            } else {
                ""
            }
            entries[decodeEscapes(rawKey)] = decodeEscapes(rawValue)
        }
        return entries
    }

    private fun findSeparatorIndex(line: String): Int {
        for (index in line.indices) {
            val current = line[index]
            if ((current == '=' || current == ':') && !isEscaped(line, index)) {
                return index
            }
        }
        return -1
    }

    private fun isEscaped(text: String, index: Int): Boolean {
        var backslashCount = 0
        var cursor = index - 1
        while (cursor >= 0 && text[cursor] == '\\') {
            backslashCount += 1
            cursor -= 1
        }
        return backslashCount % 2 == 1
    }

    private fun decodeEscapes(text: String): String {
        if ('\\' !in text) {
            return text
        }
        val decoded = StringBuilder(text.length)
        var index = 0
        while (index < text.length) {
            val current = text[index]
            if (current != '\\' || index == text.lastIndex) {
                decoded.append(current)
                index += 1
                continue
            }
            val escaped = text[index + 1]
            when (escaped) {
                't' -> decoded.append('\t')
                'r' -> decoded.append('\r')
                'n' -> decoded.append('\n')
                'f' -> decoded.append('\u000C')
                'u' -> {
                    val unicodeEnd = index + 6
                    if (unicodeEnd <= text.length) {
                        decoded.append(text.substring(index + 2, unicodeEnd).toInt(16).toChar())
                        index += 6
                        continue
                    }
                    decoded.append(escaped)
                }
                else -> decoded.append(escaped)
            }
            index += 2
        }
        return decoded.toString()
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
            "sourceInformation",
            "sourceInformationMarkerStart",
            "sourceInformationMarkerEnd",
            "traceEventStart",
            "traceEventEnd",
        )
    }
}
