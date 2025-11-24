package site.addzero.apt

import site.addzero.apt.config.DictProcessorConfig
import site.addzero.apt.config.DictProcessorSettings
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

/**
 * 字典枚举代码生成器 - 生成 Java 风格的枚举类
 */
class DictEnumCodeGenerator(
    private val filer: Filer,
    private val messager: Messager,
) {

    val settings: DictProcessorConfig = DictProcessorSettings.getSettings()
    val enumOutputDirectory: String? = settings.enumOutputDirectory
    private val useCustomOutput = !enumOutputDirectory.isNullOrBlank()

    init {


        if (useCustomOutput) {
            messager.printMessage(
                Diagnostic.Kind.NOTE,
                "[INFO] 使用自定义输出目录: $enumOutputDirectory"
            )
        }
    }

    /**
     * 生成所有字典的枚举类
     */
    fun generateEnumClasses(dictMetadataList: List<DictMetadataExtractor.DictMetadata>) {
        val generatedClassNames = mutableSetOf<String>()

        dictMetadataList.forEach { dictMetadata ->
            try {
                if (dictMetadata.items.isEmpty()) {
                    messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "[WARNING] 字典'${dictMetadata.dictCode}'没有字典项，跳过枚举生成"
                    )
                    return@forEach
                }

                val enumName = dictMetadata.dictCode.toCamelCase(capitalizeFirst = true)

                if (enumName in generatedClassNames) {
                    messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "[WARNING] 字典'${dictMetadata.dictCode}'生成的枚举类名'$enumName'重复，跳过"
                    )
                    return@forEach
                }

                generatedClassNames.add(enumName)
                generateEnumClass(dictMetadata, enumName)
            } catch (e: Exception) {
                messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "[ERROR] 为字典'${dictMetadata.dictCode}'生成枚举类失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 生成单个枚举类
     */
    private fun generateEnumClass(dictMetadata: DictMetadataExtractor.DictMetadata, enumName: String) {
        val fullEnumName = "Enum$enumName"
        val qualifiedName = "${settings.enumOutputPackage}.$fullEnumName"

        val writer = if (useCustomOutput) {
            createCustomOutputWriter(fullEnumName)
        } else {
            val fileObject = filer.createSourceFile(qualifiedName)
            PrintWriter(fileObject.openWriter())
        }

        writer.use { w ->
            // 包声明
            w.println("package $settings.enumOutputPackage;")
            w.println()

            // 类注释
            w.println("/**")
            w.println(" * ${dictMetadata.dictName}")
            w.println(" *")
            w.println(" * 数据库字典编码: ${dictMetadata.dictCode}")
            w.println(" * 自动生成的枚举类，不要手动修改")
            w.println(" */")
            w.println("public enum $fullEnumName {")
            w.println()

            // 生成枚举常量
            val enumConstants = dictMetadata.items.map { item ->
                val enumEntryName = item.code.toEnumEntryName()
                """    $enumEntryName("${item.code.escapeJavaString()}", "${item.desc.escapeJavaString()}")"""
            }
            w.println(enumConstants.joinToString(",\n") + ";")
            w.println()

            // 生成字段
            w.println("    private final String code;")
            w.println("    private final String desc;")
            w.println()

            // 生成构造函数
            w.println("    $fullEnumName(String code, String desc) {")
            w.println("        this.code = code;")
            w.println("        this.desc = desc;")
            w.println("    }")
            w.println()

            // 生成 getter 方法
            w.println("    public String getCode() {")
            w.println("        return code;")
            w.println("    }")
            w.println()
            w.println("    public String getDesc() {")
            w.println("        return desc;")
            w.println("    }")
            w.println()

            // 生成 fromCode 方法
            w.println("    /**")
            w.println("     * 根据编码获取枚举值")
            w.println("     *")
            w.println("     * @param code 编码")
            w.println("     * @return 对应的枚举值，如果不存在则返回null")
            w.println("     */")
            w.println("    public static $fullEnumName fromCode(String code) {")
            w.println("        if (code == null) {")
            w.println("            return null;")
            w.println("        }")
            w.println("        for ($fullEnumName e : values()) {")
            w.println("            if (e.code.equals(code)) {")
            w.println("                return e;")
            w.println("            }")
            w.println("        }")
            w.println("        return null;")
            w.println("    }")
            w.println()

            // 生成 fromDesc 方法
            w.println("    /**")
            w.println("     * 根据描述获取枚举值")
            w.println("     *")
            w.println("     * @param desc 描述")
            w.println("     * @return 对应的枚举值，如果不存在则返回null")
            w.println("     */")
            w.println("    public static $fullEnumName fromDesc(String desc) {")
            w.println("        if (desc == null) {")
            w.println("            return null;")
            w.println("        }")
            w.println("        for ($fullEnumName e : values()) {")
            w.println("            if (e.desc.equals(desc)) {")
            w.println("                return e;")
            w.println("            }")
            w.println("        }")
            w.println("        return null;")
            w.println("    }")

            w.println("}")
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "[INFO] 生成枚举类: $fullEnumName")
    }

    /**
     * 创建自定义输出目录的 Writer
     */
    private fun createCustomOutputWriter(fullEnumName: String): PrintWriter {
        val packagePath = settings.enumOutputPackage.replace('.', File.separatorChar)
        val enumOutputDirectory = settings.enumOutputDirectory
        val outputPath = Paths.get(enumOutputDirectory, packagePath)

        Files.createDirectories(outputPath)

        val filePath = outputPath.resolve("$fullEnumName.java")

        messager.printMessage(
            Diagnostic.Kind.NOTE,
            "[INFO] 生成枚举到自定义目录: $filePath"
        )

        return PrintWriter(Files.newBufferedWriter(filePath))
    }

    /**
     * 转换为驼峰命名
     */
    private fun String.toCamelCase(capitalizeFirst: Boolean): String {
        val words = this.replace(Regex("[_\\-\\s]+"), " ").split(" ")

        return words.withIndex().joinToString("") { (index, word) ->
            when {
                word.isEmpty() -> ""
                index == 0 && !capitalizeFirst -> word.lowercase()
                else -> word.replaceFirstChar { it.uppercase() } +
                        if (word.length > 1) word.substring(1).lowercase() else ""
            }
        }
    }

    /**
     * 转换为合法的枚举项名称
     *
     * 规则：
     * 1. 如果是纯数字，直接加下划线前缀: "0" -> "_0", "123" -> "_123"
     * 2. 如果以数字开头，加下划线前缀: "10min" -> "_10MIN", "1abc" -> "_1ABC"
     * 3. 其他情况转大写并替换非法字符: "status" -> "STATUS"
     */
    private fun String.toEnumEntryName(): String {
        if (this.isEmpty()) {
            return "UNKNOWN"
        }

        // 如果整个字符串都是数字，直接加下划线前缀
        if (this.matches(Regex("^\\d+$"))) {
            return "_$this"
        }

        // 转换为大写并替换非法字符（只保留字母、数字、下划线）
        var processed = this.uppercase()
            .replace(Regex("[^A-Z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .trimEnd('_')

        // 如果以数字开头，加下划线前缀
        if (processed.isNotEmpty() && processed[0].isDigit()) {
            processed = "_$processed"
        }

        // 处理开头的多余下划线
        if (processed.startsWith("__")) {
            processed = processed.replaceFirst(Regex("^_+"), "_")
        }

        // 如果处理后为空或只有下划线，使用默认值
        if (processed.isEmpty() || processed.matches(Regex("^_+$"))) {
            return "UNKNOWN"
        }

        return processed
    }

    /**
     * 转义 Java 字符串
     */
    private fun String.escapeJavaString(): String {
        return this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
