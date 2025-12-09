package site.addzero.apt

import site.addzero.apt.config.DictProcessorConfig
import site.addzero.apt.config.DictProcessorSettings
import site.addzero.util.str.toBigCamelCase
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

                val enumName = dictMetadata.dictCode.toBigCamelCase()

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
            val enumConstants = dictMetadata.items.joinToString(",\n") { item ->
                val enumEntryName = item.code.toEnumEntryName()
                """    $enumEntryName("${item.code.escapeJavaString()}", "${item.desc.escapeJavaString()}")"""
            }

            val code = """
                package ${settings.enumOutputPackage};

                /**
                 * ${dictMetadata.dictName}
                 *
                 * 数据库字典编码: ${dictMetadata.dictCode}
                 * 自动生成的枚举类，不要手动修改
                 */
                public enum $fullEnumName {

                $enumConstants;

                    private final String code;
                    private final String desc;

                    $fullEnumName(String code, String desc) {
                        this.code = code;
                        this.desc = desc;
                    }

                    public String getCode() {
                        return code;
                    }

                    public String getDesc() {
                        return desc;
                    }

                    /**
                     * 根据编码获取枚举值
                     *
                     * @param code 编码
                     * @return 对应的枚举值，如果不存在则返回null
                     */
                    public static $fullEnumName fromCode(String code) {
                        if (code == null) {
                            return null;
                        }
                        for ($fullEnumName e : values()) {
                            if (e.code.equals(code)) {
                                return e;
                            }
                        }
                        return null;
                    }

                    /**
                     * 根据描述获取枚举值
                     *
                     * @param desc 描述
                     * @return 对应的枚举值，如果不存在则返回null
                     */
                    public static $fullEnumName fromDesc(String desc) {
                        if (desc == null) {
                            return null;
                        }
                        for ($fullEnumName e : values()) {
                            if (e.desc.equals(desc)) {
                                return e;
                            }
                        }
                        return null;
                    }
                }
            """.trimIndent()

            w.print(code)
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
