package site.addzero.configcenter.spec

interface ConfigCenterReader {
    suspend fun getEnv(
        key: String,
        query: ConfigQuery = ConfigQuery(),
    ): String?

    suspend fun getSnapshot(
        namespace: String? = null,
        profile: String = "default",
    ): Map<String, String>

    suspend fun listEntries(
        query: ConfigQuery = ConfigQuery(),
    ): List<ConfigEntryDto>

    suspend fun getEntry(
        id: String,
    ): ConfigEntryDto?
}

interface ConfigCenterWriter {
    suspend fun addEnv(
        request: ConfigMutationRequest,
    ): ConfigEntryDto

    suspend fun updateEnv(
        id: String,
        request: ConfigMutationRequest,
    ): ConfigEntryDto

    suspend fun deleteEnv(
        id: String,
    )
}

interface ConfigCenterTargetManager {
    suspend fun listTargets(): List<ConfigTargetDto>

    suspend fun getTarget(
        id: String,
    ): ConfigTargetDto?

    suspend fun saveTarget(
        request: ConfigTargetMutationRequest,
    ): ConfigTargetDto

    suspend fun deleteTarget(
        id: String,
    )
}

interface ConfigCenterRenderer {
    suspend fun renderTarget(
        targetId: String,
    ): RenderedConfig

    suspend fun previewTarget(
        targetId: String,
    ): String

    suspend fun exportTarget(
        targetId: String,
    ): RenderedConfig
}

interface ConfigCenterGateway :
    ConfigCenterReader,
    ConfigCenterWriter,
    ConfigCenterTargetManager,
    ConfigCenterRenderer

interface ConfigRepositorySpi {
    suspend fun listEntries(
        query: ConfigQuery,
    ): List<ConfigEntryDto>

    suspend fun getEntry(
        id: String,
    ): ConfigEntryDto?

    suspend fun findEntriesByKey(
        key: String,
        query: ConfigQuery,
    ): List<ConfigEntryDto>

    suspend fun upsertEntry(
        request: ConfigMutationRequest,
    ): ConfigEntryDto

    suspend fun deleteEntry(
        id: String,
    )

    suspend fun listTargets(): List<ConfigTargetDto>

    suspend fun getTarget(
        id: String,
    ): ConfigTargetDto?

    suspend fun upsertTarget(
        request: ConfigTargetMutationRequest,
    ): ConfigTargetDto

    suspend fun deleteTarget(
        id: String,
    )

    suspend fun readBundleMeta(
        key: String,
    ): String?

    suspend fun writeBundleMeta(
        key: String,
        value: String,
    )
}

interface ConfigEncryptionSpi {
    fun encrypt(
        plainText: String,
    ): String

    fun decrypt(
        cipherText: String,
    ): String

    fun canDecrypt(): Boolean
}

interface ConfigRendererSpi {
    fun supports(
        targetKind: ConfigTargetKind,
    ): Boolean

    fun render(
        target: ConfigTargetDto,
        entries: List<ConfigEntryDto>,
    ): String
}

interface ConfigBridgeSpi {
    val bridgeName: String
}
