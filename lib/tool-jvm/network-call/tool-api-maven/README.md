# Maven Central Search Util

基于 Maven Central REST API 的搜索工具类，使用 `CurlExecutor` 执行 curl 命令进行 API 调用。

## 特性

- ✅ 支持所有 Maven Central REST API 搜索功能
- ✅ 使用 curl 命令模板（提取到 `MavenCentralApiTemplates`），易于调试和在 Postman 中测试
- ✅ 自动打印 curl 命令和响应状态
- ✅ 完整的类型安全响应解析
- ✅ 分离的测试：模糊搜索测试和精确搜索测试

## 使用示例

### 基本搜索

```kotlin
import site.addzero.network.call.maven.util.MavenCentralSearchUtil

// 按 groupId 搜索
val artifacts = MavenCentralSearchUtil.searchByGroupId("com.google.inject", 5)
artifacts.forEach { 
    println("${it.groupId}:${it.artifactId}:${it.latestVersion}")
}

// 按精确坐标搜索
val guice = MavenCentralSearchUtil.searchByCoordinates("com.google.inject", "guice")
println("最新版本: ${guice.firstOrNull()?.latestVersion}")

// 获取最新版本
val latestVersion = MavenCentralSearchUtil.getLatestVersion("com.google.inject", "guice")
println("Guice 最新版本: $latestVersion")
```

### 高级搜索

```kotlin
// 搜索所有版本
val allVersions = MavenCentralSearchUtil.searchAllVersions("com.google.inject", "guice", 10)
allVersions.forEach {
    println("版本: ${it.version}")
}

// 按类名搜索
val junitArtifacts = MavenCentralSearchUtil.searchByClassName("JUnit", 5)

// 按完全限定类名搜索
val springBootArtifacts = MavenCentralSearchUtil.searchByFullyQualifiedClassName(
    "org.springframework.boot.SpringApplication", 5
)

// 按 SHA-1 搜索
val artifact = MavenCentralSearchUtil.searchBySha1("35379fb6526fd019f331542b4e9ae2e566c57933")

// 按标签搜索（如 SBT 插件）
val sbtPlugins = MavenCentralSearchUtil.searchByTag("sbtplugin", 10)
```

### Curl 命令模板

所有 curl 命令模板都定义在 `MavenCentralApiTemplates` 对象中，使用 `{{占位符}}` 表示变量：

```kotlin
// 查看模板常量
import site.addzero.network.call.maven.util.MavenCentralApiTemplates

println(MavenCentralApiTemplates.CURL_SEARCH_BY_GROUP_TEMPLATE)
// 输出:
// curl -X GET \
//   -H "Accept: application/json" \
//   "https://search.maven.org/solrsearch/select?q=g:{{groupId}}&rows={{rows}}&wt=json"
```

curl 命令会自动打印到控制台，方便调试。

### 生成 curl 命令

```kotlin
// 生成 curl 命令用于手动测试
val curlCommand = MavenCentralSearchUtil.generateCurlCommand("g:com.google.inject AND a:guice", 20)
println(curlCommand)
// 输出:
// curl -X GET \
//   -H "Accept: application/json" \
//   "https://search.maven.org/solrsearch/select?q=g:com.google.inject+AND+a:guice&rows=20&wt=json"
```

### 下载文件

```kotlin
// 下载 POM 文件
val pomBytes = MavenCentralSearchUtil.downloadFile(
    groupId = "com.google.inject",
    artifactId = "guice",
    version = "7.0.0",
    filename = "guice-7.0.0.pom"
)

if (pomBytes != null) {
    File("guice-7.0.0.pom").writeBytes(pomBytes)
}
```

## 测试

项目包含两个测试文件：

### 1. 精确搜索测试 (`MavenCentralExactSearchTest`)
- 按 groupId 精确搜索
- 按坐标（groupId + artifactId）精确搜索
- 搜索所有版本
- 获取最新版本
- 按完整坐标搜索（含分类器）
- 文件下载
- Curl 命令生成

### 2. 模糊搜索测试 (`MavenCentralFuzzySearchTest`)
- 关键词搜索
- 按类名搜索
- 按完全限定类名搜索
- 按标签搜索
- 按 artifactId 搜索（跨组）
- 按 SHA-1 搜索

每个测试都会：
- 打印详细的搜索过程
- 显示找到的结果
- 进行必要的断言验证

## 日志输出示例

每次 API 调用都会自动打印 curl 命令和响应状态：

```
=== Executing Curl Command ===

    curl -X GET \
  -H "Accept: application/json" \
      "https://search.maven.org/solrsearch/select?q=jackson&rows=5&wt=json"

==============================
Response Status: 200
Response Headers: content-type: application/json
...

========== 测试按坐标搜索: com.google.inject:guice ==========
找到 1 个结果:
  - com.google.inject:guice:7.0.0

Guice 最新版本: 7.0.0
```

这使得调试变得非常简单，你可以直接复制 curl 命令到 Postman 或终端进行测试！

## 支持的搜索类型

| 方法 | 说明 | 示例 |
|------|------|------|
| `searchByGroupId` | 按组 ID 搜索 | `searchByGroupId("com.google.inject")` |
| `searchByArtifactId` | 按工件 ID 搜索 | `searchByArtifactId("guice")` |
| `searchByCoordinates` | 按精确坐标搜索 | `searchByCoordinates("com.google.inject", "guice")` |
| `searchAllVersions` | 搜索所有版本 | `searchAllVersions("com.google.inject", "guice")` |
| `searchByFullCoordinates` | 按完整坐标搜索（含分类器） | `searchByFullCoordinates("...", "...", version="3.0", classifier="javadoc")` |
| `searchByClassName` | 按类名搜索 | `searchByClassName("JUnit")` |
| `searchByFullyQualifiedClassName` | 按完全限定类名搜索 | `searchByFullyQualifiedClassName("org.junit.Test")` |
| `searchBySha1` | 按 SHA-1 校验和搜索 | `searchBySha1("35379fb65...")` |
| `searchByTag` | 按标签搜索 | `searchByTag("sbtplugin")` |
| `searchByKeyword` | 关键词搜索 | `searchByKeyword("jackson")` |
| `getLatestVersion` | 获取最新版本 | `getLatestVersion("com.google.inject", "guice")` |
| `downloadFile` | 下载文件 | `downloadFile("...", "...", "1.0.0", "artifact-1.0.0.jar")` |

## API 参考

详细 API 文档请参考: https://central.sonatype.org/search/rest-api-guide/
