# Spring Convention Plugins

This module provides convention plugins for Spring Boot projects with Kotlin support.

## Plugins

### 1. Spring Common Convention Plugin
**ID**: `site.addzero.gradle.plugin.spring-common-convention`

Provides basic Spring Boot configuration with:
- Spring Boot BOM (Bill of Materials)
- Standard test dependencies (JUnit 5, Spring Boot Test, H2)
- Kotlin compiler configuration

### 2. Spring Starter Convention Plugin
**ID**: `site.addzero.gradle.plugin.spring-starter-convention`

For Spring Boot starter libraries with:
- Compile-only Spring Boot dependencies
- Configuration processor
- Spring auto-configuration support

### 3. Spring App Convention Plugin
**ID**: `site.addzero.gradle.plugin.spring-app-convention`

For Spring Boot applications with:
- Implementation Spring Boot web dependencies
- Dependency management
- Full Spring plugin support

## Usage

### Basic Usage

```kotlin
plugins {
    id("site.addzero.gradle.plugin.spring-app-convention")
}
```

### Configuration Extension

All plugins provide a `springConvention` extension for customization:

```kotlin
springConvention {
    // Configure Spring Boot version
    springBootVersion = "3.2.0"

    // Control which dependencies are included
    includeStarterTest = true
    includeH2 = true
    includeWebInTest = true
    includeConfigurationProcessor = true
    includeAutoConfigure = true

    // Add additional dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.testcontainers:junit-jupiter")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework:spring-webflux")
}
```

### Plugin-specific Examples

#### Spring App
```kotlin
plugins {
    id("site.addzero.gradle.plugin.spring-app-convention")
}

// Configure for a typical Spring Boot web app
springConvention {
    springBootVersion = "3.2.0"
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
```

#### Spring Starter Library
```kotlin
plugins {
    id("site.addzero.gradle.plugin.spring-starter-convention")
}

// Configure for a Spring Boot starter library
springConvention {
    includeAutoConfigure = true
    includeConfigurationProcessor = true
    compileOnly("org.springframework.boot:spring-boot-starter-validation")
}
```

#### Spring Common (Base)
```kotlin
plugins {
    id("site.addzero.gradle.plugin.spring-common-convention")
}

// Basic Spring configuration
springConvention {
    springBootVersion = "3.2.0"
    includeH2 = false // Use other database
    testImplementation("org.testcontainers:postgresql")
}
```

## Dependencies Managed

### Automatically Included
- Spring Boot BOM
- JUnit 5
- Spring Boot Test
- H2 Database (configurable)

### Additional Configuration
The extension supports adding dependencies through:
- `implementation("dependency")`
- `testImplementation("dependency")`
- `annotationProcessor("dependency")`
- `compileOnly("dependency")`

## Publishing

These plugins are designed to be published to Maven Central and used independently without requiring version catalogs.