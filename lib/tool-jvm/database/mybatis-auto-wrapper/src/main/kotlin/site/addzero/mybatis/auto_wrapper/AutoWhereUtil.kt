@file:JvmName("AutoWhereUtil")
package site.addzero.mybatis.auto_wrapper
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.ReflectUtil
import cn.hutool.core.util.StrUtil
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.StringUtils
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import java.lang.reflect.Field

@Suppress("UNCHECKED_CAST")
private val columnProcessToSFunction: (Class<*>, String) -> SFunction<*, *> = { clazz, column ->
    val method = CreateSFunctionUtil.findMethodByColumnName(clazz, column)
    CreateSFunctionUtil.createSFunction(clazz, method)!!
}
private val columnProcessToString: (Class<*>, String) -> String = { _, column -> StringUtils.camelToUnderline(column)!! }
private val packFieldPostProcessEqField: (Class<*>, Class<*>) -> MutableSet<Field> = { entityClass, dtoClazz ->
    val entityClassMap = getFieldsList(entityClass)
    val dtoClazzMap = getFieldsList(dtoClazz)
    dtoClazzMap.filterKeys { entityClassMap.containsKey(it) }.values.toMutableSet()
}
private val packFieldPostProcessEqFieldIgnoreId: (Class<*>, Class<*>) -> MutableSet<Field> = { entityClass, dtoClazz ->
    val entityClassMap = getFieldsList(entityClass)
    val dtoClazzMap = getFieldsList(dtoClazz)

    dtoClazzMap.filterKeys {
        entityClassMap.containsKey(it) && !StrUtil.equals(it, "id")
    }.values.toMutableSet()
}
private val packFieldPostProcessEmpty: (Class<*>, Class<*>) -> MutableSet<Field> = { _, _ -> mutableSetOf() }
private fun getFieldsList(clazz: Class<*>): Map<String, Field> {
    return generateSequence(clazz) { it.superclass }.flatMap { ReflectUtil.getFields(it).asSequence() }.associateBy { it.name }
}

fun <T> lambdaQueryByAnnotation(clazz: Class<T>, dto: Any): LambdaQueryWrapper<T> {
    @Suppress("UNCHECKED_CAST") val columnProcess = columnProcessToSFunction as (Class<T>, String) -> SFunction<T, *>
    return action(Wrappers.lambdaQuery<T>(), clazz, packFieldPostProcessEmpty, columnProcess, dto)
}

private fun <T, R, W : AbstractWrapper<T, R, *>> action(wrapper: W, clazz: Class<T>, packFieldPostProcess: (Class<*>, Class<*>) -> MutableSet<Field>, columnProcess: (Class<T>, String) -> R, dto: Any?): W {
    if (ObjUtil.isNull(dto)) {
        return wrapper
    }

    val groupInfoBuilder = GroupInfoBuilder(clazz, packFieldPostProcess, columnProcess)
    val wheresGroupInfos = groupInfoBuilder.buildGroupInfo(dto!!)
    goTo(clazz, wrapper as AbstractWrapper<T, R, *>, wheresGroupInfos)
    return wrapper
}

private fun <T, R> goTo(clazz: Class<T>, wrapper: AbstractWrapper<T, R, *>, wheresGroupInfos: List<WheresGroupInfo<T, R>>) {
    wheresGroupInfos.forEach { wheresGroupInfo ->
        val joinAndNested = wheresGroupInfo as JoinAndNested<T, R>
        joinAndNested.process(clazz, wrapper)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> lambdaQueryByField(clazz: Class<T>, dto: Any): LambdaQueryWrapper<T> {
    val columnProcess = columnProcessToSFunction as (Class<T>, String) -> SFunction<T, *>
    return action(Wrappers.lambdaQuery<T>(), clazz, packFieldPostProcessEqField, columnProcess, dto)!!
}

@Suppress("UNCHECKED_CAST")
fun <T> lambdaQueryByField(clazz: Class<T>, dto: Any, ignoreId: Boolean): LambdaQueryWrapper<T> {
    val columnProcess = columnProcessToSFunction as (Class<T>, String) -> SFunction<T, *>
    return (if (ignoreId) action(Wrappers.lambdaQuery<T>(), clazz, packFieldPostProcessEqFieldIgnoreId, columnProcess, dto) else action(Wrappers.lambdaQuery<T>(), clazz, packFieldPostProcessEqField, columnProcess, dto))!!
}


fun <T> queryByAnnotation(clazz: Class<T>, dto: Any): QueryWrapper<T> {
    return action(Wrappers.query<T>(), clazz, packFieldPostProcessEmpty, columnProcessToString, dto)!!
}

fun <T> queryByField(clazz: Class<T>, dto: Any): QueryWrapper<T> {
    return action(Wrappers.query<T>(), clazz, packFieldPostProcessEqField, columnProcessToString, dto)!!
}
