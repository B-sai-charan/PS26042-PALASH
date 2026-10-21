package com.sih2026.palash.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NetworkStatus {
    ONLINE,
    OFFLINE,
    SYNCING
}

@Composable
fun OfflineStatusIndicator(
    status: NetworkStatus = NetworkStatus.OFFLINE,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when (status) {
        NetworkStatus.ONLINE -> Color(0xFF2E7D32) to "ऑनलाइन"
        NetworkStatus.OFFLINE -> Color(0xFFD84315) to "ऑफ़लाइन काम करता है"
        NetworkStatus.SYNCING -> Color(0xFF0277BD) to "सिंक हो रहा है..."
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = statusColor.copy(alpha = 0.12f),
        contentColor = statusColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = statusColor
            ) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
