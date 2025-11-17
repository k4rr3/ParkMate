package com.example.parkmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.parkmate.core.components.AppScaffold
import com.example.parkmate.ui.theme.LanguageViewModel
import com.example.parkmate.ui.theme.ParkMateTheme
import com.example.parkmate.ui.theme.ThemeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

lateinit var locationCallback: LocationCallback
lateinit var fusedLocationClient: FusedLocationProviderClient


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            val isDarkMode = themeViewModel.isDarkMode.collectAsState().value
            val language = languageViewModel.language.collectAsState().value

            ParkMateTheme(darkTheme = isDarkMode) {
                // Compose-only localized context
                val localizedContext = remember(language) {
                    this@MainActivity.createConfigurationContext(
                        resources.configuration.apply {
                            setLocale(Locale(language))
                        }
                    )
                }

                CompositionLocalProvider(LocalContext provides localizedContext) {
                    AppScaffold(themeViewModel, languageViewModel)
                }
            }
        }
    }
}
