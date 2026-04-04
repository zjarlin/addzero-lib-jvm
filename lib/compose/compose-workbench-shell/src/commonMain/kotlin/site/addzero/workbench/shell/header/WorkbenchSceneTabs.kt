package site.addzero.workbench.shell.header

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.workbench.design.button.WorkbenchButtonSize
import site.addzero.workbench.design.button.WorkbenchButtonVariant
import site.addzero.workbench.design.button.WorkbenchPillButton
import site.addzero.workbench.shell.metrics.currentWorkbenchMetrics

@Composable
fun <T> WorkbenchSceneTabs(
    items: List<T>,
    selectedId: Any?,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemId: (T) -> Any,
    itemLabel: (T) -> String,
    itemIcon: (T) -> ImageVector,
) {
    val metrics = currentWorkbenchMetrics()
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = itemId(item) == selectedId
            WorkbenchPillButton(
                onClick = { onItemClick(item) },
                variant = if (selected) {
                    WorkbenchButtonVariant.Default
                } else {
                    WorkbenchButtonVariant.Outline
                },
                size = if (metrics.compact) {
                    WorkbenchButtonSize.Sm
                } else {
                    WorkbenchButtonSize.Default
                },
            ) {
                Icon(
                    imageVector = itemIcon(item),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = itemLabel(item),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}
