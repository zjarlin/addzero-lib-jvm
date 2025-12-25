package site.addzero.assist

/**
 * 从 JDBC URL 中提取 schema
 */
fun extractSchemaFromUrl(url: String): String? {
    return when {
        // PostgreSQL: jdbc:postgresql://host:port/database?schema=schema
        url.startsWith("jdbc:postgresql:") -> {
            val uri = url.substring("jdbc:postgresql:".length)
            // 查找 schema 参数
            val schemaParam = Regex("[?&]schema=([^&]*)").find(uri)?.groupValues?.get(1)
            schemaParam
        }

        // MySQL: jdbc:mysql://host:port/database
        url.startsWith("jdbc:mysql:") -> {
            val parts = url.substringAfter("jdbc:mysql://").split("/")
            if (parts.size > 1) {
                val substringBefore = parts[1].substringBefore("?")
                substringBefore
            } else {
                null
            }
        }

        // Oracle: jdbc:oracle:thin:@host:port:sid
        url.startsWith("jdbc:oracle:") -> {
            null
        }

        // SQL Server: jdbc:sqlserver://host:port;databaseName=database
        url.startsWith("jdbc:sqlserver:") -> {
            val databaseParam = Regex("[?;]databaseName=([^;]*)").find(url)?.groupValues?.get(1)
            databaseParam ?: "dbo"
        }

        // H2: jdbc:h2:mem:testdb or jdbc:h2:file:/path/to/database
        url.startsWith("jdbc:h2:") -> {
            when {
                url.contains("mem:") -> "PUBLIC"
                url.contains("file:") -> {
                    val path = url.substringAfter("jdbc:h2:file:")
                    path.substringAfterLast("/").substringBefore(";")
                }

                else -> "PUBLIC"
            }
        }

        // SQLite: jdbc:sqlite:path/to/database.db
        url.startsWith("jdbc:sqlite:") -> "main"

        // 达梦: jdbc:dm://host:port/database
        url.startsWith("jdbc:dm:") -> {
            val parts = url.substringAfter("jdbc:dm://").split("/")
            if (parts.size > 1) {
                parts[1].substringBefore("?")
            } else {
                null
            }
        }

        // 人大金仓: jdbc:kingbase8://host:port/database
        url.startsWith("jdbc:kingbase:") -> {
            val parts = url.substringAfter("jdbc:kingbase://").split("/")
            if (parts.size > 1) {
                parts[1].substringBefore("?")
            } else {
                null
            }
        }

        // 高斯: jdbc:gaussdb://host:port/database
        url.startsWith("jdbc:gaussdb:") -> {
            val parts = url.substringAfter("jdbc:gaussdb://").split("/")
            if (parts.size > 1) {
                parts[1].substringBefore("?")
            } else {
                null
            }
        }

        // DB2: jdbc:db2://host:port/database
        url.startsWith("jdbc:db2:") -> {
            val parts = url.substringAfter("jdbc:db2://").split("/")
            if (parts.size > 1) {
                parts[1].substringBefore("?")
            } else {
                null
            }
        }

        // 其他数据库使用默认值
        else -> "public"
    }
}