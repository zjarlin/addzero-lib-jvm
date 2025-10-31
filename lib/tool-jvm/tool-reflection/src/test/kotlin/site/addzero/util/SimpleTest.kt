package site.addzero.util

import cn.hutool.core.util.ClassUtil.isPrimitiveWrapper

fun main() {
    val lng = 1L
    println("Testing with class: ${lng.javaClass.name}")
    println("Class object: ${lng.javaClass}")
    println("Long::class.java: ${Long::class.java}")
    println("java.lang.Long::class.java: ${java.lang.Long::class.java}")
    println("Are they equal? lng.javaClass == Long::class.java: ${lng.javaClass == Long::class.java}")
    println("Are they equal? lng.javaClass == java.lang.Long::class.java: ${lng.javaClass == java.lang.Long::class.java}")

    val result = isPrimitiveWrapper(lng.javaClass)//        val primitive = aClass.isPrimitive
//        return primitive || Byte::class.java.isAssignableFrom(aClass) || Short::class.java.isAssignableFrom(aClass) || Int::class.java.isAssignableFrom(
//            aClass
//        ) || Long::class.java.isAssignableFrom(aClass) || Float::class.java.isAssignableFrom(aClass) || Double::class.java.isAssignableFrom(
//            aClass
//        ) || Boolean::class.java.isAssignableFrom(aClass) || Char::class.java.isAssignableFrom(aClass)
    println("Result: $result")
}
