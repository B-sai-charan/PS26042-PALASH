package com.sih2026.palash.ui.screens.role

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sih2026.palash.R
import com.sih2026.palash.data.config.AppConfigManager
import com.sih2026.palash.ui.components.M6PinKeypad
import com.sih2026.palash.ui.components.PalashBrandHeader
import com.sih2026.palash.ui.localization.teacherStringResource

@Composable
fun TeacherPinScreen(
    onPinVerified: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val configManager = remember { AppConfigManager(context) }

    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val defaultErrorMsg = teacherStringResource(R.string.teacher_pin_error)
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                PalashBrandHeader(showOfflineBadge = true)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = teacherStringResource(R.string.teacher_pin_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = teacherStringResource(R.string.teacher_pin_subtitle),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // M6 6-Dot Indicator & Numeric Keypad
                M6PinKeypad(
                    pinLength = pinInput.length,
                    maxPinLength = 6,
                    onDigitClick = { digit ->
                        if (digit.all { it.isDigit() } && pinInput.length < 6) {
                            pinInput += digit
                            errorMessage = null
                        }
                    },
                    onBackspaceClick = {
                        if (pinInput.isNotEmpty()) {
                            pinInput = pinInput.dropLast(1)
                            errorMessage = null
                        }
                    }
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (pinInput.length == 6 && configManager.verifyTeacherPin(pinInput)) {
                            configManager.setTeacherRole()
                            onPinVerified()
                        } else {
                            errorMessage = defaultErrorMsg
                        }
                    },
                    enabled = pinInput.length == 6,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.sih2026.palash.ui.theme.M6DarkAmber,
                        contentColor = Color.White,
                        disabledContainerColor = com.sih2026.palash.ui.theme.M6Disabled,
                        disabledContentColor = Color(0xFF9E9E9E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = teacherStringResource(R.string.teacher_pin_verify_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = teacherStringResource(R.string.teacher_pin_back_button),
                        fontSize = 16.sp
                    )
                }
            }
        }

        com.sih2026.palash.ui.components.M6TopLeftRow(
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp, start = 12.dp)
        )
    }
}
}
