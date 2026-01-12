package site.addzero.util.lsi_impl.impl.reflection.field

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import site.addzero.util.lsi_impl.impl.reflection.clazz.isNullable

/**
 * 测试字段可空性判断
 */
@DisplayName("字段可空性测试")
class FieldNullabilityTest {

    // 测试类：包含各种字段类型
    class TestClass {
        // 基本类型
        val primitiveInt: Int = 0
        val primitiveBoolean: Boolean = false

        // 引用类型（无注解）
        val stringField: String? = null
        val integerField: Int? = null
    }

    @Test
    @DisplayName("基本类型字段应该不可空")
    fun `primitive type fields should not be nullable`() {
        val intField = TestClass::class.java.getDeclaredField("primitiveInt")
        val booleanField = TestClass::class.java.getDeclaredField("primitiveBoolean")

        // Kotlin 的基本类型在 JVM 上是 Java 基本类型
        // 注意：Int 在 Kotlin 中可空时会变成 Integer
        assertFalse(intField.type.isPrimitive && intField.isNullable(),
            "Int field nullability: ${intField.isNullable()}, isPrimitive: ${intField.type.isPrimitive}")
        assertFalse(booleanField.type.isPrimitive && booleanField.isNullable(),
            "Boolean field nullability: ${booleanField.isNullable()}, isPrimitive: ${booleanField.type.isPrimitive}")
    }

    @Test
    @DisplayName("引用类型字段默认应该可空（保守策略）")
    fun `reference type fields should be nullable by default`() {
        val stringField = TestClass::class.java.getDeclaredField("stringField")

        assertTrue(stringField.isNullable(),
            "String field without annotations should be nullable (conservative)")
    }

    @Test
    @DisplayName("Class.isNullable() 应该只检查基本类型")
    fun `Class isNullable should only check primitive types`() {
        // 基本类型
        assertFalse(Int::class.javaPrimitiveType?.isNullable() ?: true, "int primitive should not be nullable")
        assertFalse(Boolean::class.javaPrimitiveType?.isNullable() ?: true, "boolean primitive should not be nullable")

        // 引用类型
        assertTrue(String::class.java.isNullable(), "String class should be nullable")
        assertTrue(Integer::class.java.isNullable(), "Integer class should be nullable")
    }
}
