package com.addzero.mybatis.auto_wrapper;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class AutoWhereUtil {

    @SuppressWarnings({"rawtypes"})
    private static final BiFunction columnProcessToSFunction = (clazz, column) -> {
        Method method = CreateSFunctionUtil.findMethodByColumnName((Class<?>) clazz, (String) column);
        return CreateSFunctionUtil.createSFunction((Class<?>) clazz, method);
    };

    @SuppressWarnings({"rawtypes"})
    private static final BiFunction columnProcessToString = (clazz, column) -> StringUtils.camelToUnderline((String) column);

    @SuppressWarnings({"rawtypes"})
    private static final BiFunction packFieldPostProcessEqField = (entityClass, dtoClazz) -> {
        final Map<String, Field> entityClassMap = getFieldsList((Class<?>) entityClass);
        final Map<String, Field> dtoClazzMap = getFieldsList((Class<?>) dtoClazz);

        dtoClazzMap.entrySet().removeIf(next -> !entityClassMap.containsKey(next.getKey()));
        return new HashSet<>(dtoClazzMap.values());
    };


    private static final BiFunction packFieldPostProcessEqFieldIgnoreId = (entityClass, dtoClazz) -> {
        final Map<String, Field> entityClassMap = getFieldsList((Class<?>) entityClass);
        final Map<String, Field> dtoClazzMap = getFieldsList((Class<?>) dtoClazz);

        dtoClazzMap.entrySet().removeIf(next -> {
            boolean b = !entityClassMap.containsKey(next.getKey());
            return b || StrUtil.equals(next.getKey(), "id");
        });
        return new HashSet<>(dtoClazzMap.values());
    };


    @SuppressWarnings({"rawtypes"})
    private static final BiFunction packFieldPostProcessEmpty = (entityClass, dtoClazz) -> new HashSet<>();

    private static Map<String, Field> getFieldsList(Class<?> clazz) {
        return getFieldsListTailRec(clazz, new HashMap<>());
    }

    //private static Map<String, Field> getFieldsListTailRec(Class<?> clazz, Map<String, Field> map) {
    //    if (clazz == null) return map;
    //    final Field[] declaredFields = clazz.getDeclaredFields();
    //    for (Field field : declaredFields) {
    //        map.putIfAbsent(field.getName(), field);
    //    }
    //    return getFieldsListTailRec(clazz.getSuperclass(), map);
    //}

    private static Map<String, Field> getFieldsListTailRec(Class<?> clazz, Map<String, Field> map) {
        if (clazz == null) return map;
        final Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                map.putIfAbsent(field.getName(), field);
            }
        }
        if (map.isEmpty()) throw new RuntimeException("没有找到合适的属性");
        return getFieldsListTailRec(clazz.getSuperclass(), map);
    }

    public static <T> LambdaQueryWrapper<T> lambdaQueryByAnnotation(Class<T> clazz, Object dto) {
        return action(Wrappers.lambdaQuery(), clazz, packFieldPostProcessEmpty, columnProcessToSFunction, dto);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T, W extends AbstractWrapper> W action(W wrapper, Class<T> clazz, BiFunction packFieldPostProcess, BiFunction columnProcess, Object dto) {
        if (ObjUtil.isNull(dto)) {
            return wrapper;
        }

        final GroupInfoBuilder<T, String> groupInfoBuilder = new GroupInfoBuilder<>(clazz, packFieldPostProcess, columnProcess);
        final List<WheresGroupInfo<T, String>> wheresGroupInfos = groupInfoBuilder.buildGroupInfo(dto);
        goTo(clazz, wrapper, wheresGroupInfos);
        return wrapper;
    }

    private static <T, R> void goTo(Class<T> clazz, AbstractWrapper<T, R, ?> wrapper, List<WheresGroupInfo<T, R>> wheresGroupInfos) {
        for (WheresGroupInfo<T, R> wheresGroupInfo : wheresGroupInfos) {
            wheresGroupInfo.process(clazz, wrapper);
        }
    }

    public static <T> LambdaQueryWrapper<T> lambdaQueryByField(Class<T> clazz, Object dto) {
        return action(Wrappers.lambdaQuery(), clazz, packFieldPostProcessEqField, columnProcessToSFunction, dto);
    }

    public static <T> LambdaQueryWrapper<T> lambdaQueryByField(Class<T> clazz, Object dto, boolean ignoreId) {
        return ignoreId ? action(Wrappers.lambdaQuery(), clazz, packFieldPostProcessEqFieldIgnoreId, columnProcessToSFunction, dto) :
                action(Wrappers.lambdaQuery(), clazz, packFieldPostProcessEqField, columnProcessToSFunction, dto);
    }


    public static <T> QueryWrapper<T> queryByAnnotation(Class<T> clazz, Object dto) {
        return action(Wrappers.query(), clazz, packFieldPostProcessEmpty, columnProcessToString, dto);
    }

    public static <T> QueryWrapper<T> queryByField(Class<T> clazz, Object dto) {
        return action(Wrappers.query(), clazz, packFieldPostProcessEqField, columnProcessToString, dto);
    }

}
