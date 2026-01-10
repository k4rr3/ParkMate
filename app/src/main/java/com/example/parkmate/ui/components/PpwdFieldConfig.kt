package com.example.parkmate.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.VisualTransformation


@Stable
data class PpwdFieldConfig(
    val label: @Composable (() -> Unit)? = null,
    val placeholder: @Composable (() -> Unit)? = null,
    val trailingIcon: @Composable (() -> Unit)? = null,
    val supportingText: @Composable (() -> Unit)? = null,
    val singleLine: Boolean = false,
    val maxLines: Int = Int.MAX_VALUE,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val enabled: Boolean = true,
    val readOnly: Boolean = false,
    val isError: Boolean = false,
    val visualTransformation: VisualTransformation = VisualTransformation.None
)