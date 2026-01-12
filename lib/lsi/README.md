# LSI 核心接口与实现模块映射

## lsi-core 核心接口与各模块实现类映射表

### 1. 核心接口定义 (lsi-core)

| 接口名               | 路径                         | 主要功能    | 实现模块  |
|-------------------|----------------------------|---------|-------|
| **LsiClass**      | `element/LsiClass.kt`      | 类结构抽象   | 所有模块  |
| **LsiField**      | `element/LsiField.kt`      | 字段/属性抽象 | 所有模块  |
| **LsiMethod**     | `element/LsiMethod.kt`     | 方法抽象    | 所有模块  |
| **LsiType**       | `element/LsiType.kt`       | 类型系统抽象  | 所有模块  |
| **LsiAnnotation** | `element/LsiAnnotation.kt` | 注解抽象    | 所有模块  |
| **LsiElement**    | `element/LsiElement.kt`    | 元素基础接口  | 所有模块  |
| **LsiFile**       | `file/LsiFile.kt`          | 文件结构抽象  | PSI相关 |
| **LsiProject**    | `project/LsiProject.kt`    | 项目结构抽象  | PSI相关 |

### 2. lsi-apt 实现映射

| LSI 核心接口          | APT 实现类            | 底层映射                                         | 关键特性          |
|-------------------|--------------------|----------------------------------------------|---------------|
| **LsiClass**      | `AptLsiClass`      | `javax.lang.model.element.TypeElement`       | 支持Java类、接口、枚举 |
| **LsiField**      | `AptLsiField`      | `javax.lang.model.element.VariableElement`   | 支持字段、常量       |
| **LsiMethod**     | `AptLsiMethod`     | `javax.lang.model.element.ExecutableElement` | 支持方法、构造器      |
| **LsiType**       | `AptLsiType`       | `javax.lang.model.type.TypeMirror`           | 支持原始类型、泛型     |
| **LsiAnnotation** | `AptLsiAnnotation` | `javax.lang.model.element.AnnotationMirror`  | 支持注解属性提取      |

### 3. lsi-ksp 实现映射

| LSI 核心接口          | KSP 实现类            | 底层映射                                                   | 关键特性          |
|-------------------|--------------------|--------------------------------------------------------|---------------|
| **LsiClass**      | `KspLsiClass`      | `com.google.devtools.ksp.symbol.KSClassDeclaration`    | 支持Kotlin类、数据类 |
| **LsiField**      | `KspLsiField`      | `com.google.devtools.ksp.symbol.KSPropertyDeclaration` | 支持val/var属性   |
| **LsiMethod**     | `KspLsiMethod`     | `com.google.devtools.ksp.symbol.KSFunctionDeclaration` | 支持函数、伴生对象     |
| **LsiType**       | `KspLsiType`       | `com.google.devtools.ksp.symbol.KSType`                | 支持可空性、泛型      |
| **LsiAnnotation** | `KspLsiAnnotation` | `com.google.devtools.ksp.symbol.KSAnnotation`          | 支持注解参数        |

### 4. lsi-reflection 实现映射

| LSI 核心接口          | Reflection 实现类       | 底层映射                              | 关键特性    |
|-------------------|----------------------|-----------------------------------|---------|
| **LsiClass**      | `ClazzLsiClass`      | `java.lang.Class`                 | 运行时类分析  |
| **LsiField**      | `ClazzLsiField`      | `java.lang.reflect.Field`         | 运行时字段访问 |
| **LsiMethod**     | `ClazzLsiMethod`     | `java.lang.reflect.Method`        | 运行时方法调用 |
| **LsiType**       | `ClazzLsiType`       | `java.lang.Class`                 | 类型反射    |
| **LsiAnnotation** | `ClazzLsiAnnotation` | `java.lang.annotation.Annotation` | 运行时注解   |

### 5. lsi-psi 实现映射 (Java)

| LSI 核心接口          | PSI 实现类            | 底层映射                             | 关键特性         |
|-------------------|--------------------|----------------------------------|--------------|
| **LsiClass**      | `PsiLsiClass`      | `com.intellij.psi.PsiClass`      | Java PSI深度集成 |
| **LsiField**      | `PsiLsiField`      | `com.intellij.psi.PsiField`      | Java字段支持     |
| **LsiMethod**     | `PsiLsiMethod`     | `com.intellij.psi.PsiMethod`     | Java方法支持     |
| **LsiType**       | `PsiLsiType`       | `com.intellij.psi.PsiType`       | Java类型系统     |
| **LsiAnnotation** | `PsiLsiAnnotation` | `com.intellij.psi.PsiAnnotation` | Java注解支持     |

### 6. lsi-kt 实现映射 (Kotlin PSI)

