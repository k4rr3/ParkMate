package com.example.parkmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    var userToDelete by remember { mutableStateOf<User?>(null) }
    var userToToggleAdmin by remember { mutableStateOf<User?>(null) }

    val scope = rememberCoroutineScope()

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

        AdminSearchBar(searchQuery) { searchQuery = it }

        AdminTabs(selectedTab) { selectedTab = it }

        AdminUserList(
            isLoading = isLoading,
            users = allUsers,
            searchQuery = searchQuery,
            selectedTab = selectedTab,
            onDelete = { userToDelete = it },
            onToggleAdmin = { userToToggleAdmin = it }
        )
    }

    AdminDeleteDialog(
        userToDelete,
        onDismiss = { userToDelete = null },
        onConfirm = {
            scope.launch { repo.deleteUser(it.uid) }
            userToDelete = null
        }
    )

    AdminToggleAdminDialog(
        userToToggleAdmin,
        onDismiss = { userToToggleAdmin = null },
        onConfirm = { user, makeAdmin ->
            scope.launch { repo.updateUserField(user.uid, "admin", makeAdmin) }
            userToToggleAdmin = null
        }
    )
}


@Composable
fun AdminSearchBar(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.search_users)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors()
        )
    }
}


@Composable
fun AdminTabs(selected: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf(
        stringResource(R.string.all_users),
        stringResource(R.string.admins),
        stringResource(R.string.normal_users)
    )

    ScrollableTabRow(selectedTabIndex = selected, edgePadding = 16.dp, divider = {}) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = { Text(title) }
            )
        }
    }
}


@Composable
fun AdminUserList(
    isLoading: Boolean,
    users: List<User>,
    searchQuery: String,
    selectedTab: Int,
    onDelete: (User) -> Unit,
    onToggleAdmin: (User) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val filtered = users
        .filter { it.name.contains(searchQuery, true) || it.email.contains(searchQuery, true) }
        .filter {
            when (selectedTab) {
                1 -> it.admin
                2 -> !it.admin
                else -> true
            }
        }

    if (filtered.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_users_found))
        }
        return
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        filtered.forEach { user ->
            UserListItem(
                user = user,
                onToggleAdmin = { onToggleAdmin(user) },
                onDelete = { onDelete(user) }
            )
            Divider(Modifier.padding(vertical = 4.dp))
        }
    }
}


@Composable
fun AdminDeleteDialog(
    user: User?,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    user ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_user)) },
        text = { Text("${stringResource(R.string.are_you_sure_you_want_to_delete)} ${user.name}?") },
        confirmButton = {
            TextButton(onClick = { onConfirm(user) }) {
                Text(stringResource(R.string.delete), color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


@Composable
fun AdminToggleAdminDialog(
    user: User?,
    onDismiss: () -> Unit,
    onConfirm: (User, Boolean) -> Unit
) {
    user ?: return
    val makeAdmin = !user.admin

    AlertDialog(
        onDismissRequest = onDismiss,
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
            TextButton(onClick = { onConfirm(user, makeAdmin) }) {
                Text(
                    if (makeAdmin) stringResource(R.string.assign_admin)
                    else stringResource(R.string.remove_admin),
                    color = if (makeAdmin) Color.Green else Color.Red
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}