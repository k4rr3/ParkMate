package com.example.parkmate.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// --- LIGHT COLOR SCHEME ---
private val LightColorScheme = lightColorScheme(
    primary = Blue,
    secondary = Purple,
    tertiary = Green,
    background = White,
    surface = LightGray,
    onBackground = Black,
    onSurface = DarkGray,
    onPrimary = White,
    onSecondary = White,
    error = Red,
    onError = White,
)

// --- DARK COLOR SCHEME ---
private val DarkColorScheme = darkColorScheme(
    primary = DarkBlue,
    secondary = DarkPurple,
    tertiary = DarkGreen,

    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextSecondary,
    onPrimary = Black,
    onSecondary = Black,
    error = DarkRed,
    onError = DarkTextPrimary,
)


@Composable
fun ParkMateTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

    val colors = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    MaterialTheme(
        typography = Typography,
        shapes = Shapes,
        content = content,
        colorScheme = colors
    )
}



