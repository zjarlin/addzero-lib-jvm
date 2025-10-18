# Maven Repository Client

一个用于查询Maven中央仓库的Kotlin/Java工具类，提供简单易用的API来搜索Maven构件。

## 功能特性

- 🔍 **多种搜索方式**：支持按groupId、artifactId、关键字等搜索
- 🎯 **通配符支持**：支持使用*进行模糊匹配
- 📝 **完整的API**：提供丰富的查询方法
- 🚀 **高性能**：基于OkHttp的异步HTTP客户端
- 🔄 **Java兼容**：支持Java和Kotlin调用
- 📊 **详细信息**：返回版本号、时间戳等详细信息

## 添加依赖

```kotlin
dependencies {
    implementation("site.addzero:tool-api-maven")
}
```

## 基本用法

### 1. 获取指定组的所有模块

```kotlin
// 获取hutool组的所有模块
val modules = MavenRepositoryClient.getGroupArtifacts("cn.hutool")
modules.forEach { module ->
    println("cn.hutool:$module")
}
```

### 2. 按模式过滤模块

```kotlin
// 获取hutool组中以"hutool-"开头的模块
val coreModules = MavenRepositoryClient.getGroupArtifactsWithPattern("cn.hutool", "hutool-*")
```

### 3. 按groupId搜索

```kotlin
val artifacts = MavenRepositoryClient.searchByGroupId("org.springframework", rows = 10)
artifacts.forEach { artifact ->
    println("Found: ${artifact.getCoordinateWithVersion()}")
}
```

### 4. 按artifactId搜索

```kotlin
val artifacts = MavenRepositoryClient.searchByArtifactId("spring-boot-starter", rows = 5)
```

### 5. 精确搜索

```kotlin
val artifacts = MavenRepositoryClient.searchByGroupIdAndArtifactId(
    "cn.hutool",
    "hutool-core"
)
```

### 6. 关键字搜索

```kotlin
val artifacts = MavenRepositoryClient.searchByKeyword("json", rows = 10)
```

## 原始curl命令对比

**原始curl命令：**
```bash
curl -s "https://search.maven.org/solrsearch/select?q=g:cn.hutool+AND+a:hutool-*&rows=50&wt=json" | grep -o '"id":"cn.hutool:[^"]*"'
```

**使用MavenRepositoryClient：**
```kotlin
val modules = MavenRepositoryClient.getGroupArtifactsWithPattern("cn.hutool", "hutool-*")
// 直接返回List<String>，无需解析JSON
```

## Java调用示例

```java
import site.addzero.maven.MavenRepositoryClient;
import site.addzero.maven.model.MavenArtifact;

public class MavenExample {
    public static void main(String[] args) {
        try {
            // 获取hutool模块
            List<String> modules = MavenRepositoryClient.getGroupArtifacts("cn.hutool");
            modules.forEach(System.out::println);

            // 搜索构件
            List<MavenArtifact> artifacts = MavenRepositoryClient.searchByArtifactId("hutool-core", 10);
            artifacts.forEach(artifact -> {
                System.out.println("Found: " + artifact.getCoordinateWithVersion());
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 清理资源
            MavenRepositoryClient.shutdown();
        }
    }
}
```

## API参考

### MavenRepositoryClient

| 方法 | 描述 |
|------|------|
| `getGroupArtifacts(groupId)` | 获取指定组的所有构件ID |
| `getGroupArtifactsWithPattern(groupId, pattern)` | 按模式获取构件ID |
| `searchByGroupId(groupId, rows, start)` | 按groupId搜索 |
| `searchByArtifactId(artifactId, rows, start)` | 按artifactId搜索 |
| `searchByGroupIdAndArtifactId(groupId, artifactId, rows, start)` | 精确搜索 |
| `searchByPattern(groupId, pattern, rows, start)` | 按模式搜索 |
| `searchByKeyword(keyword, rows, start)` | 关键字搜索 |
| `shutdown()` | 关闭客户端，释放资源 |

### MavenArtifact

| 属性 | 描述 |
|------|------|
| `id` | 完整构件ID (groupId:artifactId) |
| `groupId` | 组ID |
| `artifactId` | 构件ID |
| `latestVersion` | 最新版本 |
| `versionCount` | 版本数量 |
| `timestamp` | 时间戳 |

## 异常处理

```kotlin
try {
    val artifacts = MavenRepositoryClient.searchByGroupId("cn.hutool")
} catch (e: MavenRepositoryConnectionException) {
    // 网络连接异常
    println("连接失败: ${e.message}")
} catch (e: MavenRepositoryParseException) {
    // 解析异常
    println("解析失败: ${e.message}")
} catch (e: MavenRepositoryException) {
    // 其他异常
    println("操作失败: ${e.message}")
}
```

## 注意事项

1. **资源管理**：使用完毕后调用`shutdown()`方法释放资源
2. **网络超时**：默认超时时间为30秒，可在OkHttpClient配置中调整
3. **结果限制**：默认返回50条结果，可通过`rows`参数调整
4. **分页查询**：可通过`start`参数实现分页查询

## 性能优化

- 使用连接池管理HTTP连接
- 支持分页查询，避免一次返回过多结果
- 异步HTTP请求，提高并发性能
- JSON解析使用Jackson，性能优异