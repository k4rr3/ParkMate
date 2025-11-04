package com.example.parkmate.data.models

data class InterestPoint(
    val id: String = "",
    val name: String = "",
    val type: String = "", // "GasStation" or "Mechanic"
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val schedule: String = "",
    val contact: String = "",
    val Services: List<String> = emptyList(),
    val price: FuelPrices? = null
)

data class FuelPrices(
    val DA: String = "", // Diesel A
    val DA_plus: String = "", // Diesel A+
    val G95: String = "", // Gasoline 95
    val G98: String = ""  // Gasoline 98
)
