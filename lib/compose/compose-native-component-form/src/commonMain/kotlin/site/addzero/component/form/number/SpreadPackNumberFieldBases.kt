package site.addzero.component.form.number

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import site.addzero.component.form.text.AddTextField
import site.addzero.component.form.text.RemoteValidationConfig
import site.addzero.regex.RegexEnum

data class FilteredNumberFieldArgs(
    val value: String,
    val onValueChange: (String) -> Unit,
    val label: String = "",
    val placeholder: String = "",
    val isRequired: Boolean = true,
    val modifier: Modifier = Modifier,
    val maxLength: Int? = null,
    val onValidate: ((Boolean) -> Unit)? = null,
    val leadingIcon: ImageVector? = null,
    val disable: Boolean = false,
    val supportingText: (@Composable () -> Unit)? = null,
    val trailingIcon: (@Composable () -> Unit)? = null,
    val onErrMsgChange: ((String, String) -> Unit)? = null,
    val errorMessages: List<String> = emptyList(),
    val remoteValidationConfig: RemoteValidationConfig? = null,
)

@Composable
internal fun addFilteredTextFieldBase(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",
    isRequired: Boolean = true,
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    onValidate: ((Boolean) -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    disable: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onErrMsgChange: ((String, String) -> Unit)? = null,
    errorMessages: List<String> = emptyList(),
    remoteValidationConfig: RemoteValidationConfig? = null,
    regexEnum: RegexEnum,
    keyboardType: KeyboardType,
    filterInput: (String) -> String = { input -> input },
) {
    AddTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(filterInput(newValue))
        },
        label = label,
        placeholder = placeholder,
        isRequired = isRequired,
        regexEnum = regexEnum,
        modifier = modifier,
        maxLength = maxLength,
        onValidate = onValidate,
        leadingIcon = leadingIcon,
        disable = disable,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        keyboardType = keyboardType,
        onErrMsgChange = onErrMsgChange,
        errorMessages = errorMessages,
        remoteValidationConfig = remoteValidationConfig,
    )
}
