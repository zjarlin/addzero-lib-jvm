@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.cupertino.workbench.components.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoActionSheet
import io.github.robinpcrd.cupertino.CupertinoBorderedTextField
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.cancel
import io.github.robinpcrd.cupertino.default
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.form.WorkbenchBorderedTextField
import site.addzero.cupertino.workbench.form.WorkbenchSwitch

data class CupertinoOption<T>(
  val value: T,
  val label: String,
  val caption: String? = null,
)

@Composable
fun CupertinoTextField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  singleLine: Boolean = true,
  description: String? = null,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    CupertinoText(
      text = label,
      style = CupertinoTheme.typography.subhead,
      color = CupertinoTheme.colorScheme.secondaryLabel,
    )
    WorkbenchBorderedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier.fillMaxWidth(),
      singleLine = singleLine,
      minLines = if (singleLine) 1 else 3,
      maxLines = if (singleLine) 1 else 5,
      placeholder = if (placeholder.isBlank()) {
        null
      } else {
        {
          CupertinoText(placeholder)
        }
      },
    )
    description?.takeIf(String::isNotBlank)?.let { text ->
      CupertinoText(
        text = text,
        style = CupertinoTheme.typography.footnote,
        color = CupertinoTheme.colorScheme.secondaryLabel,
      )
    }
  }
}

@Composable
fun CupertinoBooleanField(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  description: String? = null,
) {
  CupertinoSurface(
    modifier = modifier.fillMaxWidth(),
    color = CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
    shape = CupertinoTheme.shapes.medium,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        CupertinoText(
          text = label,
          style = CupertinoTheme.typography.body,
        )
        description?.takeIf(String::isNotBlank)?.let { text ->
          CupertinoText(
            text = text,
            style = CupertinoTheme.typography.footnote,
            color = CupertinoTheme.colorScheme.secondaryLabel,
          )
        }
      }
      WorkbenchSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
      )
    }
  }
}

@Composable
fun <T> CupertinoSelectionField(
  label: String,
  options: List<CupertinoOption<T>>,
  selectedValue: T?,
  onSelected: (T?) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "请选择",
  allowClear: Boolean = false,
  description: String? = null,
) {
  var sheetVisible by remember { mutableStateOf(false) }
  val selectedOption = options.firstOrNull { option -> option.value == selectedValue }
  val selectedLabel = selectedOption?.label.orEmpty()

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    CupertinoText(
      text = label,
      style = CupertinoTheme.typography.subhead,
      color = CupertinoTheme.colorScheme.secondaryLabel,
    )
    CupertinoBorderedTextField(
      value = selectedLabel,
      onValueChange = {},
      modifier = Modifier.fillMaxWidth(),
      readOnly = true,
      singleLine = true,
      placeholder = {
        CupertinoText(placeholder)
      },
      trailingIcon = {
        WorkbenchActionButton(
          text = if (selectedLabel.isBlank()) "选择" else "变更",
          onClick = { sheetVisible = true },
          variant = WorkbenchButtonVariant.Outline,
        )
      },
    )
    selectedOption?.caption?.takeIf(String::isNotBlank)?.let { text ->
      CupertinoText(
        text = text,
        style = CupertinoTheme.typography.footnote,
        color = CupertinoTheme.colorScheme.secondaryLabel,
      )
    }
    description?.takeIf(String::isNotBlank)?.let { text ->
      CupertinoText(
        text = text,
        style = CupertinoTheme.typography.footnote,
        color = CupertinoTheme.colorScheme.secondaryLabel,
      )
    }
  }

  CupertinoActionSheet(
    visible = sheetVisible,
    onDismissRequest = { sheetVisible = false },
    title = { CupertinoText(label) },
    buttons = {
      options.forEach { option ->
        default(
          onClick = {
            onSelected(option.value)
            sheetVisible = false
          },
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            CupertinoText(option.label)
            option.caption?.takeIf(String::isNotBlank)?.let { text ->
              CupertinoText(
                text = text,
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
              )
            }
          }
        }
      }
      if (allowClear) {
        default(
          onClick = {
            onSelected(null)
            sheetVisible = false
          },
        ) {
          CupertinoText("清空")
        }
      }
      cancel(
        onClick = { sheetVisible = false },
      ) {
        CupertinoText("取消")
      }
    },
  )
}
