package site.addzero.component.form.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import site.addzero.kcp.spreadpack.SpreadPack

@Composable
fun AddPasswordField(
    @SpreadPack
    args: PasswordFieldProps,
    otherIcon: (@Composable () -> Unit)? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    addPasswordFieldBase(
        value = args.value,
        onValueChange = args.onValueChange,
        label = args.label,
        enabled = args.enabled,
        validators = args.validators,
        regexValidator = args.regexValidator,
        otherIcon = otherIcon,
        onErrMsgChange = args.onErrMsgChange,
        modifier = args.modifier,
        errorMessages = args.errorMessages,
        remoteValidationConfig = args.remoteValidationConfig,
        isRequired = args.isRequired,
        passwordVisible = passwordVisible,
        onPasswordVisibilityToggle = {
            passwordVisible = !passwordVisible
        },
    )
}
