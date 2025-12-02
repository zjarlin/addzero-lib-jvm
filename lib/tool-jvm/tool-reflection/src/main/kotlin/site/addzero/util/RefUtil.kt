@file:JvmName("RefUtils") @file:Suppress("unused")

package site.addzero.util

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.util.ClassUtil.isPrimitiveWrapper
import cn.hutool.core.util.ReflectUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.core.util.TypeUtil
import com.alibaba.fastjson2.JSON
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


/**
 * 反射工具类
 *
 * @author zjarlin
 * @since 2022/06/29
 */
object RefUtil {

    fun Any?.isNotNew(): Boolean {
        if (this == null) {
            return false
        }
        val new = isNew(this)
        return !new
    }


    /**
     * 从指定对象获取泛型类信息
     *
     * @param obj 目标对象
     * @param index 泛型参数索引（从0开始）
     * @return 泛型类的Class对象，如果获取失败返回null
     */
    fun getGenericClass(obj: Any, index: Int = 0): Class<*>? {
        return try {
            when (val typeArgument = TypeUtil.getTypeArgument(obj.javaClass, index)) {
                is ParameterizedType -> typeArgument.rawType as Class<*>
                is Class<*> -> typeArgument
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从指定对象获取泛型类信息（非空版本）
     *
     * @param obj 目标对象
     * @param index 泛型参数索引（从0开始）
     * @param defaultValue 默认值（当获取失败时返回）
     * @return 泛型类的Class对象
     */
    fun getGenericClassOrDefault(obj: Any, index: Int = 0, defaultValue: Class<*>): Class<*> {
        return getGenericClass(obj, index) ?: defaultValue
    }


    /**
     * 安全地获取对象的第一个泛型参数类型
     *
     * @return 泛型参数的Class对象，如果获取失败返回null
     */
    fun Any.getClassSafely(): Class<*>? = getGenericClass(this, 0)


    /**
     * 包含忽略秩序
     *
     * @param seq       seq
     * @param searchSeq 搜索seq
     * @return 返回状态true/false
     * @author zjarlin
     * @since 2022/06/18
     */
    fun containsIgnoreOrder(seq: CharSequence?, searchSeq: CharSequence?): Boolean {
        return StrUtil.contains(seq, searchSeq) || StrUtil.contains(searchSeq, seq)
    }

    /**
     * 判断下层接口返回的结果是否是new出来的对象
     *
     * @param object 下层接口返回的obj
     * @return 返回状态true/false
     * @author zjarlin
     * @since 2022/06/11
     */
    fun isNew(`object`: Any?): Boolean {
        `object` ?: return false
        val aClass: Class<*> = `object`.javaClass
        val fields = aClass.declaredFields
        return Arrays.stream<Field>(fields).filter { field: Field -> !Modifier.isStatic(field.modifiers) }
            .map<Any?> { field: Field ->
                field.isAccessible = true
                try {
                    return@map field[`object`]
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                    return@map null
                }
            }.allMatch { value: Any? ->
                value == null || (value is String && StrUtil.isBlank(value)) || (value is Collection<*> && CollUtil.isEmpty(
                    value
                ))
            }
    }

    fun isCollection(obj: Any): Boolean {
        val assignableFrom = MutableCollection::class.java.isAssignableFrom(obj.javaClass)
        return assignableFrom
    }

    fun isNonNullField(obj: Any?, field: Field?): Boolean {
        val fieldValue = ReflectUtil.getFieldValue(obj, field)
        return Objects.nonNull(fieldValue)
    }

    fun isCollectionField(field: Field): Boolean {
        val type = field.type
        val assignableFrom = MutableCollection::class.java.isAssignableFrom(type)
        return assignableFrom
    }

    fun isObjectField(obj: Any, field: Field?): Boolean {
        val fieldValue = ReflectUtil.getFieldValue(obj, field) ?: return false
        return isT(fieldValue)
    }

    /**
     * 判断对象是否为业务对象（需要递归处理的自定义对象）
     *
     * @param obj 要检查的对象
     * @param blacklistClasses 用户自定义的黑名单类（可选，vararg）
     * @return true 表示是需要递归处理的业务对象，false 表示不需要递归
     */
    fun isT(obj: Any, vararg blacklistClasses: Class<*>): Boolean {

        val clazz = obj.javaClass
        if (clazz == String::class.java) {
            return false
        }
        if (obj is GregorianCalendar) {
            return false

        }
        if (Calendar::class.java.isAssignableFrom(clazz)) {
            return false
        }
        // 快速排除：基本类型、字符串、数组、集合、枚举、注解、Class对象
        if (isPrimitiveWrapper(clazz)) {
            return false
        }
        if (clazz.isArray || clazz.isSynthetic || clazz.isAnonymousClass || clazz.isLocalClass ) {
            return false
        }
        if (listOf(
                Collection::class,
                Map::class,
                Enum::class,
                Annotation::class,
                Class::class,
                Date::class,
                Calendar::class,
                GregorianCalendar::class,
                TimeZone::class,
                Locale::class,
                LocalDate::class,
                LocalDateTime::class,
                LocalDateTime::class,
                BigDecimal::class,
                String::class,
                Array::class,
                Boolean::class,
                Byte::class,
                Short::class,
                Integer::class,
                Long::class,
                Float::class,
                Double::class,
                Boolean::class,
                Char::class
            ).any {
                it.java.isAssignableFrom(clazz)
            }
        ) {
            return false

        }
        // 检查用户自定义黑名单
        if (blacklistClasses.any { blacklistClass ->
                blacklistClass.isAssignableFrom(clazz)
            }) {
            return false
        }

        // 核心判断：有实例字段才是业务对象
        val hasInstanceFields = clazz.declaredFields.any { !Modifier.isStatic(it.modifiers) }
        if (!hasInstanceFields) {
            return false
        }

        // JSON序列化兜底验证（可选）
        val bool = try {
            // 尝试使用fastjson2序列化和反序列化
            val jsonString = JSON.toJSONString(obj)
            JSON.parseObject(jsonString, clazz)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // 序列化或反序列化失败，说明不是标准业务对象
            false
        }
        return bool

    }

}
