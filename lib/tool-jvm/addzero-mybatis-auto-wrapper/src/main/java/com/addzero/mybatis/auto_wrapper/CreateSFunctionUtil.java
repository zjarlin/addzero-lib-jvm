package com.addzero.mybatis.auto_wrapper;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.io.Serializable;
import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class CreateSFunctionUtil {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    @SuppressWarnings("unchecked")
    public static <T> SFunction<T, ?> createSFunction(Class<T> clazz, Method method) {

        try {
            final MethodHandle getMethodHandle = lookup.unreflect(method);
            //动态调用点
            final CallSite getCallSite = LambdaMetafactory.altMetafactory(
                    lookup
                    , "apply"
                    , MethodType.methodType(SFunction.class)
                    , MethodType.methodType(Object.class, Object.class)
                    , getMethodHandle
                    , MethodType.methodType(Object.class, clazz)
                    , LambdaMetafactory.FLAG_SERIALIZABLE
                    , Serializable.class
            );
            return (SFunction<T, ?>) getCallSite.getTarget().invokeExact();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        System.err.println("SFunction 创建失败!");
        return null;
    }


    @SuppressWarnings("unchecked")
    public static <T> SFunction<T, ?> createSFunction(Class<T> clazz, String columnName) {
        Method methodByColumnName = findMethodByColumnName(clazz, columnName);
        return createSFunction(clazz, methodByColumnName);
    }


    public static <T> List<Triple<String, Function<T, ?>, BiConsumer<T, ?>>> getUniqueFields(Class<T> clazz) {
        List<Triple<String, Function<T, ?>, BiConsumer<T, ?>>> uniqueFields = new ArrayList<>();


        for (Field field : FieldUtils.getAllFields(clazz)) {
            if (field.isAnnotationPresent(Where.class)) {
                try {
                    String fieldName = field.getName();
                    // 获取 getter 方法
                    Method getterMethod = ReflectUtil.getMethod(clazz, "get" + capitalize(fieldName));
                    // 获取 setter 方法
                    Method setterMethod = ReflectUtil.getMethod(clazz, "set" + capitalize(fieldName),field.getType());

                    if (getterMethod != null && setterMethod != null) {
                        Function<T, ?> getter = entity -> {
                            try {
                                return getterMethod.invoke(entity);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to invoke getter for field: " + fieldName, e);
                            }
                        };
                        BiConsumer<T, Object> setter = (entity, value) -> {
                            try {
                                setterMethod.invoke(entity, value);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to invoke setter for field: " + fieldName, e);
                            }
                        };

                        uniqueFields.add(Triple.of(fieldName, getter, setter));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create getter/setter function for field: " + field.getName(), e);
                }
            }
        }

        return uniqueFields;
    }


    public static Method findMethodByColumnName(Class<?> clazz, String columnName) {
        if (!StringUtils.isCamel(columnName)) {
            columnName = StringUtils.underlineToCamel(columnName);
        }
        final String methodName = StringUtils.concatCapitalize("get", columnName);
        Method method = ReflectUtil.getMethod(clazz, methodName);
        if (method == null) {
            throw new RuntimeException(clazz + "的" + methodName + "方法没有找到:");
        }
        return method;
    }
}
