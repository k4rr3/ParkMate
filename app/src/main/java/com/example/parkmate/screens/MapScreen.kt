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

@OptIn(ExperimentalMaterial3Api::class)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            FilterBar(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            Image(
                painter = painterResource(id = R.drawable.map_photo_placeholder),
                contentDescription = "Map placeholder image",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                ,
            )
        }
    }
}
