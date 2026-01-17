# Parameter Extractor Processor

A KSP processor that analyzes classes and extracts common parameters used across multiple methods into a new wrapper class with a delegate pattern.

## Usage

### Before

```kotlin
class MyService {
    fun method1(apiKey: String, baseUrl: String, timeout: Int) {
        // implementation
    }

    fun method2(apiKey: String, baseUrl: String, retryCount: Int) {
        // implementation
    }

    fun method3(apiKey: String, baseUrl: String, cacheSize: Long) {
        // implementation
    }

    fun method4(apiKey: String, baseUrl: String, debug: Boolean) {
        // implementation
    }

    fun method5(timeout: Int, retryCount: Int) {
        // implementation
    }
}
```

### After

Annotate the class with `@ExtractCommonParameters`:

```kotlin
import site.addzero.ksp.singletonadapter.anno.ExtractCommonParameters

@ExtractCommonParameters
class MyService {
    fun method1(apiKey: String, baseUrl: String, timeout: Int) {
        // implementation
    }

    fun method2(apiKey: String, baseUrl: String, retryCount: Int) {
        // implementation
    }

    fun method3(apiKey: String, baseUrl: String, cacheSize: Long) {
        // implementation
    }

    fun method4(apiKey: String, baseUrl: String, debug: Boolean) {
        // implementation
    }

    fun method5(timeout: Int, retryCount: Int) {
        // implementation
    }
}
```

The processor will generate a new class `MyServiceDelegate`:

```kotlin
class MyServiceDelegate(
    val apiKey: String,
    val baseUrl: String,
    val delegate: MyService = MyService()  // Default instance if MyService has no-arg constructor
) {
    fun method1(timeout: Int) = delegate.method1(apiKey, baseUrl, timeout)

    fun method2(retryCount: Int) = delegate.method2(apiKey, baseUrl, retryCount)

    fun method3(cacheSize: Long) = delegate.method3(apiKey, baseUrl, cacheSize)

    fun method4(debug: Boolean) = delegate.method4(apiKey, baseUrl, debug)

    fun method5(timeout: Int, retryCount: Int) = delegate.method5(timeout, retryCount)
}
```

## How it works

1. The processor scans all public methods in the annotated class
2. It identifies parameters that appear in at least 2 methods
3. Among the common parameters, it selects the group with the highest repetition count
4. Generates a new wrapper class that:
   - Takes the common parameters and an instance of the original class as constructor arguments
   - Provides methods with the common parameters removed from their signatures
   - Delegates calls to the original class instance, injecting the common parameters in the correct order
   - Automatically provides a default delegate instance if the original class has a no-argument constructor

### Inline to Parameters Mode (`inlineToParameters = true`)

When `inlineToParameters = true`, the processor generates a singleton object instead of a class:

- Constructor parameters become object properties with injected default values
- Methods accept all parameters (constructor params + original method params) without defaults
- Each method creates a new instance of the original class and delegates the call
- Users can modify object properties to change default behavior

In the example above:
- `apiKey` and `baseUrl` appear in 4 methods each
- `timeout` appears in 2 methods
- The processor selects `apiKey` and `baseUrl` (higher count)
- Methods that had these parameters now omit them and use the constructor values
- Parameter order is preserved: common parameters come first (from constructor), followed by remaining parameters in their original order

## Configuration

You can customize the generated class name:

```kotlin
@ExtractCommonParameters("ServiceConfig")
class MyService {
    // ...
}
```

Or with explicit parameter name:

```kotlin
@ExtractCommonParameters(value = "ServiceConfig")
class MyService {
    // ...
}
```

This will generate `ServiceConfig` instead of `MyServiceDelegate`.

## Testing

A test example has been created in the `tool-api-suno` module. The `TestService` class demonstrates the processor in action:

```kotlin
@ExtractCommonParameters(commonParamsName = "ApiConfig")
class TestService {
    fun method1(apiKey: String, baseUrl: String, timeout: Int) { /* ... */ }
    fun method2(apiKey: String, baseUrl: String, retryCount: Int) { /* ... */ }
    fun method3(apiKey: String, baseUrl: String, cacheSize: Long) { /* ... */ }
    fun method4(apiKey: String, baseUrl: String, debug: Boolean) { /* ... */ }
    fun method5(timeout: Int, retryCount: Int) { /* ... */ }
}
```

This generates:

```kotlin
class ApiConfig(
    val apiKey: String,
    val baseUrl: String,
    val delegate: TestService = TestService()  // Default instance if TestService has no-arg constructor
) {
    // Original: method1(apiKey: String, baseUrl: String, timeout: Int)
    // Wrapper:  method1(timeout: Int) -> delegate.method1(apiKey, baseUrl, timeout)
    fun method1(timeout: Int) = delegate.method1(apiKey, baseUrl, timeout)

    // Original: method2(apiKey: String, baseUrl: String, retryCount: Int)
    // Wrapper:  method2(retryCount: Int) -> delegate.method2(apiKey, baseUrl, retryCount)
    fun method2(retryCount: Int) = delegate.method2(apiKey, baseUrl, retryCount)

    // Original: method3(apiKey: String, baseUrl: String, cacheSize: Long)
    // Wrapper:  method3(cacheSize: Long) -> delegate.method3(apiKey, baseUrl, cacheSize)
    fun method3(cacheSize: Long) = delegate.method3(apiKey, baseUrl, cacheSize)

    // Original: method4(apiKey: String, baseUrl: String, debug: Boolean)
    // Wrapper:  method4(debug: Boolean) -> delegate.method4(apiKey, baseUrl, debug)
    fun method4(debug: Boolean) = delegate.method4(apiKey, baseUrl, debug)

    // Original: method5(timeout: Int, retryCount: Int)
    // Wrapper:  method5(timeout: Int, retryCount: Int) -> delegate.method5(timeout, retryCount)
    fun method5(timeout: Int, retryCount: Int) = delegate.method5(timeout, retryCount)
}
```

## Usage Example

### Standard Mode

With the default delegate instance:

```kotlin
// If MyService has a no-argument constructor, you can use:
val service = MyServiceDelegate(apiKey = "my-key", baseUrl = "https://api.example.com")
// This automatically creates MyService() as the delegate

// Or provide your own instance:
val customService = MyService(customConfig)
val service = MyServiceDelegate(apiKey = "my-key", baseUrl = "https://api.example.com", delegate = customService)

// Now use the simplified methods:
service.method1(timeout = 5000)
service.method2(retryCount = 3)
```

### Inline to Parameters Mode

```kotlin
@SingletonAdapter(inlineToParameters = true, inject = ["apiKey=const:default-key", "baseUrl=const:default-url"])
class MyService(apiKey: String, baseUrl: String) {
    fun method1(timeout: Int) { /* ... */ }
}

// Generated object:
object MyServiceInline {
    var apiKey: String = "default-key"
    var baseUrl: String = "default-url"
    
    fun method1(apiKey: String, baseUrl: String, timeout: Int) {
        val instance = MyService(apiKey, baseUrl)
        return instance.method1(timeout)
    }
}

// Usage:
MyServiceInline.method1("custom-key", "custom-url", 5000)
// Or modify defaults:
MyServiceInline.apiKey = "new-default"
MyServiceInline.method1("custom-key", "custom-url", 5000)  // uses new-default for other calls
```

## Setup

The processor is automatically registered via the service provider mechanism. Just add the dependency to your KSP configuration.