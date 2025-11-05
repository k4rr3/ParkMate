package com.example.parkmate.screens

import androidx.annotation.StringRes
import com.example.parkmate.R

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object SplashScreen : Screen("splash_screen", resourceId = R.string.splash_screen)
    object LoginScreen : Screen("login_screen", resourceId = R.string.login_screen)
    object SignUpScreen : Screen("signup_screen", resourceId = R.string.login_screen)

    object CarDetailsScreen : Screen("car_details_screen", resourceId =R.string.car_details_screen )

    fun withVehicleId(vehicleId: String): String {
        return "$route/$vehicleId"
    }
    object MenuScreen : Screen("menu_screen", resourceId = R.string.menu_screen)
    object ProfileScreen : Screen("profile_screen", resourceId = R.string.profile_screen)
    object SettingsScreen : Screen("settings_screen", resourceId = R.string.settings_navbar)

    object CarListScreen : Screen("car_list_screen", resourceId = R.string.car_list_navbar)
    object AdminScreen : Screen("admin_screen" , resourceId = R.string.admin_navbar)

    object MapScreen : Screen( "map_screen", resourceId = R.string.map_navbar)

    object SavedCarSpotsScreen : Screen("saved_car_spots_screen", resourceId = R.string.saved_car_spots_screen)
    object SavedParkingSpotsScreen : Screen("saved_parking_spots_screen", resourceId = R.string.saved_parking_spots_screen)
    object TabLayout : Screen("tab_layout", R.string.tab_layout)
}
