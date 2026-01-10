package com.example.parkmate.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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


// "Comprar créditos"
        SectionCard(title = "Buy credits") {
            var isLoading by remember { mutableStateOf(false) }

            // Paquetes de créditos con su precio y link correspondiente
            val creditPackages = listOf(
                Triple(10, "4.99 €", "https://buy.stripe.com/test_fZu5kDg3Ocd68KC1mB9IQ01"),
                Triple(25, "9.99 €", "https://buy.stripe.com/test_bJeeVdeZKdhaaSK5CR9IQ02"),
                Triple(50, "17.99 €", "https://buy.stripe.com/test_3cIcN518U3GAd0Sd5j9IQ03")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                creditPackages.forEach { (credits, priceText, paymentLink) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            Column {
                                Text(
                                    text = "$credits credits",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = priceText,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (credits == 50) {
                                    Text(
                                        text = "¡Most popular!",
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isLoading = true
                                    // Abrir el link específico de este paquete
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                    intent.data = android.net.Uri.parse(paymentLink)
                                    context.startActivity(intent)

                                    // En modo test: añadir créditos inmediatamente (simulando éxito)
                                    viewModel.addCredits(credits) {}
                                    isLoading = false
                                },
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Buy", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Información adicional
                Text(
                    text = "The credits allow you to pay for parking, regulated zones and gas stations directly from the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
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