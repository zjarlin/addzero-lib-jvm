package site.addzero.component.tree_command

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.compose.applecorner.AppleRoundedDefaults
import site.addzero.compose.applecorner.appleRounded

/**
 * 命令工具栏
 */
@Composable
fun CommandToolbar(
    commands: Set<site.addzero.component.tree_command.TreeCommand>,
    multiSelectMode: Boolean,
    onCommandClick: (site.addzero.component.tree_command.TreeCommand) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .appleRounded(
                radius = AppleRoundedDefaults.Large,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.54f)),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            commands.forEach { command ->
                val (icon, tint) = when (command) {
                    site.addzero.component.tree_command.TreeCommand.SEARCH -> Icons.Default.Search to MaterialTheme.colorScheme.primary
                    site.addzero.component.tree_command.TreeCommand.MULTI_SELECT -> {
                        if (multiSelectMode)
                            Icons.Default.CheckBox to MaterialTheme.colorScheme.primary
                        else
                            Icons.Default.CheckBoxOutlineBlank to MaterialTheme.colorScheme.onSurface
                    }

                    site.addzero.component.tree_command.TreeCommand.EXPAND_ALL -> Icons.Default.UnfoldMore to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.COLLAPSE_ALL -> Icons.Default.UnfoldLess to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.REFRESH -> Icons.Default.Refresh to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.FILTER -> Icons.Default.FilterList to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.SORT -> Icons.AutoMirrored.Filled.Sort to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.ADD_NODE -> Icons.Default.Add to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.EDIT_NODE -> Icons.Default.Edit to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.DELETE_NODE -> Icons.Default.Delete to MaterialTheme.colorScheme.error
                    site.addzero.component.tree_command.TreeCommand.DRAG_DROP -> Icons.Default.DragIndicator to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.EXPORT -> Icons.Default.FileDownload to MaterialTheme.colorScheme.onSurface
                    site.addzero.component.tree_command.TreeCommand.IMPORT -> Icons.Default.FileUpload to MaterialTheme.colorScheme.onSurface
                }

                val isActive = command == site.addzero.component.tree_command.TreeCommand.SEARCH ||
                    (command == site.addzero.component.tree_command.TreeCommand.MULTI_SELECT && multiSelectMode)

                Box(
                    modifier = Modifier.appleRounded(
                        radius = AppleRoundedDefaults.Small,
                        containerColor = if (isActive) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.01f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isActive) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
                            },
                        ),
                    ),
                ) {
                    IconButton(onClick = { onCommandClick(command) }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = command.name,
                            tint = tint,
                        )
                    }
                }
            }
        }
    }
}
