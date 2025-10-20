package com.example.parkmate.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.parkmate.auth.LoginScreen
import com.example.parkmate.auth.SignUpScreen
import com.example.parkmate.feature_splash_screen.presentation.SplashScreen
import com.example.parkmate.ui.MapScreen

@SuppressLint("MissingPermission")
@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route,
        Modifier.padding(innerPadding)
    ) {
        composable(route = Screen.SplashScreen.route) {
            SplashScreen(navController = navController)
        }
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(Screen.MapScreen.route) {
                        // Clear back stack to prevent returning to LoginScreen or SplashScreen
                        popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(route = Screen.SignUpScreen.route) {
            SignUpScreen(
                navController = navController
            )
        }

        composable(route = Screen.CarDetailsScreen.route) {
            CarDetailsScreen()
        }

        composable(route = Screen.MenuScreen.route) {
            MenuScreen(navController = navController)
        }
        composable(route = Screen.ProfileScreen.route) {
            ProfileScreen()
        }
        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen()
        }
        composable(route = Screen.CarListScreen.route){
            CarListScreen(navController = navController)
        }
        composable(route= Screen.MapScreen.route){
            MapScreen()
        }

        /* composable(route = Screen.SavedCarSpotsScreen.route) {
             SavedCarSpotsScreen(navController = navController)
         }

         composable(route = Screen.SavedParkingSpotsScreen.route) {
             SavedParkingSpotsScreen(navController = navController)
         }

         composable(route = Screen.TabLayout.route) {
             TabLayout(navController = navController)
         }*/
    }
}