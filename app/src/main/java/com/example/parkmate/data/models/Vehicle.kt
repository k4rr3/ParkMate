package com.example.parkmate.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import java.time.Instant
import java.time.temporal.ChronoUnit

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

// --- REFACTORED CarReminder ---
data class CarReminder(
    val id: String = "",
    val vehicleId: String = "",
    val title: String = "",
    // Use Long for Firestore compatibility and easier date math.
    val dueDate: Long = 0L,
    // The 'status' field is removed from the constructor to avoid storing it in Firestore.
) {
    /**
     *  Dynamically calculates the status of the reminder based on the current time.
     *  - OVERDUE: The due date has passed.
     *  - SOON: The due date is within the next 3 days (72 hours).
     *  - PENDING: The due date is more than 3 days away.
     */
    @get:Exclude // Prevents Firestore from trying to serialize this getter.
    val status: ReminderStatus
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            // Define 'now' to get the current moment in time
            val now = Instant.now()
            val dueDateInstant = Instant.ofEpochMilli(dueDate)

            // Check if the due date is in the past.
            if (now.isAfter(dueDateInstant)) {
                return ReminderStatus.OVERDUE
            }

            // Calculate the number of hours until the due date.
            val hoursUntilDue = ChronoUnit.HOURS.between(now, dueDateInstant)

            return when {
                // If it's 72 hours (3 days) or less away, it's "SOON".
                hoursUntilDue <= 72 -> ReminderStatus.SOON
                // Otherwise, it's still "PENDING".
                else -> ReminderStatus.PENDING
            }
        }

}

enum class ReminderStatus {
    PENDING,
    SOON,
    OVERDUE
}
