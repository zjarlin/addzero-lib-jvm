package site.addzero.aop.dicttrans.util_internal

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.util.ReflectUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.extra.spring.SpringUtil
import site.addzero.aop.dicttrans.inter.TPredicate
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import org.springframework.util.ClassUtils
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.util.*

/**
 * 反射工具类
 *
 * @author zjarlin
 * @since 2022/06/29
 */
@Suppress("unused")
internal object RefUtil {

    fun isNew(`object`: Any?): Boolean {
        `object` ?: return false
        val aClass: Class<*> = `object`.javaClass

        val fields = aClass.getDeclaredFields()
        return Arrays.stream<Field?>(fields).filter { field: Field? -> !Modifier.isStatic(field!!.getModifiers()) }.map<Any?> { field: Field? ->
            field!!.setAccessible(true)
            try {
                return@map field.get(`object`)
            } catch (e: IllegalAccessException) {
                return@map null
            }
        }.allMatch { value: Any? ->
            value == null || (value is String && StrUtil.isBlank(value)) || (value is MutableCollection<*> && CollUtil.isEmpty(value))
        }
    }


    fun isNonNullField(obj: Any?, field: Field?): Boolean {
        val fieldValue = ReflectUtil.getFieldValue(obj, field)
        return Objects.nonNull(fieldValue)
    }


    fun isObjectField(obj: Any, field: Field?): Boolean {
        val fieldValue = ReflectUtil.getFieldValue(obj, field) ?: return false
        return isT(fieldValue)
    }


    fun isCollectionField(field: Field): Boolean {
        val type = field.getType()
        val assignableFrom = MutableCollection::class.java.isAssignableFrom(type)
        return assignableFrom
    }

    fun isT(obj: Any): Boolean {

        // 扩展函数：检查类是否有指定注解


        if (Objects.isNull(obj)) {
            return false
        }


//        fun isDraftByClassName(obj: Any): Boolean {
//            val javaClass = obj.javaClass
//            val className = javaClass.name
//            // 检查类名是否以 "Draft" 结尾
//            val string = $$$$"Draft$$$Impl"
//            val endsWith = className.endsWith(string)
//            return endsWith
//        }

        // 使用扩展函数检查注解

//        val draftByClassName = isDraftByClassName(obj)
//
//        if (draftByClassName) {
//            return true
//        }

        val javaClass = obj.javaClass

        if (javaClass.isPrimitive || javaClass.isArray || javaClass.isEnum ||
            javaClass.isInterface || javaClass.isAnnotation || javaClass
                .isSynthetic
        ) {
            return false
        }


        try {
            val jsonObject: JSONObject? = JSON.parseObject(JSON.toJSONString(obj))
        } catch (e: Exception) {
            return false
        }
        val bean = SpringUtil.getBean(TPredicate::class.java)
        val tBlackList = bean.tBlackList()

        val map = tBlackList.all {
            val assignableFrom = it.isAssignableFrom(javaClass)
            !assignableFrom
        }

        val bool = map && !ClassUtils.isPrimitiveOrWrapper(javaClass) && !MutableCollection::class.java.isAssignableFrom(
            javaClass
        ) && !BigDecimal::class.java.isAssignableFrom(javaClass) && !Enum::class.java.isAssignableFrom(
            javaClass
        ) && !JSON::class.java.isAssignableFrom(javaClass)
        return bool
    }
}
