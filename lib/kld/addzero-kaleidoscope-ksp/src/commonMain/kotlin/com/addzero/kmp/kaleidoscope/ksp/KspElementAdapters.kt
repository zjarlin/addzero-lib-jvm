package com.addzero.kmp.kaleidoscope.ksp

import com.addzero.kmp.kaleidoscope.core.*
import com.google.devtools.ksp.symbol.*

/**
 * 将KSAnnotated转换为KldElement
 */
fun KSAnnotated.toKldElement(): KldElement {
    return when (this) {
        is KSClassDeclaration -> this.toKldTypeElement()
        is KSFunctionDeclaration -> this.toKldExecutableElement()
        is KSPropertyDeclaration -> this.toKldVariableElement()
        is KSTypeParameter -> this.toKldTypeParameter()
        is KSValueParameter -> this.toKldVariableElement()
        else -> this.toUnknownElement()
    }
}

/**
 * 将KSClassDeclaration转换为KldTypeElement
 */
fun KSClassDeclaration.toKldTypeElement(): KldTypeElement {
    val symbol = this
    return object : KldTypeElement {
        override val simpleName= symbol.simpleName.asString()
        override val qualifiedName= symbol.qualifiedName?.asString() ?: ""
        override val kldElementType = convertSymbolKind(symbol)
        override val packageName = symbol.packageName.asString()
        override val enclosingElement = (symbol.parent as? KSAnnotated)?.toKldElement()
        override val enclosedElements = symbol.declarations.map { it.toKldElement() }.toList()
        override val annotations = symbol.annotations.map { it.toKldAnnotation() }.toList()
        override val kldModifiers = symbol.modifiers.map { convertModifier(it) }.toSet()
        override val documentation = symbol.docString
        override val kldSourceFile = symbol.containingFile?.toKldSourceFile()
        override val typeKind= when (symbol.classKind) {
            ClassKind.CLASS -> KldTypeElementKind.CLASS
            ClassKind.INTERFACE -> KldTypeElementKind.INTERFACE
            ClassKind.ENUM_CLASS -> KldTypeElementKind.ENUM
            ClassKind.ANNOTATION_CLASS -> KldTypeElementKind.ANNOTATION_TYPE
            ClassKind.OBJECT -> KldTypeElementKind.OBJECT
            ClassKind.ENUM_ENTRY -> KldTypeElementKind.ENUM_ENTRY
            else -> KldTypeElementKind.CLASS
        }
        override val superclass = symbol.superTypes.firstOrNull { !it.resolve().declaration.isInterface() }?.let { it.resolve().toKldType() }
        override val interfaces = symbol.superTypes.filter { it.resolve().declaration.isInterface() }.map { it.resolve().toKldType() }.toList()
        override val kldTypeParameters = symbol.typeParameters.map { it.toKldTypeParameter() }
        override val nestedTypes = symbol.declarations.filterIsInstance<KSClassDeclaration>().map { it.toKldTypeElement() }.toList()
        override val fields = symbol.getAllProperties().map { it.toKldVariableElement() }.toList()
        override val methods = symbol.getAllFunctions().map { it.toKldExecutableElement() }.toList()
        override val constructors = symbol.primaryConstructor?.let { listOf(it.toKldExecutableElement()) } ?: emptyList()
        override val properties = symbol.getAllProperties().map { it.toPropertyElement() }.toList()
        override val companionObject = symbol.declarations.filterIsInstance<KSClassDeclaration>().find { it.isCompanionObject }?.let { it.toKldTypeElement() }
        override val isInner = Modifier.INNER in symbol.modifiers
        override val isLocal = false
        override val isAnonymous = symbol.simpleName.asString().isEmpty()
        override fun getMethod(name: String, parameterTypes: List<KldType>): KldExecutableElement? {
            return methods.find { method ->
                method.simpleName == name && method.parameters.map { it.type } == parameterTypes
            }
        }

        override fun getMethods(name: String): List<KldExecutableElement> {
            return methods.filter { it.simpleName == name }
        }

        override fun getField(name: String): KldVariableElement? {
            return fields.find { it.simpleName == name }
        }

        override fun getProperty(name: String): KldPropertyElement? {
            return properties.find { it.simpleName == name }
        }

        override fun getDefaultConstructor(): KldExecutableElement? {
            return constructors.find { it.parameters.isEmpty() }
        }

        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            return symbol.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }?.let { it.toKldAnnotation() }
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.simpleName.asString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return symbol.annotations.filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }.map { it.toKldAnnotation() }.toList()
        }

        override fun isValid(): Boolean = true

        private fun KSDeclaration.isInterface(): Boolean {
            return this is KSClassDeclaration && this.classKind == ClassKind.INTERFACE
        }
    }
}

