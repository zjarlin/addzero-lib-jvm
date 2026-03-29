package site.addzero.kbox.core.service

import site.addzero.kbox.core.model.KboxDetectedPackageManager
import site.addzero.kbox.core.model.KboxPackageImportEntryResult

interface KboxPackageManagerAdapter {
    val managerId: String
    val displayName: String

    fun detect(): KboxDetectedPackageManager

    fun exportInstalledPackages(): List<String>

    fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult
}
