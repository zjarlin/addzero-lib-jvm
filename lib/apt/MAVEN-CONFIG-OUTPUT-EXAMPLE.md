# APT Buddy 插件 Maven 配置输出示例

## 执行命令

```bash
./gradlew :lib:apt:generateAptScript
```

## 控制台输出

当执行 `generateAptScript` 任务时，apt-buddy 插件会在控制台输出以下 Maven pom.xml 配置：

```
═══════════════════════════════════════════════════════════════
📦 Maven pom.xml 配置等价项
═══════════════════════════════════════════════════════════════

<properties>
    <apt.jdbcDriver>org.postgresql.Driver</apt.jdbcDriver>
    <apt.jdbcUrl>jdbc:postgresql://localhost:5432/my_database</apt.jdbcUrl>
    <apt.jdbcUsername>postgres</apt.jdbcUsername>
    <apt.jdbcPassword>postgres</apt.jdbcPassword>
    <apt.dictTableName>sys_dict</apt.dictTableName>
    <apt.dictIdColumn>id</apt.dictIdColumn>
    <apt.dictCodeColumn>dict_code</apt.dictCodeColumn>
    <apt.dictNameColumn>dict_name</apt.dictNameColumn>
    <apt.dictItemTableName>sys_dict_item</apt.dictItemTableName>
    <apt.dictItemForeignKeyColumn>dict_id</apt.dictItemForeignKeyColumn>
    <apt.dictItemCodeColumn>item_value</apt.dictItemCodeColumn>
    <apt.dictItemNameColumn>item_text</apt.dictItemNameColumn>
    <apt.enumOutputPackage>com.example.generated.enums</apt.enumOutputPackage>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
                    <arg>-AjdbcDriver=${apt.jdbcDriver}</arg>
                    <arg>-AjdbcUrl=${apt.jdbcUrl}</arg>
                    <arg>-AjdbcUsername=${apt.jdbcUsername}</arg>
                    <arg>-AjdbcPassword=${apt.jdbcPassword}</arg>
                    <arg>-AdictTableName=${apt.dictTableName}</arg>
                    <arg>-AdictIdColumn=${apt.dictIdColumn}</arg>
                    <arg>-AdictCodeColumn=${apt.dictCodeColumn}</arg>
                    <arg>-AdictNameColumn=${apt.dictNameColumn}</arg>
                    <arg>-AdictItemTableName=${apt.dictItemTableName}</arg>
                    <arg>-AdictItemForeignKeyColumn=${apt.dictItemForeignKeyColumn}</arg>
                    <arg>-AdictItemCodeColumn=${apt.dictItemCodeColumn}</arg>
                    <arg>-AdictItemNameColumn=${apt.dictItemNameColumn}</arg>
                    <arg>-AenumOutputPackage=${apt.enumOutputPackage}</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>

═══════════════════════════════════════════════════════════════
```

## 如何使用

1. **复制配置**：直接复制控制台输出的 XML 配置
2. **粘贴到 pom.xml**：将配置添加到 Maven 项目的 pom.xml 文件中
3. **根据环境调整**：修改 properties 中的值以适应不同环境（开发、测试、生产）

## Maven 多环境配置示例

```xml
<profiles>
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <apt.jdbcUrl>jdbc:postgresql://localhost:5432/dev_db</apt.jdbcUrl>
            <apt.jdbcUsername>dev_user</apt.jdbcUsername>
            <apt.jdbcPassword>dev_pass</apt.jdbcPassword>
        </properties>
    </profile>
    
    <profile>
        <id>prod</id>
        <properties>
            <apt.jdbcUrl>jdbc:postgresql://prod-db:5432/prod_db</apt.jdbcUrl>
            <apt.jdbcUsername>prod_user</apt.jdbcUsername>
            <apt.jdbcPassword>${env.DB_PASSWORD}</apt.jdbcPassword>
        </properties>
    </profile>
</profiles>
```

## 优势

1. **一键生成**：无需手动编写 Maven 配置
2. **格式统一**：Gradle 和 Maven 使用相同的配置参数
3. **减少错误**：避免手动输入配置参数时的拼写错误
4. **易于维护**：Gradle 项目配置变更后，重新生成即可同步到 Maven
