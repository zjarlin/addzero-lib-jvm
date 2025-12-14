# 使用示例

## 完整项目配置示例

假设你有一个 Spring Boot 项目,需要从数据库字典表生成枚举类。

### 1. 项目结构

```
my-project/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── MyApplication.java
│       └── resources/
│           └── application.yml
└── build.gradle.kts
```

### 2. Gradle 配置 (build.gradle.kts)

```kotlin
plugins {
    id("java")
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.example"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/public") }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // 添加 APT 处理器依赖
    annotationProcessor("site.addzero:apt:2025.11.27")
    
    // 数据库驱动 (根据实际情况选择)
    implementation("org.postgresql:postgresql:42.7.2")
    // 或 MySQL
    // implementation("mysql:mysql-connector-java:8.0.33")
    
    // 其他依赖...
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// 配置 APT 编译选项
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        // 数据库连接配置 - 开发环境
        "-AjdbcDriver=org.postgresql.Driver",
        "-AjdbcUrl=jdbc:postgresql://localhost:5432/my_database",
        "-AjdbcUsername=postgres",
        "-AjdbcPassword=postgres",
        
        // 字典表配置
        "-AdictTableName=sys_dict",
        "-AdictIdColumn=id",
        "-AdictCodeColumn=dict_code",
        "-AdictNameColumn=dict_name",
        
        // 字典项表配置
        "-AdictItemTableName=sys_dict_item",
        "-AdictItemForeignKeyColumn=dict_id",
        "-AdictItemCodeColumn=item_value",
        "-AdictItemNameColumn=item_text",
        
        // 生成的枚举类包名
        "-AenumOutputPackage=com.example.generated.enums"
    ))
}
```

### 3. Maven 配置 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>my-project</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <apt.version>2025.11.27</apt.version>
        
        <!-- APT 处理器配置参数 -->
        <jdbc.driver>org.postgresql.Driver</jdbc.driver>
        <jdbc.url>jdbc:postgresql://localhost:5432/my_database</jdbc.url>
        <jdbc.username>postgres</jdbc.username>
        <jdbc.password>postgres</jdbc.password>
        <dict.table.name>sys_dict</dict.table.name>
        <dict.id.column>id</dict.id.column>
        <dict.code.column>dict_code</dict.code.column>
        <dict.name.column>dict_name</dict.name.column>
        <dict.item.table.name>sys_dict_item</dict.item.table.name>
        <dict.item.foreign.key.column>dict_id</dict.item.foreign.key.column>
        <dict.item.code.column>item_value</dict.item.code.column>
        <dict.item.name.column>item_text</dict.item.name.column>
        <enum.output.package>com.example.generated.enums</enum.output.package>
    </properties>
    
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- APT 处理器 -->
        <dependency>
            <groupId>site.addzero</groupId>
            <artifactId>apt</artifactId>
            <version>${apt.version}</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- 数据库驱动 -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.2</version>
        </dependency>
        <!-- 或 MySQL
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>
        -->
        
        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <encoding>${project.build.sourceEncoding}</encoding>
                    <annotationProcessorPaths>
                        <!-- 配置 APT 处理器 -->
                        <path>
                            <groupId>site.addzero</groupId>
                            <artifactId>apt</artifactId>
                            <version>${apt.version}</version>
                        </path>
                    </annotationProcessorPaths>
                    <compilerArgs>
                        <!-- 数据库连接配置 -->
                        <arg>-AjdbcDriver=${jdbc.driver}</arg>
                        <arg>-AjdbcUrl=${jdbc.url}</arg>
                        <arg>-AjdbcUsername=${jdbc.username}</arg>
                        <arg>-AjdbcPassword=${jdbc.password}</arg>
                        
                        <!-- 字典表配置 -->
                        <arg>-AdictTableName=${dict.table.name}</arg>
                        <arg>-AdictIdColumn=${dict.id.column}</arg>
                        <arg>-AdictCodeColumn=${dict.code.column}</arg>
                        <arg>-AdictNameColumn=${dict.name.column}</arg>
                        
                        <!-- 字典项表配置 -->
                        <arg>-AdictItemTableName=${dict.item.table.name}</arg>
                        <arg>-AdictItemForeignKeyColumn=${dict.item.foreign.key.column}</arg>
                        <arg>-AdictItemCodeColumn=${dict.item.code.column}</arg>
                        <arg>-AdictItemNameColumn=${dict.item.name.column}</arg>
                        
                        <!-- 生成的枚举类包名 -->
                        <arg>-AenumOutputPackage=${enum.output.package}</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
    
    <!-- 多环境配置 -->
    <profiles>
        <profile>
            <id>dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <jdbc.url>jdbc:postgresql://localhost:5432/dev_db</jdbc.url>
                <jdbc.username>dev_user</jdbc.username>
                <jdbc.password>dev_pass</jdbc.password>
            </properties>
        </profile>
        
        <profile>
            <id>test</id>
            <properties>
                <jdbc.url>jdbc:postgresql://test-db:5432/test_db</jdbc.url>
                <jdbc.username>test_user</jdbc.username>
                <jdbc.password>test_pass</jdbc.password>
            </properties>
        </profile>
        
        <profile>
            <id>prod</id>
            <properties>
                <jdbc.url>jdbc:postgresql://prod-db:5432/prod_db</jdbc.url>
                <jdbc.username>prod_user</jdbc.username>
                <!-- 生产环境密码应从环境变量获取 -->
                <jdbc.password>${env.DB_PASSWORD}</jdbc.password>
            </properties>
        </profile>
    </profiles>
    
