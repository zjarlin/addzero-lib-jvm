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
 * JdbcTableMetadataAttach 表单属性常量
 */
object JdbcTableMetadataAttachFormProps {
    const val jdbcTableMetadataId = "jdbcTableMetadataId"
    const val showactions = "showactions"
    const val rowheight = "rowheight"

    /**
     * 获取所有字段名列表（按默认顺序）
     */
    fun getAllFields(): List<String> {
        return listOf(jdbcTableMetadataId, showactions, rowheight)
    }
}

@Composable
fun JdbcTableMetadataAttachForm(
    state: MutableState<JdbcTableMetadataAttachIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: JdbcTableMetadataAttachFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        JdbcTableMetadataAttachFormOriginal(state, dslConfig)
    }
}

@Composable
fun JdbcTableMetadataAttachFormOriginal(
    state: MutableState<JdbcTableMetadataAttachIso>,
    dslConfig: JdbcTableMetadataAttachFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = JdbcTableMetadataAttachFormDsl(state, renderMap).apply(dslConfig)

    // 默认字段渲染映射（保持原有顺序）
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        JdbcTableMetadataAttachFormProps.jdbcTableMetadataId to {
            AddIntegerField(
                value = state.value.jdbcTableMetadataId?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(jdbcTableMetadataId = if (it.isNullOrBlank()) 0L else it.parseObjectByKtx())
                },
                label = "jdbcTableMetadataId",
                isRequired = true
            )
        },
        JdbcTableMetadataAttachFormProps.showactions to {
            AddSwitchField(
                value = state.value.showactions ?: false,
                onValueChange = { state.value = state.value.copy(showactions = it) },
                label = "showactions"
            )
        },
        JdbcTableMetadataAttachFormProps.rowheight to {
            AddIntegerField(
                value = state.value.rowheight?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(rowheight = if (it.isNullOrBlank()) 0L else it.parseObjectByKtx())
                },
                label = "行高",
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

class JdbcTableMetadataAttachFormDsl(
    val state: MutableState<JdbcTableMetadataAttachIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    // 隐藏字段集合
    val hiddenFields = mutableSetOf<String>()

    // 字段显示顺序（如果为空则使用默认顺序）
    val fieldOrder = mutableListOf<String>()

    // 字段排序映射：字段名 -> 排序值
    private val fieldOrderMap = mutableMapOf<String, Int>()

    /**
     * 配置 jdbcTableMetadataId 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun jdbcTableMetadataId(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<JdbcTableMetadataAttachIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("jdbcTableMetadataId")
                renderMap.remove("jdbcTableMetadataId")
            }
            render != null -> {
                hiddenFields.remove("jdbcTableMetadataId")
                renderMap["jdbcTableMetadataId"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("jdbcTableMetadataId")
                renderMap.remove("jdbcTableMetadataId")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("jdbcTableMetadataId", orderValue)
        }
    }

    /**
     * 配置 showactions 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showactions(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<JdbcTableMetadataAttachIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showactions")
                renderMap.remove("showactions")
            }
            render != null -> {
                hiddenFields.remove("showactions")
                renderMap["showactions"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("showactions")
                renderMap.remove("showactions")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showactions", orderValue)
        }
    }

    /**
     * 配置 rowheight 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun rowheight(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<JdbcTableMetadataAttachIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("rowheight")
                renderMap.remove("rowheight")
            }
            render != null -> {
                hiddenFields.remove("rowheight")
                renderMap["rowheight"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("rowheight")
                renderMap.remove("rowheight")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("rowheight", orderValue)
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
            fieldOrder.addAll(JdbcTableMetadataAttachFormProps.getAllFields())
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
            fieldOrder.addAll(JdbcTableMetadataAttachFormProps.getAllFields())
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
        val allFields = JdbcTableMetadataAttachFormProps.getAllFields()
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
 * 记住 JdbcTableMetadataAttach 表单状态的便捷函数
 */
@Composable
fun rememberJdbcTableMetadataAttachFormState(current: JdbcTableMetadataAttachIso? = null): MutableState<JdbcTableMetadataAttachIso> {
    return remember(current) { mutableStateOf(current ?: JdbcTableMetadataAttachIso()) }
}