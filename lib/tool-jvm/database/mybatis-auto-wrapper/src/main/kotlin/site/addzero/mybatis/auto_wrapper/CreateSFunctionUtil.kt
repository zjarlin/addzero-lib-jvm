package site.addzero.mybatis.auto_wrapper

import cn.hutool.core.util.ReflectUtil
import cn.hutool.core.util.StrUtil
import com.baomidou.mybatisplus.core.toolkit.StringUtils
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import java.io.Serializable
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method


object CreateSFunctionUtil {
    private val lookup = MethodHandles.lookup()

    fun <T> createSFunction(clazz: Class<T>, method: Method): SFunction<T, *>? {
        try {
            val getMethodHandle = lookup.unreflect(method)
            //动态调用点
            val getCallSite = LambdaMetafactory.altMetafactory(
                lookup, "apply", MethodType.methodType(SFunction::class.java), MethodType.methodType(Any::class.java, Any::class.java), getMethodHandle, MethodType.methodType(Any::class.java, clazz), LambdaMetafactory.FLAG_SERIALIZABLE, Serializable::class.java
            )
            return getCallSite.getTarget().invokeExact() as SFunction<T, *>
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        System.err.println("SFunction 创建失败!")
        return null
    }


    fun <T> createSFunction(clazz: Class<T>, columnName: String): SFunction<T, *>? {
        val methodByColumnName = findMethodByColumnName(clazz, columnName)
        return createSFunction<T>(clazz, methodByColumnName)
    }


    fun <T> getUniqueFields(clazz: Class<T>): MutableList<Triple<String?, (T) -> Any?, (T, Any?) -> Any?>> {
        val toMutableList = ReflectUtil.getFields(clazz)
            .filter { it.isAnnotationPresent(Where::class.java) }
            .mapNotNull { field ->
                val fieldName = field.name
                val getterMethod = ReflectUtil.getMethod(clazz, "get" + fieldName.replaceFirstChar { it.uppercase() })
                val setterMethod = ReflectUtil.getMethod(clazz, "set" + fieldName.replaceFirstChar { it.uppercase() }, field.type)
                if (getterMethod != null && setterMethod != null) {
                    val getter = { entity: T -> getterMethod.invoke(entity) }
                    val setter = { entity: T, value: Any? -> setterMethod.invoke(entity, value) }
                    Triple(fieldName, getter, setter)
                } else {
                    null
                }
            }
            .toMutableList()
        return toMutableList
    }


    fun findMethodByColumnName(clazz: Class<*>, columnName: String): Method {
        var columnName = columnName
        if (!StringUtils.isCamel(columnName)) {
            columnName = StringUtils.underlineToCamel(columnName)
        }
        val methodName = StringUtils.concatCapitalize("get", columnName)
        val methodsDirectly = ReflectUtil.getMethodsDirectly(clazz, true, true)
        val method = methodsDirectly.firstOrNull {
            val b = StrUtil.containsIgnoreCase(it.name, methodName)
            b
        }
        if (method == null) {
            throw java.lang.RuntimeException(clazz.toString() + "的" + methodName + "方法没有找到:")
        }
        return method
    }
}
