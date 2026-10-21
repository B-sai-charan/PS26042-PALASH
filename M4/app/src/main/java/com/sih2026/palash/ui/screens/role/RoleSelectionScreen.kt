package com.sih2026.palash.ui.screens.role

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sih2026.palash.ui.components.M6BackgroundArtwork
import com.sih2026.palash.ui.components.M6RoleCard
import com.sih2026.palash.ui.components.PalashBrandHeader

@Composable
fun RoleSelectionScreen(
    onSelectTeacher: () -> Unit,
    onSelectStudent: () -> Unit
) {
    val scrollState = rememberScrollState()

    M6BackgroundArtwork {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // M6 Brand Header with Emblem & Offline Badge
                PalashBrandHeader(showOfflineBadge = true)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "अपनी भूमिका चुनें",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "ᱟᱢᱟᱜ ᱴᱷᱟᱶ ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Side-by-Side M6 Role Cards (Student vs Teacher)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    M6RoleCard(
                        roleTitle = "विद्यार्थी",
                        olChikiSubtitle = "ᱪᱮᱛᱮᱫᱤᱭᱟᱹ",
                        isTeacher = false,
                        onClick = onSelectStudent,
                        modifier = Modifier.weight(1f)
                    )

                    M6RoleCard(
                        roleTitle = "शिक्षक",
                        olChikiSubtitle = "ᱢᱟᱪᱮᱛ",
                        isTeacher = true,
                        onClick = onSelectTeacher,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = "पलाश • ᱯᱚᱞᱟᱥ — ऑफ़लाइन भाषा शिक्षण",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

