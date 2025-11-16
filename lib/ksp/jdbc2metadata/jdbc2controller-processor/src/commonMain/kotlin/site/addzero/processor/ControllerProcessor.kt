package site.addzero.processor


import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import site.addzero.context.SettingContext
import site.addzero.context.SettingContext.settings
import site.addzero.entity.JdbcTableMetadata
import site.addzero.util.JdbcMetadataExtractor
import site.addzero.util.str.*
import java.io.File

class ControllerProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        SettingContext.initialize(environment.options)
//        resolver.getde
        val tables = JdbcMetadataExtractor.initAndGetJdbcMetaDataTables(environment.options)
        // 通过KSP options获取JDBC配置
        // 获取所有表的元数据
        println("解析到${tables.size}张表元数据")
        // 为每个表生成表单和formState
        tables.forEach { table ->
            generateFormFile(table)
        }
        return emptyList()
    }


    private fun generateFormFile(table: JdbcTableMetadata) {
        val tableName = table.tableName
        val entityName = tableName.toBigCamelCase()
        val dir = SettingContext.settings.backendServerSourceDir
        val controllerOutPackage = settings.controllerOutPackage
        val withPkg = dir.withPkg(controllerOutPackage)
        val className = "${entityName}Controller"
        val fileName = className.withFileSuffix(".kt")
        val targetFile = File(withPkg.withFileName(fileName))
        targetFile.parentFile?.mkdirs()
        if (targetFile.exists()) {
            environment.logger.info("Controller文件已存在，跳过生成: ${targetFile.path}")
            return
        }
        // 写入文件
        targetFile.writeText(
            """
package $controllerOutPackage 
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/${tableName.toLowCamelCase()}")
class $className {

    @GetMapping("/page")
    fun page(): Unit {
        // TODO: 
    }

    @PostMapping("/save")
    fun save(): Unit {
        // TODO: 
    }

}
            """.trimMargin()
        )
    }
}

/**
 * 处理器提供者
 */
class ControllerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ControllerProcessor(environment)
    }
}
