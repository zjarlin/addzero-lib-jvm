# AutoWhereUtil 单元测试说明

## 测试文件
- `AutoWhereUtilTest.java` - AutoWhereUtil工具类的完整单元测试

## 测试覆盖范围

### 1. lambdaQueryByAnnotation 方法测试
- ✅ 空DTO处理
- ✅ 空值DTO处理
- ✅ 等于条件 (`=`)
- ✅ 大于等于条件 (`>=`)
- ✅ LIKE条件
- ✅ 多个条件组合
- ✅ IN条件
- ✅ 空集合处理
- ✅ NULL条件
- ✅ NOT NULL条件
- ✅ @Wheres多条件注解

### 2. lambdaQueryByField 方法测试
- ✅ 空DTO处理
- ✅ 字段匹配
- ✅ 忽略ID字段 (ignoreId=true)
- ✅ 不忽略ID字段 (ignoreId=false)
- ✅ 空字符串字段处理
- ✅ null字段处理

### 3. queryByAnnotation 方法测试
- ✅ 空DTO处理
- ✅ 带条件的查询
- ✅ 列名映射（驼峰转下划线）

### 4. queryByField 方法测试
- ✅ 空DTO处理
- ✅ 字段匹配
- ✅ 列名映射

## 测试实体类

### User (实体类)
```java
- Long id
- String name
- Integer age
- String email
- String status
```

### UserQueryDTO (使用@Where注解)
```java
@Where(value = "=") String name
@Where(value = ">=") Integer age
@Where(value = "like") String email
```

### UserFieldDTO (字段匹配)
```java
Long id
String name
Integer age
```

### UserMultiWhereDTO (多条件注解)
```java
@Wheres(value = {
    @Where(value = ">=", column = "age"),
    @Where(value = "<=", column = "age")
})
Integer ageRange
```

### UserInQueryDTO (IN查询)
```java
@Where(value = "in") List<String> name
```

### UserNullDTO (空值处理)
```java
@Where(value = "null") String email
@Where(value = "notNull") String status
```

## 运行测试

由于项目存在Gradle配置问题，建议在IDE中直接运行测试：

1. 在IntelliJ IDEA中打开 `AutoWhereUtilTest.java`
2. 右键点击类名或方法名
3. 选择 "Run 'AutoWhereUtilTest'" 或 "Run '测试方法名'"

或者在Gradle配置问题解决后，使用命令行：
```bash
./gradlew :lib:tool-jvm:database:mybatis-auto-wrapper:test
```

## 测试要点

1. **空值处理**: 测试确保null DTO、空字符串、空集合都能正确处理
2. **条件类型**: 覆盖了 =, !=, >, >=, <, <=, like, in, null, notNull 等常用条件
3. **注解支持**: 测试了 @Where 和 @Wheres 两种注解方式
4. **字段匹配**: 测试了基于字段名自动匹配的功能
5. **ID处理**: 测试了ignoreId参数的正确性
6. **Lambda vs Query**: 同时测试了LambdaQueryWrapper和QueryWrapper两种方式

## 注意事项

- 所有测试都使用JUnit 5 (jupiter)
- 测试不依赖数据库，只验证SQL片段生成的正确性
- 测试覆盖了AutoWhereUtil的所有公共方法
