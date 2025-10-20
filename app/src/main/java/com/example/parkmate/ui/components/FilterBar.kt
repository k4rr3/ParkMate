package com.example.parkmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.parkmate.R

@Composable
fun FilterBar(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            text = stringResource(R.string.parking),
            isSelected = selectedFilter == "Parking",
            onClick = { onFilterSelected("Parking") }
        )

        FilterChip(
            text = stringResource(R.string.gas),
            isSelected = selectedFilter == "Gas",
            onClick = { onFilterSelected("Gas") }
        )

        FilterChip(
            text = stringResource(R.string.mechanic),
            isSelected = selectedFilter == "Mechanic",
            onClick = { onFilterSelected("Mechanic") }
        )

        FilterChip(
            text = stringResource(R.string.wash),
            isSelected = selectedFilter == "Wash",
            onClick = { onFilterSelected("Wash") }
        )
    }
}
