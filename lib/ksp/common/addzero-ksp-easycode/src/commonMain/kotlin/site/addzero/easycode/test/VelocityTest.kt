package site.addzero.easycode.test

import site.addzero.easycode.ksp.VelocityUtil
import kotlin.reflect.KClass

fun main() {
    // 创建测试数据
    val fields = listOf(
        TestField("id", "Int"),
        TestField("name", "String"),
        TestField("email", "String")
    )
    
    val testData = TestData("com.example", "User", fields)
    
    // 创建模板内容
    val templateContent = """
        package ${'$'}packageName
        
        data class ${'$'}className(
        #foreach(${'$'}field in ${'$'}fields)
            val ${'$'}field.name: ${'$'}field.type#if(${'$'}foreach.hasNext),
        #end
        )
    """.trimIndent()
    
    // 使用VelocityUtil格式化代码
    val result = VelocityUtil.formatCode(
        templateConent = templateContent,
        meta = testData,
        kspOption = mapOf("author" to "VelocityTest"),
        kclass = TestData::class
    ) {}
    
    println("Generated code:")
    println(result)
}