/**
 * 将KSFunctionDeclaration转换为KldExecutableElement
 */
fun KSFunctionDeclaration.toKldExecutableElement(): KldExecutableElement {
    val symbol = this
    return object : KldExecutableElement {
        override val simpleName= symbol.simpleName.asString()
        override val qualifiedName= symbol.qualifiedName?.asString()
        override val kldElementType= convertSymbolKind(symbol)
        override val packageName= symbol.packageName.asString()
        override val enclosingElement= symbol.parentDeclaration?.toKldElement()
        override val enclosedElements= emptyList<KldElement>()
        override val annotations= symbol.annotations.map { it.toKldAnnotation() }.toList()
        override val kldModifiers= symbol.modifiers.map { convertModifier(it) }.toSet()
        override val documentation= symbol.docString
        override val kldSourceFile= symbol.containingFile?.toKldSourceFile()
        override val returnType= symbol.returnType?.resolve()?.let { it.toKldType() } ?: createUnitType()
        override val parameters= symbol.parameters.map { it.toKldVariableElement() }
        override val kldTypeParameters= symbol.typeParameters.map { it.toKldTypeParameter() }
        override val thrownTypes= emptyList<KldType>()
        override val receiverType= symbol.extensionReceiver?.resolve()?.toKldType()
        override val isVarArgs= symbol.parameters.any { it.isVararg }
        override val isAbstract= Modifier.ABSTRACT in symbol.modifiers
        override val isDefault= false
        override val isSuspend= Modifier.SUSPEND in symbol.modifiers
        override val isInline= Modifier.INLINE in symbol.modifiers
        override val isOperator= Modifier.OPERATOR in symbol.modifiers
        override val isInfix= Modifier.INFIX in symbol.modifiers
        override val signature= buildString {
            append(symbol.simpleName.asString())
            append("(")
            append(parameters.joinToString { "${it.simpleName}: ${it.type.typeName}" })
            append("): ${returnType.typeName}")
        }

        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            return symbol.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }?.let { it.toKldAnnotation() }
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.simpleName.asString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return symbol.annotations.filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }.map { it.toKldAnnotation() }.toList()
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将KSPropertyDeclaration转换为PropertyElement
 */
fun KSPropertyDeclaration.toPropertyElement(): KldPropertyElement {
    val symbol = this
    return object : KldPropertyElement {
        override val simpleName: String = symbol.simpleName.asString()
        override val qualifiedName: String? = symbol.qualifiedName?.asString()
        override val kldElementType: KldElementType = convertSymbolKind(symbol)
        override val packageName: String = symbol.packageName.asString()
        override val enclosingElement: KldElement? = symbol.parentDeclaration?.toKldElement()
        override val enclosedElements: List<KldElement> = emptyList()
        override val annotations: List<KldAnnotation> = symbol.annotations.map { it.toKldAnnotation() }.toList()
        override val kldModifiers: Set<KldModifier> = symbol.modifiers.map { convertModifier(it) }.toSet()
        override val documentation: String? = symbol.docString
        override val kldSourceFile: KldSourceFile? = symbol.containingFile?.toKldSourceFile()
        override val type: KldType = symbol.type.resolve().toKldType()
        override val getter: KldExecutableElement? = null
        override val setter: KldExecutableElement? = null
        override val backingField: KldVariableElement? = null
        override val isMutable: Boolean = symbol.isMutable
        override val isLateinit: Boolean = Modifier.LATEINIT in symbol.modifiers
        override val isConst: Boolean = Modifier.CONST in symbol.modifiers
        override val hasCustomGetter: Boolean = symbol.getter != null
        override val hasCustomSetter: Boolean = symbol.setter != null
        override val delegateExpression: String? = null
        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            return symbol.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }?.let { it.toKldAnnotation() }
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.simpleName.asString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return symbol.annotations.filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }.map { it.toKldAnnotation() }.toList()
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将KSAnnotated转换为VariableElement
 */
