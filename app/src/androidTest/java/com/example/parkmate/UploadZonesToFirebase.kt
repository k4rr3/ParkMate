package com.example.parkmate


import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.parkmate.data.models.Zone
import com.example.parkmate.data.repository.FirestoreRepository
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Esta no es una clase de test tradicional. Es un "seeder" (sembrador) de base de datos
 * que se ejecuta como un test instrumental para aprovechar la inyección de dependencias de Hilt
 * y la configuración de Firebase ya existente en la app.
 *
 * Su única función es poblar la base de datos de Firestore con datos iniciales.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UploadZonesToFirebase {

    // Inyecta el repositorio de Firestore directamente. Es más directo que usar el ViewModel para un script.
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    // Regla de Hilt para preparar la inyección de dependencias
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        // Prepara Hilt para inyectar las dependencias en esta clase de test
        hiltRule.inject()
    }

    /**
     * Este es nuestro "script". Al ejecutar este test, se subirán todas las zonas
     * definidas en la función a Firestore.
     */
    fun uploadZonesToFirestore() = runBlocking {
        println("--- INICIANDO SCRIPT DE SUBIDA DE ZONAS ---")

        // --- DEFINE AQUÍ LAS ZONAS QUE QUIERES SUBIR ---

        val zona1 = Zone(
            name = "Campus Cappont",
            capacity = "50",
            schedule = "08:00 - 22:00",
            tariff = "1.50€/h",
            vector = listOf(
                GeoPoint(41.60595, 0.62439),
                GeoPoint(41.60842, 0.62564),
                GeoPoint(41.60781, 0.62664),
                GeoPoint(41.60605, 0.62555)
            )
        )

        val zona2 = Zone(
            name = "La Seu Vella",
            capacity = "60",
            schedule = "08:00 - 22:00",
            tariff = "1.60€/h",
            vector = listOf(
                GeoPoint(41.6166, 0.62596),
                GeoPoint(41.61719, 0.62548),
                GeoPoint(41.61743, 0.62604),
                GeoPoint(41.6169, 0.62648)
            )
        )

        val zona3 = Zone(
            name = "C/Alfons II",
            capacity = "70",
            schedule = "08:00 - 22:00",
            tariff = "1.70€/h",
            vector = listOf(
                GeoPoint(41.61224, 0.63231),
                GeoPoint(41.6123, 0.63252),
                GeoPoint(41.61152, 0.63326),
                GeoPoint(41.61144, 0.63311)
            )
        )

        val zona4 = Zone(
            name = "Passeig de Ronda",
            capacity = "80",
            schedule = "08:00 - 22:00",
            tariff = "1.80€/h",
            vector = listOf(
                GeoPoint(41.62375, 0.62039),
                GeoPoint(41.62358, 0.62076),
                GeoPoint(41.62489, 0.62248),
                GeoPoint(41.62511, 0.62213)
            )
        )

        val zona5 = Zone(
            name = "C/Josep Piñol",
            capacity = "90",
            schedule = "08:00 - 22:00",
            tariff = "1.90€/h",
            vector = listOf(
                GeoPoint(41.62558, 0.63651),
                GeoPoint(41.62535, 0.63677),
                GeoPoint(41.62567, 0.63712),
                GeoPoint(41.62588, 0.63683)
            )
        )

        val zona6 = Zone(
            name = "C/de La Mariola",
            capacity = "100",
            schedule = "08:00 - 22:00",
            tariff = "2.00€/h",
            vector = listOf(
                GeoPoint(41.61234, 0.61157),
                GeoPoint(41.61249, 0.61167),
                GeoPoint(41.61215, 0.6137),
                GeoPoint(41.612, 0.61368)
            )
        )

        val zona7 = Zone(
            name = "Museu de Lleida",
            capacity = "110",
            schedule = "08:00 - 22:00",
            tariff = "2.10€/h",
            vector = listOf(
                GeoPoint(41.61301, 0.62037),
                GeoPoint(41.6137, 0.62069),
                GeoPoint(41.61352, 0.62144),
                GeoPoint(41.61282, 0.62116)
            )
        )

        val zona8 = Zone(
            name = "Camp d'Esports",
            capacity = "120",
            schedule = "08:00 - 22:00",
            tariff = "2.20€/h",
            vector = listOf(
                GeoPoint(41.62206, 0.6117),
                GeoPoint(41.62282, 0.61203),
                GeoPoint(41.62195, 0.61362),
                GeoPoint(41.62117, 0.61326)
            )
        )

        val zona9 = Zone(
            name = "C/Hostal de La Bordeta",
            capacity = "130",
            schedule = "08:00 - 22:00",
            tariff = "2.30€/h",
            vector = listOf(
                GeoPoint(41.60352, 0.64339),
                GeoPoint(41.60362, 0.64348),
                GeoPoint(41.60292, 0.64474),
                GeoPoint(41.60284, 0.64465)
            )
        )

        val zona10 = Zone(
            name = "C/Dr. Fleming",
            capacity = "140",
            schedule = "08:00 - 22:00",
            tariff = "2.40€/h",
            vector = listOf(
                GeoPoint(41.61962, 0.61643),
                GeoPoint(41.6196, 0.61622),
                GeoPoint(41.61948, 0.61615),
                GeoPoint(41.61941, 0.61631)
            )
        )

        // Lista de todas las zonas a subir
        val zonesToUpload = listOf(
            zona1, zona2, zona3, zona4, zona5,
            zona6, zona7, zona8, zona9, zona10
        )

        // --- BUCLE DE SUBIDA ---
        zonesToUpload.forEach { zone ->
            try {
                println("Subiendo zona: ${zone.name}...")
                val newId = firestoreRepository.addZone(zone)
                println("... Éxito. Zona '${zone.name}' subida con ID: $newId")
            } catch (e: Exception) {
                println("... Error al subir la zona '${zone.name}': ${e.message}")
            }
        }

        println("--- SCRIPT FINALIZADO ---")
    }
}