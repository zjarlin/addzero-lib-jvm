package site.addzero.generated.forms

import androidx.compose.runtime.*
import site.addzero.component.drawer.AddDrawer
import site.addzero.component.form.number.AddMoneyField
import site.addzero.component.form.switch.AddSwitchField
import site.addzero.component.form.text.AddTextField
import site.addzero.component.high_level.AddMultiColumnContainer
import site.addzero.core.ext.parseObjectByKtx
import site.addzero.generated.forms.dataprovider.Iso2DataProvider
import site.addzero.generated.isomorphic.SysColumnConfigIso
import site.addzero.generated.isomorphic.SysTableConfigIso


/**
 * SysTableConfig 表单属性常量
 */
object SysTableConfigFormProps {
    const val routeKey = "routeKey"
    const val showPagination = "showPagination"
    const val showSearchBar = "showSearchBar"
    const val showBatchActions = "showBatchActions"
    const val showRowSelection = "showRowSelection"
    const val showDefaultRowActions = "showDefaultRowActions"
    const val enableSorting = "enableSorting"
    const val enableAdvancedSearch = "enableAdvancedSearch"
    const val headerHeightDp = "headerHeightDp"
    const val rowHeightDp = "rowHeightDp"
    const val columns = "columns"

    /**
     * 获取所有字段名列表（按默认顺序）
     */
    fun getAllFields(): List<String> {
        return listOf(
            routeKey,
            showPagination,
            showSearchBar,
            showBatchActions,
            showRowSelection,
            showDefaultRowActions,
            enableSorting,
            enableAdvancedSearch,
            headerHeightDp,
            rowHeightDp,
            columns
        )
    }
}

@Composable
fun SysTableConfigForm(
    state: MutableState<SysTableConfigIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: SysTableConfigFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        SysTableConfigFormOriginal(state, dslConfig)
    }
}

