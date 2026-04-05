package site.addzero.component.form.text

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import site.addzero.component.button.AddIconButton
import site.addzero.kcp.spreadpack.SpreadPackCarrierOf
import site.addzero.regex.RegexEnum

@SpreadPackCarrierOf(
    value = "site.addzero.component.form.text.addRegexValidatedTextFieldBase",
    exclude = ["regexEnum", "leadingIcon"],
)
class RegexValidatedTextFieldProps

@SpreadPackCarrierOf(
    value = "site.addzero.component.form.text.addPasswordFieldBase",
    exclude = ["passwordVisible", "onPasswordVisibilityToggle", "otherIcon"],
)
class PasswordFieldProps

@Composable
internal fun addRegexValidatedTextFieldBase(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    isRequired: Boolean = false,
    enabled: Boolean = true,
    placeholder: String = "",
    supportingText: String? = null,
    maxLength: Int? = null,
    onValidate: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onErrMsgChange: ((String, String) -> Unit)? = null,
    errorMessages: List<String> = emptyList(),
    remoteValidationConfig: RemoteValidationConfig? = null,
    regexEnum: RegexEnum,
    leadingIcon: ImageVector,
) {
    val resolvedSupportingText = supportingText?.let { text ->
        @Composable {
            Text(text = text)
        }
    }

    AddTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        isRequired = isRequired,
        placeholder = placeholder,
        regexEnum = regexEnum,
        modifier = modifier,
        maxLength = maxLength,
        onValidate = onValidate,
        leadingIcon = leadingIcon,
        disable = !enabled,
        supportingText = resolvedSupportingText,
        onErrMsgChange = onErrMsgChange,
        errorMessages = errorMessages,
        remoteValidationConfig = remoteValidationConfig,
    )
}

@Composable
internal fun addPasswordFieldBase(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "密码",
    enabled: Boolean = true,
    validators: List<Pair<(String) -> Boolean, String>> = emptyList(),
    regexValidator: RegexEnum = RegexEnum.PASSWORD,
    otherIcon: @Composable (() -> Unit)? = null,
    onErrMsgChange: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    errorMessages: List<String> = emptyList(),
    remoteValidationConfig: RemoteValidationConfig? = null,
    isRequired: Boolean = true,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: () -> Unit,
) {
    val imageVector = if (passwordVisible) {
        Icons.Default.VisibilityOff
    } else {
        Icons.Default.Visibility
    }

    AddTextField(
        isRequired = isRequired,
        remoteValidationConfig = remoteValidationConfig,
        value = value,
        onValueChange = onValueChange,
        label = label,
        validators = validators,
        regexEnum = regexValidator,
        modifier = modifier,
        leadingIcon = Icons.Default.Lock,
        disable = !enabled,
        trailingIcon = {
            Row {
                AddIconButton(
                    text = if (passwordVisible) "隐藏密码" else "显示密码",
                    imageVector = imageVector,
                ) {
                    onPasswordVisibilityToggle()
                }
                otherIcon?.invoke()
            }
        },
        keyboardType = KeyboardType.Password,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        onErrMsgChange = onErrMsgChange,
        errorMessages = errorMessages,
    )
}