</project>
```

**Maven 编译命令:**

```bash
# 默认开发环境编译
mvn clean compile

# 测试环境编译
mvn clean compile -Ptest

# 生产环境编译 (从环境变量读取密码)
export DB_PASSWORD=your_prod_password
mvn clean compile -Pprod

# 查看生成的枚举类
ls target/generated-sources/annotations/com/example/generated/enums/
```

### 4. 数据库准备

**创建字典表:**

```sql
-- PostgreSQL
CREATE TABLE sys_dict (
    id BIGSERIAL PRIMARY KEY,
    dict_code VARCHAR(100) NOT NULL,
    dict_name VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_dict_item (
    id BIGSERIAL PRIMARY KEY,
    dict_id BIGINT NOT NULL,
    item_value VARCHAR(100) NOT NULL,
    item_text VARCHAR(200) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (dict_id) REFERENCES sys_dict(id)
);
```

**插入测试数据:**

```sql
-- 用户状态字典
INSERT INTO sys_dict (dict_code, dict_name) VALUES ('user_status', '用户状态');
INSERT INTO sys_dict_item (dict_id, item_value, item_text, sort_order) 
VALUES 
    (1, 'ACTIVE', '激活', 1),
    (1, 'INACTIVE', '未激活', 2),
    (1, 'LOCKED', '锁定', 3),
    (1, 'DELETED', '已删除', 4);

-- 订单状态字典
INSERT INTO sys_dict (dict_code, dict_name) VALUES ('order_status', '订单状态');
INSERT INTO sys_dict_item (dict_id, item_value, item_text, sort_order) 
VALUES 
    (2, 'PENDING', '待付款', 1),
    (2, 'PAID', '已付款', 2),
    (2, 'SHIPPED', '已发货', 3),
    (2, 'DELIVERED', '已送达', 4),
    (2, 'CANCELLED', '已取消', 5);

-- 性别字典
INSERT INTO sys_dict (dict_code, dict_name) VALUES ('gender', '性别');
INSERT INTO sys_dict_item (dict_id, item_value, item_text, sort_order) 
VALUES 
    (3, 'MALE', '男', 1),
    (3, 'FEMALE', '女', 2),
    (3, 'OTHER', '其他', 3);
```

### 5. 编译生成枚举类

**Gradle 编译:**

```bash
# 执行编译
./gradlew compileJava

# 查看生成的枚举类
ls build/generated/sources/annotationProcessor/java/main/com/example/generated/enums/
# 输出:
# EnumUserStatus.java
# EnumOrderStatus.java
# EnumGender.java
```

**Maven 编译:**

```bash
# 执行编译
mvn clean compile

# 查看生成的枚举类
ls target/generated-sources/annotations/com/example/generated/enums/
# 输出:
# EnumUserStatus.java
# EnumOrderStatus.java
# EnumGender.java
```

### 6. 在代码中使用生成的枚举

**实体类示例:**

```java
package com.example.entity;

import com.example.generated.enums.EnumUserStatus;
import com.example.generated.enums.EnumGender;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    private String email;
    
    // 存储枚举的 code 值
    @Column(name = "status")
    private String status;
    
    @Column(name = "gender")
    private String gender;
    
    // 业务方法 - 获取用户状态枚举
    public EnumUserStatus getStatusEnum() {
        return EnumUserStatus.fromCode(this.status);
    }
    
    // 业务方法 - 设置用户状态
    public void setStatusEnum(EnumUserStatus statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }
    
    // 业务方法 - 获取性别枚举
    public EnumGender getGenderEnum() {
        return EnumGender.fromCode(this.gender);
    }
    
    // 业务方法 - 设置性别
    public void setGenderEnum(EnumGender genderEnum) {
        this.gender = genderEnum != null ? genderEnum.getCode() : null;
    }
    
    // 标准的 getter/setter...
}
```

**Service 层示例:**

```java
package com.example.service;

