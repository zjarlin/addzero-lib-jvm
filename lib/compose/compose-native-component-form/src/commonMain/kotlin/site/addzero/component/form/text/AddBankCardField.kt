package site.addzero.component.form.text

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.runtime.Composable
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.regex.RegexEnum

@Composable
fun AddBankCardField(
    @SpreadPack
    args: RegexValidatedTextFieldProps,
) {
    addRegexValidatedTextFieldBase(
        value = args.value,
        onValueChange = args.onValueChange,
        label = args.label,
        isRequired = args.isRequired,
        enabled = args.enabled,
        placeholder = args.placeholder.ifBlank { "请输入银行卡号" },
        supportingText = args.supportingText,
        maxLength = args.maxLength ?: 19,
        onValidate = args.onValidate,
        modifier = args.modifier,
        onErrMsgChange = args.onErrMsgChange,
        errorMessages = args.errorMessages,
        remoteValidationConfig = args.remoteValidationConfig,
        regexEnum = RegexEnum.BANK_CARD,
        leadingIcon = Icons.Default.CreditCard,
    )
}
