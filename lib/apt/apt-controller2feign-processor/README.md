# Controller2Feign APT Processor

根据Spring Controller自动生成Feign Client接口（Java版本）

## Gradle

```kotlin
dependencies {
    annotationProcessor("site.addzero:apt-controller2feign-processor:版本")
}

// 可选配置
kapt {
    arguments {
        arg("feignOutputPackage", "com.example.feign")
        arg("feignEnabled", "true")
    }
}
```

## Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>site.addzero</groupId>
                        <artifactId>apt-controller2feign-processor</artifactId>
                        <version>版本</version>
                    </path>
                </annotationProcessorPaths>
                <compilerArgs>
                    <arg>-AfeignOutputPackage=com.example.feign</arg>
                    <arg>-AfeignEnabled=true</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## 配置选项

| 选项 | 默认值 | 说明 |
|------|--------|------|
| feignOutputPackage | site.addzero.generated.feign | 生成的FeignClient包名 |
| feignEnabled | true | 是否启用处理器 |

## 示例

输入Controller:
```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) { ... }
    
    @PostMapping
    public User create(@RequestBody UserDTO dto) { ... }
}
```

生成FeignClient:
```java
@FeignClient(name = "user", path = "/api/user")
public interface UserFeignClient {
    @GetMapping("/{id}")
    User getById(@PathVariable("id") Long id);
    
    @PostMapping
    User create(@RequestBody UserDTO dto);
}
```
