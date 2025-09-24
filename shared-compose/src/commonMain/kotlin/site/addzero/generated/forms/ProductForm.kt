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
 * Product 表单属性常量
 */
object ProductFormProps {
    const val name = "name"
    const val code = "code"
    const val productCategory = "productCategory"
    const val thingModel = "thingModel"
    const val devices = "devices"
    const val description = "description"
    const val accessMethod = "accessMethod"
    const val authMethod = "authMethod"
    const val enabled = "enabled"

    /**
     * 获取所有字段名列表（按默认顺序）
     */
    fun getAllFields(): List<String> {
        return listOf(name, code, productCategory, thingModel, devices, description, accessMethod, authMethod, enabled)
    }
}

@Composable
fun ProductForm(
    state: MutableState<ProductIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProductFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProductFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProductFormOriginal(
    state: MutableState<ProductIso>,
    dslConfig: ProductFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProductFormDsl(state, renderMap).apply(dslConfig)

    // 默认字段渲染映射（保持原有顺序）
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProductFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(name = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "产品名称",
                isRequired = true
            )
        },
        ProductFormProps.code to {
            AddTextField(
                value = state.value.code?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(code = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "产品编码",
                isRequired = true
            )
        },
        ProductFormProps.productCategory to {
            var dataList by remember { mutableStateOf<List<ProductCategoryIso>>(emptyList()) }

            LaunchedEffect(Unit) {
                try {
                    val provider = Iso2DataProvider.isoToDataProvider[ProductCategoryIso::class]
                    dataList = provider?.invoke("") as? List<ProductCategoryIso> ?: emptyList()
                } catch (e: Exception) {
                    println("加载 productCategory 数据失败: ${e.message}")
                    dataList = emptyList()
                }
            }

            AddGenericSingleSelector(
                value = state.value.productCategory,
                onValueChange = { state.value = state.value.copy(productCategory = it) },
                placeholder = "产品分类",
                dataProvider = { dataList },
                getId = { it.id ?: 0L },
                getLabel = { it.name ?: "" },
                
            )
        },
        ProductFormProps.thingModel to {
            var dataList by remember { mutableStateOf<List<ThingModelIso>>(emptyList()) }

            LaunchedEffect(Unit) {
                try {
                    val provider = Iso2DataProvider.isoToDataProvider[ThingModelIso::class]
                    dataList = provider?.invoke("") as? List<ThingModelIso> ?: emptyList()
                } catch (e: Exception) {
                    println("加载 thingModel 数据失败: ${e.message}")
                    dataList = emptyList()
                }
            }

            AddGenericSingleSelector(
                value = state.value.thingModel,
                onValueChange = { state.value = state.value.copy(thingModel = it) },
                placeholder = "关联的物模型",
                dataProvider = { dataList },
                getId = { it.id ?: 0L },
                getLabel = { it.name ?: "" },
                
            )
        },
        ProductFormProps.devices to {
            var dataList by remember { mutableStateOf<List<DeviceIso>>(emptyList()) }

            LaunchedEffect(Unit) {
                try {
                    val provider = Iso2DataProvider.isoToDataProvider[DeviceIso::class]
                    dataList = provider?.invoke("") as? List<DeviceIso> ?: emptyList()
                } catch (e: Exception) {
                    println("加载 devices 数据失败: ${e.message}")
                    dataList = emptyList()
                }
            }

            AddGenericMultiSelector(
                value = state.value.devices ?: emptyList(),
                onValueChange = { state.value = state.value.copy(devices = it) },
                placeholder = "关联的设备列表",
                dataProvider = { dataList },
                getId = { it.id ?: 0L },
                getLabel = { it.name ?: "" },
                
            )
        },
        ProductFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(description = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "产品描述",
                isRequired = false
            )
        },
        ProductFormProps.accessMethod to {
            AddTextField(
                value = state.value.accessMethod?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(accessMethod = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "设备接入方式，例如：MQTT",
                isRequired = true
            )
        },
        ProductFormProps.authMethod to {
            AddTextField(
                value = state.value.authMethod?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(authMethod = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "认证方式",
                isRequired = true
            )
        },
        ProductFormProps.enabled to {
            AddSwitchField(
                value = state.value.enabled ?: false,
                onValueChange = { state.value = state.value.copy(enabled = it) },
                label = "是否启用"
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

class ProductFormDsl(
    val state: MutableState<ProductIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    // 隐藏字段集合
    val hiddenFields = mutableSetOf<String>()

    // 字段显示顺序（如果为空则使用默认顺序）
    val fieldOrder = mutableListOf<String>()

    // 字段排序映射：字段名 -> 排序值
    private val fieldOrderMap = mutableMapOf<String, Int>()

    /**
     * 配置 name 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun name(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("name")
                renderMap.remove("name")
            }
            render != null -> {
                hiddenFields.remove("name")
                renderMap["name"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("name")
                renderMap.remove("name")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("name", orderValue)
        }
    }

    /**
     * 配置 code 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun code(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("code")
                renderMap.remove("code")
            }
            render != null -> {
                hiddenFields.remove("code")
                renderMap["code"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("code")
                renderMap.remove("code")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("code", orderValue)
        }
    }

    /**
     * 配置 productCategory 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun productCategory(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("productCategory")
                renderMap.remove("productCategory")
            }
            render != null -> {
                hiddenFields.remove("productCategory")
                renderMap["productCategory"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("productCategory")
                renderMap.remove("productCategory")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("productCategory", orderValue)
        }
    }

    /**
     * 配置 thingModel 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun thingModel(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("thingModel")
                renderMap.remove("thingModel")
            }
            render != null -> {
                hiddenFields.remove("thingModel")
                renderMap["thingModel"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("thingModel")
                renderMap.remove("thingModel")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("thingModel", orderValue)
        }
    }

    /**
     * 配置 devices 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun devices(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("devices")
                renderMap.remove("devices")
            }
            render != null -> {
                hiddenFields.remove("devices")
                renderMap["devices"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("devices")
                renderMap.remove("devices")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("devices", orderValue)
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
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
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
     * 配置 accessMethod 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun accessMethod(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("accessMethod")
                renderMap.remove("accessMethod")
            }
            render != null -> {
                hiddenFields.remove("accessMethod")
                renderMap["accessMethod"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("accessMethod")
                renderMap.remove("accessMethod")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("accessMethod", orderValue)
        }
    }

    /**
     * 配置 authMethod 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun authMethod(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("authMethod")
                renderMap.remove("authMethod")
            }
            render != null -> {
                hiddenFields.remove("authMethod")
                renderMap["authMethod"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("authMethod")
                renderMap.remove("authMethod")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("authMethod", orderValue)
        }
    }

    /**
     * 配置 enabled 字段
     * @param hidden 是否隐藏该字段
     * @param order 字段显示顺序（数值越小越靠前）
     * @param render 自定义渲染函数
     */
    fun enabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("enabled")
                renderMap.remove("enabled")
            }
            render != null -> {
                hiddenFields.remove("enabled")
                renderMap["enabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("enabled")
                renderMap.remove("enabled")
            }
        }

        // 处理排序
        order?.let { orderValue ->
            updateFieldOrder("enabled", orderValue)
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
            fieldOrder.addAll(ProductFormProps.getAllFields())
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
            fieldOrder.addAll(ProductFormProps.getAllFields())
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
        val allFields = ProductFormProps.getAllFields()
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
 * 记住 Product 表单状态的便捷函数
 */
@Composable
fun rememberProductFormState(current: ProductIso? = null): MutableState<ProductIso> {
    return remember(current) { mutableStateOf(current ?: ProductIso()) }
}