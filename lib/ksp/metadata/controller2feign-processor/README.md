# Controller2Feign KSP Processor

根据Spring Controller自动生成Feign Client接口（Kotlin版本）

## Gradle

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

dependencies {
    ksp("site.addzero:controller2feign-processor:版本")
}

// 可选配置
ksp {
    arg("feignOutputPackage", "com.example.feign")
    arg("feignOutputDir", "$projectDir/src/main/kotlin")
    arg("feignEnabled", "true")
}
```

## Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-plugin</artifactId>
            <version>${kotlin.version}</version>
            <executions>
                <execution>
                    <id>compile</id>
                    <phase>compile</phase>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <compilerPlugins>
                    <plugin>ksp</plugin>
                </compilerPlugins>
                <pluginOptions>
                    <option>ksp:apoption=feignOutputPackage=com.example.feign</option>
                    <option>ksp:apoption=feignEnabled=true</option>
                </pluginOptions>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>com.google.devtools.ksp</groupId>
                    <artifactId>symbol-processing-api</artifactId>
                    <version>${ksp.version}</version>
                </dependency>
                <dependency>
                    <groupId>site.addzero</groupId>
                    <artifactId>controller2feign-processor</artifactId>
                    <version>版本</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

## 配置选项

| 选项 | 默认值 | 说明 |
|------|--------|------|
| feignOutputPackage | site.addzero.generated.feign | 生成的FeignClient包名 |
| feignOutputDir | null | 自定义输出目录（默认KSP生成目录） |
| feignEnabled | true | 是否启用处理器 |

## 示例

输入Controller:
```kotlin
@RestController
@RequestMapping("/api/user")
class UserController {
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): User { ... }
    
    @PostMapping
    fun create(@RequestBody dto: UserDTO): User { ... }
}
```

生成FeignClient:
```kotlin
@FeignClient(name = "user", path = "/api/user")
interface UserFeignClient {
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long): User
    
    @PostMapping
    fun create(@RequestBody dto: UserDTO): User
}
```
