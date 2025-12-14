package com.example.parkmate.utils

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

/**
 * Calcula el centroide (centro geométrico) de un polígono.
 * Es una forma simple de encontrar el "centro" de una zona para la cámara.
 */
fun calculateCentroid(points: List<GeoPoint>): LatLng {
    if (points.isEmpty()) return LatLng(0.0, 0.0)

    val averageLat = points.sumOf { it.latitude } / points.size
    val averageLng = points.sumOf { it.longitude } / points.size
    return LatLng(averageLat, averageLng)
}
