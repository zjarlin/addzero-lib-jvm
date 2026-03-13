# Kotlin Compiler Plugin - Multi Receiver

A Kotlin compiler plugin that transforms function parameters into receivers or context parameters.

## Features

### Single Parameter → Extension Receiver

When a function annotated with `@AddGenerateExtension` has exactly one source-level value parameter, the plugin generates an extension wrapper with that parameter as the receiver.

**Before:**
```kotlin
@AddGenerateExtension
fun process(data: String): Result {
    // ...
}
```

**After (generated):**
```kotlin
fun String.process(): Result {
    // ...
}
```

### Multiple Receivers → Context Parameters

When a function has one or more parameters annotated with `@Receiver`, those parameters become context parameters in the generated wrapper.

**Before:**
```kotlin
@AddGenerateExtension
fun combine(@Receiver foo: Foo, @Receiver bar: Bar, value: Int): Result {
    // ...
}
```

**After (generated):**
```kotlin
context(foo: Foo, bar: Bar)
fun combine(value: Int): Result {
    // ...
}
```

## JVM Compatibility

Generated wrappers keep the same Kotlin source name, but they are emitted with unique `@JvmName(...)` values to avoid JVM signature clashes with the original declaration.

## Usage

1. Apply the Gradle subplugin:
```kotlin
plugins {
    id("site.addzero.kcp.multireceiver")
}
```

2. The subplugin will:
- add `site.addzero:kcp-multireceiver-annotations`
- add `-Xcontext-parameters`
- wire the compiler plugin artifact automatically

3. Annotate your functions:
```kotlin
import site.addzero.kcp.annotations.AddGenerateExtension
import site.addzero.kcp.annotations.Receiver

@AddGenerateExtension
fun wrap(value: MyType) { }

@AddGenerateExtension
fun render(@Receiver scope: Scope, value: Int) { }
```

## Modules

- `kcp-multireceiver-annotations` - Runtime annotations
- `kcp-multireceiver-plugin` - Compiler plugin implementation
- `kcp-multireceiver-gradle-plugin` - Gradle subplugin integration
- `kcp-multireceiver-idea-plugin` - IntelliJ IDEA companion plugin

## Verification

- Compiler plugin integration tests prove top-level/member extension wrappers and context wrappers compile and run.
- Gradle smoke tests prove a consumer project can apply `site.addzero.kcp.multireceiver` and call generated APIs.
- IDEA plugin builds successfully with `:lib:kcp:multireceiver:kcp-multireceiver-idea-plugin:buildPlugin`.
