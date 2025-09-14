package site.addzero.component.table.biz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun <T> RenderCheckbox(
    item: T,
    itemId: Any,
    isSelected: Boolean,
    editModeFlag: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .zIndex(2f),
        contentAlignment = Alignment.Center
    ) {
        if (editModeFlag) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange
            )
        }
    }
}
