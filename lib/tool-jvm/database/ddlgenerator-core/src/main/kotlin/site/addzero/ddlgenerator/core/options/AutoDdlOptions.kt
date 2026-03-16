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
    val includeForeignKeys: Boolean
        get() = ddlOptions.includeForeignKeys

    val includeIndexes: Boolean
        get() = ddlOptions.includeIndexes

    val includeComments: Boolean
        get() = ddlOptions.includeComments

    val includeSequences: Boolean
        get() = ddlOptions.includeSequences
}
