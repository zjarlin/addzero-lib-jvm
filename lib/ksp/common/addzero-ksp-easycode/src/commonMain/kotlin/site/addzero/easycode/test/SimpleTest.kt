package site.addzero.easycode.test

import site.addzero.easycode.ksp.VelocityUtil

data class SimpleData(
    val packageName: String,
    val className: String,
    val fields: List<Field>
)

data class Field(
    val name: String,
    val type: String
)

fun main() {
    // 创建测试数据
    val fields = listOf(
        Field("id", "Int"),
        Field("name", "String"),
        Field("email", "String")
    )
    
    val testData = SimpleData("com.example", "User", fields)
    
    // 创建简单的模板内容
    val templateContent = """
        package ${'$'}packageName
        
        data class ${'$'}className(
        #foreach(${'$'}field in ${'$'}fields)
            val ${'$'}field.name: ${'$'}field.type#if(${'$'}foreach.hasNext),
        #end
        )
    """.trimIndent()
    
    println("Template content:")
    println(templateContent)
    println()
    
    // 使用VelocityUtil格式化代码
    val result = VelocityUtil.formatCode(
        templateConent = templateContent,
        meta = testData,
        kspOption = mapOf("author" to "VelocityTest"),
        kclass = SimpleData::class
    ) {}
    
    println("Generated code:")
    println(result)
}