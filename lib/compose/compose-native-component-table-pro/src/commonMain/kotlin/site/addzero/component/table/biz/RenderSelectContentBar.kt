package site.addzero.component.table.biz

import androidx.compose.runtime.Composable

/**
 * 批量选择摘要条。
 */
@Composable
fun RenderSelectContent(
    editModeFlag: Boolean,
    selectedItemIds: Set<Any>,
    onClearSelection: () -> Unit,
    onBatchDelete: () -> Unit,
    onBatchExport: () -> Unit,
) {
    if (editModeFlag && selectedItemIds.isNotEmpty()) {
        TableSelectionSummary(
            selectedCount = selectedItemIds.size,
            onClearSelection = onClearSelection,
            onBatchDelete = {
                onBatchDelete()
                onClearSelection()
            },
            onBatchExport = onBatchExport,
        )
    }
}
