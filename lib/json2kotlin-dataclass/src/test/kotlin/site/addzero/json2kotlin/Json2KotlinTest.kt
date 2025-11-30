package site.addzero.json2kotlin

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class Json2KotlinTest {
    
    @Test
    fun `test simple object conversion`() {
        val json = """{"name": "张三", "age": 18}"""
        val result = Json2Kotlin.convert(json, "User", "user")
        
        println("=== Class Definitions ===")
        println(result.classDefinitions)
        println()
        println("=== Instance Assignment ===")
        println(result.instanceAssignment)
        println()
        println("=== Full Code ===")
        println(result.fullCode)
        
        assertTrue(result.classDefinitions.contains("data class User"))
        assertTrue(result.classDefinitions.contains("val name: String"))
        assertTrue(result.classDefinitions.contains("val age: Int"))
        assertTrue(result.instanceAssignment.contains("val user: User"))
        assertTrue(result.instanceAssignment.contains("name = \"张三\""))
        assertTrue(result.instanceAssignment.contains("age = 18"))
    }
    
    @Test
    fun `test nested object conversion`() {
        val json = """{
            "id": 1,
            "name": "测试项目",
            "owner": {
                "userId": 100,
                "username": "admin"
            }
        }"""
        
        val result = Json2Kotlin.convert(json, "Project", "project")
        
        println("=== Full Code ===")
        println(result.fullCode)
        
        assertTrue(result.classDefinitions.contains("data class Project"))
        assertTrue(result.classDefinitions.contains("data class Owner"))
    }
    
    @Test
    fun `test array conversion`() {
        val json = """{
            "users": [
                {"name": "用户1", "age": 20},
                {"name": "用户2", "age": 25}
            ]
        }"""
        
        val result = Json2Kotlin.convert(json, "Response", "response")
        
        println("=== Full Code ===")
        println(result.fullCode)
        
        assertTrue(result.classDefinitions.contains("List<User>"))
    }
    
    @Test
    fun `test with package name`() {
        val json = """{"value": 42}"""
        val result = Json2Kotlin.convert(json, "Config", "config", "com.example.generated")
        
        println("=== Full Code ===")
        println(result.fullCode)
        
        assertTrue(result.fullCode.contains("package com.example.generated"))
    }
    
    @Test
    fun `test extension function`() {
        val json = """{"message": "Hello"}"""
        val result = json.toKotlinDataClass("Greeting", "greeting")
        
        println(result.fullCode)
        
        assertTrue(result.classDefinitions.contains("data class Greeting"))
    }
}
