# dict-trans-spring-boot-starter

基于 Spring Boot AOP 的**字典翻译**自动翻译组件。在 Controller 方法返回值时，自动扫描对象上标注的 `@Dict` 注解，将字典编码（code）翻译为可读的文本（label/name），支持内置字典表和任意数据库表两种翻译数据源。

## 特性

- **零侵入**：基于 AOP 拦截，只需在 VO 字段上加 `@Dict` 注解，Controller 方法上加 `@Dict` 即可自动翻译
- **内置字典翻译**：通过 `dictCode` 查询系统内置字典表，code → label
- **任意表翻译**：指定表名、code 列、name 列，直接从任意数据库表翻译
- **多值翻译**：字段值支持逗号分隔的多 code 翻译（如 `"1,2,3"` → `"启用,禁用,删除"`）
- **嵌套对象递归翻译**：自动递归遍历嵌套对象和集合中的 `@Dict` 字段
- **ByteBuddy 动态子类**：翻译后的字段通过 ByteBuddy 动态生成子类注入，不修改原始 VO 结构
- **策略模式**：根据返回值类型（单个对象 / Collection / String）自动选择翻译策略
- **Spring Boot AutoConfiguration**：引入依赖即自动生效

## 快速开始

### 1. 引入依赖

```kotlin
// build.gradle.kts
dependencies {
    implementation("site.addzero:dict-trans-spring-boot-starter:<version>")
}
```

### 2. 实现 TransApi 接口

提供字典数据源查询能力，组件通过此接口获取翻译数据：

```kotlin
@Component
class MyTransApi : TransApi {

    @Autowired
    private lateinit var dictService: DictService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    /**
     * 内置字典批量翻译
     * @param dictCodes 逗号分隔的字典编码，如 "user_status,sex"
     * @param keys      逗号分隔的字典项 value，如 "1,2,0"
     */
    override fun translateDictBatchCode2name(dictCodes: String, keys: String?): List<DictModel> {
        // 从你的字典表中查询，返回 DictModel(dictCode, value, label)
        return dictService.findDictItems(dictCodes, keys)
    }

    /**
     * 任意表批量翻译
     * @param table 表名
     * @param text  显示列名（name 列）
     * @param code  编码列名
     * @param keys  逗号分隔的编码值
     */
    override fun translateTableBatchCode2name(
        table: String, text: String, code: String, keys: String
    ): List<Map<String, Any?>> {
        val keyList = keys.split(",").map { "'$it'" }.joinToString(",")
        val sql = "SELECT $code, $text FROM $table WHERE $code IN ($keyList)"
        return jdbcTemplate.queryForList(sql)
    }
}
```

### 3. 配置 Controller 扫描包（可选）

默认扫描 `site.addzero` 包下的 Controller，可通过配置修改：

```yaml
# application.yml
expression:
  scan:
    controller:
      pkg: com.example          # 你的 Controller 所在包
      # 也可以自定义完整表达式，支持 ${pkg} 占位符
      # expression: "execution(* ${pkg}..*Controller*+.*(..))"
```

### 4. 在 VO 上使用 @Dict 注解

`@Dict` 注解支持 `@Repeatable`，可以在同一字段上标注多次翻译。

#### 注解参数说明

| 参数               | 说明                                                  | 默认值 |
|------------------|-----------------------------------------------------|-----|
| `value`          | 内置字典编码（等同于 `dicCode`）                              | `""` |
| `dicCode`        | 内置字典编码（与 `value` 二选一，优先使用 `dicCode`）               | `""` |
| `tab`            | 任意表翻译 - 表名                                         | `""` |
| `codeColumn`     | 任意表翻译 - 编码列名（默认取字段名）                              | `""` |
| `nameColumn`     | 任意表翻译 - 显示列名                                       | `""` |
| `whereCondition` | 附加 WHERE 条件（预留）                                    | `""` |
| `serializationAlias` | 翻译后字段的序列化别名（优先作为输出字段名）                          | `""` |

#### 翻译模式判定规则

- 当 `dicCode`/`value` 非空，且 `tab`、`codeColumn`、`nameColumn` 全为空 → **内置字典翻译**
- 当指定了 `tab` 等参数 → **任意表翻译**

## 使用示例

### 示例 1：内置字典翻译

```kotlin
data class UserVO(
    val id: Long,
    val name: String,

    // 使用内置字典 "user_status" 翻译，字段值 "1" → "启用"
    @Dict(dicCode = "user_status")
    val status: String,

    // 多值翻译：字段值 "1,2" → "男,女"
    @Dict(value = "sex")
    val gender: String,
)
```

