package site.addzero.util

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.generator.FastAutoGenerator
import com.baomidou.mybatisplus.generator.config.TemplateType
import com.baomidou.mybatisplus.generator.config.rules.DateType
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine
import site.addzero.inter.MpGeneratorSettings
import java.io.File

/**
 * 代码生成器类  [VM options = -Dfile.encoding=UTF-8]  中文乱码问题
 */
class MpGenerator(private val mpGeneratorSettings: MpGeneratorSettings) {

    fun gen(outputPath: String, vararg genList: String) {
        val pkg = mpGeneratorSettings.pkg
        val authName = mpGeneratorSettings.authName

        val url = mpGeneratorSettings.url
        val username = mpGeneratorSettings.username
        val password = mpGeneratorSettings.password


        // region 代码生成新版3.5.1以上使用
        FastAutoGenerator.create(url, username, password) //全局配置
            .globalConfig({ builder ->
                builder
                    .author(authName) // 设置作者
                    .disableOpenDir()
                    .enableSwagger()
                    .dateType(DateType.ONLY_DATE)
                    .outputDir(
                        outputPath
                    )
                // 指定输出目录
            }) //包配置
            .packageConfig({
                it
                    .parent(pkg) // 设置父包名，根据实制项目路径修改
                    .entity("entity") // 后面这些是sys文件夹里新建的各分类文件夹
                    .service("service")
                    .serviceImpl("service.impl")
                    .mapper("mapper")
            }) //策略配置
            .strategyConfig({
                it
                    .addInclude(genList.toMutableList()) // 设置需要生成的表名，留空则生产全部表
                    .entityBuilder() //实体类配置
                    .enableLombok() //使用lombok
                    .enableTableFieldAnnotation() //实体类字段注解
                    .idType(IdType.INPUT)
                    .mapperBuilder() //  .enableFileOverride()  // 覆盖已生成文件
                    .enableBaseResultMap() //启用 BaseResultMap 生成
                    .enableBaseColumnList() //启用 BaseColumnList
                    .serviceBuilder()
            } //  .enableFileOverride()  // 覆盖已生成文件
                //                        .formatServiceFileName("%sService")
                //                        .formatServiceImplFileName("%sServiceImpl")
            )

            .templateConfig({ builder ->
                builder.disable(TemplateType.XML)
                builder.controller("/vm/controller.vm")
                builder.entity("/vm/entity.vm")
            })
            .templateEngine(CustomVelocityTemplateEngine(mpGeneratorSettings))
            .execute()
    }

    class CustomVelocityTemplateEngine(private val mpGeneratorSettings: MpGeneratorSettings) :
        VelocityTemplateEngine() {
        public override fun writer(objectMap: MutableMap<String?, Any?>, templatePath: String, outputFile: File) {
            objectMap["superControllerClass"] = "BaseController"
            objectMap["extBaseApiImport"] = mpGeneratorSettings.extBaseApiImport
            objectMap["extExcelApiImport"] = mpGeneratorSettings.extExcelApiImport


            super.writer(objectMap, templatePath, outputFile)
        }
    }
}
