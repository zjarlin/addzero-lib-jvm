package site.addzero.component.form.number

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import site.addzero.regex.RegexEnum
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack

@GenerateSpreadPackOverloads
@Composable
fun AddPercentageField(
    @SpreadPack
    args: FilteredNumberFieldArgs,
    showPercentSymbol: Boolean = true,
) {
    val resolvedSupportingText = if (showPercentSymbol) {
        args.supportingText ?: {
            Text(
                text = "%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        args.supportingText
    }

    addFilteredTextFieldBase(
        value = args.value,
        onValueChange = args.onValueChange,
        label = args.label,
        placeholder = args.placeholder.ifBlank { "请输入百分比" },
        isRequired = args.isRequired,
        modifier = args.modifier,
        maxLength = args.maxLength,
        onValidate = args.onValidate,
        leadingIcon = args.leadingIcon ?: Icons.Default.Percent,
        disable = args.disable,
        supportingText = resolvedSupportingText,
        trailingIcon = args.trailingIcon,
        onErrMsgChange = args.onErrMsgChange,
        errorMessages = args.errorMessages,
        remoteValidationConfig = args.remoteValidationConfig,
        regexEnum = RegexEnum.DECIMAL,
        keyboardType = KeyboardType.Decimal,
        filterInput = { input ->
            filterDecimalInput(input, allowNegative = false)
        },
    )
}