| LSI 核心接口          | Kotlin PSI 实现类    | 底层映射                                         | 关键特性         |
|-------------------|-------------------|----------------------------------------------|--------------|
| **LsiClass**      | `KtLsiClass`      | `org.jetbrains.kotlin.psi.KtClass`           | 传统Kotlin PSI |
| **LsiField**      | `KtLsiField`      | `org.jetbrains.kotlin.psi.KtProperty`        | Kotlin属性支持   |
| **LsiMethod**     | `KtLsiMethod`     | `org.jetbrains.kotlin.psi.KtNamedFunction`   | Kotlin函数支持   |
| **LsiType**       | `KtLsiType`       | `org.jetbrains.kotlin.psi.KtTypeReference`   | Kotlin类型系统   |
| **LsiAnnotation** | `KtLsiAnnotation` | `org.jetbrains.kotlin.psi.KtAnnotationEntry` | Kotlin注解支持   |

### 7. lsi-kt2 实现映射 (K2 Analysis)

| LSI 核心接口          | K2 实现类             | 底层映射                                                          | 关键特性   |
|-------------------|--------------------|---------------------------------------------------------------|--------|
| **LsiClass**      | `Kt2LsiClass`      | `org.jetbrains.kotlin.psi.KtClass` + KaClassSymbol            | K2符号系统 |
| **LsiField**      | `Kt2LsiField`      | `org.jetbrains.kotlin.psi.KtProperty` + KaPropertySymbol      | 精确类型推断 |
| **LsiMethod**     | `Kt2LsiMethod`     | `org.jetbrains.kotlin.psi.KtNamedFunction` + KaFunctionSymbol | 语义分析   |
| **LsiType**       | `Kt2LsiType`       | `org.jetbrains.kotlin.analysis.api.types.KaType`              | 完整类型系统 |
| **LsiAnnotation** | `Kt2LsiAnnotation` | `org.jetbrains.kotlin.psi.KtAnnotationEntry` + KaAnnotation   | 符号级注解  |

### 8. lsi-psiandkt 统一接口映射

| LSI 核心接口          | 统一实现类                  | 底层映射策略          | 关键特性   |
|-------------------|------------------------|-----------------|--------|
| **LsiClass**      | `UnifiedLsiClass`      | 自动识别Java/Kotlin | 统一类接口  |
| **LsiField**      | `UnifiedLsiField`      | 根据文件类型选择        | 统一字段接口 |
| **LsiMethod**     | `UnifiedLsiMethod`     | 自动适配PSI类型       | 统一方法接口 |
| **LsiType**       | `UnifiedLsiType`       | 智能类型解析          | 统一类型接口 |
| **LsiAnnotation** | `UnifiedLsiAnnotation` | 统一注解处理          | 统一注解接口 |

### 9. 扩展功能映射

| 扩展模块             | 核心功能类                 | 依赖接口               | 增强能力      |
|------------------|-----------------------|--------------------|-----------|
| **lsi-database** | `LsiFieldDatabaseExt` | LsiField, LsiClass | 数据库映射功能   |
| **lsi-database** | `LsiClassDatabaseExt` | LsiClass           | 表/索引/外键支持 |
| **lsi-intellij** | `ModuleUtil`          | LsiProject         | 模块管理功能    |
| **lsi-intellij** | `ProjectExt`          | LsiProject         | 项目级操作     |

### 10. 核心桥接模式实现

```kotlin
// 扩展函数桥接模式 - APT示例
fun TypeElement.toLsiClass(elements: Elements): LsiClass = AptLsiClass(this, elements)

// 扩展函数桥接模式 - KSP示例
fun KSClassDeclaration.toLsiClass(resolver: Resolver): LsiClass = KspLsiClass(this, resolver)

// 扩展函数桥接模式 - Reflection示例
fun <T> Class<T>.toLsiClass(): LsiClass = ClazzLsiClass(this)

// 扩展函数桥接模式 - PSI示例
fun PsiClass.toLsiClass(): LsiClass = PsiLsiClass(this)

// 统一桥接接口 - lsi-psiandkt
fun VirtualFile.toAllLsiClassesUnified(): List<LsiClass> {
    return when {
        isJavaFile -> psiFile.toLsiClasses() // 使用PSI
        isKotlinFile -> ktFile.toLsiClassesK2() // 使用K2
        else -> emptyList()
    }
}
```

### 11. 环境抽象层映射

| LSI 环境             | 实现类                        | 底层环境                         | 特性         |
|--------------------|----------------------------|------------------------------|------------|
| **LsiEnvironment** | `AptLsiEnvironment`        | `ProcessingEnvironment`      | APT处理环境    |
| **LsiEnvironment** | `KspLsiEnvironment`        | `SymbolProcessorEnvironment` | KSP处理环境    |
| **LsiEnvironment** | `ReflectionLsiEnvironment` | 运行时环境                        | 反射环境       |
| **LsiEnvironment** | `IntelliJLsiEnvironment`   | `Project`                    | IntelliJ环境 |

所有实现模块都通过统一的LSI接口提供一致的代码结构操作能力，底层通过不同的元编程技术桥接解析逻辑。