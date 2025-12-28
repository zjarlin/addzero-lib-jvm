# Kotlin Compiler Plugin - Multi Receiver

A Kotlin compiler plugin that transforms function parameters into receivers or context parameters.

## Features

### Single Parameter → Extension Receiver

When a function annotated with `@AddGenerateExtension` has **only one parameter** (in source code, excluding compiler-added parameters), the plugin generates an extension function with that parameter as the receiver.

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

When a function has **multiple parameters** annotated with `@Receiver`, all `@Receiver`-annotated parameters become context parameters.

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

## Usage

1. Add annotations dependency:
```kotlin
implementation("site.addzero:kcp-multireceiver-annotations")
```

2. Apply compiler plugin:
```kotlin
plugins {
    id("site.addzero.kcp.multireceiver")
}
```

3. Annotate your functions:
```kotlin
import site.addzero.kcp.annotations.AddGenerateExtension
import site.addzero.kcp.annotations.Receiver

@AddGenerateExtension
fun myFunction(param: MyType) { }
```

## Modules

- `kcp-multireceiver-annotations` - Runtime annotations
- `kcp-multireceiver-plugin` - Compiler plugin implementation