### 示例 2：任意表翻译

```kotlin
data class OrderVO(
    val id: Long,

    // 从 department 表翻译，department_id 列匹配，显示 department_name 列
    @Dict(tab = "department", codeColumn = "department_id", nameColumn = "department_name")
    val deptId: String,
)
```

### 示例 3：同一字段多次翻译

```kotlin
data class ReportVO(
    val id: Long,

    // 同一个字段可以同时标注内置字典和任意表翻译
    @Dict(dicCode = "region_code")
    @Dict(tab = "sys_region", codeColumn = "region_id", nameColumn = "region_name")
    val regionId: String,
)
```

### 示例 4：Controller 方法标注 @Dict

在 Controller 方法上加 `@Dict` 注解即可触发 AOP 翻译：

```kotlin
@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping("/list")
    @Dict  // 触发返回值的字典翻译
    fun list(): List<UserVO> {
        return userService.findUsers()
    }

    @GetMapping("/{id}")
    @Dict  // 单个对象也会被翻译
    fun getById(@PathVariable id: Long): UserVO {
        return userService.findById(id)
    }
}
```

### 示例 5：手动调用翻译工具函数

不依赖 AOP 时，也可以在 Service 层手动翻译：

```kotlin
import site.addzero.aop.dicttrans.util.code2name
import site.addzero.aop.dicttrans.util.code2nameT
import site.addzero.aop.dicttrans.util.name2code
import site.addzero.aop.dicttrans.util.name2codeT

// 批量翻译 List
val users: MutableList<UserVO> = userService.findUsers()
code2name(users)  // 原地翻译

// 单个对象翻译
val user: UserVO = userService.findById(1)
val translated = code2nameT(user)

// 反向翻译：name → code
name2code(users)
name2codeT(user)
```

## 自定义翻译策略

组件内置三种策略，按优先级匹配：

| 策略               | 匹配类型            | 说明                    |
|------------------|-----------------|-----------------------|
| `StringStrategy` | `String`        | 直接对字符串值做字典翻译        |
| `CollectionStrategy` | `Collection<*>` | 遍历集合内每个元素，批量翻译     |
| `TStrategy`      | 任意 POJO 对象       | 将单个对象包装为 List 后委托集合策略 |

如需扩展，可以实现 `TransStrategy<T>` 接口并注册为 Spring Bean：

```kotlin
@Component
class MyCustomStrategy : TransStrategy<MySpecialType> {
    override fun trans(t: MySpecialType): MySpecialType {
        // 自定义翻译逻辑
        return t
    }
}
```

## 自定义类型黑名单

`TPredicate` 接口用于定义哪些类型不应被当作 POJO 递归翻译。默认实现 `DefaultTPredicate` 已排除常见基本类型。如需自定义：

```kotlin
@Component
class MyTPredicate : TPredicate {
    override fun tBlackList(): List<Class<out Any>> {
        return listOf(
            String::class.java,
            Number::class.java,
            // ... 添加你不需要翻译的类型
            MyImmutableValueObject::class.java,
        )
    }
}
```

> 使用 `@ConditionalOnMissingBean`，自定义实现会自动替换默认实现。

## 翻译后字段命名规则

翻译后的字段写入到动态生成的子类中，字段名按以下优先级确定：

1. `serializationAlias`（注解参数，最高优先级）
2. `nameColumn` 的驼峰形式
3. `{原始字段名}_dictText`（默认后缀）

## 架构概览

```
DictAopConfiguration (AutoConfiguration)
├── DictAdvisor (AOP 拦截器)
│   └── MethodInterceptor → 拦截 @Dict 标注的 Controller 方法
├── TransStrategySelector (策略选择器)
│   ├── StringStrategy   → String 类型翻译
│   ├── CollectionStrategy → Collection 类型翻译
│   └── TStrategy        → POJO 对象翻译
├── TransApi (SPI 接口，用户实现)
│   ├── translateDictBatchCode2name()   → 内置字典查询
│   └── translateTableBatchCode2name()  → 任意表查询
└── TPredicate (类型黑名单，可自定义)
    └── DefaultTPredicate (默认实现)
```

## 注意事项

- `@Dict` 注解的 `Retention` 为 `SOURCE`，翻译后的字段由 ByteBuddy 在运行时动态生成
- 集合超过 1000 条时会跳过翻译并打印警告日志
- AOP 切面的切点表达式由 `ScanControllerProperties` 配置，确保 Controller 包路径正确
- 翻译过程为原地修改（通过 ByteBuddy 子类注入新字段），原始 VO 类不会被修改
