package com.example.parkmate.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.parkmate.R
import com.example.parkmate.ui.theme.LightGray
import com.example.parkmate.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    // CHANGED: Added NavController and ViewModel parameters
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Profile content
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize() // Use fillMaxSize for proper scrolling
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image and Name
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(LightGray)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AC",
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
                    .border(2.dp,  MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color =  MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aleix Cerqueda",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color =  MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${stringResource(R.string.member_since)} 2025",
            fontSize = 14.sp,
            color =  MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Personal Information Section
        SectionCard(title = stringResource(R.string.personal_information)) {
            ProfileItem(Icons.Default.Person, stringResource(R.string.full_name), "Aleix Cerqueda")
            ProfileItem(Icons.Default.Email, stringResource(R.string.email_address), "acb46@alumnes.udl.cat")
            ProfileItem(Icons.Default.Phone, stringResource(R.string.phone_number), "+34 684 02 63 88")
            ProfileItem(Icons.Default.LocationOn, stringResource(R.string.address), "C/Major 1, Bell-lloc", isLast = true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banking & Payment Section
        SectionCard(title = stringResource(R.string.banking_payment)) {
            ProfileItem(Icons.Default.CreditCard, stringResource(R.string.primary_payment_method), "Visa •••• 4567", "${stringResource(R.string.expires)} 12/26")
            ProfileItem(Icons.Default.AccountBalance, stringResource(R.string.bank_account), "Chase Bank", "•••• •••• •••• 8901", isLast = true)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Changes Button
        Button(
            onClick = { /* TODO */ },
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
                // CHANGED: This is the new sign-out logic
                viewModel.signOut()
                navController.navigate(Screen.LoginScreen.route) {
                    // This clears the entire back stack, so the user cannot go back to the profile
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    // This ensures that if the user logs in again, they don't get multiple copies of the main screen
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.sign_out),
                color = MaterialTheme.colorScheme.error, // Use error color for destructive actions
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
    additionalInfo: String? = null,
    isLast: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color =  MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (additionalInfo != null) {
                    Text(
                        text = additionalInfo,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { /* TODO: Implement edit functionality */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (!isLast) {
            Divider(
                modifier = Modifier.padding(start = 40.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}