fun KSAnnotated.toKldVariableElement(): KldVariableElement {
    val symbol = this
    return object : KldVariableElement {
        override val simpleName= when (symbol) {
            is KSPropertyDeclaration -> symbol.simpleName.asString()
            is KSValueParameter -> symbol.name?.asString() ?: "unknown"
            else -> "unknown"
        }
        override val qualifiedName= when (symbol) {
            is KSDeclaration -> symbol.qualifiedName?.asString()
            else -> null
        }
        override val kldElementType= convertSymbolKind(symbol)
        override val packageName= when (symbol) {
            is KSDeclaration -> symbol.packageName.asString()
            else -> null
        }
        override val enclosingElement= when (symbol) {
            is KSDeclaration -> symbol.parentDeclaration?.toKldElement()
            else -> null
        }
        override val enclosedElements= emptyList<KldElement>()
        override val annotations= symbol.annotations.map { it.toKldAnnotation() }.toList()
        override val kldModifiers= when (symbol) {
            is KSDeclaration -> symbol.modifiers.map { convertModifier(it) }.toSet()
            else -> emptySet()
        }
        override val documentation= when (symbol) {
            is KSDeclaration -> symbol.docString
            else -> null
        }
        override val kldSourceFile= when (symbol) {
            is KSDeclaration -> symbol.containingFile?.toKldSourceFile()
            else -> null
        }
        override val type= when (symbol) {
            is KSPropertyDeclaration -> symbol.type.resolve().toKldType()
            is KSValueParameter -> symbol.type.resolve().toKldType()
            else -> throw IllegalStateException("Neither property nor parameter is set")
        }
        override val constantValue= null
        override val isConstant= when (symbol) {
            is KSPropertyDeclaration -> Modifier.CONST in symbol.modifiers
            else -> false
        }
        override val isMutable= when (symbol) {
            is KSPropertyDeclaration -> symbol.isMutable
            else -> false
        }
        override val isLateinit= when (symbol) {
            is KSPropertyDeclaration -> Modifier.LATEINIT in symbol.modifiers
            else -> false
        }
        override val kldVariableKind= when (symbol) {
            is KSPropertyDeclaration -> KldVariableKind.FIELD
            is KSValueParameter -> KldVariableKind.PARAMETER
            else -> KldVariableKind.LOCAL_VARIABLE
        }

        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            return symbol.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }?.let { it.toKldAnnotation() }
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.simpleName.asString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return symbol.annotations.filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }.map { it.toKldAnnotation() }.toList()
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将KSTypeParameter转换为TypeParameter
 */
fun KSTypeParameter.toKldTypeParameter(): KldTypeParameter {
    val symbol = this
    return object : KldTypeParameter {
        override val simpleName = symbol.simpleName.asString()
        override val qualifiedName = symbol.qualifiedName?.asString()
        override val kldElementType = convertSymbolKind(symbol)
        override val packageName = symbol.qualifiedName?.asString()?.substringBeforeLast(".")
        override val enclosingElement = (symbol.parent as? KSAnnotated)?.toKldElement()
        override val enclosedElements = emptyList<KldElement>()
        override val annotations = symbol.annotations.map { it.toKldAnnotation() }.toList()
        override val kldModifiers = symbol.modifiers.map { convertModifier(it) }.toSet()
        override val documentation = null as String?
        override val kldSourceFile = null as KldSourceFile?
        override val name = symbol.name.asString()
        override val bounds = symbol.bounds.map { it.resolve().toKldType() }.toList()
        override val kldVariance = when (symbol.variance) {
            Variance.INVARIANT -> KldVariance.INVARIANT
            Variance.COVARIANT -> KldVariance.COVARIANT
            Variance.CONTRAVARIANT -> KldVariance.CONTRAVARIANT
            else -> KldVariance.INVARIANT
        }
        override val isReified: Boolean = Modifier.REIFIED in symbol.modifiers
        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            return symbol.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }?.let { it.toKldAnnotation() }
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.simpleName.asString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return symbol.annotations.filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }.map { it.toKldAnnotation() }.toList()
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将KSAnnotated转换为UnknownElement
 */
