# api-openai

OpenAI REST API contracts for KCloud application-layer implementations.

## Source

- OpenAPI repository: https://github.com/openai/openai-openapi
- Spec file: https://raw.githubusercontent.com/openai/openai-openapi/master/openapi.yaml
- Source spec version observed during generation: `2.3.0`
- Source commit observed during generation: `5162af98d3147432c14680df789e8e12d4891e6b`

## Usage

```kotlin
dependencies {
    implementation(project(":lib:api:api-openai"))
}
```

The interfaces are intentionally implementation-free. REST paths are fixed in
`OpenAiApiPaths` and used directly by the Ktorfit annotations on each method.

Request and response schemas are represented with generic JSON or binary body
aliases so application modules can own domain-specific validation and mapping.
