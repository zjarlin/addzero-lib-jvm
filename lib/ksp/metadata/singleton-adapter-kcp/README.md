# SingletonAdapter KCP Plugin

A Kotlin Compiler Plugin (KCP) implementation of the SingletonAdapter functionality. This provides better access to default values and more powerful code transformation capabilities compared to the KSP version.

## Overview

This KCP plugin processes `@SingletonAdapter` annotations at compile time using the Kotlin compiler's IR (Intermediate Representation), allowing for more sophisticated code transformations.

## Key Differences from KSP Version

- **Better Default Value Access**: Can access actual default parameter values from source code
- **IR-Level Transformations**: Works at the compiler's intermediate representation level
- **More Powerful**: Can perform complex code modifications that KSP cannot

## Current Status

This is a basic skeleton implementation. The plugin currently:
- Detects classes with `@SingletonAdapter` annotations
- Logs information about found classes
- Provides the foundation for implementing the full transformation logic

## Usage

To use this plugin, add it to your Kotlin compiler configuration:

```kotlin
kotlin {
    compilerOptions.configure {
        // Add plugin configuration here
    }
}
```

## Implementation Notes

The plugin uses:
- `CompilerPluginRegistrar` for registration
- `IrGenerationExtension` for IR-level processing
- Auto-service for automatic discovery

## Future Development

This plugin is intended to eventually replace or complement the KSP version with:
- Full `@SingletonAdapter` processing
- Better handling of default values
- More advanced code generation capabilities