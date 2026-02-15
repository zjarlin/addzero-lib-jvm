# 方法语义化特化生成器 (Method Semanticizer)

这是一个 KSP 插件，用于将底层的“通用方法”通过固定参数组合，特化为具有明确业务语义的扩展函数。

## 模块分层与 Maven 坐标

本项目已发布至 Maven Central，坐标前缀为 `site.addzero`。

1.  **`site.addzero:method-semanticizer-api`**: 
    - 职责：提供 `@SemanticVariation` 等注解。
    - 依赖：无。适用于业务模块的接口层。
2.  **`site.addzero:method-semanticizer-spi`**: 
    - 职责：提供 SPI 接口与核心数据模型。
    - 依赖：无。适用于 SPI 实现模块。
3.  **`site.addzero:method-semanticizer-processor`**: 
    - 职责：KSP 核心执行引擎。
    - 依赖：`api` + `spi`。仅作为编译器插件引入。

---

## 1. 注解用法 (快速定义)

适用于在源码中直接声明常用的变体。

**业务模块配置 (`build.gradle.kts`)：**
```kotlin
dependencies {
    implementation("site.addzero:method-semanticizer-api:最新版本")
    ksp("site.addzero:method-semanticizer-processor:最新版本")
}
```

**代码示例：**
注意 :   @SemanticVariation是一个可重复注解 

```kotlin
interface UserRepository {
    /**
     * 原始方法：save( mode, associatedMode)
     * 特化方法：saveVariation()
     */
    @SemanticVariation(
      //变体(特化)方法名
        name = "saveVariation", 
        args = ["mode=SaveMode.UPSERT", "associatedMode=AssociatedSaveMode.REPLACE"],
        doc = "变体的方法注释"
    )
    @SemanticVariation(
      //变体(特化)方法名
      name = "saveVariation1",
      args = ["mode=SaveMode.UPSERT1", "associatedMode=AssociatedSaveMode.REPLACE1"],
      doc = "变体的方法注释1"
    )
    fun save( mode: SaveMode, associatedMode: AssociatedSaveMode)
    
}
```

---

## 2. SPI 用法 (对照表增强)

适用于需要解耦、从外部文件（CSV/JSON）加载对照表或对三方库（如 Jimmer）进行无侵入增强的场景。

### 业务对照关系示例 (Jimmer 保存模式)

假设有以下对照表,可以将其遍历为List<SemanticMethodDefinition>(方法名随便起的,只是举例说明)：

| SaveMode (根对象) | AssociatedSaveMode (关联对象) | 目标方法名 | 业务语义 |
| :--- | :--- | :--- | :--- |
| `UPSERT` | `REPLACE` | **`sync`** | 全量同步 (刷新 Diff) |
| `INSERT_ONLY` | `APPEND` | **`init`** | 初始化 (仅插入追加) |
| `UPDATE_ONLY` | `UPDATE` | **`patch`** | 局部更新 (仅更新已有) |

### 步骤一：创建并实现 SPI 模块

创建一个独立的 Kotlin 模块（如 `my-jimmer-extension`），依赖 `method-semanticizer-spi`：

```kotlin
dependencies {
    implementation("site.addzero:method-semanticizer-spi:最新版本")
}
```

实现 `SemanticMappingProvider` 接口：

```kotlin
class JimmerSemanticProvider : SemanticMappingProvider {

    /** 声明需要处理的类（支持处理三方库中的类） */
    override fun getSupportedClassNames() = listOf("site.addzero.repo.BaseRepository")

    override fun getMappings(qualifiedName: String): Map<String, List<SemanticMethodDefinition>>? {
        if (qualifiedName != "site.addzero.repo.BaseRepository") return null

        val variations = listOf(
            SemanticMethodDefinition(
                newMethodName = "saveVariation1",
                fixedParameters = mapOf(
                    "mode" to "org.babyfish.jimmer.sql.ast.mutation.SaveMode.UPSERT",
                    "associatedMode" to "org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode.REPLACE"
                ),
                doc = "save变体1"
            ),
                      SemanticMethodDefinition(
                newMethodName = "saveVariation2",
                fixedParameters = mapOf(
                    "mode" to "org.babyfish.jimmer.sql.ast.mutation.SaveMode.NON_IDEMPOTENT_UPSERT",
                    "associatedMode" to "org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode.APPEND_IF_ABSENT"
                ),
                doc = "save变体2"
            )

        )
      //原方法名 to 变体元数据列表
        return mapOf("save" to variations)
    }
}
```

***注册服务(重要!!!)**：在模块的 `src/commonMain/resources/META-INF/services/site.addzero.ksp.metadata.semantic.SemanticMappingProvider` 中填入实现类全限定名。

### 步骤二：在业务模块中注入 SPI

在业务模块的 `build.gradle.kts` 中，将上述 SPI 模块以 `ksp` 方式引入：

```kotlin
dependencies {
    implementation("site.addzero:method-semanticizer-api:最新版本")
    ksp("site.addzero:method-semanticizer-processor:最新版本")
    
    // 【核心步骤】将你的 SPI 增强模块注入到编译器的类路径下
    ksp(project(":my-jimmer-extension"))
}
```

---

## 3. 核心机制说明

- **Raw Code 识别**: `fixedParameters` 的 Value 中如果包含 `.` (点号)，处理器会将其视为代码表达式（如 `SaveMode.UPSERT`）直接写入调用处；否则视为普通字符串（自动加双引号）。
- **泛型镜像**: 支持 `reified` 泛型和 `inline` 函数，生成的扩展函数会自动带上泛型定义并显式透传类型参数。
- **修饰符继承**: 自动保留 `suspend`, `inline`, `infix`, `operator` 等。
- **参数智能剥离**: 生成的方法会自动剔除已绑定的参数，仅保留用户需要传入的参数。

---
更新后的 README.md 后半部分预览：

  4. 生成代码预览 (Generated Code Examples)


  以下展示了在不同配置下，插件自动生成的 Kotlin 扩展函数。


  场景 A：普通方法特化 (Jimmer 风格)
  配置： variation("sync", "mode" to "SaveMode.UPSERT", "associatedMode" to "AssociatedSaveMode.REPLACE")
  生成结果：
```kotlin


/** 全量同步实体及其关联对象 */
public fun <T : Any> UserRepository<T>.sync(entity: T): T {
   // 仅保留了未固定的 entity 参数，其余参数已自动填充
   return save(
       entity = entity,
       mode = org.babyfish.jimmer.sql.ast.mutation.SaveMode.UPSERT,
       associatedMode = org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode.REPLACE
       )
   }

```

  场景 B：挂起函数与 Raw Code (网络请求)
  配置： @SemanticVariation(name = "searchSongs", args = ["type=1"])
  生成结果：

```kotlin


1 /** 搜索歌曲 */
public suspend fun NeteaseApi.searchSongs(
  s: String,
  limit: Int,
  offset: Int
): NeteaseSearchRes {
  // 保持了 suspend 异步特性，并将 type 固定为 1
  return search(
    s = s,
     type = 1,
     limit = limit,
     offset = offset
       )
   }

```

  场景 C：复杂泛型与内联 (高级特化)
  配置： 针对 inline fun <reified T> search(...)
  生成结果：
```kotlin
    /** 搜索并转换为指定类型 */
public inline fun <reified T : Any> MusicClient.searchAs(s: String): List<T> {
  //  生成的方法保留了reified 泛型 T
  return search<T>(
    s = s,
    type = 1
  )
}


```


  ---
