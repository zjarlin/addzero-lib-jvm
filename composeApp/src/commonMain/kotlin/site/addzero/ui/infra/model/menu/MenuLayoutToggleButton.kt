package site.addzero.ui.infra.model.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.viewmodel.SysRouteViewModel

/**
 * 导航栏横纵切换按钮
 * 点击时切换菜单的展开/折叠状态
 */
@Composable
context(sysRouteViewModel: SysRouteViewModel)
fun MenuLayoutToggleButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = sysRouteViewModel.isExpand,
    onToggle: () -> Unit = { sysRouteViewModel.isExpand = !sysRouteViewModel.isExpand }
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Default.Menu,
            contentDescription = if (isExpanded) "收起菜单" else "展开菜单",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}
