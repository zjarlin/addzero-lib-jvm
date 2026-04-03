package site.addzero.component.table.biz

import androidx.compose.runtime.Composable

/**
 * 批量选择摘要条。
 */
@Composable
fun <ID : Any> RenderSelectContent(
    editModeFlag: Boolean,
    selectedItemIds: Set<ID>,
    onClearSelection: () -> Unit,
    onBatchDelete: (() -> Unit)?,
    onBatchExport: (() -> Unit)?,
) {
    if (editModeFlag && selectedItemIds.isNotEmpty()) {
        TableSelectionSummary(
            selectedCount = selectedItemIds.size,
            onClearSelection = onClearSelection,
            onBatchExport = onBatchExport,
            onBatchDelete = onBatchDelete?.let { action ->
                {
                    action()
                    onClearSelection()
                }
            },
        )
    }
}
