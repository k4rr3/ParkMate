package com.example.parkmate.data.models

import com.google.firebase.firestore.GeoPoint

data class Vehicle(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val plate: String = "",
    val fuelType: String = "",
    val dgtLabel: String = "",
    val parkingLocation: GeoPoint = GeoPoint(0.0, 0.0),
    val maintenance: Maintenance? = null
)

data class Maintenance(
    val insuranceExpiry: String = "",
    val nextITV: String = "",
    val technicalReview: String = ""
)

