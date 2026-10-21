package com.sih2026.palash.ui.screens.voice

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih2026.palash.R
import com.sih2026.palash.asr.AsrState
import com.sih2026.palash.audio.PcmPlaybackState
import com.sih2026.palash.audio.RecordingState
import com.sih2026.palash.translation.TranslationStatus
import com.sih2026.palash.ui.components.NetworkStatus
import com.sih2026.palash.ui.components.OfflineStatusIndicator
import com.sih2026.palash.ui.localization.teacherStringResource
import java.io.File

@Composable
fun VoiceTranslationScreen(
    grade: String,
    subject: String,
    topic: String,
    onViewResult: (hindi: String, santaliNotice: String, liveAudioPath: String?, originalAudioPath: String?) -> Unit = { _, _, _, _ -> },
    onBack: () -> Unit = {},
    viewModel: VoiceTranslationViewModel = run {
        val context = LocalContext.current
        val application = context.applicationContext as Application
        val factory = remember(application) { VoiceTranslationViewModelFactory(application) }
        viewModel(factory = factory)
    }
) {
    val context = LocalContext.current

    LaunchedEffect(grade, subject, topic) {
        viewModel.setContext(grade, subject, topic)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            if (uiState.recordingState == RecordingState.RECORDING) {
                viewModel.cancelRecording()
            }
            viewModel.stopOriginalAudio()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording(context.cacheDir)
        } else {
            viewModel.onPermissionDenied()
        }
    }

    fun triggerRecording() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.startRecording(context.cacheDir)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
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
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Information
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.sih2026.palash.ui.components.M6TopLeftRow(onBack = onBack)
                        OfflineStatusIndicator(status = NetworkStatus.OFFLINE)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "कक्षा २ — गणित",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "हिंदी ➔ ᱥᱟᱱᱛᱟᱲᱤ ( Ol Chiki )",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Center Mic & Status Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val isRecording = uiState.recordingState == RecordingState.RECORDING
                    val micDesc = if (isRecording) teacherStringResource(R.string.voice_desc_stop_recording) else teacherStringResource(R.string.voice_desc_start_recording)

                    Text(
                        text = when {
                            uiState.recordingState == RecordingState.RECORDING -> "सुन रहे हैं... (${uiState.recordedDurationSeconds.toInt()}s)"
                            uiState.recordingState == RecordingState.STOPPING -> teacherStringResource(R.string.voice_processing)
                            uiState.isTranslating -> teacherStringResource(R.string.voice_translating)
                            uiState.recordingState == RecordingState.RECORDED -> {
                                if (uiState.asrState == AsrState.ASR_PROCESSING) teacherStringResource(R.string.voice_processing)
                                else if (uiState.translationStatus == TranslationStatus.SUCCESS) "अनुवाद तैयार है"
                                else "पहचाना गया हिंदी वाक्य"
                            }
                            else -> teacherStringResource(R.string.voice_speak_hindi)
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.statusText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Circular Mic Button
                    val micBgColor by animateColorAsState(
                        targetValue = if (isRecording) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                        label = "micBgColor"
                    )

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(micBgColor)
                            .clickable {
                                if (isRecording) {
                                    viewModel.stopRecording()
                                } else {
                                    triggerRecording()
                                }
                            }
                            .semantics {
                                contentDescription = micDesc
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRecording) "⏹️" else "🎤",
                            fontSize = 44.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isRecording) teacherStringResource(R.string.voice_tap_stop_hint) else teacherStringResource(R.string.voice_tap_mic_hint),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRecording) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                    )

                    // Replay recorded original voice button
                    val hasRecordedFile = !uiState.recordedFilePath.isNullOrBlank() && File(uiState.recordedFilePath!!).exists()
                    if (hasRecordedFile) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val isOriginalPlaying = uiState.isPlayingOriginalAudio || uiState.originalAudioState == PcmPlaybackState.PLAYING
                        OutlinedButton(
                            onClick = {
                                if (isOriginalPlaying) {
                                    viewModel.stopOriginalAudio()
                                } else {
                                    viewModel.playOriginalAudio()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isOriginalPlaying) "🔊 आवाज़ चल रही है..." else "▶ फिर से सुनें",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Two-Screen Result Card & Navigation Button
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Section 1: Hindi ASR Header & Text
                            Text(
                                text = teacherStringResource(R.string.voice_hindi_text_header),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            when (uiState.asrState) {
                                AsrState.ASR_PROCESSING -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = teacherStringResource(R.string.voice_processing_speech),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                AsrState.ASR_RESULT -> {
                                    val text = uiState.recognizedHindiText
                                    if (!text.isNullOrBlank()) {
                                        Text(
                                            text = text,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    } else {
                                        Text(
                                            text = teacherStringResource(R.string.voice_no_speech),
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                AsrState.ASR_ERROR -> {
                                    Text(
                                        text = uiState.errorMessage ?: teacherStringResource(R.string.voice_error_asr),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFC62828)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = teacherStringResource(R.string.voice_tap_mic_hint),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Section 2: Santali Ol Chiki Header & Text
                            Text(
                                text = teacherStringResource(R.string.voice_santali_header),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (uiState.isTranslating) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "अनुवाद हो रहा है...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                val santaliText = uiState.translatedSantaliText
                                if (!santaliText.isNullOrBlank()) {
                                    Text(
                                        text = santaliText,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                } else {
                                    Text(
                                        text = if (uiState.asrState == AsrState.ASR_RESULT && uiState.translationStatus != TranslationStatus.SUCCESS)
                                            "इस वाक्य का ऑफ़लाइन अनुवाद उपलब्ध नहीं है"
                                        else
                                            "माइक दबाएं और हिंदी में बोलें",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Navigation Button to TranslationResultScreen
                    val hasResultToView = !uiState.recognizedHindiText.isNullOrBlank() || !uiState.translatedSantaliText.isNullOrBlank()
                    Button(
                        onClick = {
                            onViewResult(
                                uiState.recognizedHindiText ?: "",
                                uiState.translatedSantaliText ?: "इस वाक्य का ऑफ़लाइन अनुवाद उपलब्ध नहीं है",
                                uiState.liveTtsAudioFile?.absolutePath,
                                uiState.recordedFilePath
                            )
                        },
                        enabled = hasResultToView,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "अनुवाद परिणाम देखें ➔",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
