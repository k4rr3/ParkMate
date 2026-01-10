package com.example.parkmate.ui.theme

// REMOVED @Composable related imports
import androidx.compose.ui.graphics.Color
import com.example.parkmate.data.models.ReminderStatus

// ... (your existing color definitions)

data class StatusColors(val container: Color, val content: Color)

/**
 * A simple utility function that provides theme-aware colors for a ReminderStatus.
 * It is NOT a composable.
 *
 * @param status The status of the reminder (PENDING, SOON, OVERDUE).
 * @param isDarkMode The current theme state (true for dark, false for light).
 * @return A [StatusColors] object containing the appropriate container and content colors.
 */
// NO @Composable annotation
fun getStatusColors(
    status: ReminderStatus,
    isDarkMode: Boolean
): StatusColors {
    // The function now directly uses the isDarkMode parameter.
    return when (status) {
        ReminderStatus.PENDING -> if (isDarkMode) {
            StatusColors(container = DarkOrangeContainer, content = DarkOrangeContent)
        } else {
            StatusColors(container = LightOrange, content = Orange)
        }
        ReminderStatus.SOON -> if (isDarkMode) {
            StatusColors(container = DarkGreenContainer, content = DarkGreenContent)
        } else {
            StatusColors(container = LightGreen, content = Green)
        }
        ReminderStatus.OVERDUE -> if (isDarkMode) {
            StatusColors(container = DarkRedContainer, content = DarkRedContent)
        } else {
            StatusColors(container = LightRed, content = Red)
        }
    }
}
