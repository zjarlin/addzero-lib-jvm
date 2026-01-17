package site.addzero.json2kotlin

import site.addzero.json2kotlin.generator.GeneratedCode
import site.addzero.json2kotlin.generator.GeneratorConfig
import site.addzero.json2kotlin.generator.KotlinCodeGenerator
import site.addzero.json2kotlin.parser.JsonParser
import site.addzero.json2kotlin.parser.ParserConfig

object Json2Kotlin {
    
    fun convert(
        json: String,
        rootClassName: String = "Root",
        variableName: String = "data",
        packageName: String? = null
    ): GeneratedCode {
        val parser = JsonParser(ParserConfig())
        val parseResult = parser.parse(json, rootClassName)
        
        val generator = KotlinCodeGenerator(GeneratorConfig(packageName = packageName))
        return generator.generate(parseResult, variableName)
    }
    
    fun convertToFullCode(
        json: String,
        rootClassName: String = "Root",
        variableName: String = "data",
        packageName: String? = null
    ): String = convert(json, rootClassName, variableName, packageName).fullCode
}

fun String.toKotlinDataClass(
    rootClassName: String = "Root",
    variableName: String = "data",
    packageName: String? = null
): GeneratedCode = Json2Kotlin.convert(this, rootClassName, variableName, packageName)
