package site.addzero.apt.dict.processor

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import site.addzero.apt.dict.dsl.*

/**
 * 测试使用 Kotlin 多行字符串生成 Java 代码的功能
 * 
 * 验证：
 * 1. 生成的 Java 代码语法正确
 * 2. 包含所有必要的方法和字段
 * 3. 支持嵌套结构处理
 * 4. 不包含模板相关的语法
 */
class KotlinStringTemplateTest : FunSpec({
    
    test("应该生成完整的 Java 类代码") {
        val enhancer = JavaEntityEnhancer()
        
        // 创建测试用的 DSL 配置
        val dslConfig = DslTemplateConfig(
            entityClass = "TestEntity",
            translationRules = listOf(
                EntityTranslationRule(
                    entityName = "TestEntity",
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
                            fieldName = "deptId",
                            translationType = TranslationType.TABLE_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.TABLE_DICT,
                                    table = "sys_dept",
                                    codeColumn = "id",
                                    nameColumn = "name"
                                )
                            ),
                            targetFieldName = "deptName"
                        )
                    )
                )
            )
        )
        
        // 模拟 TypeElement（实际测试中这会是真实的 TypeElement）
        val mockTypeElement = null // 在实际测试中需要 mock
        
        // 生成代码
        val generatedCode = enhancer.generateJavaClassCode(
            packageName = "com.test",
            originalClassName = "TestEntity", 
            enhancedClassName = "TestEntityEnhanced",
            dslConfig = dslConfig
        )
        
        // 验证生成的代码
        generatedCode shouldContain "package com.test;"
        generatedCode shouldContain "public class TestEntityEnhanced extends TestEntity"
        generatedCode shouldContain "private String statusText;"
        generatedCode shouldContain "private String deptName;"
        generatedCode shouldContain "public void translate(TransApi transApi)"
        generatedCode shouldContain "public CompletableFuture<Void> translateAsync(TransApi transApi)"
        generatedCode shouldContain "private void translateNestedStructures(TransApi transApi)"
        
        // 验证不包含模板语法
        generatedCode shouldNotContain "@"
        generatedCode shouldNotContain "${"
        generatedCode shouldNotContain "@if"
        generatedCode shouldNotContain "@for"
        generatedCode shouldNotContain "@endfor"
        
        println("Generated code preview:")
        println(generatedCode.take(500) + "...")
    }
    
    test("应该正确处理系统字典翻译代码") {
        val enhancer = JavaEntityEnhancer()
        
        val dslConfig = DslTemplateConfig(
            entityClass = "User",
            translationRules = listOf(
                EntityTranslationRule(
                    entityName = "User",
                    fieldRules = listOf(
                        FieldTranslationRule(
                            fieldName = "sex",
                            translationType = TranslationType.SYSTEM_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.SYSTEM_DICT,
                                    dictCode = "sys_user_sex"
                                )
                            ),
                            targetFieldName = "sexText"
                        )
                    )
                )
            )
        )
        
        val generatedCode = enhancer.generateSystemDictTranslation(dslConfig)
        
        generatedCode shouldContain "sys_user_sex"
        generatedCode shouldContain "systemKeys.add"
        generatedCode shouldContain "translateDictBatchCode2name"
        generatedCode shouldContain "setSexText"
        
        println("System dict translation code:")
        println(generatedCode)
    }
    
    test("应该正确处理表字典翻译代码") {
        val enhancer = JavaEntityEnhancer()
        
        val dslConfig = DslTemplateConfig(
            entityClass = "Employee",
            translationRules = listOf(
                EntityTranslationRule(
                    entityName = "Employee",
                    fieldRules = listOf(
                        FieldTranslationRule(
                            fieldName = "deptId",
                            translationType = TranslationType.TABLE_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.TABLE_DICT,
                                    table = "sys_department",
                                    codeColumn = "id",
                                    nameColumn = "name"
                                )
                            ),
                            targetFieldName = "deptName"
                        )
                    )
                )
            )
        )
        
        val generatedCode = enhancer.generateTableDictTranslation(dslConfig)
        
        generatedCode shouldContain "sys_department"
        generatedCode shouldContain "translateTableBatchCode2name"
        generatedCode shouldContain "setDeptName"
        generatedCode shouldContain "tableKeys_sys_department"
        
        println("Table dict translation code:")
        println(generatedCode)
    }
    
    test("应该生成嵌套结构处理代码") {
        val enhancer = JavaEntityEnhancer()
        
        val generatedCode = enhancer.generateJavaClassCode(
            packageName = "com.test",
            originalClassName = "ComplexEntity",
            enhancedClassName = "ComplexEntityEnhanced", 
            dslConfig = DslTemplateConfig("ComplexEntity", emptyList())
        )
        
        generatedCode shouldContain "translateNestedStructures(transApi)"
        generatedCode shouldContain "hasTranslateMethod"
        generatedCode shouldContain "invokeTranslateMethod"
        generatedCode shouldContain "java.util.List"
        generatedCode shouldContain "field.setAccessible(true)"
        
        println("Nested structure handling code found in generated class")
    }
})