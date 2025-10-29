package com.example.parkmate.screens

import com.example.parkmate.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DirectionsCarFilled
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.parkmate.ui.theme.Blue
import com.example.parkmate.ui.theme.Green
import com.example.parkmate.ui.theme.LightBlue
import com.example.parkmate.ui.theme.LightGreen
import com.example.parkmate.ui.theme.LightOrange
import com.example.parkmate.ui.theme.LightRed
import com.example.parkmate.ui.theme.Orange
import com.example.parkmate.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavHostController) {
    Scaffold()
    { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF8F9FA))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                MenuButton(
                    navController = navController,
                    icon = Icons.Outlined.Settings,
                    iconBackground = LightBlue,
                    iconTint = Blue,
                    title = stringResource(R.string.settings_navbar),
                    destination = Screen.SettingsScreen.route,
                )

                MenuButton(
                    navController = navController,
                    icon = Icons.Outlined.DirectionsCarFilled,
                    iconBackground = LightOrange,
                    iconTint = Orange,
                    title = stringResource(R.string.car_list),
                    destination = Screen.CarListScreen.route,

                )

                MenuButton(
                    navController = navController,
                    icon = Icons.Outlined.AccountCircle,
                    iconBackground = LightGreen,
                    iconTint = Green,
                    title = stringResource(R.string.user_profile_navbar),
                    destination = Screen.ProfileScreen.route,
                )
                MenuButton(
                    navController = navController,
                    icon = Icons.Outlined.ManageAccounts,
                    iconBackground = LightRed,
                    iconTint = Red,
                    title = stringResource(R.string.admin_navbar),
                    destination = Screen.AdminScreen.route,
                )
            }
        }

        }
}
@Composable
fun MenuButton(
    navController: NavHostController,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    destination: String

) {
    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable{
                navController.navigate(destination)
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(

                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

            }

            // Status
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowForwardIos ,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )

            }
        }
    }
}
