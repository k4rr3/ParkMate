package com.example.parkmate.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.firebase.auth.FirebaseAuth
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetContract
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val user by viewModel.user.collectAsState()
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val emailValid by viewModel.emailValid.collectAsState()
    val phoneValid by viewModel.phoneValid.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(LightGray)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user?.name?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    ?.joinToString("") ?: "--",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomEnd)
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.name ?: stringResource(R.string.unknown_user),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Personal Information
        SectionCard(title = stringResource(R.string.personal_information)) {
            ProfileItem(
                icon = Icons.Default.Person,
                label = stringResource(R.string.full_name),
                value = name,
                isValid = true,
                errorMessage = "",
                fieldKey = "name",
                onValueChange = viewModel::updateName,
                viewModel = viewModel
            )
            ProfileItem(
                icon = Icons.Default.Email,
                label = stringResource(R.string.email_address),
                value = email,
                isValid = emailValid,
                errorMessage = stringResource(R.string.invalid_email),
                fieldKey = "email",
                onValueChange = viewModel::updateEmail,
                viewModel = viewModel
            )
            ProfileItem(
                icon = Icons.Default.Phone,
                label = stringResource(R.string.phone_number),
                value = phone,
                isValid = phoneValid,
                errorMessage = stringResource(R.string.invalid_phone),
                fieldKey = "phone",
                onValueChange = viewModel::updatePhone,
                viewModel = viewModel,
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        // Premium Upgrade Section
        SectionCard(title = stringResource(R.string.banking_payment)) {
            var isLoading by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("ParkMate Premium", fontWeight = FontWeight.Bold)
                            Text("$9.99/month • No ads • Priority spots", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    // Open the Stripe-hosted payment link in browser
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                    intent.data = android.net.Uri.parse("https://buy.stripe.com/test_5kQ5kD7xib923qi4yN9IQ00")
                    context.startActivity(intent)
                    isLoading = false  // No need to wait for result in test mode
                    Toast.makeText(context, "Opening payment page...", Toast.LENGTH_SHORT).show()
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("Upgrade to Premium • $9.99", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Changes Button
        Button(
            onClick = {
                scope.launch {
                    viewModel.saveChanges { success ->
                        Toast.makeText(
                            context,
                            if (success) "Changes saved!" else "Failed to save",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (success) viewModel.exitEditingAll()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.save_changes), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sign Out
        TextButton(
            onClick = {
                viewModel.signOut {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sign_out), color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// === Reusable Components ===
@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
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
    val isEditing by remember { derivedStateOf { viewModel.editingStates[fieldKey] == true } }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isEditing) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        isError = !isValid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isValid) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                } else {
                    Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            IconButton(
                onClick = {
                    if (isEditing && isValid) {
                        scope.launch {
                            viewModel.saveSingleField(fieldKey, value) { }
                        }
                    }
                    viewModel.editingStates[fieldKey] = !isEditing
                },
                enabled = !isEditing || isValid
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = null
                )
            }
        }
        if (!isLast) Divider()
    }
}