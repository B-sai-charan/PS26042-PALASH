package com.sih2026.palash.ui.screens.lesson

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.sih2026.palash.data.model.m5.M5Section
import com.sih2026.palash.ui.components.M6BackgroundArtwork
import com.sih2026.palash.ui.components.M6PalashLogo
import com.sih2026.palash.ui.components.NetworkStatus
import com.sih2026.palash.ui.components.OfflineStatusIndicator
import com.sih2026.palash.ui.components.StudentTranslationButton
import com.sih2026.palash.ui.localization.teacherStringResource
import com.sih2026.palash.ui.theme.PalashTerracotta

private fun String.cleanHtml(): String {
    return this.replace(Regex("<[^>]*>"), "").trim()
}

@Composable
fun LessonScreen(
    gradeName: String = "Grade 2",
    subjectName: String = "Mathematics",
    topicName: String = "Counting",
    sectionName: String = "A",
    onOpenWorksheet: () -> Unit,
    onStartClassroom: () -> Unit = {},
    onOpenTranslation: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: LessonViewModel = viewModel()
) {
    androidx.compose.runtime.LaunchedEffect(gradeName) {
        viewModel.loadLesson(gradeName)
    }

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val playbackEngine = remember { AndroidAudioPlaybackEngineImpl(context) }
    val audioRepo = remember { SantaliAudioRepositoryImpl(context) }

    DisposableEffect(Unit) {
        onDispose {
            playbackEngine.release()
        }
    }

    val currentPlayingAudioId by com.sih2026.palash.audio.GlobalAudioPlayer.currentPlayingAudioId.collectAsState()

    val onPlayAudio: (String) -> Unit = { audioRef ->
        val resolvedPath = audioRepo.getAssetPathForAudio(audioRef)
        if (resolvedPath != null) {
            android.util.Log.i("LessonScreenAudio", "Requested audioRef: '$audioRef', resolvedPath: '$resolvedPath'")
            playbackEngine.playAsset(resolvedPath, audioId = audioRef) { state, errorMsg ->
                if (state == AudioPlaybackState.ERROR) {
                    Toast.makeText(context, errorMsg ?: "Audio missing or playback error", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Audio missing for $audioRef", Toast.LENGTH_SHORT).show()
        }
    }

    var showTranslationDialog by remember { mutableStateOf(false) }

    M6BackgroundArtwork(onBack = onBack) {
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
                // Top M6 Header Bar with Student Translation Button at Top-Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentTranslationButton(onOpenTranslation = { showTranslationDialog = true })
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    M6PalashLogo(size = 42.dp)
                        Text(
                            text = "पलाश",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = PalashTerracotta
                        )
                        Text(
                            text = "ᱯᱚᱞᱟᱥ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudentTranslationButton(onOpenTranslation = onOpenTranslation)
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // M6 Lesson Title Banner Card
                val lesson = uiState.lessonContent
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFFCC80))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFE0B2),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "📖", fontSize = 24.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val titleText = lesson?.title?.cleanHtml() ?: ""
                            val cleanLessonTitle = if (titleText.isBlank() || titleText.contains("PALASH", ignoreCase = true)) {
                                "1 से 10 तक गिनती / ᱑ ᱠᱷᱚᱱ ᱑᱐ ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ"
                            } else titleText

                            Text(
                                text = cleanLessonTitle,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF3E2723)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PalashTerracotta)
                    }
                } else if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Error loading lesson",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                } else if (lesson != null) {
                    // Main White Lesson Container Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFFFE0B2)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Display all 6 sections in exact order
                            lesson.sections.forEachIndexed { index, section ->
                                LessonSectionCard(
                                    sectionIndex = index + 1,
                                    section = section,
                                    currentPlayingAudioId = currentPlayingAudioId,
                                    onPlayAudio = onPlayAudio
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenWorksheet,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFF9800))
                    ) {
                        Text(
                            text = "📝 कार्यपत्रक",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "← " + teacherStringResource(R.string.nav_previous),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showTranslationDialog) {
            val sampleSec = uiState.lessonContent?.sections?.firstOrNull()
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showTranslationDialog = false },
                title = {
                    Text(
                        text = "पाठ अनुवाद",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(text = "हिंदी शीर्षक:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = sampleSec?.heading ?: "गिनती सीखें", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "ᱥᱟᱱᱛᱟᱲᱤ (Ol Chiki):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = sampleSec?.santali ?: "ᱞᱮᱠᱷᱟ ᱪᱮᱫᱚᱜ ᱢᱮ।", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
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
private fun LessonSectionCard(
    sectionIndex: Int,
    section: M5Section,
    currentPlayingAudioId: String? = null,
    onPlayAudio: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
        border = BorderStroke(1.dp, Color(0xFFFFECB3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFCC80))
                ) {
                    Text(
                        text = "भाग $sectionIndex",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD84315),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                val typeText = section.type.cleanHtml().lowercase()
                val internalMetadataTypes = setOf("introduction", "teacher_instruction", "number_learning", "example", "student_activity", "assessment_prompt")
                if (!internalMetadataTypes.contains(typeText) && !typeText.contains("sec")) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = section.type.cleanHtml(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (!section.heading.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = section.heading.cleanHtml(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF3E2723)
                )
            }

            // Hindi Text
            if (!section.hindi.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "हिंदी:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD84315)
                )
                Text(
                    text = section.hindi.cleanHtml(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121),
                    lineHeight = 24.sp
                )
            }

            // Santali Ol Chiki Text
            if (!section.santali.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ᱥᱟᱱᱛᱟᱲᱤ (Ol Chiki):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A148C)
                )
                Text(
                    text = section.santali.cleanHtml(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037),
                    lineHeight = 26.sp
                )
            }

            // Visual Prompt
            if (!section.visual.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFF8E1),
                    border = BorderStroke(1.dp, Color(0xFFFFE082)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = section.visual.cleanHtml(),
                            fontSize = 32.sp
                        )
                    }
                }
            }

            // Numbers List Table
            if (!section.numbers.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                ) {
                    section.numbers.forEach { num ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayAudio("number_${num.number}") }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${num.number}. ${num.hindi.cleanHtml()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = num.santali.cleanHtml(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFF3E0),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🔊", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Audio Action Button
            if (!section.audioFile.isNullOrBlank()) {
                val isSecPlaying = currentPlayingAudioId == section.audioFile
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onPlayAudio(section.audioFile) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSecPlaying) Color(0xFFD32F2F) else Color(0xFFFF9800)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSecPlaying) "⏸ ध्वनि चल रही है" else "▶ ध्वनि सुनें",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

