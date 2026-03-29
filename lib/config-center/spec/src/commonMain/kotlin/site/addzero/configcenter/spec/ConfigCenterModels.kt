package site.addzero.configcenter.spec

import kotlinx.serialization.Serializable

@Serializable
enum class ConfigDomain {
    SYSTEM,
    BUSINESS,
}

@Serializable
enum class ConfigValueType {
    STRING,
    BOOLEAN,
    INTEGER,
    NUMBER,
    JSON,
    TEXT,
}

@Serializable
enum class ConfigStorageMode {
    REPO_PLAIN,
    REPO_ENCRYPTED,
    LOCAL_OVERRIDE,
}

@Serializable
enum class ConfigTargetKind {
    KTOR_HOCON,
    SPRING_YAML,
    DOTENV,
    JSON_FILE,
    YAML_FILE,
    PROPERTIES_FILE,
    DOCKER_COMPOSE_TEMPLATE,
    DOCKERFILE_TEMPLATE,
    DOTFILE_TEMPLATE,
    OS_ENV_EXPORT,
    GENERIC_TEXT_TEMPLATE,
}

@Serializable
data class ConfigQuery(
    val namespace: String? = null,
    val domain: ConfigDomain? = null,
    val profile: String = "default",
    val keyword: String? = null,
    val includeDisabled: Boolean = false,
)

@Serializable
data class ConfigEntryDto(
    val id: String = "",
    val key: String = "",
    val namespace: String = "",
    val domain: ConfigDomain = ConfigDomain.SYSTEM,
    val profile: String = "default",
    val valueType: ConfigValueType = ConfigValueType.STRING,
    val storageMode: ConfigStorageMode = ConfigStorageMode.REPO_PLAIN,
    val value: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val enabled: Boolean = true,
    val decryptionAvailable: Boolean = true,
    val createdAtEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L,
)

@Serializable
data class ConfigMutationRequest(
    val id: String? = null,
    val key: String,
    val namespace: String = "",
    val domain: ConfigDomain = ConfigDomain.SYSTEM,
    val profile: String = "default",
    val valueType: ConfigValueType = ConfigValueType.STRING,
    val storageMode: ConfigStorageMode = ConfigStorageMode.REPO_PLAIN,
    val value: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val enabled: Boolean = true,
)

@Serializable
data class ConfigTargetDto(
    val id: String = "",
    val name: String = "",
    val targetKind: ConfigTargetKind = ConfigTargetKind.DOTENV,
    val outputPath: String = "",
    val namespaceFilter: String? = null,
    val profile: String = "default",
    val templateText: String? = null,
    val enabled: Boolean = true,
    val sortOrder: Int = 0,
)

@Serializable
data class ConfigTargetMutationRequest(
    val id: String? = null,
    val name: String,
    val targetKind: ConfigTargetKind,
    val outputPath: String = "",
    val namespaceFilter: String? = null,
    val profile: String = "default",
    val templateText: String? = null,
    val enabled: Boolean = true,
    val sortOrder: Int = 0,
)

@Serializable
data class RenderedConfig(
    val targetId: String = "",
    val targetName: String = "",
    val targetKind: ConfigTargetKind = ConfigTargetKind.GENERIC_TEXT_TEMPLATE,
    val outputPath: String = "",
    val content: String = "",
    val exportedAtEpochMillis: Long = 0L,
)

@Serializable
data class ConfigSnapshotDto(
    val items: Map<String, String> = emptyMap(),
)

@Serializable
data class ConfigValueResponse(
    val key: String = "",
    val value: String? = null,
)

