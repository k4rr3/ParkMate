package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkmate.data.models.User

@Composable
fun UserListItem(
    user: User,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(user.name, fontWeight = FontWeight.Bold)
            Text(user.email, fontSize = 12.sp, color = Color.Gray)
            Text(if (user.premium) "Premium User" else "Standard User", fontSize = 12.sp)
        }

        Row {

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete User", tint = Color.Red)
            }
        }
    }
}
