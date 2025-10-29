package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.parkmate.R
import com.example.parkmate.mock.User

@Composable
fun UserListItem(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        UserAvatar(user = user)

        Spacer(modifier = Modifier.width(16.dp))

        // User info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Status badge
        StatusBadge(status = user.status)

        Spacer(modifier = Modifier.width(8.dp))

        // Action buttons
        Row {
            IconButton(
                onClick = { /* Edit action */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = Color.Gray
                )
            }

            IconButton(
                onClick = { /* View action */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = stringResource(R.string.view),
                    tint = Color.Gray
                )
            }

            IconButton(
                onClick = { /* Delete action */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}
