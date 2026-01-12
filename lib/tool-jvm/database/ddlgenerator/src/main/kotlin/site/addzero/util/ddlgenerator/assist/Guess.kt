package site.addzero.util.ddlgenerator.assist

import site.addzero.util.db.DatabaseType

fun guessDabaseType(url: String): DatabaseType {
    val fromUrl = DatabaseType.fromUrl(url)
    val type = fromUrl ?: throw IllegalArgumentException("Can't guess database type from jdbcUrl '$url'")
    return type
}
private fun extractDatabaseFromUrl(url: String, prefix: String): String? =
    url.substringAfter("$prefix//").split("/")
        .takeIf { it.size > 1 }
        ?.get(1)
        ?.substringBefore("?")
fun guessSchema(url: String): String? = when {
    url.startsWith("jdbc:postgresql:") ->
        Regex("[?&]schema=([^&]*)").find(url)?.groupValues?.get(1)

    url.startsWith("jdbc:mysql:") ->
        extractDatabaseFromUrl(url, "jdbc:mysql:")

    url.startsWith("jdbc:oracle:") -> null

    url.startsWith("jdbc:sqlserver:") ->
        Regex("[?;]databaseName=([^;]*)").find(url)?.groupValues?.get(1) ?: "dbo"

    url.startsWith("jdbc:h2:") -> when {
        url.contains("mem:") -> "PUBLIC"
        url.contains("file:") -> url.substringAfter("jdbc:h2:file:")
            .substringAfterLast("/").substringBefore(";")

        else -> "PUBLIC"
    }

    url.startsWith("jdbc:sqlite:") -> "main"

    url.startsWith("jdbc:dm:") ->
        extractDatabaseFromUrl(url, "jdbc:dm:")

    url.startsWith("jdbc:kingbase:") ->
        extractDatabaseFromUrl(url, "jdbc:kingbase:")

    url.startsWith("jdbc:gaussdb:") ->
        extractDatabaseFromUrl(url, "jdbc:gaussdb:")

    url.startsWith("jdbc:db2:") ->
        extractDatabaseFromUrl(url, "jdbc:db2:")

    else -> "public"
}