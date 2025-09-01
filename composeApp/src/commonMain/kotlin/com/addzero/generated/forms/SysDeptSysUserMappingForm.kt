package com.addzero.generated.forms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.addzero.core.ext.parseObjectByKtx
import com.addzero.generated.isomorphic.SysDeptSysUserMappingIso


/**
 * SysDeptSysUserMapping 表单属性常量
 */
object SysDeptSysUserMappingFormProps {
    const val sysDeptId = "sysDeptId"
    const val sysUserId = "sysUserId"

    /**
     * 获取所有字段名列表（按默认顺序）
     */
    fun getAllFields(): List<String> {
        return listOf(sysDeptId, sysUserId)
    }
}

@Composable
fun SysDeptSysUserMappingForm(
    state: MutableState<SysDeptSysUserMappingIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: SysDeptSysUserMappingFormDsl.() -> Unit = {}
) {
    com.addzero.component.drawer.AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        SysDeptSysUserMappingFormOriginal(state, dslConfig)
    }
}

@Composable
fun SysDeptSysUserMappingFormOriginal(
    state: MutableState<SysDeptSysUserMappingIso>,
    dslConfig: SysDeptSysUserMappingFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = SysDeptSysUserMappingFormDsl(state, renderMap).apply(dslConfig)

    // 默认字段渲染映射（保持原有顺序）
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        SysDeptSysUserMappingFormProps.sysDeptId to {
            com.addzero.component.form.number.AddIntegerField(
                value = state.value.sysDeptId?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sysDeptId = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "sysDeptId",
                isRequired = false
            )
        },
        SysDeptSysUserMappingFormProps.sysUserId to {
            com.addzero.component.form.number.AddIntegerField(
                value = state.value.sysUserId?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sysUserId = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "sysUserId",
                isRequired = false
            )
        }
    )

    // 根据 DSL 配置计算最终要渲染的字段列表（保持顺序）
    val finalItems = remember(renderMap, dsl.hiddenFields, dsl.fieldOrder) {
        // 1. 获取字段顺序
        val orderedFieldNames = if (dsl.fieldOrder.isNotEmpty()) {
            // 使用 DSL 中设置的顺序（可能通过 order() 方法或 order 参数设置）
            dsl.fieldOrder
        } else {
            // 使用默认顺序
            defaultRenderMap.keys.toList()
        }

        // 2. 按顺序构建最终的渲染列表
        orderedFieldNames
            .filter { fieldName -> fieldName !in dsl.hiddenFields } // 过滤隐藏字段
            .mapNotNull { fieldName ->
                // 优先使用自定义渲染，否则使用默认渲染
                when {
                    renderMap.containsKey(fieldName) -> renderMap[fieldName]
                    defaultRenderMap.containsKey(fieldName) -> defaultRenderMap[fieldName]
                    else -> null
                }
            }
    }

    com.addzero.component.high_level.AddMultiColumnContainer(
        howMuchColumn = 2,
        items = finalItems
    )
}

class SysDeptSysUserMappingFormDsl(
    val state: MutableState<SysDeptSysUserMappingIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    // 隐藏字段集合
    val hiddenFields = mutableSetOf<String>()

    // 字段显示顺序（如果为空则使用默认顺序）
    val fieldOrder = mutableListOf<String>()

    // 字段排序映射：字段名 -> 排序值
    private val fieldOrderMap = mutableMapOf<String, Int>()

    /**
     * 配置 sysDeptId 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun sysDeptId(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysDeptSysUserMappingIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("sysDeptId")
                renderMap.remove("sysDeptId")
            }

            render != null -> {
                hiddenFields.remove("sysDeptId")
                renderMap["sysDeptId"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("sysDeptId")
                renderMap.remove("sysDeptId")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("sysDeptId", orderValue)
        }
    }

    /**
     * 配置 sysUserId 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun sysUserId(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysDeptSysUserMappingIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("sysUserId")
                renderMap.remove("sysUserId")
            }

            render != null -> {
                hiddenFields.remove("sysUserId")
                renderMap["sysUserId"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("sysUserId")
                renderMap.remove("sysUserId")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("sysUserId", orderValue)
        }
    }

    /**
     * 隐藏指定字段
     */
    fun hide(vararg fields: String) {
        hiddenFields.addAll(fields)
    }

    /**
     * 设置字段显示顺序
     * @param fields 字段名列表，按显示顺序排列
     */
    fun order(vararg fields: String) {
        fieldOrder.clear()
        fieldOrder.addAll(fields)
    }

    /**
     * 在指定字段之前插入字段
     */
    fun insertBefore(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            // 如果没有自定义顺序，先初始化为默认顺序
            fieldOrder.addAll(SysDeptSysUserMappingFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    /**
     * 在指定字段之后插入字段
     */
    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            // 如果没有自定义顺序，先初始化为默认顺序
            fieldOrder.addAll(SysDeptSysUserMappingFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
    }

    /**
     * 更新字段排序
     * @param fieldName 字段名
     * @param orderValue 排序值（数值越小越靠前）
     */
    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue

        // 重新计算字段顺序
        val allFields = SysDeptSysUserMappingFormProps.getAllFields()
        val sortedFields = allFields.sortedWith { field1, field2 ->
            val order1 = fieldOrderMap[field1] ?: Int.MAX_VALUE
            val order2 = fieldOrderMap[field2] ?: Int.MAX_VALUE
            when {
                order1 != Int.MAX_VALUE && order2 != Int.MAX_VALUE -> order1.compareTo(order2)
                order1 != Int.MAX_VALUE -> -1 // field1 有排序值，排在前面
                order2 != Int.MAX_VALUE -> 1  // field2 有排序值，排在前面
                else -> allFields.indexOf(field1).compareTo(allFields.indexOf(field2)) // 都没有排序值，保持原有顺序
            }
        }

        fieldOrder.clear()
        fieldOrder.addAll(sortedFields)
    }
}

/**
 * 记住 SysDeptSysUserMapping 表单状态的便捷函数
 */
@Composable
fun rememberSysDeptSysUserMappingFormState(current: SysDeptSysUserMappingIso? = null): MutableState<SysDeptSysUserMappingIso> {
    return remember(current) { mutableStateOf(current ?: SysDeptSysUserMappingIso()) }
}
