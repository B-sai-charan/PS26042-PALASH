package com.sih2026.palash.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih2026.palash.R
import com.sih2026.palash.ui.components.NetworkStatus
import com.sih2026.palash.ui.components.OfflineStatusIndicator
import com.sih2026.palash.ui.localization.teacherStringResource

@Composable
fun TranslationResultScreen(
    hindiText: String,
    santaliNotice: String,
    liveAudioPath: String? = null,
    originalAudioPath: String? = null,
    onBack: () -> Unit,
    viewModel: TranslationResultViewModel = viewModel()
) {
    LaunchedEffect(hindiText, santaliNotice, liveAudioPath, originalAudioPath) {
        viewModel.setTranslation(hindiText, santaliNotice, liveAudioPath, originalAudioPath)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val playAudioDesc = teacherStringResource(R.string.result_desc_play_audio)

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Top Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.sih2026.palash.ui.components.M6TopLeftRow(onBack = onBack)
                        OfflineStatusIndicator(status = NetworkStatus.OFFLINE)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = teacherStringResource(R.string.result_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // PRIMARY — Santali / Ol Chiki Translation Result
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .semantics { testTag = "santali_result_card" },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            val isExactMatch = com.sih2026.palash.data.repository.PreloadedPhraseRepository.isPreloaded(hindiText) ||
                                    com.sih2026.palash.translation.CurriculumDictionary.isVerified(hindiText)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isExactMatch) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (isExactMatch) teacherStringResource(R.string.result_target_santali) else "डेमो नमूना • संताली (ओल चिकी)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExactMatch) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            val santaliResult = uiState.santaliNotice
                            if (santaliResult.isNotBlank()) {
                                Text(
                                    text = santaliResult,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20),
                                    modifier = Modifier.semantics { testTag = "santali_result_text" }
                                )
                            } else {
                                Text(
                                    text = "— अनुवाद उपलब्ध नहीं है —",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Audio Playback Button (Supports Play & Replay)
                    Button(
                        onClick = { viewModel.playAudioClicked() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .semantics { contentDescription = playAudioDesc },
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        )
                    ) {
                        Text(
                            text = if (uiState.isPlayingAudio) "🔊 चला रहे हैं..." else "🔊 संताली ध्वनि सुनें / ᱟᱸᱡᱚᱢ ᱢᱮ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    val origPath = uiState.originalAudioPath
                    val hasOrigFile = !origPath.isNullOrBlank() && java.io.File(origPath).exists()
                    if (hasOrigFile) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val isOrigPlaying = uiState.isPlayingOriginalAudio
                        OutlinedButton(
                            onClick = { viewModel.playOriginalAudioClicked() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (isOrigPlaying) "🔊 मूल हिंदी रिकॉर्डिंग बजा रहे हैं..." else "▶ अपनी आवाज़ सुनें (मूल हिंदी)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bottom Actions — Back only, NO "Open Worksheet" button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .semantics { testTag = "result_back_button" },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "← " + teacherStringResource(R.string.result_back_to_classroom),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
