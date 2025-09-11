# APT和KSP的对应关系清单

## 1. 元素类型对应关系

| APT (Java) | KSP (Kotlin) | KLD通用接口 | 说明 |
|------------|--------------|-------------|------|
| Element | KSDeclaration | KLDeclaration | 基础声明元素 |
| PackageElement | KSPackageDeclaration | KLDeclaration | 包声明 |
| TypeElement | KSClassDeclaration | KLClassDeclaration | 类/接口声明 |
| ExecutableElement | KSFunctionDeclaration | KLFunctionDeclaration | 方法/构造函数声明 |
| VariableElement | KSPropertyDeclaration | KLPropertyDeclaration | 字段/属性声明 |
| TypeParameterElement | KSTypeParameter | KLTypeParameter | 类型参数声明 |

## 2. 类型系统对应关系

| APT (Java) | KSP (Kotlin) | KLD通用接口 | 说明 |
|------------|--------------|-------------|------|
| TypeMirror | KSType | KLType | 类型表示 |
| DeclaredType | KSType (Class) | KLType | 声明类型 |
| PrimitiveType | KSType (Primitive) | KLType | 基本类型 |
| ArrayType | KSType (Array) | KLType | 数组类型 |
| TypeVariable | KSTypeParameter | KLTypeParameter | 类型变量 |
| WildcardType | KSTypeArgument | KLTypeArgument | 通配符类型 |

## 3. 注解系统对应关系

| APT (Java) | KSP (Kotlin) | KLD通用接口 | 说明 |
|------------|--------------|-------------|------|
| AnnotationMirror | KSAnnotation | KLAnnotation | 注解实例 |
| AnnotationValue | KSValueArgument | KLValueArgument | 注解值 |

## 4. 引用类型对应关系

| APT (Java) | KSP (Kotlin) | KLD通用接口 | 说明 |
|------------|--------------|-------------|------|
| - | KSReferenceElement | KLReferenceElement | 引用元素 |
| - | KSTypeReference | KLTypeReference | 类型引用 |

## 5. 其他对应关系

| APT (Java) | KSP (Kotlin) | KLD通用接口 | 说明 |
|------------|--------------|-------------|------|
| Name | KSName | KLName | 名称 |
| Modifier | Modifier | Modifier | 修饰符 |
| ElementKind | ClassKind/FunctionKind | ClassKind/FunctionKind | 元素种类 |

## 6. APT适配层实现检查清单

### 已实现的转换适配器：

1. ✅ AnnotationMirror → KLAnnotation (AnnotationMirror2KLAnnotation.kt)
2. ✅ Element → KLAnnotated (Element2KlAnnotated.kt)
3. ✅ Element → KLReferenceElement (ElementtoKLReferenceElement.kt)
4. ✅ DeclaredType → KLTypeReference (DeclaredTypetoKLTypeReference.kt)
5. ✅ AnnotatedConstruct → KLNode (AnnotatedConstruct2KLNode.kt)
6. ✅ TypeMirror → KLNode 和 KLTypeReference (TypeMirror2KLNode.kt)
7. ✅ RoundEnvironment → KLResolver (RoundEnvironment2Kld.kt)
8. ✅ ProcessingEnvironment → KLSymbolProcessorEnvironment (OkProcessingEnvironment2Kld.kt)
9. ✅ Messager → KSPLogger (OkMessager2KldLogger.kt)

### 部分实现的功能：

1. ⚠️ RoundEnvironment2Kld.kt 中大部分方法未实现 (有多个TODO)

### 待实现的功能：

1. ⬜ ExecutableElement → KLFunctionDeclaration 的实现
2. ⬜ VariableElement → KLPropertyDeclaration 的实现
3. ⬜ PackageElement → KLDeclaration 的实现
4. ⬜ TypeParameterElement → KLTypeParameter 的实现
5. ⬜ AnnotationValue → KLValueArgument 的完整实现

## 7. 类型层次结构映射

### APT Element 层次结构：
```
Element
├── PackageElement
├── TypeElement
├── ExecutableElement
├── VariableElement
└── TypeParameterElement
```

### KSP Declaration 层次结构：
```
KSDeclaration
├── KSPackageDeclaration
├── KSClassDeclaration
├── KSFunctionDeclaration
├── KSPropertyDeclaration
└── KSTypeParameter
```

### KLD Declaration 层次结构：
```
KLDeclaration
├── KLClassDeclaration
├── KLFunctionDeclaration
├── KLPropertyDeclaration
└── KLTypeParameter
```