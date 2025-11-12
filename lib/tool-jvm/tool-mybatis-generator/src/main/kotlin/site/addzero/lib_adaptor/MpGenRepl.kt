package site.addzero.lib_adaptor

import cn.hutool.core.io.FileUtil
import site.addzero.app.AdvancedRepl
import site.addzero.app.ParamDef
import site.addzero.util.MpGenerator
import site.addzero.util.PropertyUtil

/**
 * MyBatis Plus代码生成器REPL命令
 */
class MpGenRepl : AdvancedRepl<MpGenRepl.MpGenParams, String> {
    data class MpGenParams(
        val tables: String
    )

    override val command: String = "mpgen"
    override val description: String = "MyBatis Plus代码生成器"

    override val paramDefs: List<ParamDef> = listOf(
        ParamDef(
            name = "tables",
            type = kotlin.reflect.typeOf<String>(),
            description = "表名列表，用逗号分隔",
            defaultValue = "",
            isRequired = false
        )
    )

    override fun eval(params: MpGenParams): String {
        try {
            val configFile = "generator.properties"
            val projectRoot = PropertyUtil.getProperty(configFile, "project.root")
            val module = PropertyUtil.getProperty(configFile, "project.module")
            val pkg = PropertyUtil.getProperty(configFile, "project.package")


            val outputPath = getGenDir(projectRoot, module)
            val mpGenerator = MpGenerator(MpGeneratorSettingsImpl())

            val tableList = if (params.tables.isNotBlank()) {
                params.tables.split(",").map { it.trim() }.toTypedArray()
            } else {
                // 从配置文件读取表列表
                PropertyUtil.getPropertyOrNull(configFile, "generator.tables")?.split(",")?.map { it.trim() }?.toTypedArray()
                    ?: arrayOf()
            }

            if (tableList.isEmpty()) {
                return "未指定任何表"
            }

            mpGenerator.gen(outputPath, pkg, *tableList)
            return "代码生成完成，输出路径: $outputPath"
        } catch (e: Exception) {
            return "代码生成失败: ${e.message}"
        }
    }

    private fun getGenDir(projectRoot: String, module: String): String {
        val file = FileUtil.file(projectRoot, module, "src", "main", "java")
        val outputPath = file.absolutePath
        return outputPath ?: throw RuntimeException("无法构建输出路径")
    }
}
