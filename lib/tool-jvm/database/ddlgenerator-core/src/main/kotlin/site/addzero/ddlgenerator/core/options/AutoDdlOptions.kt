package site.addzero.ddlgenerator.core.options

data class AutoDdlOptions(
    val includeForeignKeys: Boolean = true,
    val includeIndexes: Boolean = true,
    val includeComments: Boolean = true,
    val includeSequences: Boolean = true,
)

data class AutoDdlDiffOptions(
    val ddlOptions: AutoDdlOptions = AutoDdlOptions(),
    val allowDestructiveChanges: Boolean = false,
    val excludeTables: List<String> = emptyList(),
    val excludeColumns: List<String> = emptyList(),
) {
    val includeForeignKeys
        get() = ddlOptions.includeForeignKeys

    val includeIndexes
        get() = ddlOptions.includeIndexes

    val includeComments
        get() = ddlOptions.includeComments

    val includeSequences
        get() = ddlOptions.includeSequences
}
