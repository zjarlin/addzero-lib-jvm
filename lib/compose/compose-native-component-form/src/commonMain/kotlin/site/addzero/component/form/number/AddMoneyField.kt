package site.addzero.component.form.number

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.regex.RegexEnum

@Composable
fun AddMoneyField(
    @SpreadPack
    args: FilteredNumberFieldProps,
    supportingText: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    currency: String = "",
) {
    addFilteredTextFieldBase(
        value = args.value,
        onValueChange = args.onValueChange,
        label = args.label,
        placeholder = args.placeholder.ifBlank { "请输入金额" },
        isRequired = args.isRequired,
        modifier = args.modifier,
        maxLength = args.maxLength,
        onValidate = args.onValidate,
        leadingIcon = args.leadingIcon ?: getCurrencyIcon(currency),
        disable = args.disable,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        onErrMsgChange = args.onErrMsgChange,
        errorMessages = args.errorMessages,
        remoteValidationConfig = args.remoteValidationConfig,
        regexEnum = RegexEnum.MONEY,
        keyboardType = KeyboardType.Decimal,
        filterInput = { input ->
            filterDecimalInput(input, allowNegative = false)
        },
    )
}
