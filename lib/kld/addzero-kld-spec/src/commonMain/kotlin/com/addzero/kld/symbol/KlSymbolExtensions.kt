package com.addzero.kld.symbol

/**
 * KL符号扩展函数
 */

/**
 * 获取类的所有属性（包括继承的）
 */
fun KLClassDeclaration.getAllProperties(): Sequence<KLPropertyDeclaration> {
    return sequenceOf(this.getAllProperties()).flatten()
}

/**
 * 获取类的所有函数（包括继承的）
 */
fun KLClassDeclaration.getAllFunctions(): Sequence<KLFunctionDeclaration> {
    return sequenceOf(this.getAllFunctions()).flatten()
}

/**
 * 检查类型是否可以赋值给另一个类型
 */
fun KLType.isAssignableTo(other: KLType): Boolean {
    return other.isAssignableFrom(this)
}

/**
 * 获取类型名称
 */
val KLType.name: String
    get() = this.declaration?.simpleName?.asString() ?: "unknown"

/**
 * 获取声明的简单名称
 */
val KLDeclaration.name: String
    get() = this.simpleName.asString()

/**
 * 获取声明的全限定名称
 */
val KLDeclaration.fqName: String
    get() = this.qualifiedName?.asString() ?: this.simpleName.asString()

/**
 * 检查声明是否具有指定注解
 */
fun KLDeclaration.hasAnnotation(qualifiedName: String): Boolean {
    return when (this) {
        is KLAnnotated -> this.hasAnnotation(qualifiedName)
        else -> false
    }
}

/**
 * 查找声明的指定注解
 */
fun KLDeclaration.findAnnotation(qualifiedName: String): KLAnnotation? {
    return when (this) {
        is KLAnnotated -> this.findAnnotation(qualifiedName)
        else -> null
    }
}

/**
 * 获取声明的所有注解
 */
val KLDeclaration.annotations: Sequence<KLAnnotation>
    get() = when (this) {
        is KLAnnotated -> this.annotations
        else -> emptySequence()
    }

/**
 * 检查声明是否具有指定修饰符
 */
fun KLDeclaration.hasModifier(modifier: Modifier): Boolean {
    return when (this) {
        is KLModifierListOwner -> modifier in this.modifiers
        else -> false
    }
}

/**
 * 获取声明的修饰符集合
 */
val KLDeclaration.modifiers: Set<Modifier>
    get() = when (this) {
        is KLModifierListOwner -> this.modifiers
        else -> emptySet()
    }

/**
 * 获取函数的完整签名
 */
val KLFunctionDeclaration.signature: String
    get() {
        val paramTypes = this.parameters.joinToString(", ") { it.type.resolve().name }
        val returnType = this.returnType?.resolve()?.name ?: "void"
        return "${this.name}($paramTypes): $returnType"
    }

/**
 * 获取属性的类型名称
 */
val KLPropertyDeclaration.typeName: String
    get() = this.type.resolve().name

/**
 * 检查属性是否为可变的
 */
val KLPropertyDeclaration.isVar: Boolean
    get() = this.isMutable

/**
 * 检查属性是否为只读的
 */
val KLPropertyDeclaration.isVal: Boolean
    get() = !this.isMutable

/**
 * 获取参数的类型名称
 */
val KLValueParameter.typeName: String
    get() = this.type.resolve().name

/**
 * 获取类型参数的名称
 */
val KLTypeParameter.parameterName: String
    get() = this.name.asString()

/**
 * 获取类型参数的上界
 */
val KLTypeParameter.upperBounds: Sequence<KLTypeReference>
    get() = this.bounds

/**
 * 获取文件中的所有类声明
 */
fun KLFile.getClassDeclarations(): Sequence<KLClassDeclaration> {
    return this.declarations.filterIsInstance<KLClassDeclaration>()
}

/**
 * 获取文件中的所有函数声明
 */
fun KLFile.getFunctionDeclarations(): Sequence<KLFunctionDeclaration> {
    return this.declarations.filterIsInstance<KLFunctionDeclaration>()
}

/**
 * 获取文件中的所有属性声明
 */
fun KLFile.getPropertyDeclarations(): Sequence<KLPropertyDeclaration> {
    return this.declarations.filterIsInstance<KLPropertyDeclaration>()
}
