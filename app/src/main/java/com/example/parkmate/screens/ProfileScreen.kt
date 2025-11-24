package com.example.parkmate.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.parkmate.R
import com.example.parkmate.ui.theme.LightGray
import com.example.parkmate.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userState = viewModel.user.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()

    val emailValid by viewModel.emailValid.collectAsState()
    val phoneValid by viewModel.phoneValid.collectAsState()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image and initials
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(LightGray)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userState.value?.let {
                    it.name.split(" ").map { n -> n.firstOrNull()?.uppercase() ?: "" }.joinToString("")
                } ?: "--",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomEnd)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = userState.value?.name ?: stringResource(R.string.unknown_user),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )


        Spacer(modifier = Modifier.height(24.dp))

        // Personal Information Section
        SectionCard(title = stringResource(R.string.personal_information)) {

            ProfileItem(
                icon = Icons.Default.Person,
                label = stringResource(R.string.full_name),
                value = name,
                isValid = true,
                errorMessage = "",
                fieldKey = "name",
                onValueChange = { viewModel.name.value = it },
                viewModel = viewModel
            )

            ProfileItem(
                icon = Icons.Default.Email,
                label = stringResource(R.string.email_address),
                value = email,
                isValid = emailValid,
                errorMessage = stringResource(R.string.invalid_email),
                fieldKey = "email",
                onValueChange = { viewModel.email.value = it },
                viewModel = viewModel
            )

            ProfileItem(
                icon = Icons.Default.Phone,
                label = stringResource(R.string.phone_number),
                value = phone,
                isValid = phoneValid,
                errorMessage = stringResource(R.string.invalid_phone),
                fieldKey = "phone",
                onValueChange = { viewModel.phone.value = it },
                viewModel = viewModel,
                isLast = true
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Banking & Payment Section
        SectionCard(title = stringResource(R.string.banking_payment)) {
            /*userState.value?.let { user ->
                ProfileItem(
                    Icons.Default.CreditCard,
                    stringResource(R.string.primary_payment_method),
                    user.primaryCard,
                    user.cardExpiry
                )
                ProfileItem(
                    Icons.Default.AccountBalance,
                    stringResource(R.string.bank_account),
                    user.bankName,
                    user.bankAccountMasked,
                    isLast = true
                )
            }

             */
        }

        Spacer(modifier = Modifier.height(24.dp))



        Button(
            onClick = {
                scope.launch {
                    val success = viewModel.saveChanges()
                    viewModel.exitEditingAll()
                    Toast.makeText(
                        context,
                        if (success) context.getString(R.string.changes_saved) else context.getString(R.string.error_saving_changes),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.save_changes),
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 16.sp
            )
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Sign Out Button
        TextButton(
            onClick = {
                viewModel.signOut()
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.sign_out),
                color = MaterialTheme.colorScheme.error,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Use a slightly different color
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String,
    isValid: Boolean,
    errorMessage: String,
    fieldKey: String,
    onValueChange: (String) -> Unit,
    viewModel: ProfileViewModel,
    isLast: Boolean = false
) {
    val editing by remember { derivedStateOf { viewModel.editingStates[fieldKey] ?: false } }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)

            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            ) {
                Text(label, fontSize = 14.sp)

                if (editing) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        isError = !isValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isValid) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(value, fontSize = 16.sp)
                }
            }

            IconButton(
                enabled = !editing || isValid,
                onClick = {
                    if (editing && isValid) {
                        scope.launch { viewModel.saveSingleField(fieldKey, value) }
                    }
                    viewModel.editingStates[fieldKey] = !editing

                }
            ) {
                Icon(if (editing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null)
            }
        }

        if (!isLast) Divider()
    }
}
