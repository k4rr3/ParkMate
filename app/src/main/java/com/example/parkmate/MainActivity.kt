package com.example.parkmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.parkmate.core.components.AppScaffold

import com.example.parkmate.ui.theme.ParkMateTheme
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint

lateinit var locationCallback: LocationCallback
lateinit var fusedLocationClient: FusedLocationProviderClient
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //installSplashScreen()

        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            ParkMateTheme {
                AppScaffold()
            }
        }
    }

}



