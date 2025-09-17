package site.addzero.cli.os

interface OperatingSystem {
    val packageManager: String
    val configFile: String

    fun detect(): Boolean
    fun setupPackageManager(config: PackageManagerConfig): Boolean
    fun createSymlink(source: String, target: String): Boolean
    fun executeCommand(command: String): String
}
