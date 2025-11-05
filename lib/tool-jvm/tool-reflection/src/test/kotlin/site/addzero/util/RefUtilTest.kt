package site.addzero.util

import cn.hutool.core.util.ClassUtil.isPrimitiveWrapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.lang.Boolean
import java.lang.Byte
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Short
import java.util.Date

class RefUtilTest {

    @Test
    fun testIsPrimitiveOrWrapper() {
        // 测试基本类型
//        val condition = RefUtil.isPrimitiveOrWrapper(java.lang.Long)
//        assertTrue(condition)

        // 测试包装类
        assertTrue(
            isPrimitiveWrapper(Long::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
        assertTrue(
            isPrimitiveWrapper(Integer::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
        assertTrue(
            isPrimitiveWrapper(Boolean::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
        assertTrue(
            isPrimitiveWrapper(Double::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
        assertTrue(
            isPrimitiveWrapper(Float::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
        assertTrue(
            isPrimitiveWrapper(Byte::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
        assertTrue(
            isPrimitiveWrapper(Short::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
        assertTrue(
            isPrimitiveWrapper(Character::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )

        // 测试非基本类型
        assertFalse(
            isPrimitiveWrapper(String::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
        assertFalse(
            isPrimitiveWrapper(RefUtilTest::class.java)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
        )
    }

    @Test
    fun testIsT() {
        // 测试基本类型值应该返回 false
        assertFalse(RefUtil.isT(1L))
        assertFalse(RefUtil.isT(1))
        assertFalse(RefUtil.isT(true))
        assertFalse(RefUtil.isT(1.0))
        assertFalse(RefUtil.isT('a'))
        assertFalse(RefUtil.isT("test"))
        assertFalse(RefUtil.isT(Date()))

    }
}
