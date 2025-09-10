package com.addzero.generated.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.high_level.AddMultiColumnContainer
import com.addzero.component.drawer.AddDrawer
import com.addzero.component.form.*
import com.addzero.component.form.number.AddMoneyField
import com.addzero.component.form.number.AddNumberField
import com.addzero.component.form.number.AddIntegerField
import com.addzero.component.form.number.AddDecimalField
import com.addzero.component.form.number.AddPercentageField
import com.addzero.component.form.text.AddTextField
import com.addzero.component.form.text.AddPasswordField
import com.addzero.component.form.text.AddEmailField
import com.addzero.component.form.text.AddPhoneField
import com.addzero.component.form.text.AddUrlField
import com.addzero.component.form.text.AddUsernameField
import com.addzero.component.form.text.AddIdCardField
import com.addzero.component.form.text.AddBankCardField
import com.addzero.component.form.date.AddDateField
import com.addzero.component.form.date.DateType
import com.addzero.component.form.switch.AddSwitchField
import com.addzero.component.form.selector.AddGenericSingleSelector
import com.addzero.component.form.selector.AddGenericMultiSelector
import com.addzero.core.ext.parseObjectByKtx
import com.addzero.core.validation.RegexEnum
import com.addzero.generated.isomorphic.*
import com.addzero.generated.forms.dataprovider.Iso2DataProvider
            import com.addzero.generated.enums.*


/**
 * SysColumnConfig 表单属性常量
 */
object SysColumnConfigFormProps {
    const val columnKey = "columnKey"
    const val columnComment = "columnComment"
    const val kmpType = "kmpType"
    const val sortOrder = "sortOrder"
    const val showFilter = "showFilter"
    const val showSort = "showSort"
    const val routeKey = "routeKey"

    /**
     * 获取所有字段名列表（按默认顺序）
     */
    fun getAllFields(): List<String> {
        return listOf(columnKey, columnComment, kmpType, sortOrder, showFilter, showSort, routeKey)
    }
}

@Composable
fun SysColumnConfigForm(
    state: MutableState<SysColumnConfigIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: SysColumnConfigFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        SysColumnConfigFormOriginal(state, dslConfig)
    }
}

@Composable
fun SysColumnConfigFormOriginal(
    state: MutableState<SysColumnConfigIso>,
    dslConfig: SysColumnConfigFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = SysColumnConfigFormDsl(state, renderMap).apply(dslConfig)

    // 默认字段渲染映射（保持原有顺序）
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        SysColumnConfigFormProps.columnKey to {
            AddTextField(
                value = state.value.columnKey?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(columnKey = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "列唯一键",
                isRequired = true
            )
        },
        SysColumnConfigFormProps.columnComment to {
            AddTextField(
                value = state.value.columnComment?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(columnComment = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "列描述",
                isRequired = true
            )
        },
        SysColumnConfigFormProps.kmpType to {
            AddTextField(
                value = state.value.kmpType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(kmpType = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "kmp类型",
                isRequired = true
            )
        },
        SysColumnConfigFormProps.sortOrder to {
            AddIntegerField(
                value = state.value.sortOrder?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortOrder = if (it.isNullOrEmpty()) 0L else it.parseObjectByKtx())
                },
                label = "列排序",
                isRequired = true
            )
        },
        SysColumnConfigFormProps.showFilter to {
            AddSwitchField(
                value = state.value.showFilter ?: false,
                onValueChange = { state.value = state.value.copy(showFilter = it) },
                label = "对该列启用过滤"
            )
        },
        SysColumnConfigFormProps.showSort to {
            AddSwitchField(
                value = state.value.showSort ?: false,
                onValueChange = { state.value = state.value.copy(showSort = it) },
                label = "对该列启用排序"
            )
        },
        SysColumnConfigFormProps.routeKey to {
            AddTextField(
                value = state.value.routeKey?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(routeKey = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "路由键",
                isRequired = true
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

class SysColumnConfigFormDsl(
    val state: MutableState<SysColumnConfigIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    // 隐藏字段集合
    val hiddenFields = mutableSetOf<String>()

    // 字段显示顺序（如果为空则使用默认顺序）
    val fieldOrder = mutableListOf<String>()

    // 字段排序映射：字段名 -> 排序值
    private val fieldOrderMap = mutableMapOf<String, Int>()

    /**
     * 配置 columnKey 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun columnKey(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysColumnConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("columnKey")
                renderMap.remove("columnKey")
            }
            render != null -> {
                hiddenFields.remove("columnKey")
                renderMap["columnKey"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("columnKey")
                renderMap.remove("columnKey")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("columnKey", orderValue)
        }
    }

    /**
     * 配置 columnComment 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun columnComment(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysColumnConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("columnComment")
                renderMap.remove("columnComment")
            }
            render != null -> {
                hiddenFields.remove("columnComment")
                renderMap["columnComment"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("columnComment")
                renderMap.remove("columnComment")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("columnComment", orderValue)
        }
    }

    /**
     * 配置 kmpType 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun kmpType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysColumnConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("kmpType")
                renderMap.remove("kmpType")
            }
            render != null -> {
                hiddenFields.remove("kmpType")
                renderMap["kmpType"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("kmpType")
                renderMap.remove("kmpType")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("kmpType", orderValue)
        }
    }

    /**
     * 配置 sortOrder 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun sortOrder(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysColumnConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("sortOrder")
                renderMap.remove("sortOrder")
            }
            render != null -> {
                hiddenFields.remove("sortOrder")
                renderMap["sortOrder"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("sortOrder")
                renderMap.remove("sortOrder")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("sortOrder", orderValue)
        }
    }

    /**
     * 配置 showFilter 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showFilter(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysColumnConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showFilter")
                renderMap.remove("showFilter")
            }
            render != null -> {
                hiddenFields.remove("showFilter")
                renderMap["showFilter"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("showFilter")
                renderMap.remove("showFilter")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showFilter", orderValue)
        }
    }

    /**
     * 配置 showSort 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showSort(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysColumnConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showSort")
                renderMap.remove("showSort")
            }
            render != null -> {
                hiddenFields.remove("showSort")
                renderMap["showSort"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("showSort")
                renderMap.remove("showSort")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showSort", orderValue)
        }
    }

    /**
     * 配置 routeKey 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun routeKey(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysColumnConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("routeKey")
                renderMap.remove("routeKey")
            }
            render != null -> {
                hiddenFields.remove("routeKey")
                renderMap["routeKey"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("routeKey")
                renderMap.remove("routeKey")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("routeKey", orderValue)
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
            fieldOrder.addAll(SysColumnConfigFormProps.getAllFields())
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
            fieldOrder.addAll(SysColumnConfigFormProps.getAllFields())
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
        val allFields = SysColumnConfigFormProps.getAllFields()
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
 * 记住 SysColumnConfig 表单状态的便捷函数
 */
@Composable
fun rememberSysColumnConfigFormState(current: SysColumnConfigIso? = null): MutableState<SysColumnConfigIso> {
    return remember(current) { mutableStateOf(current ?: SysColumnConfigIso()) }
}