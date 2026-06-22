# mybatis-auto-wrapper AI 使用指南

这个包用于把查询 DTO 自动转换成 MyBatis-Plus `QueryWrapper` / `LambdaQueryWrapper`。
AI 生成业务代码时，优先使用这里的工具函数，避免手写大量 `wrapper.eq(...)`、`wrapper.like(...)`、`wrapper.in(...)` 条件。

## 依赖与包名

- 工具包：`site.addzero.mybatis.auto_wrapper`
- 核心注解：`@Where`、`@Wheres`
- 主要入口：`AutoWhereUtil.kt` 中的顶层函数（Java 侧可用 `AutoWhereUtil.xxx(...)` 调用）

常用 import：

```kotlin
import site.addzero.mybatis.auto_wrapper.Where
import site.addzero.mybatis.auto_wrapper.Wheres
import site.addzero.mybatis.auto_wrapper.queryByAnnotation
import site.addzero.mybatis.auto_wrapper.queryByField
import site.addzero.mybatis.auto_wrapper.lambdaQueryByAnnotation
import site.addzero.mybatis.auto_wrapper.lambdaQueryByField
```

## 入口函数怎么选

| 函数 | 返回值 | 字段选择规则 | 推荐场景 |
| --- | --- | --- | --- |
| `queryByAnnotation(Entity::class.java, dto)` | `QueryWrapper<T>` | 只处理带 `@Where` / `@Wheres` 的字段 | DTO 是专门的查询对象，推荐优先用 |
| `lambdaQueryByAnnotation(Entity::class.java, dto)` | `LambdaQueryWrapper<T>` | 只处理带 `@Where` / `@Wheres` 的字段 | 需要 Lambda wrapper 时用 |
| `queryByField(Entity::class.java, dto)` | `QueryWrapper<T>` | 处理 DTO 所有字段；无注解字段按 `=` | 简单全字段等值查询，或兼容旧代码 |
| `lambdaQueryByField(Entity::class.java, dto)` | `LambdaQueryWrapper<T>` | 处理 DTO 所有字段；无注解字段按 `=` | 简单全字段 Lambda 查询 |
| `lambdaQueryByField(Entity::class.java, dto, ignoreId = true)` | `LambdaQueryWrapper<T>` | 同上，但可忽略 DTO 的 `id` 字段 | 根据表单对象查重、排除当前记录 |

AI 默认决策：

1. 查询 DTO 上有明确查询语义时，用 `queryByAnnotation` / `lambdaQueryByAnnotation`。
2. 只是把非空字段全部作为等值条件时，用 `queryByField` / `lambdaQueryByField`。
3. 如果实体 getter 不稳定、字段名和实体不完全一致，优先用 `QueryWrapper` 版本，并通过 `@Where(column = "db_column")` 指定列名。

## 最小示例

```kotlin
data class UserQueryDto(
    @field:Where(value = "like%", column = "username")
    var username: String? = null,

    @field:Where(value = "in", column = "status")
    var statuses: List<Int>? = null,

    @field:Where(value = ">=", column = "created_at")
    var startTime: String? = null,

    @field:Where(value = "<=", column = "created_at")
    var endTime: String? = null
)

val dto = UserQueryDto(
    username = "zhang",
    statuses = listOf(1, 2),
    startTime = "2026-01-01 00:00:00"
)

val wrapper = queryByAnnotation(UserEntity::class.java, dto)
userMapper.selectList(wrapper)
```

生成条件大致等价于：

```kotlin
wrapper
    .likeRight("username", "zhang")
    .`in`("status", listOf(1, 2))
    .ge("created_at", "2026-01-01 00:00:00")
```

## `@Where` 参数

```kotlin
@Where(
    value = "=",
    column = "",
    join = false,
    ignore = false,
    condition = ""
)
```

| 参数 | 含义 |
| --- | --- |
| `value` | 操作符，默认 `=` |
| `column` | 数据库列名；为空时使用字段名，`QueryWrapper` 下会转下划线，`LambdaQueryWrapper` 下会找实体 getter |
| `join` | 多个同列条件内部是否用 `OR` 连接 |
| `ignore` | 是否忽略该条件；可配合 `condition` 动态忽略 |
| `condition` | SpEL 表达式，返回 `true` 才应用条件；当 `ignore = true` 时，返回 `true` 表示忽略条件 |

SpEL 可用变量：

- `#value`：当前字段值
- `#field`：当前 Java `Field`
- `#dto`：整个 DTO 对象

示例：

```kotlin
class DeviceQueryDto {
    @Where(column = "product_key")
    var productKey: String? = null

    // deviceId 为空时查询 IS NULL，有值时忽略这个 IS NULL 条件
    @Where(value = "null", column = "device_id", condition = "#dto.deviceId == null")
    @Where(ignore = true, column = "device_id", condition = "#dto.deviceId != null")
    var deviceId: String? = null
}
```

## 支持的操作符

| `value` | 生成条件 | 默认触发条件 |
| --- | --- | --- |
| `=` | `eq` | `value` 非空/非空串/非空集合 |
| `!=` | `ne` | `value` 非空 |
| `null` | `isNull` | `value == null` |
| `notNull` | `isNotNull` | `value != null` |
| `in` | `in` | 集合/数组/逗号字符串非空 |
| `notIn` | `notIn` | 集合/数组/逗号字符串非空 |
| `findInSet` | `FIND_IN_SET(x, column) > 0` | 集合/数组/逗号字符串非空 |
| `like` | `like` | `value` 非空 |
| `like%` | `likeRight` | `value` 非空 |
| `<` | `lt` | `value` 非空 |
| `<=` | `le` | `value` 非空 |
| `>` | `gt` | `value` 非空 |
| `>=` | `ge` | `value` 非空 |

