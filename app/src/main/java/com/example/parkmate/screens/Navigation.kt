package com.example.parkmate.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.parkmate.auth.LoginScreen
import com.example.parkmate.auth.SignUpScreen
import com.example.parkmate.feature_splash_screen.presentation.SplashScreen
import com.example.parkmate.ui.MapScreen
import com.example.parkmate.ui.screens.AdminScreen
import com.example.parkmate.ui.theme.ThemeViewModel

@SuppressLint("MissingPermission")
@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    themeViewModel: ThemeViewModel
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

        composable(
            route = Screen.CarDetailsScreen.route + "/{vehicleId}"
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            CarDetailsScreenWrapper(vehicleId = vehicleId,navController)
        }

        composable(route = Screen.MenuScreen.route) {
            MenuScreen(navController = navController)
        }
        composable(route = Screen.ProfileScreen.route) {
            ProfileScreen()
        }
        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(themeViewModel = themeViewModel)
        }
        composable(route = Screen.CarListScreen.route){
            CarListScreen(navController = navController)
        }
        composable(route= Screen.MapScreen.route){
            MapScreen()
        }
        composable(route= Screen.AdminScreen.route){
            AdminScreen()
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