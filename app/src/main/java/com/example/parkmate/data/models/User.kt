package com.example.parkmate.data.models

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val premium: Boolean = false,
    val vehicleID: List<String> = emptyList(),
    val paymentMethod: Map<String, PaymentCard> = emptyMap(),
    val createdAt: Timestamp = Timestamp.now(),
    val admin: Boolean = false,
    var credits: Int = 0
)

data class PaymentCard(
    val cardNumber: String = "",
    val expirationDate: String = "",
    val cvv: String = ""
)