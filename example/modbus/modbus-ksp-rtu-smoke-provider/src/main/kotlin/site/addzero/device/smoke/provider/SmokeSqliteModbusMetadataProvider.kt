package site.addzero.device.smoke.provider

import java.io.File
import java.sql.DriverManager
import site.addzero.device.protocol.modbus.ksp.core.CollectedModbusService
import site.addzero.device.protocol.modbus.ksp.core.ModbusDatabaseMetadataProvider
import site.addzero.device.protocol.modbus.ksp.core.ModbusMetadataCollectionContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusMetadataProvider

class SmokeSqliteModbusMetadataProvider : ModbusMetadataProvider {
    override val providerId: String = "smoke-sqlite"

    override fun isEnabled(context: ModbusMetadataCollectionContext): Boolean =
        SmokeModbusProviderSupport.resolveSqliteFile(context.environment) != null

    override fun collect(context: ModbusMetadataCollectionContext): List<CollectedModbusService> {
        val sqliteFile =
            requireNotNull(SmokeModbusProviderSupport.resolveSqliteFile(context.environment)) {
                "Missing ${SmokeModbusProviderSupport.SQLITE_PATH_OPTION} for smoke sqlite metadata provider."
            }
        seedDatabase(sqliteFile)
        context.environment.logger.logging("Seeded smoke Modbus metadata sqlite: ${sqliteFile.absolutePath}")
        return ModbusDatabaseMetadataProvider().collect(context)
    }

    private fun seedDatabase(sqliteFile: File) {
        sqliteFile.parentFile?.mkdirs()
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:${sqliteFile.absolutePath}").use { connection ->
            connection.autoCommit = false
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    create table if not exists modbus_metadata (
                        id integer primary key autoincrement,
                        json_payload text not null
                    )
                    """.trimIndent(),
                )
                statement.executeUpdate("delete from modbus_metadata")
            }
            connection.prepareStatement("insert into modbus_metadata(json_payload) values (?)").use { statement ->
                RESOURCE_PATHS.forEach { resourcePath ->
                    statement.setString(1, readResource(resourcePath))
                    statement.addBatch()
                }
                statement.executeBatch()
            }
            connection.commit()
        }
    }

    private fun readResource(resourcePath: String): String =
        resolveSourceResourceFile(resourcePath)
            ?.takeIf(File::exists)
            ?.readText()
            ?: requireNotNull(javaClass.getResource(resourcePath)) {
                "Missing smoke metadata resource: $resourcePath"
            }.readText()

    private fun resolveSourceResourceFile(resourcePath: String): File? {
        val codeSourceFile =
            runCatching { File(javaClass.protectionDomain.codeSource.location.toURI()) }.getOrNull()
                ?: return null
        var current: File? = if (codeSourceFile.isFile) codeSourceFile.parentFile else codeSourceFile
        while (current != null) {
            val resourcesDir = current.resolve("src/main/resources")
            if (resourcesDir.isDirectory) {
                return resourcesDir.resolve(resourcePath.removePrefix("/"))
            }
            current = current.parentFile
        }
        return null
    }

    companion object {
        private val RESOURCE_PATHS =
            listOf(
                "/site/addzero/device/smoke/provider/device-service.json",
                "/site/addzero/device/smoke/provider/flash-service.json",
            )
    }
}
