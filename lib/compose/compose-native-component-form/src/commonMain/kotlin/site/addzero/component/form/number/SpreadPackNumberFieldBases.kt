package site.addzero.component.form.number

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import site.addzero.component.form.text.AddTextField
import site.addzero.component.form.text.RemoteValidationConfig
import site.addzero.kcp.spreadpack.SpreadPackCarrierOf
import site.addzero.regex.RegexEnum

@SpreadPackCarrierOf(
    value = "site.addzero.component.form.number.addFilteredTextFieldBase",
    exclude = ["regexEnum", "keyboardType", "filterInput", "supportingText", "trailingIcon"],
)
class FilteredNumberFieldProps

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
