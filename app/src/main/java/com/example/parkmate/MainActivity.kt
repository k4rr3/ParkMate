package com.example.parkmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.example.parkmate.core.components.AppScaffold
import com.example.parkmate.ui.theme.ParkMateTheme
import com.example.parkmate.ui.theme.ThemeViewModel
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationCallback
import dagger.hilt.android.AndroidEntryPoint

lateinit var locationCallback: LocationCallback
lateinit var fusedLocationClient: FusedLocationProviderClient

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            // Collect the dark mode state from DataStore through the ViewModel
            val isDarkMode = themeViewModel.isDarkMode.collectAsState().value

            ParkMateTheme(darkTheme = isDarkMode) {
                // Pass the ViewModel down so SettingsScreen can toggle the theme
                AppScaffold(themeViewModel)
            }
        }
    }
}
