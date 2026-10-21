package com.sih2026.palash.ui.screens.assessment

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih2026.palash.R
import com.sih2026.palash.audio.AndroidAudioPlaybackEngineImpl
import com.sih2026.palash.audio.AudioPlaybackState
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import com.sih2026.palash.data.model.m5.M5AssessmentQuestion
import com.sih2026.palash.ui.components.M6BackgroundArtwork
import androidx.compose.material3.AlertDialog
import com.sih2026.palash.ui.components.NetworkStatus
import com.sih2026.palash.ui.components.OfflineStatusIndicator
import com.sih2026.palash.ui.components.StudentTranslationButton
import com.sih2026.palash.ui.localization.teacherStringResource

@Composable
fun AssessmentScreen(
    gradeName: String = "Grade 2",
    onBackToClassroom: () -> Unit,
    onOpenTranslation: () -> Unit = {},
    viewModel: AssessmentViewModel = viewModel()
) {
    androidx.compose.runtime.LaunchedEffect(gradeName) {
        viewModel.setGrade(gradeName)
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val audioRepo = remember { SantaliAudioRepositoryImpl(context) }
    val playbackEngine = remember { AndroidAudioPlaybackEngineImpl(context) }
    val currentPlayingAudioId by com.sih2026.palash.audio.GlobalAudioPlayer.currentPlayingAudioId.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            playbackEngine.release()
        }
    }

    var showTranslationDialog by remember { mutableStateOf(false) }

    M6BackgroundArtwork(onBack = onBackToClassroom) {
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
                // Header
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
                        text = if (uiState.gradeName.contains("1")) "कक्षा १ | गणित > मूल्यांकन" else "कक्षा २ | गणित > मूल्यांकन",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val rawTitle = uiState.assessmentContent?.title ?: ""
                val cleanTitle = if (rawTitle.isBlank() || rawTitle.contains("PALASH", ignoreCase = true)) {
                    "मूल्यांकन — गिनती एवं संख्याएँ"
                } else rawTitle

                Text(
                    text = cleanTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.isGrade1Notice) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1B5E20).copy(alpha = 0.85f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "कक्षा १ मूल्यांकन सामग्री",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "कक्षा १ के लिए मूल्यांकन सामग्री शीघ्र उपलब्ध होगी। वर्तमान मूल्यांकन कक्षा २ के लिए उपलब्ध है।",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                } else if (uiState.questions.isNotEmpty()) {
                    // Submission Score Banner
                    if (uiState.isSubmitted) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎉 मूल्यांकन पूरा हुआ!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "कुल प्राप्तांक: ${uiState.score} / ${uiState.totalQuestions}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Validation Error Banner
                    if (uiState.validationError != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text(
                                text = uiState.validationError!!,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Render all Assessment questions vertically
                    uiState.questions.forEachIndexed { index, question ->
                        AssessmentQuestionCard(
                            questionIndex = index + 1,
                            totalQuestions = uiState.totalQuestions,
                            question = question,
                            selectedAnswer = uiState.selectedAnswers[question.assessmentQuestionId],
                            isSubmitted = uiState.isSubmitted,
                            onAnswerSelected = { answer ->
                                viewModel.selectAnswer(question.assessmentQuestionId, answer)
                            },
                            audioRepo = audioRepo,
                            playbackEngine = playbackEngine,
                            isPlaying = currentPlayingAudioId == question.assessmentQuestionId,
                            onAudioStateChanged = { _ -> }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "कोई मूल्यांकन प्रश्न उपलब्ध नहीं हैं।",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Bottom Actions Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                if (!uiState.isGrade1Notice && uiState.questions.isNotEmpty()) {
                    if (!uiState.isSubmitted) {
                        Button(
                            onClick = { viewModel.submitAssessment() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "मूल्यांकन जमा करें",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.resetAssessment() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "पुनः प्रयास करें",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onBackToClassroom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "← पिछला",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (showTranslationDialog) {
            val currentQ = uiState.questions.firstOrNull()
            AlertDialog(
                onDismissRequest = { showTranslationDialog = false },
                title = {
                    Text(
                        text = "मूल्यांकन अनुवाद",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(text = "हिंदी प्रश्न:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = currentQ?.hindi ?: "कितने आम हैं?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "ᱥᱟᱱᱛᱟᱲᱤ:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = currentQ?.santali ?: "ᱚᱛե ᱟᱸᱵᱚ ᱢᱮᱱᱟᱜᱼᱟ?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
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

@Composable
private fun AssessmentQuestionCard(
    questionIndex: Int,
    totalQuestions: Int,
    question: M5AssessmentQuestion,
    selectedAnswer: String?,
    isSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit,
    audioRepo: SantaliAudioRepositoryImpl,
    playbackEngine: AndroidAudioPlaybackEngineImpl,
    isPlaying: Boolean,
    onAudioStateChanged: (AudioPlaybackState) -> Unit
) {
    val qId = question.assessmentQuestionId
    // Primary lookup: by question ID (e.g. AS001 → AS001_question.wav).
    // Fallback: by content ID (e.g. G2C004 → G2C004_question.wav) — only fires if no AS-prefixed asset exists.
    val audioAssetPath = audioRepo.getAssetPathForAudio("${qId}_question.wav")
        ?: audioRepo.getAssetPathForAudio("${question.contentId}_question.wav")
    // IMPORTANT: audioAssetPath is already the verified relative asset path returned by getAssetPathForAudio,
    // which only returns non-null when the audio is present in the indexed asset map.
    // Do NOT pass audioAssetPath to hasAudioAsset() — that method expects an audioId (key), not a full path.
    val hasAudio = audioAssetPath != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "प्रश्न $questionIndex / $totalQuestions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = if (question.questionType.contains("MULTIPLE", ignoreCase = true)) "बहुविकल्पीय" else "प्रश्न",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hindi Question Prompt
            if (!question.hindi.isNullOrBlank()) {
                Text(
                    text = question.hindi!!,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Santali Ol Chiki Prompt
            if (!question.santali.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.santali!!,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Optional Audio Button
            if (hasAudio && audioAssetPath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        playbackEngine.playAsset(audioAssetPath, audioId = qId) { state, _ ->
                            onAudioStateChanged(state)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isPlaying) "⏸ ध्वनि चल रही है" else "▶ ध्वनि सुनें",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Available Options Selection
            val availableOptions = if (question.options.isNotEmpty()) {
                question.options
            } else {
                // For number_input / counting_range without explicit options list, provide structured choices from correctAnswer
                val ans = question.correctAnswer.trim()
                if (ans == "1-10") listOf("1-5", "1-10", "1-20") else listOf("2", "5", "7", "10")
            }

            availableOptions.forEach { option ->
                val isSelected = selectedAnswer?.trim() == option.trim()
                val isCorrectAnswer = option.trim().equals(question.correctAnswer.trim(), ignoreCase = true)

                // 70% opacity color styling after submission
                val cardColor = when {
                    isSubmitted && isCorrectAnswer -> Color(0xB32E7D32) // 70% Green
                    isSubmitted && isSelected && !isCorrectAnswer -> Color(0xB3D32F2F) // 70% Red
                    isSubmitted -> Color(0xB39E9E9E) // 70% Gray
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }

                val textColor = when {
                    isSubmitted -> Color.White
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !isSubmitted) {
                            onAnswerSelected(option)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = cardColor,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected && !isSubmitted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = if (!isSubmitted) { { onAnswerSelected(option) } } else null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = if (isSubmitted) Color.White else MaterialTheme.colorScheme.primary,
                                unselectedColor = if (isSubmitted) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = option,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
