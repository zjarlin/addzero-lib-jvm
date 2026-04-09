package site.addzero.cupertino.workbench.scaffolding

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CupertinoWorkbenchSidebarMode {
  Expanded,
  Collapsed,
}

val CupertinoWorkbenchCollapsedSidebarWidth: Dp = 88.dp

val LocalCupertinoWorkbenchSidebarMode = staticCompositionLocalOf {
  CupertinoWorkbenchSidebarMode.Expanded
}
