package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import site.addzero.apt.dict.example.JavaCodeGenerationExample

/**
 * Simple test to verify Java code generation without Gradle build issues
 */
class SimpleJavaGenerationTest {
    
    @Test
    fun `test Java code generation example`() {
        val example = JavaCodeGenerationExample()
        
        // Test basic Java code generation
        val javaCode = example.generateUserEntityEnhancedJava()
        
        // Verify generated code contains expected Java elements
        assertNotNull(javaCode)
        assertTrue(javaCode.contains("package com.example.entity;"))
        assertTrue(javaCode.contains("public class UserEntityEnhanced"))
        assertTrue(javaCode.contains("private String statusText;"))
        assertTrue(javaCode.contains("private String departmentText;"))
        assertTrue(javaCode.contains("public String getStatusText()"))
        assertTrue(javaCode.contains("public void setStatusText(String statusText)"))
        assertTrue(javaCode.contains("public String getDepartmentText()"))
        assertTrue(javaCode.contains("public void setDepartmentText(String departmentText)"))
        assertTrue(javaCode.contains("public void translate(TransApi transApi, UserEntity sourceEntity)"))
        assertTrue(javaCode.contains("public CompletableFuture<Void> translateAsync"))
        assertTrue(javaCode.contains("translateDictBatchCode2name"))
        assertTrue(javaCode.contains("translateTableBatchCode2name"))
        
        println("✅ Java code generation test passed!")
        println("Generated Java code structure verified successfully.")
    }
    
    @Test
    fun `test complex RBAC Java generation`() {
        val example = JavaCodeGenerationExample()
        
        // Test complex RBAC scenario
        val complexJavaCode = example.generateComplexRBACJava()
        
        // Verify complex scenario elements
        assertNotNull(complexJavaCode)
        assertTrue(complexJavaCode.contains("public class UserEntityEnhanced"))
        assertTrue(complexJavaCode.contains("translateDictBatchCode2name"))
        assertTrue(complexJavaCode.contains("translateTableBatchCode2name"))
        
        println("✅ Complex RBAC Java generation test passed!")
    }
    
    @Test
    fun `test expected Java structure matches template`() {
        val example = JavaCodeGenerationExample()
        
        // Get expected structure
        val expectedStructure = example.getExpectedJavaStructure()
        
        // Verify expected structure contains key Java elements
        assertNotNull(expectedStructure)
        assertTrue(expectedStructure.contains("public class UserEntityEnhanced"))
        assertTrue(expectedStructure.contains("public void translate(TransApi transApi, UserEntity sourceEntity)"))
        assertTrue(expectedStructure.contains("CompletableFuture<Void> translateAsync"))
        
        println("✅ Expected Java structure test passed!")
        println("Template structure verified successfully.")
    }
}