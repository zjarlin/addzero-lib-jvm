package com.addzero.component.form.text

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.addzero.component.button.AddIconButton
import com.addzero.core.validation.RegexEnum

/**
 * Password input field with validation and visibility toggle
 *
 * @param value Current password value
 * @param onValueChange Callback when the password changes
 * @param regexEnum Regex pattern for validation
 * @param label Label text for the field
 * @param enabled Whether the field is enabled
 */
@Composable
fun AddPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "密码",
    enabled: Boolean = true,
    validators: List<Pair<(String) -> Boolean, String>> = emptyList(),
    regexValidator: RegexEnum = RegexEnum.PASSWORD,
    otherIcon: @Composable (() -> Unit)? = null,
    onErrMsgChange: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    errorMessages: List<String> = emptyList<String>(),
    remoteValidationConfig: com.addzero.component.form.text.RemoteValidationConfig? = null,
    isRequired: Boolean = true,

    ) {
    var passwordVisible by remember { mutableStateOf(false) }
    val imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
    _root_ide_package_.com.addzero.component.form.text.AddTextField(
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
                _root_ide_package_.com.addzero.component.button.AddIconButton(
                    text = if (passwordVisible) "隐藏密码" else "显示密码",
                    imageVector = imageVector
                ) {
                    passwordVisible = !passwordVisible
                }
                otherIcon?.let { it() }
            }
        },
        keyboardType = KeyboardType.Password,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        onErrMsgChange = onErrMsgChange,
        errorMessages = errorMessages,
    )
}
