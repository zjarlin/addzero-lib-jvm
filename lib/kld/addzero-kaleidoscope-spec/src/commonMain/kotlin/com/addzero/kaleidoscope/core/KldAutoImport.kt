package com.addzero.kaleidoscope.core

/**
 * 自动导入配置
 *
 * @param blacklistPredicate 黑名单过滤谓词，返回true表示需要过滤掉该导入
 * @param packageConverter 包名转换函数，可以将某些包名转换为其他包名
 */
data class KldAutoImportConfig(
    val blacklistPredicate: (String) -> Boolean = { false },
    val packageConverter: (String) -> String = { it }
)

/**
 * 默认的自动导入配置
 */
val DefaultKldAutoImportConfig by lazy {
    KldAutoImportConfig(
        blacklistPredicate = { qualifiedName ->
            qualifiedName.startsWith("java.lang.")
//            ||
//            qualifiedName.startsWith("kotlin.") ||
//            qualifiedName.startsWith("org.babyfish.jimmer.")
        }
    )
}

/**
 * 从KldTypeElement自动收集需要导入的类型
 *
 * @param config 自动导入配置
 * @return 需要导入的类型全限定名集合
 */
fun KldTypeElement.autoImport(config: KldAutoImportConfig = DefaultKldAutoImportConfig): Set<String> {
    val imports = mutableSetOf<String>()

    // 使用扩展函数委托收集各种类型的导入
    this.superclass?.collectImportsTo(imports, config)
    this.interfaces.forEach { it.collectImportsTo(imports, config) }
    this.fields.forEach { it.type.collectImportsTo(imports, config) }

    // 收集方法相关导入
    this.methods.forEach { method ->
        method.collectImportsTo(imports, config)
    }

    // 收集构造函数参数类型导入
    this.constructors.forEach { constructor ->
        constructor.parameters.forEach { it.type.collectImportsTo(imports, config) }
    }

    // 收集属性类型导入（Kotlin特有）
    this.properties.forEach { property ->
        property.collectImportsTo(imports, config)
    }

    return imports
}

/**
 * 扩展函数：收集KldExecutableElement的导入
 */
private fun KldExecutableElement.collectImportsTo(imports: MutableSet<String>, config: KldAutoImportConfig) {
    // 返回类型
    this.returnType.collectImportsTo(imports, config)

    // 参数类型
    this.parameters.forEach { it.type.collectImportsTo(imports, config) }

    // 异常类型
    this.thrownTypes.forEach { it.collectImportsTo(imports, config) }
}

/**
 * 扩展函数：收集KldPropertyElement的导入
 */
private fun KldPropertyElement.collectImportsTo(imports: MutableSet<String>, config: KldAutoImportConfig) {
    // 属性类型
    this.type.collectImportsTo(imports, config)

    // 收集getter和setter相关类型
    this.getter?.collectImportsTo(imports, config)
    this.setter?.let { setter ->
        setter.parameters.forEach { it.type.collectImportsTo(imports, config) }
    }
}

/**
 * 扩展函数：收集KldType的导入
 */
private fun KldType.collectImportsTo(imports: MutableSet<String>, config: KldAutoImportConfig) {
    // 获取类型声明元素
    val typeElement = when (this) {
        is KldDeclaredType -> this.kldTypeElement
        else -> this.declaration as? KldTypeElement
    }

    typeElement?.qualifiedName?.let { qualifiedName ->
        // 检查是否在黑名单中
        if (!config.blacklistPredicate(qualifiedName)) {
            // 应用包名转换
            val convertedName = config.packageConverter(qualifiedName)
            imports.add(convertedName)
        }
    }

    // 处理泛型参数
    this.typeArguments.forEach { it.collectImportsTo(imports, config) }

    // 处理数组组件类型
    if (this is KldArrayType) {
        this.kldComponentType.collectImportsTo(imports, config)
    }

    // 处理通配符边界
    if (this is KldWildcardType) {
        this.extendsBound?.collectImportsTo(imports, config)
        this.superBound?.collectImportsTo(imports, config)
    }
}
