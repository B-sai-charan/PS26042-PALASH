package com.sih2026.palash.ui.screens.worksheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih2026.palash.R
import com.sih2026.palash.ui.components.M6BackgroundArtwork
import com.sih2026.palash.ui.components.NetworkStatus
import com.sih2026.palash.ui.components.OfflineStatusIndicator
import com.sih2026.palash.ui.components.StudentTranslationButton
import com.sih2026.palash.ui.localization.teacherStringResource
import com.sih2026.palash.ui.theme.PalashTerracotta

@Composable
fun WorksheetScreen(
    gradeName: String = "Grade 2",
    onBackToClassroom: () -> Unit,
    onOpenFlashcards: () -> Unit = {},
    onOpenTranslation: () -> Unit = {},
    viewModel: WorksheetViewModel = viewModel()
) {
    androidx.compose.runtime.LaunchedEffect(gradeName) {
        viewModel.loadWorksheet(gradeName)
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val questions = uiState.questions
    val totalQuestions = questions.size
    val scrollState = rememberScrollState()

    val audioRepo = remember { com.sih2026.palash.audio.SantaliAudioRepositoryImpl(context) }
    val playbackEngine = remember { com.sih2026.palash.audio.AndroidAudioPlaybackEngineImpl(context) }
    val currentPlayingAudioId by com.sih2026.palash.audio.GlobalAudioPlayer.currentPlayingAudioId.collectAsState()

    var showTranslationDialog by remember { mutableStateOf(false) }

    M6BackgroundArtwork(onBack = onBackToClassroom) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
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
                    Text(
                        text = "पलाश",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudentTranslationButton(onOpenTranslation = { showTranslationDialog = true })
                        OfflineStatusIndicator(status = NetworkStatus.OFFLINE)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Topic Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "कक्षा २ | गणित",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val rawWsTitle = uiState.worksheetContent?.title ?: ""
                val cleanWsTitle = if (rawWsTitle.isBlank() || rawWsTitle.contains("PALASH", ignoreCase = true)) {
                    "कार्यपत्रक — गिनती एवं संख्याएँ"
                } else rawWsTitle

                Text(
                    text = cleanWsTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (questions.isNotEmpty()) {
                    // Render ALL questions on single vertically scrollable page
                    questions.forEachIndexed { index, q ->
                        val selectedOption = uiState.selectedOptions[q.questionId]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Question Header (Ordinal number)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "प्रश्न ${index + 1} / $totalQuestions",
                                        fontSize = 14.sp,
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Hindi Question Text
                                if (!q.hindi.isNullOrBlank()) {
                                    Text(
                                        text = "हिंदी:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = q.hindi,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Santali Ol Chiki Question Text
                                if (!q.santali.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ᱥᱟᱱᱛᱟᱲᱤ (OL CHIKI)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = q.santali,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (!q.numbers.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "संख्याएँ: ${q.numbers.joinToString(", ")}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val wsQAudioId = q.questionId
                                val wsAudioAssetPath = audioRepo.getAssetPathForAudio("${wsQAudioId}_question.wav")
                                    ?: audioRepo.getAssetPathForAudio(wsQAudioId)
                                if (wsAudioAssetPath != null) {
                                    val isWsAudioPlaying = (currentPlayingAudioId == wsQAudioId)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            playbackEngine.playAsset(wsAudioAssetPath, audioId = wsQAudioId) { _, _ -> }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isWsAudioPlaying) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                        ),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = if (isWsAudioPlaying) "⏸ ध्वनि चल रही है" else "▶ ध्वनि सुनें",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Answer Options
                                val displayOptions = q.options?.takeIf { it.isNotEmpty() }
                                    ?: q.numbers?.takeIf { it.isNotEmpty() }
                                    ?: listOf("5", "6", "7", "8")

                                if (displayOptions.isNotEmpty()) {
                                    Text(
                                        text = "उत्तर चुनें:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        displayOptions.forEach { option ->
                                            val isSelected = selectedOption == option
                                            val isCorrectOption = option.trim() == q.correctAnswer.trim()

                                            // Post-submit colors specified in Part 5
                                            val btnBgColor = if (uiState.isSubmitted) {
                                                when {
                                                    isCorrectOption -> Color(0xB32E7D32) // 70% opacity GREEN
                                                    isSelected && !isCorrectOption -> Color(0xB3D32F2F) // 70% opacity RED
                                                    else -> Color(0xB39E9E9E) // 70% opacity GRAY
                                                }
                                            } else {
                                                when {
                                                    isSelected -> PalashTerracotta
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            }

                                            val btnTextColor = if (uiState.isSubmitted || isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                                            Surface(
                                                modifier = Modifier
                                                    .padding(horizontal = 4.dp)
                                                    .clickable(enabled = !uiState.isSubmitted) {
                                                        viewModel.selectOption(q.questionId, option)
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                color = btnBgColor,
                                                border = BorderStroke(
                                                    width = if (isSelected && !uiState.isSubmitted) 2.dp else 1.dp,
                                                    color = if (isSelected && !uiState.isSubmitted) PalashTerracotta else MaterialTheme.colorScheme.outline
                                                )
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = btnTextColor,
                                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Per-question post-submit feedback
                                if (uiState.isSubmitted) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val isUserCorrect = selectedOption?.trim() == q.correctAnswer.trim()
                                    Text(
                                        text = if (isUserCorrect) "✓ सही उत्तर!" else "✕ गलत उत्तर (सही: ${q.correctAnswer})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUserCorrect) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Validation Alert Banner (if unanswered questions exist on Submit)
                    if (uiState.validationError != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "⚠️ ${uiState.validationError}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Score Summary Card (after Submit)
                    if (uiState.isSubmitted) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(2.dp, Color(0xFF2E7D32)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎉 कार्यपत्रक पूरा हुआ!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "अंक: ${uiState.score} / ${uiState.totalQuestions} सही",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }

                    // SUBMIT BUTTON
                    Button(
                        onClick = { viewModel.submitWorksheet() },
                        enabled = !uiState.isSubmitted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PalashTerracotta,
                            disabledContainerColor = Color(0xB39E9E9E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (uiState.isSubmitted) "जमा किया गया" else "उत्तर जमा करें",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Navigation Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Button(
                    onClick = onOpenFlashcards,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = teacherStringResource(R.string.worksheet_practice_flashcards),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onBackToClassroom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = teacherStringResource(R.string.worksheet_back_to_classroom),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (showTranslationDialog) {
            val sampleItem = questions.firstOrNull()
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showTranslationDialog = false },
                title = {
                    Text(
                        text = "कार्यपत्रक अनुवाद",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(text = "हिंदी:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = sampleItem?.hindi ?: "कितने आम हैं?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "ᱥᱟᱱᱛᱟᱲᱤ (Ol Chiki):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = sampleItem?.santali ?: "ᱚᱛᱮ ᱟᱸᱵᱚ ᱢᱮᱱᱟᱜᱼᱟ?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                },
                confirmButton = {
                    Button(onClick = { showTranslationDialog = false }) {
                        Text("ठीक है")
                    }
                }
            )
        }
    }
}
