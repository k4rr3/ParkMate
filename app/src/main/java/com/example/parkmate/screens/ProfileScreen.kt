package com.example.parkmate.screens
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkmate.R
import com.example.parkmate.ui.theme.LightGray

@Composable
fun ProfileScreen() {

    var notificationsEnabled by remember { mutableStateOf(true) }



    // Profile content
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            ,
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
            // Replace with actual profile image
            Text(
                text = "AC",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Blue verification badge
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
                    color =  MaterialTheme.colorScheme.background,
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
            color =  MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "${stringResource(R.string.member_since)} 2025",
            fontSize = 14.sp,
            color =  MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Personal Information Section
        SectionCard(title = stringResource(R.string.personal_information)) {
            ProfileItem(
                icon = Icons.Default.Person,
                label = stringResource(R.string.full_name),
                value = "Aleix Cerqueda"
            )

            ProfileItem(
                icon = Icons.Default.Email,
                label = stringResource(R.string.email_address),
                value = "acb46@alumnes.udl.cat"
            )

            ProfileItem(
                icon = Icons.Default.Phone,
                label = stringResource(R.string.phone_number),
                value = "+34 684 02 63 88"
            )

            ProfileItem(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.address),
                value = "C/Major 1, Bell-lloc",
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banking & Payment Section
        SectionCard(title = stringResource(R.string.banking_payment)) {
            ProfileItem(
                icon = Icons.Default.CreditCard,
                label = stringResource(R.string.primary_payment_method),
                value = "Visa •••• 4567",
                additionalInfo = "${stringResource(R.string.expires)} 12/26"
            )

            ProfileItem(
                icon = Icons.Default.AccountBalance,
                label = stringResource(R.string.bank_account),
                value = "Chase Bank",
                additionalInfo = "•••• •••• •••• 8901",
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.sign_out),
                color =  MaterialTheme.colorScheme.background,
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
        colors = CardDefaults.cardColors(containerColor =  MaterialTheme.colorScheme.background),
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
                color =  MaterialTheme.colorScheme.onBackground
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
                    color =  MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = value,
                    fontSize = 16.sp
                )

                if (additionalInfo != null) {
                    Text(
                        text = additionalInfo,
                        fontSize = 12.sp,
                        color =  MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (!isLast) {
            Divider(
                modifier = Modifier.padding(start = 40.dp),
                color = LightGray,
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
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

            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                fontSize = 16.sp
            )

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Navigate",
                tint =  MaterialTheme.colorScheme.surface
            )
        }

        if (!isLast) {
            Divider(
                modifier = Modifier.padding(start = 40.dp),
                color = LightGray,
                thickness = 1.dp
            )
        }
    }
}
