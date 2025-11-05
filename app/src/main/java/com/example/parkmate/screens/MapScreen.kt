package com.example.parkmate.ui

import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.parkmate.mock.parkingSpots
import com.example.parkmate.ui.components.FilterBar
import com.example.parkmate.ui.components.MapView
import com.example.parkmate.ui.components.SearchBar
import com.example.parkmate.R
import com.example.parkmate.ui.theme.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.parkmate.mock.ParkingSpot
import com.example.parkmate.mock.parkingSpots
import com.example.parkmate.ui.components.FilterBar
import com.example.parkmate.ui.components.MapView
import com.example.parkmate.ui.components.ParkingSpotDetailCard
import com.example.parkmate.ui.components.SearchBar
import kotlinx.coroutines.launch

// Add OptIn for the M2 Experimental API.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MapScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Parking") }
    var filteredSpots by remember { mutableStateOf(parkingSpots) }

    // Filter spots based on search query
    val spots = if (searchQuery.isEmpty()) {
        parkingSpots
    } else {
        parkingSpots.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold { paddingValues ->


            // --- BOTTOM SHEET LOGIC ---
            // Holds the currently selected spot's data. Null if no spot is selected.
            var selectedSpot by remember { mutableStateOf<ParkingSpot?>(null) }
            // Scope for launching coroutines to control the sheet's state.
            val scope = rememberCoroutineScope()

            // Create state controllers for the Material 2 BottomSheet.
            // This state has two main values: Collapsed and Expanded.
            val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
            val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

            // This effect runs when the sheet is collapsed (e.g., by the user dragging it down).
            // It clears the selected spot, which causes the sheet to fully hide.
            if (sheetState.isCollapsed) {
                LaunchedEffect(key1 = sheetState.isCollapsed) {
                    selectedSpot = null
                }
            }

            // The main layout component that provides a bottom sheet.
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetContent = {
                    // The content of the bottom sheet. It only renders the detail card
                    // if a spot is actually selected, otherwise it's empty.
                    selectedSpot?.let { spot ->
                        ParkingSpotDetailCard(spot = spot)
                    }
                },
                // This is the height of the sheet when it's in the 'Collapsed' state.
                // It's a non-zero value only when a spot is selected, making it "peek".
                // It's 0.dp when no spot is selected, making it completely disappear.
                sheetPeekHeight = if (selectedSpot != null) 120.dp else 0.dp
            ) {
                // --- MAIN SCREEN CONTENT (behind the sheet) ---
                Scaffold { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it }
                        )
                        FilterBar(
                            selectedFilter = selectedFilter,
                            onFilterSelected = { selectedFilter = it }
                        )
                        MapView(
                            parkingSpots = spots,
                            onSpotClick = { spot ->
                                // When a spot is clicked, update the state...
                                selectedSpot = spot
                                // ...and programmatically expand the sheet to its full height.
                                scope.launch { sheetState.expand() }
                            },
                            onMapClick = {
                                // When the map is clicked, collapse the sheet to its peek height.
                                scope.launch { sheetState.collapse() }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
