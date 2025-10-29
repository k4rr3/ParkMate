package com.example.parkmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkmate.mock.UserStatus
import com.example.parkmate.ui.theme.Green
import com.example.parkmate.ui.theme.Orange
import com.example.parkmate.ui.theme.Purple

@Composable
fun StatusBadge(status: UserStatus) {
    val (backgroundColor, textColor, label) = when (status) {
        UserStatus.ACTIVE -> Triple(Green.copy(alpha = 0.1f), Green, "Active")
        UserStatus.PENDING -> Triple(Orange.copy(alpha = 0.1f), Orange, "Pending")
        UserStatus.INACTIVE -> Triple(Color.Gray.copy(alpha = 0.1f), Color.Gray, "Inactive")
        UserStatus.ADMIN -> Triple(Purple.copy(alpha = 0.1f), Purple, "Admin")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
