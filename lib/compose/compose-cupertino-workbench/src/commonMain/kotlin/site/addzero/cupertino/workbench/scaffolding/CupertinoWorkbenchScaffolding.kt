package site.addzero.cupertino.workbench.scaffolding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.addzero.appsidebar.LocalWorkbenchWindowFrame
import site.addzero.appsidebar.WorkbenchScaffold
import site.addzero.appsidebar.spi.scaffoldConfig
import site.addzero.appsidebar.spi.sidebarResizeConfig
import site.addzero.appsidebar.workbenchScaffoldDecor
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Surface
import site.addzero.cupertino.workbench.metrics.currentWorkbenchMetrics
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Composable
fun RenderCupertinoWorkbenchScaffolding(
  scaffolding: ScaffoldingSpi,
  modifier: Modifier = Modifier,
  sidebarVisible: Boolean = true,
  onSidebarToggle: (() -> Unit)? = null,
  defaultSidebarRatio: Float = currentWorkbenchMetrics().sidebarRatio,
  minSidebarWidth: Dp = currentWorkbenchMetrics().sidebarMinWidth,
  maxSidebarWidth: Dp = currentWorkbenchMetrics().sidebarMaxWidth,
) {
  val metrics = currentWorkbenchMetrics()
  val windowFrame = LocalWorkbenchWindowFrame.current
  val topBarHeight = if (windowFrame.immersiveTopBar) {
    windowFrame.topBarHeight
  } else {
    metrics.topBarHeight
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    androidx.compose.foundation.layout.Column(
      modifier = Modifier.fillMaxSize(),
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 10.dp),
        shape = RoundedCornerShape(metrics.headerPanelRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .padding(
              start = windowFrame.leadingInset + 16.dp,
              top = 10.dp,
              end = windowFrame.trailingInset + 16.dp,
              bottom = 10.dp,
            ),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            with(scaffolding) {
              RenderBrand()
              RenderHeader(
                modifier = Modifier.weight(1f),
              )
            }
          }
          with(scaffolding) {
            RenderTopBarActions()
          }
        }
      }

      WorkbenchScaffold(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        sidebar = {
          scaffolding.RenderSidebar(
            modifier = Modifier.fillMaxSize(),
          )
        },
        content = {
          scaffolding.RenderContent(
            modifier = Modifier.fillMaxSize(),
          )
        },
        config = scaffoldConfig(
          defaultSidebarRatio = if (sidebarVisible) defaultSidebarRatio else 0f,
          minSidebarWidth = if (sidebarVisible) minSidebarWidth else 0.dp,
          maxSidebarWidth = if (sidebarVisible) maxSidebarWidth else 0.dp,
        ),
        decor = workbenchScaffoldDecor(
          sidebarContainerModifier = Modifier.background(MaterialTheme.colorScheme.background),
          mainContainerModifier = Modifier.background(MaterialTheme.colorScheme.background),
          headerContainerModifier = Modifier.background(MaterialTheme.colorScheme.background),
          detailContainerModifier = Modifier.background(MaterialTheme.colorScheme.background),
          resizeConfig = sidebarResizeConfig(
            dividerColor = MaterialTheme.colorScheme.outlineVariant,
            thumbColor = MaterialTheme.colorScheme.surfaceVariant,
            thumbBorderColor = MaterialTheme.colorScheme.outline,
          ),
        ),
      )
    }
  }
}
