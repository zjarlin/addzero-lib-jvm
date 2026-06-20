package site.addzero.common.kt_util

import kotlin.reflect.KClass

val <T : Enum<T>> T.bitValue: Int
    get() = 1 shl ordinal

/**
 * 将枚举集合转换为二进制掩码
 */
fun <T : Enum<T>> Collection<T>.toBitmask(): Int {
    return fold(0) { mask, enumValue ->
        mask or enumValue.bitValue
    }
}

object EnumBitmaskUtils {

    /**
     * 将二进制掩码转换为枚举集合
     */
    inline fun <reified T : Enum<T>> Int.toEnumList(): List<T> {
        return enumValues<T>().filter { enumValue ->
            (this and enumValue.bitValue) != 0
        }
    }

    /**
     * 将二进制掩码转换为枚举集合
     */
    fun <T : Enum<T>> Int.toEnumList(clazz: KClass<T>): List<T> {
        return toEnumList(clazz.java)
    }


    /**
     * 将二进制掩码转换为枚举集合
     */
    fun <T : Enum<T>> Int.toEnumList(clazz: Class<T>): List<T> {
        return clazz.enumConstants.filter { enumValue ->
            (this and enumValue.bitValue) != 0
        }
    }
}
