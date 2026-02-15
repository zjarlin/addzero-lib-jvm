# 方法语义化特化生成器 (Method Semanticizer)

这是一个 KSP 插件，用于将底层的“通用方法”通过固定参数组合，特化为具有明确业务语义的扩展函数。

## 模块分层

1.  **`method-semanticizer-api`**: 包含 `@SemanticVariation` 注解。
2.  **`method-semanticizer-spi`**: 包含 SPI 接口与核心数据模型。
3.  **`method-semanticizer-processor`**: KSP 核心执行引擎。

---

## 1. 注解用法 (快速定义)

适用于在源码中直接声明常用的变体。

```kotlin
interface UserRepository {
    /**
     * 原始方法：save(user, mode, associatedMode)
     * 特化方法：syncUser(user)
     */
    @SemanticVariation(
        name = "syncUser", 
        args = ["mode=SaveMode.UPSERT", "associatedMode=AssociatedSaveMode.REPLACE"],
        doc = "全量同步用户信息"
    )
    fun save(user: User, mode: SaveMode, associatedMode: AssociatedSaveMode)
}
```

---

## 2. SPI 用法 (灵活对接元数据)

适用于需要解耦、从外部文件加载对照表或批量生成变体的场景。

### 业务对照关系示例 (Jimmer 保存模式)

我们可以将复杂的参数组合建模为易于理解的业务方法：

| SaveMode (根对象) | AssociatedSaveMode (关联对象) | 目标方法名 | 业务语义 |
| :--- | :--- | :--- | :--- |
| `UPSERT` | `REPLACE` | **`sync`** | 全量同步 (刷新 Diff) |
| `INSERT_ONLY` | `APPEND` | **`init`** | 初始化 (仅插入追加) |
| `UPDATE_ONLY` | `UPDATE` | **`patch`** | 局部更新 (仅更新已有) |

### 实现 SPI

你只需要实现 `SemanticMappingProvider` 接口，并返回 `SemanticMethodDefinition` 列表。

```kotlin
class JimmerSemanticProvider : SemanticMappingProvider {

    /** 声明需要处理的类 */
    override fun getSupportedClassNames() = listOf("site.addzero.repo.BaseRepository")

    /** 提供映射表 */
    override fun getMappings(qualifiedName: String): Map<String, List<SemanticMethodDefinition>>? {
        if (qualifiedName != "site.addzero.repo.BaseRepository") return null

        // 直接构建定义列表
        val variations = listOf(
            SemanticMethodDefinition(
                newMethodName = "sync",
              //固定两个参数
                fixedParameters = mapOf(
                    "mode" to "org.babyfish.jimmer.sql.ast.mutation.SaveMode.UPSERT",
                    "associatedMode" to "org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode.REPLACE"
                ),
                doc = "全量同步实体及其关联对象"
            ),
            SemanticMethodDefinition(
                newMethodName = "init",
              //固定两个参数
                fixedParameters = mapOf(
                    "mode" to "org.babyfish.jimmer.sql.ast.mutation.SaveMode.INSERT_ONLY",
                    "associatedMode" to "org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode.APPEND"
                ),
                doc = "以插入模式初始化实体"
            )
        )

        // 返回 [原始方法名] -> [变体列表定义]
        return mapOf("save" to variations)
    }
}
```

### 注册 SPI
在模块资源目录创建文件：
`src/commonMain/resources/META-INF/services/site.addzero.ksp.metadata.semantic.SemanticMappingProvider`
写入实现类全限定名：
`com.yourpkg.JimmerSemanticProvider`

---

## 3. 关键数据模型说明

`SemanticMethodDefinition` 决定了代码生成的行为：

- **`newMethodName`**: 生成的扩展函数名称。
- **`fixedParameters`**: 参数绑定表 (`Map<String, Any?>`)。
    - **Key**: 原始方法的参数名。
    - **Value**: 要固定的值。
    - **Raw Code 逻辑**: 如果字符串中包含 `.` (点号)，处理器会将其视为代码表达式（如 `SaveMode.UPSERT`）直接写入调用处；否则视为普通字符串（自动加双引号）。
- **`doc`**: 生成方法的 KDoc 注释。

---

## 核心特性

- **泛型镜像 (Generic Mirroring)**: 支持 `reified` 泛型和 `inline` 函数，自动透传类型参数。
- **修饰符继承**: 自动保留 `suspend`, `inline`, `infix`, `operator` 等。
- **参数智能剥离**: 生成的方法会自动剔除已绑定的参数，仅保留用户需要传入的参数。

## 依赖配置

```kotlin
dependencies {
    implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-api"))
    ksp(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-processor"))
    // 将你的 SPI 实现模块注入 KSP 类路径
    ksp(project(":your-spi-impl-module")) 
}
```
