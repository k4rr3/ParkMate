package com.example.parkmate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parkmate.data.models.User
import com.example.parkmate.ui.screens.admin.AdminViewModel
import com.example.parkmate.R


@Composable
fun UserListItem(
    user: User,
    onDelete: () -> Unit,
    onToggleAdmin: (Boolean) -> Unit
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
            Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(if (user.admin) stringResource(R.string.admin_user) else stringResource(R.string.standard_user), fontSize = 12.sp)
        }

        Row {
            IconButton(onClick = { onToggleAdmin(user.admin) }) {
                Icon(
                    imageVector = if (user.admin) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = if (user.admin) Color.Red else Color.Green
                )
            }


            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete User", tint = Color.Red)
            }
        }
    }
}
