package site.addzero.cupertino.workbench.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

interface WorkbenchTopBarActionContributor {
  val order
    get() = 0

  @Composable
  fun RowScope.Render()
}

@Composable
fun WorkbenchTopBarActionsHost(
  contributors: List<WorkbenchTopBarActionContributor>,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    contributors
      .sortedBy(WorkbenchTopBarActionContributor::order)
      .forEach { contributor ->
        with(contributor) {
          Render()
        }
      }
  }
}

@Composable
fun RowScope.CupertinoWorkbenchTopBarActionsHost(
  contributors: List<WorkbenchTopBarActionContributor>,
) {
  WorkbenchTopBarActionsHost(
    contributors = contributors,
  )
}
