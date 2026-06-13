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

Request and response schemas are generated as Kotlin serialization DTOs under
`site.addzero.kcloud.api.openai.models`. Application modules implement the
interfaces against concrete request and response types such as
`CreateChatCompletionRequest`, `CreateChatCompletionResponse`, `CreateResponse`,
and `Response`.

`JsonElement` remains only as a field-level or named typealias fallback when the
OpenAPI schema does not expose a fixed serializable object shape.

## Regeneration

```bash
python3 lib/api/api-openai/scripts/generate-openai-api.py
./gradlew :lib:api:api-openai:compileKotlinJvm --rerun-tasks
```
