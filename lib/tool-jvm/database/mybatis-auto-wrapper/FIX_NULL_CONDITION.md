# 修复 @Where(value="null") 和 @Where(value="notNull") 条件逻辑

## 问题描述

之前的实现中，`@Where(value="null")` 和 `@Where(value="notNull")` 的条件判断逻辑有误：

### 原有问题：
- `email = null` 时 → **没有生成任何条件**（期望：生成 `IS NULL`）
- `email = "111"` 时 → **既生成了 `IS NULL` 又生成了 `=`**（期望：只生成 `=` 或不生成条件）

### 期望行为：
- `@Where(value="null")` + `email = null` → 生成 `WHERE email IS NULL`
- `@Where(value="null")` + `email = "111"` → 不生成条件
- `@Where(value="=")` + `email = null` → 不生成条件
- `@Where(value="=")` + `email = "111"` → 生成 `WHERE email = '111'`

## 修复方案

### 修改文件：`PackField.java`

在 `ColumnInfo` 类的 `getCondition()` 方法中，针对 `"null"` 和 `"notNull"` 操作符使用相反的逻辑：

```java
@Override
public boolean getCondition() {
    // 对于 null 和 notNull 操作符，逻辑相反
    if ("null".equals(symbol)) {
        // null 操作符：当值为 null 时才生成 IS NULL 条件
        return value == null;
    }
    if ("notNull".equals(symbol)) {
        // notNull 操作符：当值不为 null 时才生成 IS NOT NULL 条件
        if (value instanceof String) {
            return StringUtils.isNotBlank((String) value);
        }
        if (value instanceof Collection) {
            return CollectionUtils.isNotEmpty((Collection<?>) value);
        }
        return value != null;
    }

    // 其他操作符：当值不为空时才生成条件
    if (value instanceof String) {
        return StringUtils.isNotBlank((String) value);
    }
    if (value instanceof Collection) {
        return CollectionUtils.isNotEmpty((Collection<?>) value);
    }
    return value != null;
}
```

### 核心逻辑：

1. **`@Where(value="null")`**：
   - `value == null` → `getCondition()` 返回 `true` → 生成 `IS NULL` 条件
   - `value != null` → `getCondition()` 返回 `false` → 不生成条件

2. **`@Where(value="notNull")`**：
   - `value == null` → `getCondition()` 返回 `false` → 不生成条件
   - `value != null` → `getCondition()` 返回 `true` → 生成 `IS NOT NULL` 条件

3. **其他操作符（如 `=`, `>`, `like` 等）**：
   - `value == null` 或空 → `getCondition()` 返回 `false` → 不生成条件
   - `value != null` 且非空 → `getCondition()` 返回 `true` → 生成对应条件

## 测试用例

创建了 7 个测试用例来验证修复：

1. ✅ `testNullCondition_whenValueIsNull_shouldGenerateIsNull` - null值生成IS NULL
2. ✅ `testNullCondition_whenValueIsNotNull_shouldNotGenerateCondition` - 非null值不生成条件
3. ✅ `testEqualCondition_whenValueIsNull_shouldNotGenerateCondition` - 等于条件null值不生成
4. ✅ `testEqualCondition_whenValueIsNotNull_shouldGenerateEqual` - 等于条件非null值生成=
5. ✅ `testNotNullCondition_whenValueIsNull_shouldNotGenerateCondition` - notNull条件null值不生成
6. ✅ `testNotNullCondition_whenValueIsNotNull_shouldGenerateIsNotNull` - notNull条件非null值生成IS NOT NULL
7. ✅ `testCombinedConditions` - 组合条件测试

## 使用示例

```java
// 示例 1：查询 email 为 null 的记录
class QueryDTO {
    @Where(value = "null")
    private String email;
}

QueryDTO dto = new QueryDTO();
dto.setEmail(null);  // 生成: WHERE email IS NULL

// 示例 2：查询 email 等于某值的记录
class QueryDTO2 {
    @Where(value = "=")
    private String email;
}

QueryDTO2 dto2 = new QueryDTO2();
dto2.setEmail("test@example.com");  // 生成: WHERE email = 'test@example.com'

// 示例 3：查询 status 不为 null 的记录
class QueryDTO3 {
    @Where(value = "notNull")
    private String status;
}

QueryDTO3 dto3 = new QueryDTO3();
dto3.setStatus("active");  // 生成: WHERE status IS NOT NULL
```

## 运行测试

在 IntelliJ IDEA 中：
1. 打开 `AutoWhereUtilTest.java`
2. 右键点击类名
3. 选择 "Run 'AutoWhereUtilTest'"

所有测试应该通过，验证修复成功。
