# Processor Configuration

## Consumer Usage

### Gradle (KSP)

\`\`\`kotlin
plugins {
    id("com.google.devtools.ksp")
}

ksp {
    arg("jdbcUrl", "")
    arg("jdbcUsername", "")
    arg("jdbcPassword", "")
    arg("autoddlForeignKeys", "true")
    arg("autoddlKeys", "true")
    arg("autoddlAllowDeleteColumn", "false")
    arg("springResourcePath", "")
    arg("autoddlExcludeTables", "flyway_schema_history,vector_store,*_mapping")
}
\`\`\`

---

### Maven (APT)

\`\`\`xml
<properties>
    <apt.jdbcUrl></apt.jdbcUrl>
    <apt.jdbcUsername></apt.jdbcUsername>
    <apt.jdbcPassword></apt.jdbcPassword>
    <apt.autoddlForeignKeys>true</apt.autoddlForeignKeys>
    <apt.autoddlKeys>true</apt.autoddlKeys>
    <apt.autoddlAllowDeleteColumn>false</apt.autoddlAllowDeleteColumn>
    <apt.springResourcePath></apt.springResourcePath>
    <apt.autoddlExcludeTables>flyway_schema_history,vector_store,*_mapping</apt.autoddlExcludeTables>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
                    <arg>-AjdbcUrl=${apt.jdbcUrl}</arg>
                    <arg>-AjdbcUsername=${apt.jdbcUsername}</arg>
                    <arg>-AjdbcPassword=${apt.jdbcPassword}</arg>
                    <arg>-AautoddlForeignKeys=${apt.autoddlForeignKeys}</arg>
                    <arg>-AautoddlKeys=${apt.autoddlKeys}</arg>
                    <arg>-AautoddlAllowDeleteColumn=${apt.autoddlAllowDeleteColumn}</arg>
                    <arg>-AspringResourcePath=${apt.springResourcePath}</arg>
                    <arg>-AautoddlExcludeTables=${apt.autoddlExcludeTables}</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
\`\`\`

---

## Library Author Usage

Apply the plugin in your `build.gradle.kts`:

\`\`\`kotlin
plugins {
    id("site.addzero.gradle.plugin.processorbuddy")
}

processorBuddy {
    mustMap.set(mapOf(
        "jdbcUrl" to "",
        "jdbcUsername" to "",
        "jdbcPassword" to "",
        "autoddlForeignKeys" to "true",
        "autoddlKeys" to "true",
        "autoddlAllowDeleteColumn" to "false",
        "springResourcePath" to "",
        "autoddlExcludeTables" to "flyway_schema_history,vector_store,*_mapping"
    ))
}
\`\`\`

This generates:
- Interface: `org.babyfish.jimmer.config.autoddl.SettingContext`
- Singleton: `org.babyfish.jimmer.config.autoddl.Settings`

\`\`\`kotlin
import org.babyfish.jimmer.config.autoddl.SettingContext
import org.babyfish.jimmer.config.autoddl.Settings
import org.babyfish.jimmer.config.autoddl.merge

// Default singleton
val config: SettingContext = Settings

// Load config
config.fromOptions(mapOf("key" to "value"))

// Export config
val options = config.toOptions()

// Merge configs (non-empty values win)
val merged = merge(config1, config2)
\`\`\`