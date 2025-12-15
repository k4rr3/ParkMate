package com.example.parkmate.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    config: PpwdFieldConfig = PpwdFieldConfig()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = config.label,
        placeholder = config.placeholder,
        trailingIcon = config.trailingIcon,
        supportingText = config.supportingText,
        singleLine = config.singleLine,
        maxLines = config.maxLines,
        keyboardOptions = config.keyboardOptions,
        enabled = config.enabled,
        readOnly = config.readOnly,
        isError = config.isError,
        visualTransformation = config.visualTransformation.takeIf {
            it != VisualTransformation.None
        } ?: PasswordVisualTransformation()
    )
}
