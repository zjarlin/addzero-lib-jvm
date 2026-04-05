package site.addzero.cupertino.workbench.material3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.AlertDialog as M3AlertDialog
import androidx.compose.material3.Button as M3Button
import androidx.compose.material3.CircularProgressIndicator as M3CircularProgressIndicator
import androidx.compose.material3.Checkbox as M3Checkbox
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults as M3CardDefaults
import androidx.compose.material3.Card as M3Card
import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.ElevatedCard as M3ElevatedCard
import androidx.compose.material3.FilterChip as M3FilterChip
import androidx.compose.material3.AssistChip as M3AssistChip
import androidx.compose.material3.FilledTonalButton as M3FilledTonalButton
import androidx.compose.material3.HorizontalDivider as M3HorizontalDivider
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.OutlinedTextField as M3OutlinedTextField
import androidx.compose.material3.OutlinedButton as M3OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults as M3OutlinedTextFieldDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Switch as M3Switch
import androidx.compose.material3.Tab as M3Tab
import androidx.compose.material3.SecondaryTabRow as M3SecondaryTabRow
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MaterialTheme {
  val colorScheme
      @Composable
    @ReadOnlyComposable
    get() = M3MaterialTheme.colorScheme

  val typography
      @Composable
    @ReadOnlyComposable
    get() = M3MaterialTheme.typography

  val shapes
      @Composable
    @ReadOnlyComposable
    get() = M3MaterialTheme.shapes
}

object CardDefaults {
  @Composable
  fun cardColors(
    containerColor: Color = M3MaterialTheme.colorScheme.surface,
    contentColor: Color = M3MaterialTheme.colorScheme.onSurface,
  ): CardColors {
    return M3CardDefaults.cardColors(
      containerColor = containerColor,
      contentColor = contentColor,
    )
  }
}

object OutlinedTextFieldDefaults {
  @Composable
  fun colors(
    focusedTextColor: Color = M3MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor: Color = M3MaterialTheme.colorScheme.onSurface,
    focusedBorderColor: Color = M3MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color = M3MaterialTheme.colorScheme.outlineVariant,
    disabledBorderColor: Color = M3MaterialTheme.colorScheme.outlineVariant,
    errorBorderColor: Color = M3MaterialTheme.colorScheme.error,
    focusedLabelColor: Color = M3MaterialTheme.colorScheme.primary,
    unfocusedLabelColor: Color = M3MaterialTheme.colorScheme.onSurfaceVariant,
    focusedContainerColor: Color = Color.Transparent,
    unfocusedContainerColor: Color = Color.Transparent,
    disabledContainerColor: Color = Color.Transparent,
    errorContainerColor: Color = Color.Transparent,
    cursorColor: Color = M3MaterialTheme.colorScheme.primary,
  ): TextFieldColors {
    return M3OutlinedTextFieldDefaults.colors(
      focusedTextColor = focusedTextColor,
      unfocusedTextColor = unfocusedTextColor,
      focusedBorderColor = focusedBorderColor,
      unfocusedBorderColor = unfocusedBorderColor,
      disabledBorderColor = disabledBorderColor,
      errorBorderColor = errorBorderColor,
      focusedLabelColor = focusedLabelColor,
      unfocusedLabelColor = unfocusedLabelColor,
      focusedContainerColor = focusedContainerColor,
      unfocusedContainerColor = unfocusedContainerColor,
      disabledContainerColor = disabledContainerColor,
      errorContainerColor = errorContainerColor,
      cursorColor = cursorColor,
    )
  }
}

@Composable
fun Text(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = Color.Unspecified,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontStyle: FontStyle? = null,
  fontWeight: FontWeight? = null,
  fontFamily: FontFamily? = null,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  textDecoration: TextDecoration? = null,
  textAlign: TextAlign? = null,
  lineHeight: TextUnit = TextUnit.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  style: TextStyle = M3MaterialTheme.typography.bodyMedium,
  maxLines: Int = Int.MAX_VALUE,
  softWrap: Boolean = true,
  onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
  M3Text(
    text = text,
    modifier = modifier,
    color = color,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    style = style,
    maxLines = maxLines,
    softWrap = softWrap,
    onTextLayout = onTextLayout,
  )
}

@Composable
fun Icon(
  imageVector: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  tint: Color = androidx.compose.material3.LocalContentColor.current,
) {
  M3Icon(
    imageVector = imageVector,
    contentDescription = contentDescription,
    modifier = modifier,
    tint = tint,
  )
}

@Composable
fun Surface(
  modifier: Modifier = Modifier,
  shape: Shape = M3MaterialTheme.shapes.large,
  color: Color = M3MaterialTheme.colorScheme.surface,
  contentColor: Color = M3MaterialTheme.colorScheme.onSurface,
  border: BorderStroke? = null,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
  tonalElevation: Dp = 0.dp,
  shadowElevation: Dp = 0.dp,
  content: @Composable () -> Unit,
) {
  M3Surface(
    modifier = if (onClick != null) {
      modifier.clickable(
        enabled = enabled,
        onClick = onClick,
      )
    } else {
      modifier
    },
    shape = shape,
    color = color,
    contentColor = contentColor,
    border = border,
    tonalElevation = tonalElevation,
    shadowElevation = shadowElevation,
    content = content,
  )
}

