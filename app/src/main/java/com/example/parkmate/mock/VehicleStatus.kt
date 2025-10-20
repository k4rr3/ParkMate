package com.example.parkmate.mock


import androidx.compose.ui.graphics.Color
import com.example.parkmate.ui.theme.*


enum class VehicleStatus {
    PARKED,
    DRIVING,
    EXPIRED
}

data class Vehicle(
    val make: String,
    val model: String,
    val licensePlate: String,
    val description: String,
    val status: VehicleStatus,
    val statusDetail: String,
    val iconColor: Color
)

val sampleVehicles = listOf(
    Vehicle(
        make = "Tesla",
        model = "Model 3",
        licensePlate = "ABC-123",
        description = "Electric sedan with autopilot features. Perfect for city driving and long trips.",
        status = VehicleStatus.PARKED,
        statusDetail = "2h 5m left",
        iconColor = Blue
    ),
    Vehicle(
        make = "BMW",
        model = "X5",
        licensePlate = "XYZ-456",
        description = "Luxury SUV with premium interior. Ideal for family trips and business meetings.",
        status = VehicleStatus.DRIVING,
        statusDetail = "Moving",
        iconColor = Red
    ),
    Vehicle(
        make = "Honda",
        model = "Civic",
        licensePlate = "DEF-789",
        description = "Reliable compact car with excellent fuel efficiency. Great for daily commuting.",
        status = VehicleStatus.PARKED,
        statusDetail = "45m left",
        iconColor = Green
    ),
    Vehicle(
        make = "Audi",
        model = "A4",
        licensePlate = "GHI-012",
        description = "Premium sedan with advanced technology. Parking ticket has expired.",
        status = VehicleStatus.EXPIRED,
        statusDetail = "15m ago",
        iconColor = Pink40
    )
)
