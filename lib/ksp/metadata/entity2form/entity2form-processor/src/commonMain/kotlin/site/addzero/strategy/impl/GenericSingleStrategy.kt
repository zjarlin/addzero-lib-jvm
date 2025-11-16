package site.addzero.strategy.impl

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import site.addzero.strategy.FormStrategy
import site.addzero.util.*

/**
 * 🎯 单个对象选择策略
 *
 * 处理单个对象类型字段，生成单选选择器
 */
object GenericSingleStrategy : FormStrategy {

    override val name: String = "GenericSingleStrategy"

    override fun calculateWeight(prop: KSPropertyDeclaration): Int {
        val typeName = prop.typeName
        val declaration = prop.type.resolve().declaration

        // 检查是否为 Jimmer 实体
        val isJimmerEntityType = isJimmerEntity(declaration)

        // 检查是否为枚举类型
        val isEnumType = isEnum(declaration)

        // 检查是否为集合类型
        val isCollectionType = prop.isCollectionType()

        println("GenericSingleStrategy: ${declaration.simpleName.asString()}, isJimmerEntity: $isJimmerEntityType, isEnum: $isEnumType, isCollection: $isCollectionType")

        // 如果是基础类型，直接返回 0，不处理
        if (isBasicType(typeName)) {
            return 0
        }

        // 使用布尔值 + 操作符累加计算权重
        return isJimmerEntityType +
                (!isEnumType) +  // 排除枚举类型
                (!isCollectionType)  // 排除集合类型
    }

    private fun isBasicType(typeName: String): Boolean {
        val basicTypes = setOf(
            "String", "Long", "Int", "Boolean", "Double", "Float",
            "BigDecimal", "LocalDate", "LocalDateTime", "Instant"
        )
        return basicTypes.any { typeName.contains(it) }
    }


    override fun genCode(prop: KSPropertyDeclaration): String {
        val name = prop.name
        val label = prop.label
        val isRequired = prop.isRequired
        val defaultValue = prop.defaultValue
        val typeName = prop.typeName.replace("?", "").trim()
        val declaration = prop.type.resolve().declaration
        val typeOrGenericClassDeclaration = declaration as KSClassDeclaration

        val entityClassName = (prop.parentDeclaration as? KSClassDeclaration)?.simpleName?.asString()
            ?: throw IllegalStateException("无法获取实体类名")

        // 新逻辑：查找字段类型的属性中带有 @LabelProp 注解的属性,找不到就不生成了
        val labelField = findLabelPropInType(typeOrGenericClassDeclaration) ?: return    "${entityClassName}FormProps.$name to {}"

        val simpleName = typeOrGenericClassDeclaration.simpleName.asString()
        val isoTypeName = "${simpleName}Iso"

        val istree = typeOrGenericClassDeclaration.hasProperty("children")
        val treedsl = if (istree) """getChildren = { it.children?:emptyList() }""" else ""

        return """
            |        ${entityClassName}FormProps.$name to {
            |            var dataList by remember { mutableStateOf<List<${isoTypeName}>>(emptyList()) }
            |
            |            LaunchedEffect(Unit) {
            |                try {
            |                    val provider = Iso2DataProvider.isoToDataProvider[${isoTypeName}::class]
            |                    dataList = provider?.invoke("") as? List<${isoTypeName}> ?: emptyList()
            |                } catch (e: Exception) {
            |                    println("加载 $name 数据失败: ${'$'}{e.message}")
            |                    dataList = emptyList()
            |                }
            |            }
            |
            |            AddGenericSingleSelector(
            |                value = state.value.$name,
            |                onValueChange = { state.value = state.value.copy($name = it) },
            |                placeholder = $label,
            |                dataProvider = { dataList },
            |                getId = { it.id ?: 0L },
            |                getLabel = { it.$labelField ?: "" },
            |               $treedsl 
            |            )
            |        }
        """.trimMargin()
    }

}