import com.example.entity.User;
import com.example.generated.enums.EnumUserStatus;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    // 创建用户 - 使用枚举设置初始状态
    public User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setStatusEnum(EnumUserStatus.INACTIVE);  // 初始状态:未激活
        return userRepository.save(user);
    }
    
    // 激活用户
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 只有未激活的用户才能激活
        if (user.getStatusEnum() == EnumUserStatus.INACTIVE) {
            user.setStatusEnum(EnumUserStatus.ACTIVE);
            userRepository.save(user);
        } else {
            throw new IllegalStateException("用户状态不正确,无法激活");
        }
    }
    
    // 查询所有激活用户
    public List<User> findActiveUsers() {
        return userRepository.findByStatus(EnumUserStatus.ACTIVE.getCode());
    }
}
```

**Controller 层示例:**

```java
package com.example.controller;

import com.example.generated.enums.EnumUserStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    // 获取所有用户状态枚举
    @GetMapping("/statuses")
    public List<Map<String, String>> getAllStatuses() {
        return Arrays.stream(EnumUserStatus.values())
            .map(status -> Map.of(
                "code", status.getCode(),
                "desc", status.getDesc()
            ))
            .collect(Collectors.toList());
    }
    
    // 更新用户状态
    @PutMapping("/{id}/status")
    public void updateUserStatus(
            @PathVariable Long id,
            @RequestParam String statusCode) {
        
        EnumUserStatus status = EnumUserStatus.fromCode(statusCode);
        if (status == null) {
            throw new IllegalArgumentException("无效的状态码: " + statusCode);
        }
        
        userService.updateUserStatus(id, status);
    }
}
```

**DTO 示例:**

```java
package com.example.dto;

import com.example.generated.enums.EnumUserStatus;
import com.example.generated.enums.EnumGender;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String statusCode;
    private String statusDesc;
    private String genderCode;
    private String genderDesc;
    
    // 从 User 实体转换
    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        
        // 使用枚举填充状态信息
        EnumUserStatus status = user.getStatusEnum();
        if (status != null) {
            dto.setStatusCode(status.getCode());
            dto.setStatusDesc(status.getDesc());
        }
        
        // 使用枚举填充性别信息
        EnumGender gender = user.getGenderEnum();
        if (gender != null) {
            dto.setGenderCode(gender.getCode());
            dto.setGenderDesc(gender.getDesc());
        }
        
        return dto;
    }
    
    // getter/setter...
}
```

### 7. 前端集成示例

**获取字典数据的 API:**

```java
@RestController
@RequestMapping("/api/dict")
public class DictController {
    
    // 获取所有用户状态选项
    @GetMapping("/user-status")
    public List<DictOption> getUserStatusOptions() {
        return Arrays.stream(EnumUserStatus.values())
            .map(e -> new DictOption(e.getCode(), e.getDesc()))
            .collect(Collectors.toList());
    }
    
    // 获取所有订单状态选项
    @GetMapping("/order-status")
    public List<DictOption> getOrderStatusOptions() {
        return Arrays.stream(EnumOrderStatus.values())
            .map(e -> new DictOption(e.getCode(), e.getDesc()))
            .collect(Collectors.toList());
    }
    
    // 通用字典选项类
    public static class DictOption {
        private String value;
        private String label;
        
        public DictOption(String value, String label) {
            this.value = value;
            this.label = label;
        }
        
