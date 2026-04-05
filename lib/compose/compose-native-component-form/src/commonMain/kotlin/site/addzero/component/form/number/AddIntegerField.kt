package site.addzero.component.form.number

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.regex.RegexEnum

@Composable
fun AddIntegerField(
    @SpreadPack
    args: FilteredNumberFieldProps,
    supportingText: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    allowNegative: Boolean = true,
) {
    addFilteredTextFieldBase(
        value = args.value,
        onValueChange = args.onValueChange,
        label = args.label,
        placeholder = args.placeholder.ifBlank { "请输入整数" },
        isRequired = args.isRequired,
        modifier = args.modifier,
        maxLength = args.maxLength,
        onValidate = args.onValidate,
        leadingIcon = args.leadingIcon ?: Icons.Default.Numbers,
        disable = args.disable,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        onErrMsgChange = args.onErrMsgChange,
        errorMessages = args.errorMessages,
        remoteValidationConfig = args.remoteValidationConfig,
        regexEnum = if (allowNegative) {
            RegexEnum.INTEGER
        } else {
            RegexEnum.POSITIVE_INTEGER
        },
        keyboardType = KeyboardType.Number,
        filterInput = { input ->
            filterIntegerInput(input, allowNegative)
        },
    )
}
