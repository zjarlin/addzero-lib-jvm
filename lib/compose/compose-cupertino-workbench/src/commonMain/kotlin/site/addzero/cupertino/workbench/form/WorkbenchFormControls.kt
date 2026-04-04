package site.addzero.cupertino.workbench.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import io.github.robinpcrd.cupertino.CupertinoBorderedTextField
import io.github.robinpcrd.cupertino.CupertinoSlider
import io.github.robinpcrd.cupertino.CupertinoSwitch
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import site.addzero.cupertino.workbench.material3.MaterialTheme

@Composable
fun WorkbenchBorderedTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  singleLine: Boolean = true,
  minLines: Int = 1,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
  placeholder: (@Composable () -> Unit)? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None,
) {
  CupertinoBorderedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = singleLine,
    minLines = minLines,
    maxLines = maxLines,
    textStyle = textStyle,
    placeholder = placeholder,
    visualTransformation = visualTransformation,
    contentAlignment = Alignment.CenterVertically,
  )
}

@Composable
fun WorkbenchSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  CupertinoSwitch(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
  )
}

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun WorkbenchSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
  CupertinoSlider(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    valueRange = valueRange,
  )
}
