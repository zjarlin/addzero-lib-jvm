package site.addzero.apt.dict.processor

import site.addzero.apt.dict.dsl.*

/**
 * Simple test to verify nested structure auto-detection functionality
 */
fun main() {
    println("🧪 Testing Nested Structure Auto-Detection")
    
    val enhancer = JavaEntityEnhancer()
    
    // Create a DSL config with nested field rules
    val dslConfig = DslTemplateConfig(
        entityClass = "UserEntity",
        translationRules = listOf(
            EntityTranslationRule(
                entityName = "UserEntity",
                fieldRules = listOf(
                    FieldTranslationRule(
                        fieldName = "status",
                        translationType = TranslationType.SYSTEM_DICT,
                        dictConfigs = listOf(
                            DictConfig(
                                type = TranslationType.SYSTEM_DICT,
                                dictCode = "USER_STATUS"
                            )
                        ),
                        targetFieldName = "statusText"
                    ),
                    FieldTranslationRule(
                        fieldName = "department.type",
                        translationType = TranslationType.SYSTEM_DICT,
                        dictConfigs = listOf(
                            DictConfig(
                                type = TranslationType.SYSTEM_DICT,
                                dictCode = "DEPT_TYPE"
                            )
                        ),
                        targetFieldName = "department_typeText"
                    ),
                    FieldTranslationRule(
                        fieldName = "roles[].level",
                        translationType = TranslationType.TABLE_DICT,
                        dictConfigs = listOf(
                            DictConfig(
                                type = TranslationType.TABLE_DICT,
                                table = "sys_role_level",
                                codeColumn = "level_code",
                                nameColumn = "level_name"
                            )
                        ),
                        targetFieldName = "roles_list_levelText"
                    )
                )
            )
        )
    )
    
    val javaCode = enhancer.generateJavaClassCode(
        packageName = "com.example.entity",
        originalClassName = "UserEntity",
        enhancedClassName = "UserEntityEnhanced",
        dslConfig = dslConfig
    )
    
    println("✅ Generated Java code successfully!")
    println("📄 Code length: ${javaCode.length} characters")
    
    // Verify key features
    val checks = listOf(
        "public class UserEntityEnhanced extends UserEntity" to "Inheritance structure",
        "private String statusText;" to "Simple field generation",
        "private String department_typeText;" to "Nested field generation",
        "private String roles_list_levelText;" to "List field generation",
        "translateNestedStructures(TransApi transApi)" to "Nested structure handling",
        "hasTranslateMethod(Class<?> clazz)" to "Reflection helper methods",
        "java.lang.reflect.Field[] fields" to "Reflection-based nested handling",
        "USER_STATUS" to "System dict translation",
        "sys_role_level" to "Table dict translation"
    )
    
    var passedChecks = 0
    checks.forEach { (pattern, description) ->
        if (javaCode.contains(pattern)) {
            println("✅ $description")
            passedChecks++
        } else {
            println("❌ $description - Pattern not found: $pattern")
        }
    }
    
    println("\n📊 Test Results: $passedChecks/${checks.size} checks passed")
    
    if (passedChecks == checks.size) {
        println("🎉 All tests passed! Nested structure auto-detection is working correctly.")
    } else {
        println("⚠️  Some tests failed. Please check the implementation.")
    }
    
    // Print a sample of the generated code
    println("\n📝 Sample of generated code:")
    println("=" * 50)
    println(javaCode.take(500) + "...")
    println("=" * 50)
}

private operator fun String.times(n: Int): String = this.repeat(n)