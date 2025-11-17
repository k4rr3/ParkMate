package com.example.parkmate.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.Locale

@Composable
fun localizedString(@androidx.annotation.StringRes resId: Int, language: String): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    val config = context.resources.configuration
    val localizedContext = remember(language) {
        context.createConfigurationContext(config.apply {
            setLocale(Locale(language))
        })
    }
    return localizedContext.resources.getString(resId)
}
