package site.addzero.component.table.biz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * 行选择复选框插槽。
 */
@Composable
fun RenderCheckbox(
    isSelected: Boolean,
    editModeFlag: Boolean,
    slotWidthDp: Dp = 80.dp,
    onSelectionChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(slotWidthDp)
            .fillMaxHeight()
            .zIndex(2f),
        contentAlignment = Alignment.Center,
    ) {
        if (editModeFlag) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange,
            )
        }
    }
}
