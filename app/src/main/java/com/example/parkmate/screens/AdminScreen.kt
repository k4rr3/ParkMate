package com.example.parkmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkmate.R
import com.example.parkmate.data.models.User
import com.example.parkmate.data.repository.FirestoreRepository
import com.example.parkmate.ui.components.UserListItem
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    val repo = remember { FirestoreRepository() }

    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = All, 1 = Admins, 2 = Normal
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var userToToggleAdmin by remember { mutableStateOf<User?>(null) }



    val tabs = listOf(
        stringResource(R.string.all_users),
        stringResource(R.string.admins),
        stringResource(R.string.normal_users)
    )

    // 🔄 Subscribe to realtime updates
    LaunchedEffect(Unit) {
        repo.getAllUsersRealtime { users ->
            allUsers = users
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 🔍 Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.search_users)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {}),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // 🗂 Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            divider = {},
            containerColor = MaterialTheme.colorScheme.background
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // ➕ Add New User Button
        Button(
            onClick = { /* TODO: open new user form */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_new_user))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 👥 User List
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val filteredUsers = allUsers
                .filter { user ->
                    // Search filter
                    user.name.contains(searchQuery, ignoreCase = true) ||
                            user.email.contains(searchQuery, ignoreCase = true)
                }
                .filter { user ->
                    // Tab filter
                    when (selectedTab) {
                        1 -> user.admin == true // Admins
                        2 -> user.admin == false // Normal users
                        else -> true // All
                    }
                }

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_users_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // inside the AdminScreen user list section:
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    filteredUsers.forEach { user ->
                        UserListItem(
                            user = user,
                            onToggleAdmin = {
                                userToToggleAdmin = user
                            },
                            onDelete = {
                                userToDelete = user
                            }
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

            }
        }
    }
    // 🗑 Delete user confirmation dialog
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text(stringResource(R.string.delete_user)) },
            text = { Text("${stringResource(R.string.are_you_sure_you_want_to_delete)} ${user.name}? ${stringResource(R.string.this_action_cannot_be_undone)}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { repo.deleteUser(user.uid) }
                        userToDelete = null
                    }
                ) { Text(stringResource(R.string.delete), color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

// 👤 Admin assign/remove confirmation dialog
    userToToggleAdmin?.let { user ->
        val makeAdmin = !user.admin
        AlertDialog(
            onDismissRequest = { userToToggleAdmin = null },
            title = {
                Text(
                    if (makeAdmin) stringResource(R.string.assign_admin_role)
                    else stringResource(R.string.remove_admin_role)
                )
            },
            text = {
                Text(
                    if (makeAdmin)
                        "${stringResource(R.string.are_you_sure_you_want_to_assign_admin_role_to)} ${user.name}?"
                    else
                        "${stringResource(R.string.are_you_sure_you_want_to_remove_admin_role_from)} ${user.name}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repo.updateUserField(user.uid, "admin", makeAdmin)
                        }
                        userToToggleAdmin = null
                    }
                ) {
                    Text(
                        if (makeAdmin) stringResource(R.string.assign_admin)
                        else stringResource(R.string.remove_admin),
                        color = if (makeAdmin) Color.Green else Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { userToToggleAdmin = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

}

// 🧩 Example StatCard (if you want to reuse)
@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
            }
        }
    }
}
