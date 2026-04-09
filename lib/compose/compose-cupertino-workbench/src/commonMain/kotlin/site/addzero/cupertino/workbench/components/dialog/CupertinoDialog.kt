@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.cupertino.workbench.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.robinpcrd.cupertino.CupertinoHorizontalDivider
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.theme.CupertinoTheme

@Composable
fun CupertinoDialog(
  title: String,
  onDismissRequest: () -> Unit,
  width: Dp = 760.dp,
  actions: @Composable RowScope.() -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
  ) {
    CupertinoSurface(
      modifier = Modifier.widthIn(max = width),
      color = CupertinoTheme.colorScheme.systemGroupedBackground,
      shape = CupertinoTheme.shapes.extraLarge,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        CupertinoText(
          text = title,
          style = CupertinoTheme.typography.title2,
        )
        CupertinoHorizontalDivider()
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 620.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          content = content,
        )
        CupertinoHorizontalDivider()
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
          content = actions,
        )
      }
    }
  }
}
