package com.example.parkmate.data.models

import com.google.firebase.firestore.GeoPoint

data class Zone(
    val id: String = "",
    val name: String = "",
    val capacity: String = "",
    val schedule: String = "",
    val tariff: String = "",
    val vector: List<GeoPoint> = emptyList()
)
