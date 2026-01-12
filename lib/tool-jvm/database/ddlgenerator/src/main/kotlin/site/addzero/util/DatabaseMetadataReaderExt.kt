package site.addzero.util

import site.addzero.util.lsi.database.model.IndexDefinition
import site.addzero.util.lsi.database.model.IndexType
import java.sql.DriverManager

/**
 * Get index metadata for a specific table
 */
fun DatabaseMetadataReader.getIndexMetadata(
    schema: String? = null,
    tableName: String
): List<IndexDefinition> {
    return withConnectionExt { connection ->
        val metadata = connection.metaData
        val indexes = mutableMapOf<String, MutableList<String>>()
        val indexTypes = mutableMapOf<String, Boolean>() // true = unique

        val indexInfo = metadata.getIndexInfo(null, schema, tableName, false, false)

        while (indexInfo.next()) {
            val indexName = indexInfo.getString("INDEX_NAME") ?: continue
            val columnName = indexInfo.getString("COLUMN_NAME") ?: continue
            val nonUnique = indexInfo.getBoolean("NON_UNIQUE")

            // Skip primary key indexes
            if (indexName.uppercase().contains("PRIMARY") ||
                indexName.uppercase().startsWith("PK_")) {
                continue
            }

            indexes.getOrPut(indexName) { mutableListOf() }.add(columnName)
            indexTypes[indexName] = !nonUnique
        }

        indexes.map { (indexName, columns) ->
            IndexDefinition(
                name = indexName,
                columns = columns,
                type = if (indexTypes[indexName] == true) IndexType.UNIQUE else IndexType.NORMAL
            )
        }
    }
}

/**
 * Helper function to execute database operations with connection
 */
private fun <T> DatabaseMetadataReader.withConnectionExt(block: (java.sql.Connection) -> T): T {
    // Access private fields via reflection (temporary solution)
    val urlField = this::class.java.getDeclaredField("url").apply { isAccessible = true }
    val usernameField = this::class.java.getDeclaredField("username").apply { isAccessible = true }
    val passwordField = this::class.java.getDeclaredField("password").apply { isAccessible = true }

    val url = urlField.get(this) as String
    val username = usernameField.get(this) as String
    val password = passwordField.get(this) as String

    return try {
        DriverManager.getConnection(url, username, password).use(block)
    } catch (e: Exception) {
        throw RuntimeException("Failed to connect to database", e)
    }
}
