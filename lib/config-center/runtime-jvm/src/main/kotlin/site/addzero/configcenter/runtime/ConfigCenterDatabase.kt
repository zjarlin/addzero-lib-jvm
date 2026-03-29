package site.addzero.configcenter.runtime

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

class ConfigCenterDatabase(
    private val bootstrap: ConfigCenterBootstrap,
) {
    val jdbcUrl: String
        get() = "jdbc:sqlite:${bootstrap.dbFile.absolutePath}"

    init {
        Class.forName("org.sqlite.JDBC")
        ensureDatabaseReady()
    }

    fun <T> withConnection(
        block: (Connection) -> T,
    ): T {
        return DriverManager.getConnection(jdbcUrl).use(block)
    }

    private fun ensureDatabaseReady() {
        val dbFile = bootstrap.dbFile
        dbFile.parentFile?.ensureDirectory()
        if (!dbFile.exists()) {
            dbFile.createNewFile()
        }
        withConnection { connection ->
            executeSchema(connection)
            seedDefaults(connection, dbFile)
        }
    }

    private fun executeSchema(
        connection: Connection,
    ) {
        val schemaSql = javaClass
            .getResource("/site/addzero/configcenter/runtime/config-center-schema.sql")
            ?.readText()
            ?: error("缺少配置中心 schema 资源")

        splitSqlStatements(schemaSql).forEach { statement ->
            connection.createStatement().use { jdbc ->
                jdbc.execute(statement)
            }
        }
    }

    private fun seedDefaults(
        connection: Connection,
        dbFile: File,
    ) {
        val entryCount = connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM config_entry").use { resultSet ->
                if (resultSet.next()) resultSet.getInt(1) else 0
            }
        }
        if (entryCount == 0) {
            insertDefaultEntry(
                connection = connection,
                id = UUID.randomUUID().toString(),
                key = "ktor.deployment.host",
                namespace = "kcloud",
                domain = "SYSTEM",
                profile = "default",
                valueType = "STRING",
                storageMode = "REPO_PLAIN",
                plainText = "0.0.0.0",
                description = "Ktor 服务监听地址",
            )
            insertDefaultEntry(
                connection = connection,
                id = UUID.randomUUID().toString(),
                key = "ktor.deployment.port",
                namespace = "kcloud",
                domain = "SYSTEM",
                profile = "default",
                valueType = "INTEGER",
                storageMode = "REPO_PLAIN",
                plainText = "8080",
                description = "Ktor 服务监听端口",
            )
        }

        val targetCount = connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM config_target").use { resultSet ->
                if (resultSet.next()) resultSet.getInt(1) else 0
            }
        }
        if (targetCount == 0) {
            insertDefaultTarget(
                connection = connection,
                id = UUID.randomUUID().toString(),
                name = "KCloud Ktor HOCON",
                targetKind = "KTOR_HOCON",
                outputPath = "apps/kcloud/server/src/jvmMain/resources/application-generated.conf",
                namespaceFilter = "kcloud",
                profile = "default",
                templateText = null,
                sortOrder = 10,
            )
            insertDefaultTarget(
                connection = connection,
                id = UUID.randomUUID().toString(),
                name = "KCloud Dotenv",
                targetKind = "DOTENV",
                outputPath = "apps/kcloud/.env.generated",
                namespaceFilter = "kcloud",
                profile = "default",
                templateText = null,
                sortOrder = 20,
            )
            insertDefaultTarget(
                connection = connection,
                id = UUID.randomUUID().toString(),
                name = "Docker Compose 模板",
                targetKind = "DOCKER_COMPOSE_TEMPLATE",
                outputPath = "apps/kcloud/docker-compose.generated.yml",
                namespaceFilter = "kcloud",
                profile = "default",
                templateText = """
services:
  kcloud:
    environment:
      KTOR_DEPLOYMENT_HOST: "{{ktor.deployment.host}}"
      KTOR_DEPLOYMENT_PORT: "{{ktor.deployment.port}}"
""".trimIndent(),
                sortOrder = 30,
            )
        }

        writeBundleMeta(connection, "bundle.id", "kcloud-config-center")
        writeBundleMeta(connection, "bundle.path", dbFile.absolutePath)
        writeBundleMeta(connection, "schema.version", "1")
    }

    private fun insertDefaultEntry(
        connection: Connection,
        id: String,
        key: String,
        namespace: String,
        domain: String,
        profile: String,
        valueType: String,
        storageMode: String,
        plainText: String,
        description: String?,
    ) {
        val now = System.currentTimeMillis()
        connection.prepareStatement(
            """
            INSERT INTO config_entry (
                id, key, namespace, domain, profile, value_type, storage_mode,
                cipher_text, plain_text, description, tags_json, enabled, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, '[]', 1, ?, ?)
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, id)
            statement.setString(2, key)
            statement.setString(3, namespace)
            statement.setString(4, domain)
            statement.setString(5, profile)
            statement.setString(6, valueType)
            statement.setString(7, storageMode)
            statement.setString(8, plainText)
            statement.setString(9, description)
            statement.setLong(10, now)
            statement.setLong(11, now)
            statement.executeUpdate()
        }
    }

    private fun insertDefaultTarget(
        connection: Connection,
        id: String,
        name: String,
        targetKind: String,
        outputPath: String,
        namespaceFilter: String?,
        profile: String,
        templateText: String?,
        sortOrder: Int,
    ) {
        connection.prepareStatement(
            """
            INSERT INTO config_target (
                id, name, target_kind, output_path, namespace_filter, profile,
                template_text, enabled, sort_order
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?)
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, id)
            statement.setString(2, name)
            statement.setString(3, targetKind)
            statement.setString(4, outputPath)
            statement.setString(5, namespaceFilter)
            statement.setString(6, profile)
            statement.setString(7, templateText)
            statement.setInt(8, sortOrder)
            statement.executeUpdate()
        }
    }

    private fun writeBundleMeta(
        connection: Connection,
        key: String,
        value: String,
    ) {
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

    private fun splitSqlStatements(
        rawSql: String,
    ): List<String> {
        return rawSql
            .lineSequence()
            .filterNot { it.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}

private fun File.ensureDirectory(): File {
    if (!exists()) {
        mkdirs()
    }
    return this
}