fun KSAnnotated.toUnknownElement(): KldElement {
    val symbol = this
    return object : KldElement {
        override val simpleName= when (symbol) {
            is KSDeclaration -> symbol.simpleName.asString()
            else -> "unknown"
        }
        override val qualifiedName= when (symbol) {
            is KSDeclaration -> symbol.qualifiedName?.asString()
            else -> null
        }
        override val kldElementType= convertSymbolKind(symbol)
        override val packageName= when (symbol) {
            is KSDeclaration -> symbol.packageName.asString()
            else -> null
        }
        override val enclosingElement= when (symbol) {
            is KSDeclaration -> symbol.parentDeclaration?.toKldElement()
            else -> null
        }
        override val enclosedElements= when (symbol) {
            is KSDeclarationContainer -> symbol.declarations.map { it.toKldElement() }.toList()
            else -> emptyList()
        }
        override val annotations= symbol.annotations.map { it.toKldAnnotation() }.toList()
        override val kldModifiers= when (symbol) {
            is KSDeclaration -> symbol.modifiers.map { convertModifier(it) }.toSet()
            else -> emptySet()
        }
        override val documentation= when (symbol) {
            is KSDeclaration -> symbol.docString
            else -> null
        }
        override val kldSourceFile= when (symbol) {
            is KSDeclaration -> symbol.containingFile?.toKldSourceFile()
            else -> null
        }

        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            return symbol.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }?.let { it.toKldAnnotation() }
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.simpleName.asString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return symbol.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return symbol.annotations.filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }.map { it.toKldAnnotation() }.toList()
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将KSFile转换为KldSourceFile
 */
fun KSFile.toKldSourceFile(): KldSourceFile {
    val file = this
    return object : KldSourceFile {
        override val filePath= file.filePath
        override val fileName= file.fileName
        override val packageName= file.packageName.asString()
        override val content= null
        override val isGenerated= false
    }
}

/**
 * 转换符号类型
 */
private fun convertSymbolKind(symbol: KSAnnotated): KldElementType {
    return when (symbol) {
        is KSClassDeclaration -> when (symbol.classKind) {
            ClassKind.CLASS -> KldElementType.CLASS
            ClassKind.INTERFACE -> KldElementType.INTERFACE
            ClassKind.ENUM_CLASS -> KldElementType.ENUM
            ClassKind.ANNOTATION_CLASS -> KldElementType.ANNOTATION_TYPE
            ClassKind.OBJECT -> KldElementType.CLASS
            ClassKind.ENUM_ENTRY -> KldElementType.ENUM_CONSTANT
            else -> KldElementType.CLASS
        }

        is KSFunctionDeclaration -> KldElementType.METHOD
        is KSPropertyDeclaration -> KldElementType.PROPERTY
        is KSValueParameter -> KldElementType.PARAMETER
        is KSTypeParameter -> KldElementType.TYPE_PARAMETER
        else -> KldElementType.OTHER
    }
}

/**
 * 转换修饰符
 */
private fun convertModifier(modifier: Modifier): KldModifier {
    return when (modifier) {
        Modifier.PUBLIC -> KldModifier.PUBLIC
        Modifier.PRIVATE -> KldModifier.PRIVATE
        Modifier.PROTECTED -> KldModifier.PROTECTED
        Modifier.INTERNAL -> KldModifier.INTERNAL
        Modifier.FINAL -> KldModifier.FINAL
        Modifier.OPEN -> KldModifier.OPEN
        Modifier.ABSTRACT -> KldModifier.ABSTRACT
        Modifier.SEALED -> KldModifier.SEALED
        Modifier.OVERRIDE -> KldModifier.OVERRIDE
        Modifier.LATEINIT -> KldModifier.LATEINIT
        Modifier.DATA -> KldModifier.DATA
        Modifier.INLINE -> KldModifier.INLINE
        Modifier.SUSPEND -> KldModifier.SUSPEND
        Modifier.INFIX -> KldModifier.INFIX
        Modifier.OPERATOR -> KldModifier.OPERATOR
        Modifier.INNER -> KldModifier.INNER
        Modifier.CONST -> KldModifier.CONST
        Modifier.CROSSINLINE -> KldModifier.CROSSINLINE
        Modifier.NOINLINE -> KldModifier.NOINLINE
        Modifier.REIFIED -> KldModifier.REIFIED
        Modifier.EXTERNAL -> KldModifier.EXTERNAL
        Modifier.TAILREC -> KldModifier.TAILREC
        Modifier.VARARG -> KldModifier.VARARG
        Modifier.IN -> KldModifier.IN
        Modifier.OUT -> KldModifier.OUT
        else -> KldModifier.PUBLIC
    }
}
