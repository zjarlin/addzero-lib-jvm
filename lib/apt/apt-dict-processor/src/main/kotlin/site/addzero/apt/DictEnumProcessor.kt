package site.addzero.apt

import site.addzero.apt.config.DictProcessorSettings
import java.sql.SQLException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * 字典枚举 APT 处理器
 *
 * 从数据库字典表生成 Java 风格的枚举类
 *
 * 使用 apt-buddy 插件生成的强类型配置类
 *
 * 支持的配置选项（小驼峰格式）：
 * - dictAptEnabled: 是否启用字典 APT 处理器（默认: false）
 * - jdbcDriver: JDBC 驱动类名
 * - jdbcUrl: 数据库连接 URL
 * - jdbcUsername: 数据库用户名
 * - jdbcPassword: 数据库密码
 * - dictTableName: 字典主表名称
 * - dictIdColumn: 字典主表 ID 列名
 * - dictCodeColumn: 字典主表代码列名
 * - dictNameColumn: 字典主表名称列名
 * - dictItemTableName: 字典项表名称
 * - dictItemForeignKeyColumn: 字典项表外键列名
 * - dictItemCodeColumn: 字典项代码列名
 * - dictItemNameColumn: 字典项名称列名
 * - enumOutputPackage: 生成的枚举类包名
 * - enumOutputDirectory: 枚举类输出目录（可选）
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DictEnumProcessor : AbstractProcessor() {

    private lateinit var metadataExtractor: DictMetadataExtractor
    private lateinit var enumCodeGenerator: DictEnumCodeGenerator
    private var processed = false

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)

        val options = processingEnv.options

        try {
            // 初始化 apt-buddy 生成的配置（第一行）
            DictProcessorSettings.initialize(options)
            val config = DictProcessorSettings.getSettings()

            // 检查是否启用字典 APT 处理器
            val enabled = config.dictAptEnabled?.toBoolean() ?: false
            if (!enabled) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "[INFO] 字典 APT 处理器未启用 (dictAptEnabled=false)，跳过处理"
                )
                return
            }

            // 打印所有接收到的选项（调试用）
            if (options.isNotEmpty()) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "[DEBUG] 接收到的 APT 选项: ${options.keys}"
                )
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "[WARNING] 未接收到任何 APT 选项，将跳过字典枚举生成"
                )
                return
            }

            // 验证必填字段
            if (config.jdbcUrl.isNullOrEmpty()) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "[ERROR] jdbcUrl 是必填项"
                )
                return
            }

            if (config.enumOutputPackage.isNullOrEmpty()) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "[ERROR] enumOutputPackage 是必填项"
                )
                return
            }


            // 打印配置信息（不包含敏感信息）
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                """
                [INFO] 字典处理器配置:
                  启用状态: ✅ 已启用
                  数据库URL: ${config.jdbcUrl}
                  字典表: ${config.dictTableName ?: "sys_dict_type"}
                  字典项表: ${config.dictItemTableName ?: "sys_dict_data"}
                  输出包名: ${config.enumOutputPackage}
                  输出目录: ${config.enumOutputDirectory ?: "默认(target/generated-sources/annotations)"}
                """.trimIndent()
            )


            // 初始化组件
            this.metadataExtractor = DictMetadataExtractor(processingEnv.messager)
            this.enumCodeGenerator = DictEnumCodeGenerator(processingEnv.filer, processingEnv.messager)

        } catch (e: IllegalArgumentException) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "[ERROR] 初始化失败: ${e.message}"
            )
        } catch (e: Exception) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "[ERROR] 初始化失败: ${e.message}\n${e.stackTraceToString()}"
            )
        }
    }

    override fun process(
        annotations: Set<TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (processed || roundEnv.processingOver()) {
            return false
        }

        processed = true

        // 检查是否初始化成功
        if (!::metadataExtractor.isInitialized || !::enumCodeGenerator.isInitialized) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.WARNING,
                "[WARNING] 字典处理器未正确初始化，跳过处理"
            )
            return false
        }

        try {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "[INFO] 开始收集字典元数据..."
            )

            val dictMetadataList = metadataExtractor.extractDictMetadata()

            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "[INFO] 成功收集到 ${dictMetadataList.size} 个字典的元数据"
            )

            if (dictMetadataList.isNotEmpty()) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "[INFO] 开始生成 Java 枚举类..."
                )

                enumCodeGenerator.generateEnumClasses(dictMetadataList)

                processingEnv.messager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "[INFO] ✅ 成功生成 ${dictMetadataList.size} 个 Java 枚举类"
                )
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "[WARNING] 未找到字典数据，没有生成任何枚举类"
                )
            }

        } catch (e: ClassNotFoundException) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] ========== JDBC驱动加载失败 ==========")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 找不到JDBC驱动: ${e.message}")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 请检查 annotationProcessorPaths 中是否包含 mysql-connector-java")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[WARNING] 跳过字典枚举生成过程")
        } catch (e: SQLException) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] ========== 数据库操作失败 ==========")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] SQL异常类型: ${e.javaClass.name}")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 错误消息: ${e.message}")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 错误代码: ${e.errorCode}")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] SQL状态: ${e.sqlState}")
            
            e.cause?.let { cause ->
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 根本原因类型: ${cause.javaClass.name}")
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 根本原因消息: ${cause.message}")
                
                // 如果还有更深层的原因
                cause.cause?.let { deeperCause ->
                    processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 更深层原因: ${deeperCause.javaClass.name}")
                    processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 更深层消息: ${deeperCause.message}")
                }
            }
            
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[WARNING] 跳过字典枚举生成过程")
        } catch (e: Exception) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] ========== 未预期的异常 ==========")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 异常类型: ${e.javaClass.name}")
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 错误消息: ${e.message}")
            
            e.cause?.let { cause ->
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 原因类型: ${cause.javaClass.name}")
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[ERROR] 原因消息: ${cause.message}")
            }
            
            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG] 完整堆栈跟踪:")
            e.stackTraceToString().lines().take(20).forEach { line ->
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "[DEBUG]   $line")
            }
            
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "[WARNING] 跳过字典枚举生成过程")
        }

        return false
    }

    override fun getSupportedOptions(): Set<String> {
        return setOf(
            // 小驼峰格式（apt-buddy 生成的配置类对应）
            "dictAptEnabled",
            "jdbcDriver", "jdbcUrl", "jdbcUsername", "jdbcPassword",
            "dictTableName", "dictIdColumn", "dictCodeColumn", "dictNameColumn",
            "dictItemTableName", "dictItemForeignKeyColumn",
            "dictItemCodeColumn", "dictItemNameColumn",
            "enumOutputPackage", "enumOutputDirectory"
        )
    }
}

/**
 * 内部配置数据类
 * 用于将 apt-buddy 生成的配置转换为处理器需要的格式
 */
//data class ProcessorConfig(
//    val jdbcDriver: String,
//    val jdbcUrl: String,
//    val jdbcUsername: String,
//    val jdbcPassword: String,
//    val dictTableName: String,
//    val dictIdColumn: String,
//    val dictCodeColumn: String,
//    val dictNameColumn: String,
//    val dictItemTableName: String,
//    val dictItemForeignKeyColumn: String,
//    val dictItemCodeColumn: String,
//    val dictItemNameColumn: String,
//    val enumOutputPackage: String,
//    val enumOutputDirectory: String?
//)