`in`、`notIn`、`findInSet` 的值可以是：

- `Collection`
- Kotlin/Java 数组
- 基础类型数组，如 `LongArray`、`IntArray`
- 逗号分隔字符串，如 `"1,2, 3"`

## 多条件与分组：`@Wheres`

一个字段上有多个 `@Where` 时，Kotlin 可重复标注，底层会归入 `@Wheres`。
需要显式控制分组时可使用 `@Wheres`：

```kotlin
class UserQueryDto {
    @Wheres(
        Where(value = "=", column = "phone", join = true),
        Where(value = "=", column = "email", join = true),
        group = "keyword",
        outerJoin = false,
        innerJoin = true
    )
    var keyword: String? = null
}
```

语义提示：

- `group`：同组字段会被包到同一个嵌套条件里。
- `outerJoin = false`：组与组之间默认用 `AND`；为 `true` 时用 `OR`。
- `innerJoin = true`：组内不同列之间用 `OR`；否则默认 `AND`。
- 单个 `@Where(join = true)`：同一列多个条件之间用 `OR`。

## 字段名、列名和 `@TableField`

- `queryByField` / `queryByAnnotation` 返回 `QueryWrapper`：列名最终是字符串；未显式 `column` 时使用字段名并转下划线。
- `lambdaQueryByField` / `lambdaQueryByAnnotation` 返回 `LambdaQueryWrapper`：会根据列/字段名在实体类上找 getter 并创建 `SFunction`，所以实体必须有符合 Java Bean 规范的 getter。
- 无 `@Where` 的字段在 `queryByField` / `lambdaQueryByField` 中按 `=` 处理。
- 无 `@Where` 字段如果标了 MyBatis-Plus `@TableField("xxx")`，会使用 `@TableField.value` 作为列名；`@TableField(exist = false)` 会跳过。
- `queryByAnnotation` / `lambdaQueryByAnnotation` 只处理 `@Where` / `@Wheres` 字段，不会处理未标注字段。

## AI 生成代码规则

生成新查询 DTO 时请遵守：

1. DTO 查询字段优先显式写 `@field:Where(column = "真实数据库列名")`，避免字段名和列名不一致。
2. 时间范围字段用两个不同 DTO 字段映射同一列：`startTime -> >=`，`endTime -> <=`。
3. 模糊前缀搜索用 `like%`；双边模糊搜索用 `like`。
4. 多选筛选用 `in`，排除多选用 `notIn`。
5. MySQL 逗号字段才使用 `findInSet`，普通关系表不要用它。
6. 需要动态启停条件时，用 `condition`，不要在 service 里手写大量 `if`。
7. 只想忽略某个条件时，用 `@Where(ignore = true, condition = "...")`；`ignore=true` 且无 condition 会永远跳过。
8. 默认不要把空字符串、空集合生成查询条件；当前实现会自动跳过大多数空值。
9. 需要只处理显式查询字段时，必须用 `queryByAnnotation` / `lambdaQueryByAnnotation`，不要用 `queryByField`。
10. 需要排除 id 查重时，用 `lambdaQueryByField(Entity::class.java, dto, ignoreId = true)`。

## 常见模板

### 列表查询 DTO

```kotlin
data class OrderQueryDto(
    @field:Where(value = "like%", column = "order_no")
    var orderNo: String? = null,

    @field:Where(value = "in", column = "status")
    var statuses: Collection<Int>? = null,

    @field:Where(value = ">=", column = "create_time")
    var beginTime: String? = null,

    @field:Where(value = "<=", column = "create_time")
    var endTime: String? = null
)

val wrapper = queryByAnnotation(OrderEntity::class.java, dto)
```

### 简单等值查询

```kotlin
data class UserUniqueDto(
    var tenantId: Long? = null,
    var username: String? = null
)

val wrapper = lambdaQueryByField(UserEntity::class.java, dto, ignoreId = true)
```

### 动态条件

```kotlin
data class UserQueryDto(
    @field:Where(value = "=", column = "nickname", condition = "#value != null && #value.startsWith('test')")
    var nickname: String? = null,

    @field:Where(value = "null", column = "deleted_at", condition = "#dto.onlyDeleted == true")
    var deletedAt: String? = null,

    var onlyDeleted: Boolean = false
)
```

## 注意事项

- 这是 wrapper 构建工具，不负责分页；分页按项目 MyBatis-Plus 习惯另外传 `Page`。
- `findInSet` 使用 SQL 片段，适合 MySQL 逗号分隔字段；跨数据库时要谨慎。
- `condition` 是 SpEL，表达式写错时当前实现会打印异常，并回退到非空判断。
- `LambdaQueryWrapper` 版本依赖实体 getter；如果报 “getXxx 方法没有找到”，改用 `QueryWrapper` 或修正实体字段/getter/`column`。
- DTO 字段如果是 Kotlin 注解目标，推荐写 `@field:Where`，确保注解落在字段上。
