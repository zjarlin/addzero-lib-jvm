package site.addzero.cupertino.workbench.components.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip

@Composable
fun CupertinoEditorSaveBar(
  saving: Boolean,
  enabled: Boolean,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
  message: String = "右侧当前节点面板已进入编辑模式，修改后点击保存。",
  secondaryActions: @Composable RowScope.() -> Unit = {},
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      content = secondaryActions,
    )
    CupertinoStatusStrip(
      text = message,
      modifier = Modifier.weight(1f),
    )
    WorkbenchActionButton(
      text = if (saving) "保存中" else "保存",
      onClick = onSave,
      enabled = !saving && enabled,
    )
  }
}

@Composable
fun CupertinoEditorScaffold(
  modifier: Modifier = Modifier,
  summary: @Composable ColumnScope.() -> Unit,
  editor: @Composable ColumnScope.() -> Unit,
  saveBar: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    summary()
    editor()
    saveBar()
  }
}
