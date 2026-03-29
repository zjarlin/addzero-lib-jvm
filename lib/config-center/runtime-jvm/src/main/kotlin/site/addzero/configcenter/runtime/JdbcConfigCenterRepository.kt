package site.addzero.configcenter.runtime

import java.sql.ResultSet
import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import site.addzero.configcenter.spec.ConfigDomain
import site.addzero.configcenter.spec.ConfigEntryDto
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigRepositorySpi
import site.addzero.configcenter.spec.ConfigStorageMode
import site.addzero.configcenter.spec.ConfigTargetDto
import site.addzero.configcenter.spec.ConfigTargetKind
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.ConfigValueType

class JdbcConfigCenterRepository(
    private val database: ConfigCenterDatabase,
    private val encryption: site.addzero.configcenter.spec.ConfigEncryptionSpi,
    private val json: Json,
) : ConfigRepositorySpi {
    override suspend fun listEntries(
        query: ConfigQuery,
    ): List<ConfigEntryDto> {
        val keyword = query.keyword?.trim()?.takeIf { it.isNotEmpty() }
        return database.withConnection { connection ->
            val sql = buildString {
                append(
                    """
                    SELECT * FROM config_entry
                    WHERE profile = ?
                    """.trimIndent()
                )
                if (query.namespace != null) append(" AND namespace = ?")
                if (query.domain != null) append(" AND domain = ?")
                if (!query.includeDisabled) append(" AND enabled = 1")
                if (keyword != null) append(" AND (key LIKE ? OR description LIKE ?)")
                append(" ORDER BY namespace ASC, key ASC, updated_at DESC")
            }
            connection.prepareStatement(sql).use { statement ->
                var index = 1
                statement.setString(index++, query.profile)
                query.namespace?.let { statement.setString(index++, it) }
                query.domain?.let { statement.setString(index++, it.name) }
                if (keyword != null) {
                    val likeValue = "%$keyword%"
                    statement.setString(index++, likeValue)
                    statement.setString(index++, likeValue)
                }
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(resultSet.toEntryDto())
                        }
                    }
                }
            }
        }
    }

    override suspend fun getEntry(
        id: String,
    ): ConfigEntryDto? {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT * FROM config_entry WHERE id = ?",
            ).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.toEntryDto() else null
                }
            }
        }
    }

    override suspend fun findEntriesByKey(
        key: String,
        query: ConfigQuery,
    ): List<ConfigEntryDto> {
        return database.withConnection { connection ->
            val sql = buildString {
                append("SELECT * FROM config_entry WHERE key = ? AND profile = ?")
                if (query.namespace != null) append(" AND namespace = ?")
                if (query.domain != null) append(" AND domain = ?")
                if (!query.includeDisabled) append(" AND enabled = 1")
                append(" ORDER BY CASE storage_mode WHEN 'LOCAL_OVERRIDE' THEN 0 ELSE 1 END, updated_at DESC")
            }
            connection.prepareStatement(sql).use { statement ->
                var index = 1
                statement.setString(index++, key)
                statement.setString(index++, query.profile)
                query.namespace?.let { statement.setString(index++, it) }
                query.domain?.let { statement.setString(index++, it.name) }
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(resultSet.toEntryDto())
                        }
                    }
                }
            }
        }
    }

    override suspend fun upsertEntry(
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        return database.withConnection { connection ->
            val existing = request.id?.let { id ->
                connection.prepareStatement("SELECT created_at FROM config_entry WHERE id = ?").use { statement ->
                    statement.setString(1, id)
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) resultSet.getLong("created_at") else null
                    }
                }
            }
            if (request.id != null) {
                connection.prepareStatement("DELETE FROM config_entry WHERE id = ?").use { statement ->
                    statement.setString(1, request.id)
                    statement.executeUpdate()
                }
            } else if (request.storageMode == ConfigStorageMode.LOCAL_OVERRIDE) {
                connection.prepareStatement(
                    """
                    DELETE FROM config_entry
                    WHERE namespace = ? AND key = ? AND profile = ? AND storage_mode = 'LOCAL_OVERRIDE'
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, request.namespace)
                    statement.setString(2, request.key)
                    statement.setString(3, request.profile)
                    statement.executeUpdate()
                }
            } else {
                connection.prepareStatement(
                    """
                    DELETE FROM config_entry
                    WHERE namespace = ? AND key = ? AND profile = ?
                      AND storage_mode IN ('REPO_PLAIN', 'REPO_ENCRYPTED')
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, request.namespace)
                    statement.setString(2, request.key)
                    statement.setString(3, request.profile)
                    statement.executeUpdate()
                }
            }

            val now = System.currentTimeMillis()
            val entryId = request.id ?: UUID.randomUUID().toString()
            val cipherText = if (request.storageMode == ConfigStorageMode.REPO_ENCRYPTED) {
                encryption.encrypt(request.value)
            } else {
                null
            }
            val plainText = if (request.storageMode == ConfigStorageMode.REPO_ENCRYPTED) {
                null
            } else {
                request.value
            }

            connection.prepareStatement(
                """
                INSERT INTO config_entry (
                    id, key, namespace, domain, profile, value_type, storage_mode,
                    cipher_text, plain_text, description, tags_json, enabled, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, entryId)
                statement.setString(2, request.key)
                statement.setString(3, request.namespace)
                statement.setString(4, request.domain.name)
                statement.setString(5, request.profile)
                statement.setString(6, request.valueType.name)
                statement.setString(7, request.storageMode.name)
                statement.setString(8, cipherText)
                statement.setString(9, plainText)
                statement.setString(10, request.description)
                statement.setString(11, json.encodeToString(request.tags))
                statement.setInt(12, if (request.enabled) 1 else 0)
                statement.setLong(13, existing ?: now)
                statement.setLong(14, now)
                statement.executeUpdate()
            }

            connection.prepareStatement(
                "SELECT * FROM config_entry WHERE id = ?",
            ).use { statement ->
                statement.setString(1, entryId)
                statement.executeQuery().use { resultSet ->
                    require(resultSet.next()) {
                        "配置项写入后未找到记录"
                    }
                    resultSet.toEntryDto()
                }
            }
        }
    }

    override suspend fun deleteEntry(
        id: String,
    ) {
        database.withConnection { connection ->
            connection.prepareStatement("DELETE FROM config_entry WHERE id = ?").use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun listTargets(): List<ConfigTargetDto> {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT * FROM config_target ORDER BY sort_order ASC, name ASC",
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(resultSet.toTargetDto())
                        }
                    }
                }
            }
        }
    }

    override suspend fun getTarget(
        id: String,
    ): ConfigTargetDto? {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT * FROM config_target WHERE id = ?",
            ).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.toTargetDto() else null
                }
            }
        }
    }

    override suspend fun upsertTarget(
        request: ConfigTargetMutationRequest,
    ): ConfigTargetDto {
        return database.withConnection { connection ->
            val targetId = request.id ?: UUID.randomUUID().toString()
            connection.prepareStatement(
                """
                INSERT INTO config_target (
                    id, name, target_kind, output_path, namespace_filter, profile,
                    template_text, enabled, sort_order
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    name = excluded.name,
                    target_kind = excluded.target_kind,
                    output_path = excluded.output_path,
                    namespace_filter = excluded.namespace_filter,
                    profile = excluded.profile,
                    template_text = excluded.template_text,
                    enabled = excluded.enabled,
                    sort_order = excluded.sort_order
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, targetId)
                statement.setString(2, request.name)
                statement.setString(3, request.targetKind.name)
                statement.setString(4, request.outputPath)
                statement.setString(5, request.namespaceFilter)
                statement.setString(6, request.profile)
                statement.setString(7, request.templateText)
                statement.setInt(8, if (request.enabled) 1 else 0)
                statement.setInt(9, request.sortOrder)
                statement.executeUpdate()
            }
            connection.prepareStatement(
                "SELECT * FROM config_target WHERE id = ?",
            ).use { statement ->
                statement.setString(1, targetId)
                statement.executeQuery().use { resultSet ->
                    require(resultSet.next()) {
                        "渲染目标写入后未找到记录"
                    }
                    resultSet.toTargetDto()
                }
            }
        }
    }

    override suspend fun deleteTarget(
        id: String,
    ) {
        database.withConnection { connection ->
            connection.prepareStatement("DELETE FROM config_target WHERE id = ?").use { statement ->
                statement.setString(1, id)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun readBundleMeta(
        key: String,
    ): String? {
        return database.withConnection { connection ->
            connection.prepareStatement(
                "SELECT meta_value FROM config_bundle_meta WHERE meta_key = ?",
            ).use { statement ->
                statement.setString(1, key)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.getString("meta_value") else null
                }
            }
        }
    }

    override suspend fun writeBundleMeta(
        key: String,
        value: String,
    ) {
        database.withConnection { connection ->
            connection.prepareStatement(
                """
                INSERT INTO config_bundle_meta (meta_key, meta_value)
                VALUES (?, ?)
                ON CONFLICT(meta_key) DO UPDATE SET meta_value = excluded.meta_value
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, key)
                statement.setString(2, value)
                statement.executeUpdate()
            }
        }
    }

    private fun ResultSet.toEntryDto(): ConfigEntryDto {
        val storageMode = ConfigStorageMode.valueOf(getString("storage_mode"))
        val decryptedValue = when (storageMode) {
            ConfigStorageMode.REPO_ENCRYPTED -> {
                val cipherText = getString("cipher_text")
                if (cipherText.isNullOrBlank()) {
                    null
                } else {
                    runCatching { encryption.decrypt(cipherText) }.getOrNull()
                }
            }

            else -> getString("plain_text")
        }

        return ConfigEntryDto(
            id = getString("id"),
            key = getString("key"),
            namespace = getString("namespace"),
            domain = ConfigDomain.valueOf(getString("domain")),
            profile = getString("profile"),
            valueType = ConfigValueType.valueOf(getString("value_type")),
            storageMode = storageMode,
            value = decryptedValue,
            description = getString("description"),
            tags = decodeTags(getString("tags_json")),
            enabled = getInt("enabled") == 1,
            decryptionAvailable = storageMode != ConfigStorageMode.REPO_ENCRYPTED || decryptedValue != null,
            createdAtEpochMillis = getLong("created_at"),
            updatedAtEpochMillis = getLong("updated_at"),
        )
    }

    private fun ResultSet.toTargetDto(): ConfigTargetDto {
        return ConfigTargetDto(
            id = getString("id"),
            name = getString("name"),
            targetKind = ConfigTargetKind.valueOf(getString("target_kind")),
            outputPath = getString("output_path"),
            namespaceFilter = getString("namespace_filter"),
            profile = getString("profile"),
            templateText = getString("template_text"),
            enabled = getInt("enabled") == 1,
            sortOrder = getInt("sort_order"),
        )
    }

    private fun decodeTags(
        rawValue: String?,
    ): List<String> {
        if (rawValue.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching { json.decodeFromString<List<String>>(rawValue) }.getOrDefault(emptyList())
    }
}
