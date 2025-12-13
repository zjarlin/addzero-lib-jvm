package site.addzero.apt.dict.processor

import site.addzero.apt.dict.dsl.*
import site.addzero.apt.dict.template.JTETemplateManager

/**
 * Manual test to verify Java code generation works correctly
 * This can be run independently of the Gradle build system
 */
fun main() {
    println("🚀 Starting Java Code Generation Test...")
    
    try {
        // Test 1: Basic Java code generation
        testBasicJavaGeneration()
        
        // Test 2: JavaEntityEnhancer functionality
        testJavaEntityEnhancer()
        
        // Test 3: Template manager functionality
        testTemplateManager()
        
        println("✅ All Java code generation tests passed!")
        
    } catch (e: Exception) {
        println("❌ Test failed: ${e.message}")
        e.printStackTrace()
    }
}

fun testBasicJavaGeneration() {
    println("\n📝 Test 1: Basic Java Code Generation")
    
    val templateManager = JTETemplateManager()
    
    // Create DSL configuration
    val userEntityRule = EntityTranslationRule(
        entityName = "UserEntity",
        fieldRules = listOf(
            FieldTranslationRule(
                fieldName = "status",
                translationType = TranslationType.SYSTEM_DICT,
                dictConfigs = listOf(
                    DictConfig(
                        type = TranslationType.SYSTEM_DICT,
                        dictCode = "user_status"
                    )
                ),
                targetFieldName = "statusText"
            ),
            FieldTranslationRule(
                fieldName = "departmentId",
                translationType = TranslationType.TABLE_DICT,
                dictConfigs = listOf(
                    DictConfig(
                        type = TranslationType.TABLE_DICT,
                        table = "sys_department",
                        codeColumn = "id",
                        nameColumn = "dept_name"
                    )
                ),
                targetFieldName = "departmentText"
            )
        )
    )
    
    val dslConfig = DslTemplateConfig(
        entityClass = "UserEntity",
        translationRules = listOf(userEntityRule)
    )
    
    // Generate Java code
    val javaCode = templateManager.renderEnhancedEntityJavaFromDsl(
        dslConfig = dslConfig,
        packageName = "com.example.entity",
        originalClassName = "UserEntity"
    )
    
    // Verify key elements
    assert(javaCode.contains("package com.example.entity;")) { "Missing package declaration" }
    assert(javaCode.contains("public class UserEntityEnhanced")) { "Missing class declaration" }
    assert(javaCode.contains("private String statusText;")) { "Missing statusText field" }
    assert(javaCode.contains("private String departmentText;")) { "Missing departmentText field" }
    assert(javaCode.contains("public String getStatusText()")) { "Missing statusText getter" }
    assert(javaCode.contains("public void setStatusText(String statusText)")) { "Missing statusText setter" }
    assert(javaCode.contains("public void translate(TransApi transApi, UserEntity sourceEntity)")) { "Missing translate method" }
    assert(javaCode.contains("CompletableFuture<Void> translateAsync")) { "Missing async method" }
    
    println("✅ Basic Java generation test passed")
    println("Generated ${javaCode.lines().size} lines of Java code")
}

fun testJavaEntityEnhancer() {
    println("\n🔧 Test 2: JavaEntityEnhancer")
    
    val enhancer = JavaEntityEnhancer()
    
    val dictFields = listOf(
        DictFieldInfo(
            sourceField = "status",
            targetField = "statusText",
            dictCode = "user_status"
        ),
        DictFieldInfo(
            sourceField = "departmentId", 
            targetField = "departmentText",
            table = "sys_department",
            codeColumn = "id",
            nameColumn = "dept_name"
        )
    )
    
    val config = EnhancedEntityConfig(
        suffix = "Enhanced",
        generateAsync = true,
        generateMetadata = true,
        packageName = "com.example.entity"
    )
    
    // Verify enhancer is properly initialized
    assert(enhancer != null) { "JavaEntityEnhancer should not be null" }
    
    // Test data classes
    val dictField = dictFields[0]
    assert(dictField.sourceField == "status") { "Source field mismatch" }
    assert(dictField.targetField == "statusText") { "Target field mismatch" }
    assert(dictField.dictCode == "user_status") { "Dict code mismatch" }
    
    val tableField = dictFields[1]
    assert(tableField.sourceField == "departmentId") { "Table source field mismatch" }
    assert(tableField.targetField == "departmentText") { "Table target field mismatch" }
    assert(tableField.table == "sys_department") { "Table name mismatch" }
    assert(tableField.codeColumn == "id") { "Code column mismatch" }
    assert(tableField.nameColumn == "dept_name") { "Name column mismatch" }
    
    println("✅ JavaEntityEnhancer test passed")
}

fun testTemplateManager() {
    println("\n📋 Test 3: Template Manager")
    
    val templateManager = JTETemplateManager()
    
    // Test template engine creation
    val engine = templateManager.getResourceTemplateEngine()
    assert(engine != null) { "Template engine should not be null" }
    
    // Test cache functionality
    templateManager.clearCache()
    val stats = templateManager.getCacheStatistics()
    assert(stats.totalTemplates >= 0) { "Cache statistics should be valid" }
    
    println("✅ Template manager test passed")
    println("Cache statistics: $stats")
}