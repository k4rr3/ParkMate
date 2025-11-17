package com.example.parkmate.core.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.example.parkmate.ui.theme.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  AppScaffold(
    themeViewModel: ThemeViewModel
) {
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
        Screen.AdminScreen.route -> stringResource(R.string.admin_navbar)
        Screen.CarDetailsScreen.route.plus("/{vehicleId}") -> stringResource(R.string.car_details_navbar)
        Screen.AboutUsScreen.route -> stringResource(R.string.about_us_navbar)
        Screen.TermsAndConditionsScreen.route -> stringResource(R.string.terms_and_conditions_navbar)

        else -> stringResource(R.string.add_the_page_in_appscafold_file)
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHost.snackbarHostState)
        },
        scaffoldState = scaffoldState,
        topBar = {
            if (navBackStackEntry?.destination?.route !in screensWithoutTopBar) {

                TopAppBar(
                    title = { Text(currentTitle,color = MaterialTheme.colorScheme.onBackground ) },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigate(Screen.MenuScreen.route){
                                popUpTo(Screen.MenuScreen.route) {
                                    inclusive = true
                                }
                            }}) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Screen.MapScreen.route)
                        }) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor =  MaterialTheme.colorScheme.background
                    )
                )
            }


        },
    ) { innerPadding ->
        Navigation(navController = navController, innerPadding = innerPadding, snackbarHostState = snackbarHost.snackbarHostState, themeViewModel = themeViewModel)
    }
}