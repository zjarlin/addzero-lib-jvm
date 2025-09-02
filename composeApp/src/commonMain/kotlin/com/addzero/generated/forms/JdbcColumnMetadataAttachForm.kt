package com.addzero.generated.forms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.addzero.component.drawer.AddDrawer
import com.addzero.component.form.switch.AddSwitchField
import com.addzero.component.high_level.AddMultiColumnContainer
import com.addzero.generated.isomorphic.JdbcColumnMetadataAttachIso


/**
 * JdbcColumnMetadataAttach 表单属性常量
 */
object JdbcColumnMetadataAttachFormProps {
    const val showInListFlag = "showInListFlag"
    const val showInFormFlag = "showInFormFlag"
    const val showInSearchFlag = "showInSearchFlag"

    /**
     * 获取所有字段名列表（按默认顺序）
     */
    fun getAllFields(): List<String> {
        return listOf(showInListFlag, showInFormFlag, showInSearchFlag)
    }
}

@Composable
fun JdbcColumnMetadataAttachForm(
    state: MutableState<JdbcColumnMetadataAttachIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: JdbcColumnMetadataAttachFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        JdbcColumnMetadataAttachFormOriginal(state, dslConfig)
    }
}

@Composable
fun JdbcColumnMetadataAttachFormOriginal(
    state: MutableState<JdbcColumnMetadataAttachIso>,
    dslConfig: JdbcColumnMetadataAttachFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = JdbcColumnMetadataAttachFormDsl(state, renderMap).apply(dslConfig)

    // 默认字段渲染映射（保持原有顺序）
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        JdbcColumnMetadataAttachFormProps.showInListFlag to {
            AddSwitchField(
                value = state.value.showInListFlag ?: false,
                onValueChange = { state.value = state.value.copy(showInListFlag = it) },
                label = "显示在列表"
            )
        },
        JdbcColumnMetadataAttachFormProps.showInFormFlag to {
            AddSwitchField(
                value = state.value.showInFormFlag ?: false,
                onValueChange = { state.value = state.value.copy(showInFormFlag = it) },
                label = "显示在表单"
            )
        },
        JdbcColumnMetadataAttachFormProps.showInSearchFlag to {
            AddSwitchField(
                value = state.value.showInSearchFlag ?: false,
                onValueChange = { state.value = state.value.copy(showInSearchFlag = it) },
                label = "显示在搜索"
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

    AddMultiColumnContainer(
        howMuchColumn = 2,
        items = finalItems
    )
}

class JdbcColumnMetadataAttachFormDsl(
    val state: MutableState<JdbcColumnMetadataAttachIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    // 隐藏字段集合
    val hiddenFields = mutableSetOf<String>()

    // 字段显示顺序（如果为空则使用默认顺序）
    val fieldOrder = mutableListOf<String>()

    // 字段排序映射：字段名 -> 排序值
    private val fieldOrderMap = mutableMapOf<String, Int>()

    /**
     * 配置 showInListFlag 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showInListFlag(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<JdbcColumnMetadataAttachIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showInListFlag")
                renderMap.remove("showInListFlag")
            }

            render != null -> {
                hiddenFields.remove("showInListFlag")
                renderMap["showInListFlag"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("showInListFlag")
                renderMap.remove("showInListFlag")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showInListFlag", orderValue)
        }
    }

    /**
     * 配置 showInFormFlag 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showInFormFlag(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<JdbcColumnMetadataAttachIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showInFormFlag")
                renderMap.remove("showInFormFlag")
            }

            render != null -> {
                hiddenFields.remove("showInFormFlag")
                renderMap["showInFormFlag"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("showInFormFlag")
                renderMap.remove("showInFormFlag")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showInFormFlag", orderValue)
        }
    }

    /**
     * 配置 showInSearchFlag 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showInSearchFlag(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<JdbcColumnMetadataAttachIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showInSearchFlag")
                renderMap.remove("showInSearchFlag")
            }

            render != null -> {
                hiddenFields.remove("showInSearchFlag")
                renderMap["showInSearchFlag"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("showInSearchFlag")
                renderMap.remove("showInSearchFlag")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showInSearchFlag", orderValue)
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
            fieldOrder.addAll(JdbcColumnMetadataAttachFormProps.getAllFields())
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
            fieldOrder.addAll(JdbcColumnMetadataAttachFormProps.getAllFields())
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
        val allFields = JdbcColumnMetadataAttachFormProps.getAllFields()
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
 * 记住 JdbcColumnMetadataAttach 表单状态的便捷函数
 */
@Composable
fun rememberJdbcColumnMetadataAttachFormState(current: JdbcColumnMetadataAttachIso? = null): MutableState<JdbcColumnMetadataAttachIso> {
    return remember(current) { mutableStateOf(current ?: JdbcColumnMetadataAttachIso()) }
}
