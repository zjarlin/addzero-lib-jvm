package site.addzero.ddlgenerator.core.flyway

import java.math.BigInteger
import java.security.MessageDigest
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ExistingFlywayMigration(
    val version: String,
    val sql: String,
)

data class FlywayMigrationVersionRequest(
    val appliedVersions: Collection<String> = emptyList(),
    val existingMigrations: Collection<ExistingFlywayMigration> = emptyList(),
    val namespace: String,
    val sql: String,
)

data class FlywayMigrationVersionResolution(
    val version: String,
    val reusedExisting: Boolean,
)

/**
 * 根据 Flyway 历史、源码迁移和 SQL 内容生成稳定的新迁移版本。
 */
class FlywayMigrationVersionGenerator(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun generate(request: FlywayMigrationVersionRequest): FlywayMigrationVersionResolution {
        require(request.namespace.isNotBlank()) { "Flyway 迁移命名空间不能为空" }
        require(request.sql.isNotBlank()) { "Flyway 迁移 SQL 不能为空" }

        val appliedVersions = request.appliedVersions.map(::parseVersion)
        val latestAppliedVersion = appliedVersions.maxOrNull()
        val normalizedSql = normalizeSql(request.sql)
        val existingMigrations = request.existingMigrations.map { migration ->
            ParsedExistingMigration(
                source = migration,
                version = parseVersion(migration.version),
                normalizedSql = normalizeSql(migration.sql),
            )
        }
        val reusableMigration = existingMigrations
            .asSequence()
            .filter { migration -> migration.normalizedSql == normalizedSql }
            .filter { migration -> latestAppliedVersion == null || migration.version > latestAppliedVersion }
            .filter { migration -> migration.source.version.length <= MAX_FLYWAY_VERSION_LENGTH }
            .maxByOrNull(ParsedExistingMigration::version)
        if (reusableMigration != null) {
            return FlywayMigrationVersionResolution(
                version = reusableMigration.source.version,
                reusedExisting = true,
            )
        }

        val latestKnownVersion = (appliedVersions + existingMigrations.map(ParsedExistingMigration::version)).maxOrNull()
        val identityFingerprint = stableNumericFingerprint("${request.namespace}\u0000$normalizedSql")
        val clockVersion = clockVersion(identityFingerprint)
        val resolvedVersion = if (latestKnownVersion == null || clockVersion > latestKnownVersion) {
            clockVersion
        } else {
            versionAfter(
                latestVersion = latestKnownVersion,
                identityFingerprint = identityFingerprint,
            )
        }
        check(resolvedVersion.value.length <= MAX_FLYWAY_VERSION_LENGTH) {
            "无法在 Flyway 历史表 version VARCHAR($MAX_FLYWAY_VERSION_LENGTH) 限制内生成更高版本: ${resolvedVersion.value}"
        }
        return FlywayMigrationVersionResolution(
            version = resolvedVersion.value,
            reusedExisting = false,
        )
    }

    private fun clockVersion(identityFingerprint: BigInteger): ParsedFlywayVersion {
        val now = LocalDateTime.now(clock)
        return ParsedFlywayVersion(
            parts = listOf(
                now.format(DATE_FORMATTER).toBigInteger(),
                now.format(TIME_FORMATTER).toBigInteger(),
                now.nano.div(NANOS_PER_MILLISECOND).toBigInteger(),
                BigInteger.ZERO,
                identityFingerprint,
            ),
        )
    }

    private fun versionAfter(
        latestVersion: ParsedFlywayVersion,
        identityFingerprint: BigInteger,
    ): ParsedFlywayVersion {
        val datePart = latestVersion.parts.getOrElse(0) { BigInteger.ZERO }
        val timePart = latestVersion.parts.getOrElse(1) { BigInteger.ZERO }
        val millisecondPart = latestVersion.parts.getOrElse(2) { BigInteger.ZERO }
        val sequencePart = latestVersion.parts.getOrElse(3) { BigInteger.ZERO } + BigInteger.ONE
        val parts = listOf(
            datePart,
            timePart,
            millisecondPart,
            sequencePart,
            identityFingerprint,
        )
        return ParsedFlywayVersion(
            parts = parts,
        )
    }

    private data class ParsedExistingMigration(
        val source: ExistingFlywayMigration,
        val version: ParsedFlywayVersion,
        val normalizedSql: String,
    )

    private companion object {
        const val NANOS_PER_MILLISECOND = 1_000_000
        const val MAX_FLYWAY_VERSION_LENGTH = 50
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    }
}

internal data class ParsedFlywayVersion(
    val parts: List<BigInteger>,
) : Comparable<ParsedFlywayVersion> {
    val value: String
        get() = parts.joinToString(".")

    override fun compareTo(other: ParsedFlywayVersion): Int {
        val maxPartCount = maxOf(parts.size, other.parts.size)
        repeat(maxPartCount) { index ->
            val leftPart = parts.getOrElse(index) { BigInteger.ZERO }
            val rightPart = other.parts.getOrElse(index) { BigInteger.ZERO }
            val comparison = leftPart.compareTo(rightPart)
            if (comparison != 0) {
                return comparison
            }
        }
        return 0
    }
}

internal fun parseVersion(value: String): ParsedFlywayVersion {
    val normalizedValue = value.trim()
    require(normalizedValue.isNotBlank()) { "Flyway 版本不能为空" }
    val rawParts = normalizedValue.split('.', '_')
    require(rawParts.all { part -> part.isNotBlank() && part.all(Char::isDigit) }) {
        "Flyway 版本只能包含数字、点和下划线: $value"
    }
    return ParsedFlywayVersion(
        parts = rawParts.map(String::toBigInteger),
    )
}

private fun normalizeSql(sql: String): String {
    return sql
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .trim()
}

private fun stableNumericFingerprint(value: String): BigInteger {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
    return BigInteger(1, digest.copyOfRange(0, Long.SIZE_BYTES)).max(BigInteger.ONE)
}
