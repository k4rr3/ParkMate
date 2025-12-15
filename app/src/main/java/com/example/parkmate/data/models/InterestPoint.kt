package com.example.parkmate.data.models

import com.google.firebase.firestore.GeoPoint

data class InterestPoint(
    val id: String = "",
    val name: String = "",
    val type: String = "", // "GasStation" or "Mechanic"
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val schedule: String = "",
    val contact: String = "",
    val services: List<String> = emptyList(),
    val price: FuelPrices? = null
)

data class FuelPrices(
    val da: String = "", // Diesel A
    val daPlus: String = "", // Diesel A+
    val g95: String = "", // Gasoline 95
    val g98: String = ""  // Gasoline 98
)
