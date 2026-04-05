package site.addzero.component.form.text

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.regex.RegexEnum

@Composable
fun AddPhoneField(
    @SpreadPack
    args: RegexValidatedTextFieldProps,
) {
    addRegexValidatedTextFieldBase(
        value = args.value,
        onValueChange = args.onValueChange,
        label = args.label,
        isRequired = args.isRequired,
        enabled = args.enabled,
        placeholder = args.placeholder.ifBlank { "请输入手机号" },
        supportingText = args.supportingText,
        maxLength = args.maxLength ?: 11,
        onValidate = args.onValidate,
        modifier = args.modifier,
        onErrMsgChange = args.onErrMsgChange,
        errorMessages = args.errorMessages,
        remoteValidationConfig = args.remoteValidationConfig,
        regexEnum = RegexEnum.PHONE,
        leadingIcon = Icons.Default.Phone,
    )
}