@Composable
fun SysTableConfigFormOriginal(
    state: MutableState<SysTableConfigIso>,
    dslConfig: SysTableConfigFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = SysTableConfigFormDsl(state, renderMap).apply(dslConfig)

    // 默认字段渲染映射（保持原有顺序）
    val function = @Composable
    fun() {
        var dataList by remember { mutableStateOf<List<SysColumnConfigIso>>(emptyList()) }

        LaunchedEffect(Unit) {
            try {
                val provider = Iso2DataProvider.isoToDataProvider[SysColumnConfigIso::class]
                dataList = provider?.invoke("") as? List<SysColumnConfigIso> ?: emptyList()
            } catch (e: Exception) {
                println("加载 columns 数据失败: ${e.message}")
                dataList = emptyList()
            }
        }
        with(dataList) {
            TODO("Not yet implemented")
        }
    }
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        SysTableConfigFormProps.routeKey to {
            AddTextField(
                value = state.value.routeKey?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(routeKey = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "路由键",
                isRequired = true
            )
        },
        SysTableConfigFormProps.showPagination to {
            AddSwitchField(
                value = state.value.showPagination ?: false,
                onValueChange = { state.value = state.value.copy(showPagination = it) },
                label = "显示分页控件"
            )
        },
        SysTableConfigFormProps.showSearchBar to {
            AddSwitchField(
                value = state.value.showSearchBar ?: false,
                onValueChange = { state.value = state.value.copy(showSearchBar = it) },
                label = "显示搜索栏"
            )
        },
        SysTableConfigFormProps.showBatchActions to {
            AddSwitchField(
                value = state.value.showBatchActions ?: false,
                onValueChange = { state.value = state.value.copy(showBatchActions = it) },
                label = "显示批量操作"
            )
        },
        SysTableConfigFormProps.showRowSelection to {
            AddSwitchField(
                value = state.value.showRowSelection ?: false,
                onValueChange = { state.value = state.value.copy(showRowSelection = it) },
                label = "显示每行的选择"
            )
        },
        SysTableConfigFormProps.showDefaultRowActions to {
            AddSwitchField(
                value = state.value.showDefaultRowActions ?: false,
                onValueChange = { state.value = state.value.copy(showDefaultRowActions = it) },
                label = "显示默认行操作"
            )
        },
        SysTableConfigFormProps.enableSorting to {
            AddSwitchField(
                value = state.value.enableSorting ?: false,
                onValueChange = { state.value = state.value.copy(enableSorting = it) },
                label = "对该字段启用排序"
            )
        },
        SysTableConfigFormProps.enableAdvancedSearch to {
            AddSwitchField(
                value = state.value.enableAdvancedSearch ?: false,
                onValueChange = { state.value = state.value.copy(enableAdvancedSearch = it) },
                label = "对该字段启用高级搜索"
            )
        },
        SysTableConfigFormProps.headerHeightDp to {
            AddMoneyField(
                value = state.value.headerHeightDp?.toString() ?: "",
                onValueChange = {
                    state.value =
                        state.value.copy(headerHeightDp = if (it.isNullOrEmpty()) 0f else it.parseObjectByKtx())
                },
                label = "表头高度",
                isRequired = true,
                currency = "CNY"
            )
        },
        SysTableConfigFormProps.rowHeightDp to {
            AddMoneyField(
                value = state.value.rowHeightDp?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(rowHeightDp = if (it.isNullOrEmpty()) 0f else it.parseObjectByKtx())
                },
                label = "行高",
                isRequired = true,
                currency = "CNY"
            )
        },
        SysTableConfigFormProps.columns to function
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


class SysTableConfigFormDsl(
    val state: MutableState<SysTableConfigIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    // 隐藏字段集合
    val hiddenFields = mutableSetOf<String>()

    // 字段显示顺序（如果为空则使用默认顺序）
    val fieldOrder = mutableListOf<String>()

    // 字段排序映射：字段名 -> 排序值
    private val fieldOrderMap = mutableMapOf<String, Int>()

    /**
     * 配置 routeKey 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun routeKey(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
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
     * 配置 showPagination 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showPagination(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showPagination")
                renderMap.remove("showPagination")
            }

            render != null -> {
                hiddenFields.remove("showPagination")
                renderMap["showPagination"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("showPagination")
                renderMap.remove("showPagination")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showPagination", orderValue)
        }
    }

    /**
     * 配置 showSearchBar 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showSearchBar(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showSearchBar")
                renderMap.remove("showSearchBar")
            }

            render != null -> {
                hiddenFields.remove("showSearchBar")
                renderMap["showSearchBar"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("showSearchBar")
                renderMap.remove("showSearchBar")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showSearchBar", orderValue)
        }
    }

    /**
     * 配置 showBatchActions 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showBatchActions(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showBatchActions")
                renderMap.remove("showBatchActions")
            }

            render != null -> {
                hiddenFields.remove("showBatchActions")
                renderMap["showBatchActions"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("showBatchActions")
                renderMap.remove("showBatchActions")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showBatchActions", orderValue)
        }
    }

    /**
     * 配置 showRowSelection 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showRowSelection(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showRowSelection")
                renderMap.remove("showRowSelection")
            }

            render != null -> {
                hiddenFields.remove("showRowSelection")
                renderMap["showRowSelection"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("showRowSelection")
                renderMap.remove("showRowSelection")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showRowSelection", orderValue)
        }
    }

    /**
     * 配置 showDefaultRowActions 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun showDefaultRowActions(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("showDefaultRowActions")
                renderMap.remove("showDefaultRowActions")
            }

            render != null -> {
                hiddenFields.remove("showDefaultRowActions")
                renderMap["showDefaultRowActions"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("showDefaultRowActions")
                renderMap.remove("showDefaultRowActions")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("showDefaultRowActions", orderValue)
        }
    }

    /**
     * 配置 enableSorting 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun enableSorting(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("enableSorting")
                renderMap.remove("enableSorting")
            }

            render != null -> {
                hiddenFields.remove("enableSorting")
                renderMap["enableSorting"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("enableSorting")
                renderMap.remove("enableSorting")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("enableSorting", orderValue)
        }
    }

    /**
     * 配置 enableAdvancedSearch 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun enableAdvancedSearch(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("enableAdvancedSearch")
                renderMap.remove("enableAdvancedSearch")
            }

            render != null -> {
                hiddenFields.remove("enableAdvancedSearch")
                renderMap["enableAdvancedSearch"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("enableAdvancedSearch")
                renderMap.remove("enableAdvancedSearch")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("enableAdvancedSearch", orderValue)
        }
    }

    /**
     * 配置 headerHeightDp 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun headerHeightDp(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("headerHeightDp")
                renderMap.remove("headerHeightDp")
            }

            render != null -> {
                hiddenFields.remove("headerHeightDp")
                renderMap["headerHeightDp"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("headerHeightDp")
                renderMap.remove("headerHeightDp")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("headerHeightDp", orderValue)
        }
    }

    /**
     * 配置 rowHeightDp 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun rowHeightDp(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("rowHeightDp")
                renderMap.remove("rowHeightDp")
            }

            render != null -> {
                hiddenFields.remove("rowHeightDp")
                renderMap["rowHeightDp"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("rowHeightDp")
                renderMap.remove("rowHeightDp")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("rowHeightDp", orderValue)
        }
    }

    /**
     * 配置 columns 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun columns(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<SysTableConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("columns")
                renderMap.remove("columns")
            }

            render != null -> {
                hiddenFields.remove("columns")
                renderMap["columns"] = { render(state) }
            }

            else -> {
                hiddenFields.remove("columns")
                renderMap.remove("columns")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("columns", orderValue)
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
            fieldOrder.addAll(SysTableConfigFormProps.getAllFields())
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
            fieldOrder.addAll(SysTableConfigFormProps.getAllFields())
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
        val allFields = SysTableConfigFormProps.getAllFields()
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
 * 记住 SysTableConfig 表单状态的便捷函数
 */
@Composable
fun rememberSysTableConfigFormState(current: SysTableConfigIso? = null): MutableState<SysTableConfigIso> {
    return remember(current) { mutableStateOf(current ?: SysTableConfigIso()) }
}
