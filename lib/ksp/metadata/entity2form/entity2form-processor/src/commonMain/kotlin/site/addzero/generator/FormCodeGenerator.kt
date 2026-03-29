package site.addzero.generator

import androidx.room.compiler.processing.XAnnotated
import androidx.room.compiler.processing.XFieldElement
import androidx.room.compiler.processing.XMethodElement
import androidx.room.compiler.processing.XNullability
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.XTypeElement
import com.google.devtools.ksp.processing.KSPLogger
import java.io.File
import site.addzero.context.SettingContext

/**
 * 表单代码生成器（XProcessing 版本）
 */
class FormCodeGenerator(
    private val logger: KSPLogger
) {
    fun generateFormCodeWithStrategy(
        entity: XTypeElement,
        packageName: String = "site.addzero.forms"
    ): String {
        val entityClassName = entity.name
        val isoClassName = "${entityClassName}Iso"
        val properties = collectProperties(entity)
        val settings = SettingContext.settings
        val apiProviderImport = "${settings.iso2DataProviderPackage}.Iso2DataProvider"

        val formFields = properties.joinToString(",\n") { property ->
            generateFieldCode(entityClassName, property)
        }
        val formProps = generateFormPropsWithStrategy(entityClassName, properties)

        return """
            |package $packageName
            |
            |import androidx.compose.foundation.layout.*
            |import androidx.compose.material3.*
            |import androidx.compose.runtime.*
            |import androidx.compose.ui.Modifier
            |import androidx.compose.ui.unit.dp
            |import site.addzero.component.high_level.AddMultiColumnContainer
            |import site.addzero.component.drawer.AddDrawer
            |import site.addzero.component.form.*
            |import site.addzero.component.form.number.AddMoneyField
            |import site.addzero.component.form.number.AddNumberField
            |import site.addzero.component.form.number.AddIntegerField
            |import site.addzero.component.form.number.AddDecimalField
            |import site.addzero.component.form.number.AddPercentageField
            |import site.addzero.component.form.text.AddTextField
            |import site.addzero.component.form.text.AddPasswordField
            |import site.addzero.component.form.text.AddEmailField
            |import site.addzero.component.form.text.AddPhoneField
            |import site.addzero.component.form.text.AddUrlField
            |import site.addzero.component.form.text.AddUsernameField
            |import site.addzero.component.form.text.AddIdCardField
            |import site.addzero.component.form.text.AddBankCardField
            |import site.addzero.component.form.date.AddDateField
            |import site.addzero.component.form.date.DateType
            |import site.addzero.component.form.switch.AddSwitchField
            |import site.addzero.component.form.selector.AddGenericSingleSelector
            |import site.addzero.component.form.selector.AddGenericMultiSelector
            |import site.addzero.core.ext.parseObjectByKtx
            |import site.addzero.core.validation.RegexEnum
            |import ${settings.isomorphicPackageName}.*
            |import $apiProviderImport
            |import ${settings.enumOutputPackage}.*
            |
            |$formProps
            |
            |@Composable
            |fun ${entityClassName}Form(
            |    state: MutableState<${isoClassName}>,
            |    visible: Boolean,
            |    title: String,
            |    onClose: () -> Unit,
            |    onSubmit: () -> Unit,
            |    confirmEnabled: Boolean = true,
            |    dslConfig: ${entityClassName}FormDsl.() -> Unit = {}
            |) {
            |    AddDrawer(
            |        visible = visible,
            |        title = title,
            |        onClose = onClose,
            |        onSubmit = onSubmit,
            |        confirmEnabled = confirmEnabled,
            |    ) {
            |        ${entityClassName}FormOriginal(state, dslConfig)
            |    }
            |}
            |
            |@Composable
            |fun ${entityClassName}FormOriginal(
            |    state: MutableState<${isoClassName}>,
            |    dslConfig: ${entityClassName}FormDsl.() -> Unit = {}
            |) {
            |    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
            |    val dsl = ${entityClassName}FormDsl(state, renderMap).apply(dslConfig)
            |    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
            |$formFields
            |    )
            |
            |    val finalItems = remember(renderMap, dsl.hiddenFields, dsl.fieldOrder) {
            |        val orderedFieldNames = if (dsl.fieldOrder.isNotEmpty()) {
            |            dsl.fieldOrder
            |        } else {
            |            defaultRenderMap.keys.toList()
            |        }
            |
            |        orderedFieldNames
            |            .filter { fieldName -> fieldName !in dsl.hiddenFields }
            |            .mapNotNull { fieldName ->
            |                when {
            |                    renderMap.containsKey(fieldName) -> renderMap[fieldName]
            |                    defaultRenderMap.containsKey(fieldName) -> defaultRenderMap[fieldName]
            |                    else -> null
            |                }
            |            }
            |    }
            |
            |    AddMultiColumnContainer(
            |        howMuchColumn = 2,
            |        items = finalItems
            |    )
            |}
            |
            |class ${entityClassName}FormDsl(
            |    val state: MutableState<${isoClassName}>,
            |    private val renderMap: MutableMap<String, @Composable () -> Unit>
            |) {
            |    val hiddenFields = mutableSetOf<String>()
            |    val fieldOrder = mutableListOf<String>()
            |    private val fieldOrderMap = mutableMapOf<String, Int>()
            |
            |${generateDslMethodsWithStrategy(entityClassName, properties)}
            |
            |    fun hide(vararg fields: String) {
            |        hiddenFields.addAll(fields)
            |    }
            |
            |    fun order(vararg fields: String) {
            |        fieldOrder.clear()
            |        fieldOrder.addAll(fields)
            |    }
            |
            |    fun insertBefore(targetField: String, vararg newFields: String) {
            |        if (fieldOrder.isEmpty()) {
            |            fieldOrder.addAll(${entityClassName}FormProps.getAllFields())
            |        }
            |        val index = fieldOrder.indexOf(targetField)
            |        if (index >= 0) {
            |            fieldOrder.addAll(index, newFields.toList())
            |        }
            |    }
            |
            |    fun insertAfter(targetField: String, vararg newFields: String) {
            |        if (fieldOrder.isEmpty()) {
            |            fieldOrder.addAll(${entityClassName}FormProps.getAllFields())
            |        }
            |        val index = fieldOrder.indexOf(targetField)
            |        if (index >= 0) {
            |            fieldOrder.addAll(index + 1, newFields.toList())
            |        }
            |    }
            |
            |    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
            |        fieldOrderMap[fieldName] = orderValue
            |        val allFields = ${entityClassName}FormProps.getAllFields()
            |        val sortedFields = allFields.sortedWith { field1, field2 ->
            |            val order1 = fieldOrderMap[field1] ?: Int.MAX_VALUE
            |            val order2 = fieldOrderMap[field2] ?: Int.MAX_VALUE
            |            when {
            |                order1 != Int.MAX_VALUE && order2 != Int.MAX_VALUE -> order1.compareTo(order2)
            |                order1 != Int.MAX_VALUE -> -1
            |                order2 != Int.MAX_VALUE -> 1
            |                else -> allFields.indexOf(field1).compareTo(allFields.indexOf(field2))
            |            }
            |        }
            |
            |        fieldOrder.clear()
            |        fieldOrder.addAll(sortedFields)
            |    }
            |}
            |
            |@Composable
            |fun remember${entityClassName}FormState(current: ${isoClassName}? = null): MutableState<${isoClassName}> {
            |    return remember(current) { mutableStateOf(current ?: ${isoClassName}()) }
            |}
        """.trimMargin()
    }

    fun writeFormFileWithStrategy(entity: XTypeElement, outputDir: String, packageName: String) {
        val formCode = generateFormCodeWithStrategy(entity, packageName)
        val fileName = "${entity.name}Form.kt"
        val file = File("$outputDir/$fileName")
        file.parentFile?.mkdirs()
        file.writeText(formCode)
        logger.info("生成表单文件（XProcessing）: ${file.absolutePath}")
    }

    private fun collectProperties(entity: XTypeElement): List<PropertyModel> {
        val baseEntityFields = setOf(
            "id", "createTime", "updateTime", "createBy", "updateBy",
            "deleted", "version", "tenantId"
        )
        val fieldByName = entity.getAllFieldsIncludingPrivateSupers().associateBy { it.name }
        val byName = linkedMapOf<String, PropertyModel>()

        entity.getAllMethods()
            .asSequence()
            .filter { it.isKotlinPropertyGetter() }
            .filter { it.parameters.isEmpty() }
            .filter { !it.isStatic() }
            .forEach { method ->
                val propertyName = method.propertyName ?: normalizeMethodPropertyName(method)
                if (propertyName.isBlank() || propertyName in baseEntityFields) {
                    return@forEach
                }
                val relatedField = fieldByName[propertyName]
                if (hasFormIgnore(method) || (relatedField != null && hasFormIgnore(relatedField))) {
                    return@forEach
                }
                byName[propertyName] = createPropertyModel(
                    name = propertyName,
                    type = method.returnType,
                    nullable = method.returnType.nullability == XNullability.NULLABLE,
                    comment = method.docComment
                )
            }

        if (byName.isNotEmpty()) {
            return byName.values.toList()
        }

        fieldByName.values.forEach { field ->
            val name = field.name
            if (name in baseEntityFields || hasFormIgnore(field)) {
                return@forEach
            }
            byName[name] = createPropertyModel(
                name = name,
                type = field.type,
                nullable = field.type.nullability == XNullability.NULLABLE,
                comment = field.docComment
            )
        }

        return byName.values.toList()
    }

    private fun createPropertyModel(
        name: String,
        type: XType,
        nullable: Boolean,
        comment: String?
    ): PropertyModel {
        val label = comment?.lineSequence()?.firstOrNull()?.trim()?.takeIf { it.isNotBlank() } ?: name
        return PropertyModel(
            name = name,
            label = label,
            nullable = nullable,
            kind = classifyFieldKind(type),
            defaultValue = defaultValueFor(type, nullable)
        )
    }

    private fun hasFormIgnore(target: XAnnotated): Boolean {
        return target.getAllAnnotations().any { annotation ->
            annotation.qualifiedName == "site.addzero.entity2form.annotation.FormIgnore" ||
                annotation.name == "FormIgnore"
        }
    }

    private fun normalizeMethodPropertyName(method: XMethodElement): String {
        val methodName = method.name
        if (methodName.startsWith("get") && methodName.length > 3) {
            return methodName.substring(3).replaceFirstChar { it.lowercase() }
        }
        if (methodName.startsWith("is") && methodName.length > 2) {
            return methodName.substring(2).replaceFirstChar { it.lowercase() }
        }
        return methodName
    }

    private fun generateFieldCode(entityClassName: String, property: PropertyModel): String {
        val name = property.name
        val label = quote(property.label)
        val isRequired = !property.nullable

        return when (property.kind) {
            FieldKind.BOOLEAN -> """
                |        ${entityClassName}FormProps.$name to {
                |            AddSwitchField(
                |                value = state.value.$name ?: false,
                |                onValueChange = { state.value = state.value.copy($name = it) },
                |                label = $label
                |            )
                |        }
            """.trimMargin()

            FieldKind.DATE, FieldKind.DATETIME -> """
                |        ${entityClassName}FormProps.$name to {
                |            AddDateField(
                |                value = state.value.$name,
                |                onValueChange = { if (it != null) state.value = state.value.copy($name = it) },
                |                label = $label,
                |                isRequired = $isRequired,
                |            )
                |        }
            """.trimMargin()

            FieldKind.TEXT -> """
                |        ${entityClassName}FormProps.$name to {
                |            AddTextField(
                |                value = state.value.$name?.toString() ?: "",
                |                onValueChange = {
                |                    state.value = state.value.copy($name = if (it.isNullOrEmpty()) ${property.defaultValue} else it.parseObjectByKtx())
                |                },
                |                label = $label,
                |                isRequired = $isRequired
                |            )
                |        }
            """.trimMargin()
        }
    }

    private fun generateFormPropsWithStrategy(
        entityClassName: String,
        properties: List<PropertyModel>
    ): String {
        val propConstants = properties.joinToString("\n") { prop ->
            "    const val ${prop.name} = \"${prop.name}\""
        }
        val allFieldsList = properties.joinToString(", ") { "\"${it.name}\"" }

        return """
            |/**
            | * $entityClassName 表单属性常量
            | */
            |object ${entityClassName}FormProps {
            |$propConstants
            |
            |    fun getAllFields(): List<String> {
            |        return listOf($allFieldsList)
            |    }
            |}
        """.trimMargin()
    }

    private fun generateDslMethodsWithStrategy(
        entityClassName: String,
        properties: List<PropertyModel>
    ): String {
        return properties.joinToString("\n\n") { prop ->
            val propName = prop.name
            """
                |    fun $propName(
                |        hidden: Boolean = false,
                |        order: Int? = null,
                |        render: (@Composable (MutableState<${entityClassName}Iso>) -> Unit)? = null
                |    ) {
                |        when {
                |            hidden -> {
                |                hiddenFields.add("$propName")
                |                renderMap.remove("$propName")
                |            }
                |            render != null -> {
                |                hiddenFields.remove("$propName")
                |                renderMap["$propName"] = { render(state) }
                |            }
                |            else -> {
                |                hiddenFields.remove("$propName")
                |                renderMap.remove("$propName")
                |            }
                |        }
                |
                |        order?.let { orderValue ->
                |            updateFieldOrder("$propName", orderValue)
                |        }
                |    }
            """.trimMargin()
        }
    }

    private fun classifyFieldKind(type: XType): FieldKind {
        val qualifiedName = type.typeElement?.qualifiedName ?: type.asTypeName().toString()
        return when {
            qualifiedName == "kotlin.Boolean" || qualifiedName == "java.lang.Boolean" || qualifiedName == "boolean" -> FieldKind.BOOLEAN
            qualifiedName.contains("LocalDateTime") || qualifiedName.contains("Instant") -> FieldKind.DATETIME
            qualifiedName.contains("LocalDate") -> FieldKind.DATE
            else -> FieldKind.TEXT
        }
    }

    private fun defaultValueFor(type: XType, nullable: Boolean): String {
        if (nullable) {
            return "null"
        }
        val qualifiedName = type.typeElement?.qualifiedName ?: type.asTypeName().toString()
        return when {
            qualifiedName == "kotlin.Boolean" || qualifiedName == "java.lang.Boolean" || qualifiedName == "boolean" -> "false"
            qualifiedName == "kotlin.Long" || qualifiedName == "java.lang.Long" || qualifiedName == "long" -> "0L"
            qualifiedName == "kotlin.Float" || qualifiedName == "java.lang.Float" || qualifiedName == "float" -> "0f"
            qualifiedName == "kotlin.Double" || qualifiedName == "java.lang.Double" || qualifiedName == "double" -> "0.0"
            qualifiedName == "kotlin.Int" || qualifiedName == "java.lang.Integer" || qualifiedName == "int" -> "0"
            else -> "\"\""
        }
    }

    private fun quote(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        return "\"$escaped\""
    }

    private enum class FieldKind {
        BOOLEAN,
        DATE,
        DATETIME,
        TEXT
    }

    private data class PropertyModel(
        val name: String,
        val label: String,
        val nullable: Boolean,
        val kind: FieldKind,
        val defaultValue: String
    )
}
