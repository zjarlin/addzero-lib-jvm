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
 * SysDict 表单属性常量
 */
object SysDictFormProps {
    const val dictName = "dictName"
    const val dictCode = "dictCode"
    const val description = "description"
    const val sysDictItems = "sysDictItems"

    /**
     * 获取所有字段名列表（按默认顺序）
     */
    fun getAllFields(): List<String> {
        return listOf(dictName, dictCode, description, sysDictItems)
    }
}

@Composable
fun SysDictForm(
    state: MutableState<SysDictIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: SysDictFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        SysDictFormOriginal(state, dslConfig)
    }
}

@Composable
fun SysDictFormOriginal(
    state: MutableState<SysDictIso>,
    dslConfig: SysDictFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = SysDictFormDsl(state, renderMap).apply(dslConfig)

    // 默认字段渲染映射（保持原有顺序）
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        SysDictFormProps.dictName to {
            AddTextField(
                value = state.value.dictName?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(dictName = if (it.isNullOrBlank()) "" else it.parseObjectByKtx())
                },
                label = "字典名称",
                isRequired = true
            )
        },
        SysDictFormProps.dictCode to {
            AddTextField(
                value = state.value.dictCode?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(dictCode = if (it.isNullOrBlank()) "" else it.parseObjectByKtx())
                },
                label = "字典编码",
                isRequired = true
            )
        },
        SysDictFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(description = if (it.isNullOrBlank()) null else it.parseObjectByKtx())
                },
                label = "描述",
                isRequired = false
            )
        },
        SysDictFormProps.sysDictItems to {
            var dataList by remember { mutableStateOf<List<SysDictItemIso>>(emptyList()) }

            LaunchedEffect(Unit) {
                try {
                    val provider = Iso2DataProvider.isoToDataProvider[SysDictItemIso::class]
                    dataList = provider?.invoke("") as? List<SysDictItemIso> ?: emptyList()
                } catch (e: Exception) {
                    println("加载 sysDictItems 数据失败: ${e.message}")
                    dataList = emptyList()
                }
            }

            AddGenericMultiSelector(
                value = state.value.sysDictItems ?: emptyList(),
                onValueChange = { state.value = state.value.copy(sysDictItems = it) },
                placeholder = "sysDictItems",
                dataProvider = { dataList },
                getId = { it.id ?: 0L },
                getLabel = { it.itemText ?: "" },
                
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

class SysDictFormDsl(
    val state: MutableState<SysDictIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    // 隐藏字段集合
    val hiddenFields = mutableSetOf<String>()

    // 字段显示顺序（如果为空则使用默认顺序）
    val fieldOrder = mutableListOf<String>()

    // 字段排序映射：字段名 -> 排序值
    private val fieldOrderMap = mutableMapOf<String, Int>()

    /**
     * 配置 dictName 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun dictName(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysDictIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("dictName")
                renderMap.remove("dictName")
            }
            render != null -> {
                hiddenFields.remove("dictName")
                renderMap["dictName"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("dictName")
                renderMap.remove("dictName")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("dictName", orderValue)
        }
    }

    /**
     * 配置 dictCode 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun dictCode(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysDictIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("dictCode")
                renderMap.remove("dictCode")
            }
            render != null -> {
                hiddenFields.remove("dictCode")
                renderMap["dictCode"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("dictCode")
                renderMap.remove("dictCode")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("dictCode", orderValue)
        }
    }

    /**
     * 配置 description 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun description(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysDictIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("description")
                renderMap.remove("description")
            }
            render != null -> {
                hiddenFields.remove("description")
                renderMap["description"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("description")
                renderMap.remove("description")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("description", orderValue)
        }
    }

    /**
     * 配置 sysDictItems 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun sysDictItems(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysDictIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("sysDictItems")
                renderMap.remove("sysDictItems")
            }
            render != null -> {
                hiddenFields.remove("sysDictItems")
                renderMap["sysDictItems"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("sysDictItems")
                renderMap.remove("sysDictItems")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("sysDictItems", orderValue)
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
            fieldOrder.addAll(SysDictFormProps.getAllFields())
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
            fieldOrder.addAll(SysDictFormProps.getAllFields())
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
        val allFields = SysDictFormProps.getAllFields()
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
 * 记住 SysDict 表单状态的便捷函数
 */
@Composable
fun rememberSysDictFormState(current: SysDictIso? = null): MutableState<SysDictIso> {
    return remember(current) { mutableStateOf(current ?: SysDictIso()) }
}