        // getter/setter...
    }
}
```

**前端使用 (React 示例):**

```jsx
function UserForm() {
    const [statuses, setStatuses] = useState([]);
    
    useEffect(() => {
        // 获取字典数据
        fetch('/api/dict/user-status')
            .then(res => res.json())
            .then(data => setStatuses(data));
    }, []);
    
    return (
        <select name="status">
            {statuses.map(status => (
                <option key={status.value} value={status.value}>
                    {status.label}
                </option>
            ))}
        </select>
    );
}
```

### 8. 测试示例

```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testCreateUser() {
        User user = userService.createUser("test", "test@example.com");
        
        // 验证初始状态是未激活
        assertEquals(EnumUserStatus.INACTIVE, user.getStatusEnum());
        assertEquals("未激活", user.getStatusEnum().getDesc());
    }
    
    @Test
    void testActivateUser() {
        User user = userService.createUser("test", "test@example.com");
        userService.activateUser(user.getId());
        
        User activated = userService.findById(user.getId());
        assertEquals(EnumUserStatus.ACTIVE, activated.getStatusEnum());
    }
    
    @Test
    void testEnumFromCode() {
        EnumUserStatus status = EnumUserStatus.fromCode("ACTIVE");
        assertNotNull(status);
        assertEquals("ACTIVE", status.getCode());
        assertEquals("激活", status.getDesc());
    }
    
    @Test
    void testEnumFromDesc() {
        EnumUserStatus status = EnumUserStatus.fromDesc("激活");
        assertNotNull(status);
        assertEquals(EnumUserStatus.ACTIVE, status);
    }
}
```

## 多环境配置

### Gradle 多环境配置

对于不同环境使用不同的数据库配置:

```kotlin
// build.gradle.kts
tasks.withType<JavaCompile> {
    val env = System.getProperty("env") ?: "dev"
    
    val dbConfig = when(env) {
        "prod" -> mapOf(
            "jdbcUrl" to "jdbc:postgresql://prod-db:5432/prod_db",
            "jdbcUsername" to "prod_user",
            "jdbcPassword" to System.getenv("DB_PASSWORD")
        )
        "test" -> mapOf(
            "jdbcUrl" to "jdbc:postgresql://test-db:5432/test_db",
            "jdbcUsername" to "test_user",
            "jdbcPassword" to "test_pass"
        )
        else -> mapOf(
            "jdbcUrl" to "jdbc:postgresql://localhost:5432/dev_db",
            "jdbcUsername" to "dev_user",
            "jdbcPassword" to "dev_pass"
        )
    }
    
    options.compilerArgs.addAll(listOf(
        "-AjdbcDriver=org.postgresql.Driver",
        "-AjdbcUrl=${dbConfig["jdbcUrl"]}",
        "-AjdbcUsername=${dbConfig["jdbcUsername"]}",
        "-AjdbcPassword=${dbConfig["jdbcPassword"]}",
        "-AdictTableName=sys_dict",
        "-AdictIdColumn=id",
        "-AdictCodeColumn=dict_code",
        "-AdictNameColumn=dict_name",
        "-AdictItemTableName=sys_dict_item",
        "-AdictItemForeignKeyColumn=dict_id",
        "-AdictItemCodeColumn=item_value",
        "-AdictItemNameColumn=item_text",
        "-AenumOutputPackage=com.example.generated.enums"
    ))
}
```

编译时指定环境:

```bash
# 生产环境
./gradlew compileJava -Denv=prod

# 测试环境
./gradlew compileJava -Denv=test

# 开发环境 (默认)
./gradlew compileJava
```

### Maven 多环境配置

Maven 已在 pom.xml 中配置了 profiles，编译时指定环境:

```bash
# 开发环境 (默认)
mvn clean compile

# 测试环境
mvn clean compile -Ptest

# 生产环境 (从环境变量读取密码)
export DB_PASSWORD=your_prod_password
mvn clean compile -Pprod
```

## CI/CD 集成

**GitHub Actions 示例:**

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: test_db
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Init database
        run: |
          PGPASSWORD=postgres psql -h localhost -U postgres -d test_db -f scripts/init_dict.sql
      
      - name: Build with Gradle
        run: ./gradlew build
```

## 总结

这个 APT 处理器的主要优势:

1. **类型安全**: 使用枚举代替字符串常量
2. **自动同步**: 编译时自动从数据库同步最新字典数据
3. **零维护**: 无需手动维护枚举代码
4. **IDE 友好**: 生成标准 Java 枚举,IDE 完全支持
5. **文档化**: 枚举带有完整的 JavaDoc 注释