@Composable
fun Card(
  modifier: Modifier = Modifier,
  shape: Shape = M3MaterialTheme.shapes.large,
  colors: CardColors = CardDefaults.cardColors(),
  border: BorderStroke? = null,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  M3Card(
    modifier = if (onClick != null) {
      modifier.clickable(
        enabled = enabled,
        onClick = onClick,
      )
    } else {
      modifier
    },
    shape = shape,
    colors = colors,
    border = border,
    content = content,
  )
}

@Composable
fun ElevatedCard(
  modifier: Modifier = Modifier,
  shape: Shape = M3MaterialTheme.shapes.large,
  colors: CardColors = CardDefaults.cardColors(
    containerColor = M3MaterialTheme.colorScheme.surface,
    contentColor = M3MaterialTheme.colorScheme.onSurface,
  ),
  border: BorderStroke? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  M3ElevatedCard(
    modifier = modifier,
    shape = shape,
    colors = colors,
    content = content,
  )
}

@Composable
fun HorizontalDivider(
  modifier: Modifier = Modifier,
  color: Color = M3MaterialTheme.colorScheme.outlineVariant,
) {
  M3HorizontalDivider(
    modifier = modifier,
    color = color,
  )
}

@Composable
fun Button(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  M3Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    content = content,
  )
}

@Composable
fun TextButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  M3TextButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    content = content,
  )
}

@Composable
fun OutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  M3OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    content = content,
  )
}

@Composable
fun FilledTonalButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  M3FilledTonalButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    content = content,
  )
}

@Composable
fun IconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable () -> Unit,
) {
  M3IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    content = content,
  )
}

@Composable
fun Checkbox(
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  M3Checkbox(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
  )
}

@Composable
fun Switch(
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  M3Switch(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
  )
}

@Composable
fun CircularProgressIndicator(
  modifier: Modifier = Modifier,
  color: Color = M3MaterialTheme.colorScheme.primary,
  trackColor: Color = M3MaterialTheme.colorScheme.surfaceVariant,
  strokeWidth: Dp = 4.dp,
) {
  M3CircularProgressIndicator(
    modifier = modifier,
    color = color,
    trackColor = trackColor,
    strokeWidth = strokeWidth,
  )
}

@Composable
fun AlertDialog(
  onDismissRequest: () -> Unit,
  confirmButton: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  dismissButton: (@Composable () -> Unit)? = null,
  icon: (@Composable () -> Unit)? = null,
  title: (@Composable () -> Unit)? = null,
  text: (@Composable () -> Unit)? = null,
  shape: Shape = M3MaterialTheme.shapes.extraLarge,
  containerColor: Color = M3MaterialTheme.colorScheme.surface,
  iconContentColor: Color = M3MaterialTheme.colorScheme.secondary,
  titleContentColor: Color = M3MaterialTheme.colorScheme.onSurface,
  textContentColor: Color = M3MaterialTheme.colorScheme.onSurfaceVariant,
  tonalElevation: Dp = 0.dp,
  properties: DialogProperties = DialogProperties(),
) {
  M3AlertDialog(
    onDismissRequest = onDismissRequest,
    confirmButton = confirmButton,
    modifier = modifier,
    dismissButton = dismissButton,
    icon = icon,
    title = title,
    text = text,
    shape = shape,
    containerColor = containerColor,
    iconContentColor = iconContentColor,
    titleContentColor = titleContentColor,
    textContentColor = textContentColor,
    tonalElevation = tonalElevation,
    properties = properties,
  )
}

@Composable
fun OutlinedTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  shape: Shape = M3MaterialTheme.shapes.large,
  textStyle: TextStyle = M3MaterialTheme.typography.bodyMedium,
  label: (@Composable () -> Unit)? = null,
  placeholder: (@Composable () -> Unit)? = null,
  supportingText: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  leadingIcon: (@Composable () -> Unit)? = null,
  isError: Boolean = false,
  singleLine: Boolean = false,
  minLines: Int = 1,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
  keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
  keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
) {
  M3OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    shape = shape,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    supportingText = supportingText,
    trailingIcon = trailingIcon,
    leadingIcon = leadingIcon,
    isError = isError,
    singleLine = singleLine,
    minLines = minLines,
    maxLines = maxLines,
    visualTransformation = visualTransformation,
    colors = colors,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
  )
}

@Composable
fun FilterChip(
  selected: Boolean,
  onClick: () -> Unit,
  label: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
) {
  M3FilterChip(
    selected = selected,
    onClick = onClick,
    label = label,
    modifier = modifier,
    enabled = enabled,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
  )
}

@Composable
fun AssistChip(
  onClick: () -> Unit,
  label: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
) {
  M3AssistChip(
    onClick = onClick,
    label = label,
    modifier = modifier,
    enabled = enabled,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
  )
}

@Composable
fun DropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  M3DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    content = content,
  )
}

@Composable
fun DropdownMenuItem(
  text: @Composable () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
) {
  M3DropdownMenuItem(
    text = text,
    onClick = onClick,
    modifier = modifier,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
  )
}

@Composable
fun SecondaryTabRow(
  selectedTabIndex: Int,
  modifier: Modifier = Modifier,
  tabs: @Composable () -> Unit,
) {
  M3SecondaryTabRow(
    selectedTabIndex = selectedTabIndex,
    modifier = modifier,
    tabs = tabs,
  )
}

@Composable
fun Tab(
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  text: @Composable (() -> Unit)? = null,
  icon: @Composable (() -> Unit)? = null,
) {
  M3Tab(
    selected = selected,
    onClick = onClick,
    modifier = modifier,
    text = text,
    icon = icon,
  )
}
