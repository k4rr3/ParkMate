package com.example.parkmate.mock

import androidx.compose.ui.graphics.Color
import com.example.parkmate.ui.theme.Blue
import com.example.parkmate.ui.theme.Green
import com.example.parkmate.ui.theme.Orange
import com.example.parkmate.ui.theme.Red

data class ParkingSpot(
    val id: Int,
    val name: String,
    val spots: Int,
    val price: String,
    val latitude: Double,
    val longitude: Double,
    val color: Color,
    val category: String
)

val parkingSpots = listOf(
    ParkingSpot(
        id = 1,
        name = "Downtown",
        spots = 15,
        price = "Free",
        latitude = 41.6175,
        longitude = 0.6200,
        color = Green,
        category = "Parking"
    ),
    ParkingSpot(
        id = 2,
        name = "Central Mall",
        spots = 8,
        price = "$2/hr",
        latitude = 41.615476,
        longitude = 0.625473,
        color = Orange,
        category = "Parking"
    ),
    ParkingSpot(
        id = 3,
        name = "Metro Station",
        spots = 22,
        price = "$3/hr",
        latitude = 41.6155,
        longitude = 0.6180,
        color = Green,
        category = "Parking"
    ),
    ParkingSpot(
        id = 4,
        name = "City Plaza",
        spots = 9,
        price = "$4/hr",
        latitude = 41.6185,
        longitude = 0.6270,
        color = Red,
        category = "Parking"
    ),
    ParkingSpot(
        id = 5,
        name = "University",
        spots = 30,
        price = "$1/hr",
        latitude = 41.6145,
        longitude = 0.6230,
        color = Blue,
        category = "Parking"
    )
)
