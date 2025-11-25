// feature_splash_screen/presentation/SplashScreen.kt
package com.example.parkmate.feature_splash_screen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.parkmate.R
import com.example.parkmate.auth.AuthViewModel
import com.example.parkmate.screens.Screen
import com.example.parkmate.ui.theme.UserPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_lkxz2cg2))
    val progress by animateLottieCompositionAsState(composition)

    var navigationHandled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Wait for animation to finish
        while (progress < 0.99f) {
            delay(50)
        }
        delay(300) // small extra delay for smoothness

        if (navigationHandled) return@LaunchedEffect
        navigationHandled = true

        // Check if user has logged in before + Firebase session is active
        val hasLoggedInBefore = UserPreference.isUserLoggedInBefore(context).first()
        val isFirebaseUserLoggedIn = authViewModel.isUserLoggedIn()

        if (hasLoggedInBefore && isFirebaseUserLoggedIn) {
            navController.navigate(Screen.MapScreen.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}