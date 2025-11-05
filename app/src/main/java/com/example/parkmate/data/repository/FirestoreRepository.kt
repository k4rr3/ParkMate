// data/repository/FirestoreRepository.kt
package com.example.parkmate.data.repository

import android.util.Log
import com.example.parkmate.data.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirestoreRepository @Inject constructor() {
    private val db: FirebaseFirestore = Firebase.firestore

    // Collections
    private val usersCollection = db.collection("users")
    private val vehiclesCollection = db.collection("vehicles")
    private val interestPointsCollection = db.collection("interestPoints")
    private val zonesCollection = db.collection("zones")
    private val ticketsCollection = db.collection("tickets")

    // User Operations
    suspend fun createUser(user: User): Boolean {
        return try {
            usersCollection.document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            usersCollection.document(userId).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Vehicle Operations
    suspend fun addVehicle(vehicle: Vehicle): String {
        return try {
            val documentRef = if (vehicle.id.isNotEmpty()) {
                vehiclesCollection.document(vehicle.id).set(vehicle).await()
                vehiclesCollection.document(vehicle.id)
            } else {
                vehiclesCollection.add(vehicle).await()
            }
            documentRef.id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserVehicles(userId: String): List<Vehicle> {
        return try {
            val user = getUser(userId)
            val vehicleIds = user?.vehicleID ?: emptyList()

            val vehicles = mutableListOf<Vehicle>()
            for (vehicleId in vehicleIds) {
                val vehicle = getVehicle(vehicleId)
                vehicle?.let { vehicles.add(it) }
            }
            vehicles
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVehicle(vehicleId: String): Vehicle? {
        return try {
            val document = vehiclesCollection.document(vehicleId).get().await()
            if (document.exists()) {
                document.toObject(Vehicle::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateVehicle(vehicleId: String, updates: Map<String, Any>): Boolean {
        return try {
            vehiclesCollection.document(vehicleId).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteVehicle(vehicleId: String): Boolean {
        return try {
            vehiclesCollection.document(vehicleId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Interest Points Operations
    suspend fun getGasStations(): List<InterestPoint> {
        return try {
            val querySnapshot = interestPointsCollection
                .whereEqualTo("type", "GasStation")
                .get()
                .await()
            querySnapshot.documents.map { doc ->
                doc.toObject(InterestPoint::class.java)?.copy(id = doc.id) ?: InterestPoint()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMechanics(): List<InterestPoint> {
        return try {
            val querySnapshot = interestPointsCollection
                .whereEqualTo("type", "Mechanic")
                .get()
                .await()
            querySnapshot.documents.map { doc ->
                doc.toObject(InterestPoint::class.java)?.copy(id = doc.id) ?: InterestPoint()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getNearbyGasStationsRealTime(
        centerLat: Double,
        centerLon: Double,
        radius: Double,
        onUpdate: (List<InterestPoint>) -> Unit
    ) {
        interestPointsCollection
            .whereEqualTo("type", "GasStation")
            .addSnapshotListener { snapshot, error ->
                error?.let {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val points = it.documents.mapNotNull { doc ->
                        doc.toObject(InterestPoint::class.java)?.copy(id = doc.id)
                    }
                    // Simple distance filtering (in production, use GeoFirestore)
                    val filteredPoints = points.filter { point ->
                        calculateDistance(
                            centerLat, centerLon,
                            point.location.latitude, point.location.longitude
                        ) <= radius
                    }
                    onUpdate(filteredPoints)
                }
            }
    }

    // Zone Operations
    suspend fun getZones(): List<Zone> {
        return try {
            val querySnapshot = zonesCollection.get().await()
            querySnapshot.documents.map { doc ->
                doc.toObject(Zone::class.java)?.copy(id = doc.id) ?: Zone()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Ticket Operations
    suspend fun createTicket(ticket: Ticket): String {
        return try {
            val documentRef = ticketsCollection.add(ticket).await()
            documentRef.id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserTickets(userId: String): List<Ticket> {
        return try {
            // This would need to query tickets by user ID through vehicles
            // For now, return empty list - implement based on your business logic
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Helper function for distance calculation
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
}