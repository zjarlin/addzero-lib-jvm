package site.addzero.component.form.number

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import site.addzero.regex.RegexEnum
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack

@GenerateSpreadPackOverloads
@Composable
fun AddIntegerField(
    @SpreadPack
    args: FilteredNumberFieldArgs,
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
        supportingText = args.supportingText,
        trailingIcon = args.trailingIcon,
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
