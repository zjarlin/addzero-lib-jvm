package site.addzero.generated.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.high_level.AddMultiColumnContainer
import site.addzero.component.drawer.AddDrawer
import site.addzero.component.form.*
import site.addzero.component.form.number.AddMoneyField
import site.addzero.component.form.number.AddNumberField
import site.addzero.component.form.number.AddIntegerField
import site.addzero.component.form.number.AddDecimalField
import site.addzero.component.form.number.AddPercentageField
import site.addzero.component.form.text.AddTextField
import site.addzero.component.form.text.AddPasswordField
import site.addzero.component.form.text.AddEmailField
import site.addzero.component.form.text.AddPhoneField
import site.addzero.component.form.text.AddUrlField
import site.addzero.component.form.text.AddUsernameField
import site.addzero.component.form.text.AddIdCardField
import site.addzero.component.form.text.AddBankCardField
import site.addzero.component.form.date.AddDateField
import site.addzero.component.form.date.DateType
import site.addzero.component.form.switch.AddSwitchField
import site.addzero.component.form.selector.AddGenericSingleSelector
import site.addzero.component.form.selector.AddGenericMultiSelector
import site.addzero.core.ext.parseObjectByKtx
import site.addzero.core.validation.RegexEnum
import site.addzero.generated.isomorphic.*
import site.addzero.generated.forms.dataprovider.Iso2DataProvider
            import site.addzero.generated.enums.*


/**
 * SysMenu 表单属性常量
 */
object SysMenuFormProps {
    const val parentId = "parentId"
    const val title = "title"
    const val routePath = "routePath"
    const val icon = "icon"
    const val order = "order"

    /**
     * 获取所有字段名列表（按默认顺序）
     */
    fun getAllFields(): List<String> {
        return listOf(parentId, title, routePath, icon, order)
    }
}

@Composable
fun SysMenuForm(
    state: MutableState<SysMenuIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: SysMenuFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        SysMenuFormOriginal(state, dslConfig)
    }
}

@Composable
fun SysMenuFormOriginal(
    state: MutableState<SysMenuIso>,
    dslConfig: SysMenuFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = SysMenuFormDsl(state, renderMap).apply(dslConfig)

    // 默认字段渲染映射（保持原有顺序）
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        SysMenuFormProps.parentId to {
            AddIntegerField(
                value = state.value.parentId?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(parentId = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "父节点ID",
                isRequired = false
            )
        },
        SysMenuFormProps.title to {
            AddTextField(
                value = state.value.title?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(title = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "路由标题",
                isRequired = false
            )
        },
        SysMenuFormProps.routePath to {
            AddTextField(
                value = state.value.routePath?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(routePath = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "路由地址",
                isRequired = false
            )
        },
        SysMenuFormProps.icon to {
            AddTextField(
                value = state.value.icon?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(icon = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "图标",
                isRequired = false
            )
        },
        SysMenuFormProps.order to {
            AddMoneyField(
                value = state.value.order?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(order = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "排序",
                isRequired = false,
                currency = "CNY"
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

class SysMenuFormDsl(
    val state: MutableState<SysMenuIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    // 隐藏字段集合
    val hiddenFields = mutableSetOf<String>()

    // 字段显示顺序（如果为空则使用默认顺序）
    val fieldOrder = mutableListOf<String>()

    // 字段排序映射：字段名 -> 排序值
    private val fieldOrderMap = mutableMapOf<String, Int>()

    /**
     * 配置 parentId 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun parentId(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysMenuIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("parentId")
                renderMap.remove("parentId")
            }
            render != null -> {
                hiddenFields.remove("parentId")
                renderMap["parentId"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("parentId")
                renderMap.remove("parentId")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("parentId", orderValue)
        }
    }

    /**
     * 配置 title 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun title(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysMenuIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("title")
                renderMap.remove("title")
            }
            render != null -> {
                hiddenFields.remove("title")
                renderMap["title"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("title")
                renderMap.remove("title")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("title", orderValue)
        }
    }

    /**
     * 配置 routePath 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun routePath(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysMenuIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("routePath")
                renderMap.remove("routePath")
            }
            render != null -> {
                hiddenFields.remove("routePath")
                renderMap["routePath"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("routePath")
                renderMap.remove("routePath")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("routePath", orderValue)
        }
    }

    /**
     * 配置 icon 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun icon(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysMenuIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("icon")
                renderMap.remove("icon")
            }
            render != null -> {
                hiddenFields.remove("icon")
                renderMap["icon"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("icon")
                renderMap.remove("icon")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("icon", orderValue)
        }
    }

    /**
     * 配置 order 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun order(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysMenuIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("order")
                renderMap.remove("order")
            }
            render != null -> {
                hiddenFields.remove("order")
                renderMap["order"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("order")
                renderMap.remove("order")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("order", orderValue)
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
            fieldOrder.addAll(SysMenuFormProps.getAllFields())
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
            fieldOrder.addAll(SysMenuFormProps.getAllFields())
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
        val allFields = SysMenuFormProps.getAllFields()
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
 * 记住 SysMenu 表单状态的便捷函数
 */
@Composable
fun rememberSysMenuFormState(current: SysMenuIso? = null): MutableState<SysMenuIso> {
    return remember(current) { mutableStateOf(current ?: SysMenuIso()) }
}