package com.example.parkmate.core.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.parkmate.screens.Navigation
import com.example.parkmate.screens.Screen
import com.example.parkmate.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  AppScaffold() {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val snackbarHost = rememberScaffoldState()

    val screensWithoutTopBar = listOf<String>(
        Screen.SplashScreen.route,
        Screen.LoginScreen.route,
        Screen.SignUpScreen.route

    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = when (currentRoute) {
        Screen.CarDetailsScreen.route -> stringResource(R.string.car_details_navbar)
        Screen.MenuScreen.route -> stringResource(R.string.menu_navbar)
        Screen.ProfileScreen.route -> stringResource(R.string.user_profile_navbar)
        Screen.SettingsScreen.route -> stringResource(R.string.settings_navbar)
        Screen.CarListScreen.route -> stringResource(R.string.car_list_navbar)
        Screen.MapScreen.route -> stringResource(R.string.map_navbar)
        else -> "Add the page in AppScafold file"
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHost.snackbarHostState)
        },
        scaffoldState = scaffoldState,
        topBar = {
            if (navBackStackEntry?.destination?.route !in screensWithoutTopBar) {

                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigate(Screen.MenuScreen.route)}) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Screen.MapScreen.route)
                        }) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }


        },
    ) { innerPadding ->
        Navigation(navController = navController, innerPadding = innerPadding, snackbarHostState = snackbarHost.snackbarHostState)
    }
}