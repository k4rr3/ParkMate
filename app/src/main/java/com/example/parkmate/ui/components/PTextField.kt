package com.example.parkmate.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    config: PTextFieldConfig = PTextFieldConfig()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = config.label,
        placeholder = config.placeholder,
        singleLine = config.singleLine,
        keyboardOptions = config.keyboardOptions,
        trailingIcon = config.trailingIcon,
        enabled = config.enabled,
        readOnly = config.readOnly,
        isError = config.isError,
        supportingText = config.supportingText,
        maxLines = config.maxLines
    )
}
