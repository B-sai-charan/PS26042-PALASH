package com.sih2026.palash.ui.screens.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.sih2026.palash.audio.AndroidAudioPlaybackEngineImpl
import com.sih2026.palash.audio.AudioPlaybackEngine
import com.sih2026.palash.audio.AudioPlaybackState
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import com.sih2026.palash.classroom.ClassroomFetchResult
import com.sih2026.palash.classroom.ClassroomMessageResult
import com.sih2026.palash.classroom.ClassroomTranslationMessage
import com.sih2026.palash.classroom.SectionMatcher
import com.sih2026.palash.classroom.StudentClassroomClient
import com.sih2026.palash.data.config.AppConfigManager
import com.sih2026.palash.ui.components.PalashBrandHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class LocalConnectionState {
    CONNECTING,
    CONNECTED_WAITING,
    CONNECTED_RECEIVED,
    TEACHER_UNAVAILABLE,
    SECTION_MISMATCH
}

@Composable
fun StudentHomeScreen(
    onReconfigure: () -> Unit,
    onOpenLesson: (grade: String, subject: String, topic: String, section: String) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val configManager = remember { AppConfigManager(context) }
    val studentConfig = remember { configManager.getStudentConfig() }
    val client = remember { StudentClassroomClient() }
    val audioRepo = remember { SantaliAudioRepositoryImpl(context) }
    val playbackEngine = remember { AndroidAudioPlaybackEngineImpl(context) }
    val scrollState = rememberScrollState()

    var connectionState by remember { mutableStateOf(LocalConnectionState.CONNECTING) }
    var latestAcceptedMessage by remember { mutableStateOf<ClassroomTranslationMessage?>(null) }
    var lastMessageId by remember { mutableStateOf<String?>(null) }
    var statusDetailText by remember { mutableStateOf("Connecting to local classroom network...") }
    val currentPlayingAudioId by com.sih2026.palash.audio.GlobalAudioPlayer.currentPlayingAudioId.collectAsState()
    var audioError by remember { mutableStateOf<String?>(null) }

    val gradeDigitOlChiki = when (studentConfig.grade) {
        1 -> "᱑"
        2 -> "᱒"
        3 -> "᱓"
        else -> "${studentConfig.grade}"
    }

    DisposableEffect(Unit) {
        onDispose {
            playbackEngine.release()
        }
    }

    // Classroom Network Polling Loop (2 second interval)
    LaunchedEffect(studentConfig) {
        while (true) {
            val result = withContext(Dispatchers.IO) {
                client.fetchLatestMessage(studentConfig.teacherIp, studentConfig.teacherPort)
            }
            when (result) {
                is ClassroomFetchResult.Success -> {
                    val msg = result.message
                    val evalResult = SectionMatcher.evaluateMessage(
                        studentGrade = studentConfig.grade,
                        studentSection = studentConfig.section,
                        lastMessageId = lastMessageId,
                        message = msg
                    )
                    when (evalResult) {
                        ClassroomMessageResult.ACCEPTED -> {
                            lastMessageId = msg.messageId
                            latestAcceptedMessage = msg
                            connectionState = LocalConnectionState.CONNECTED_RECEIVED
                            statusDetailText = "Connected to ${studentConfig.teacherIp}:${studentConfig.teacherPort}"
                        }
                        ClassroomMessageResult.DUPLICATE -> {
                            connectionState = LocalConnectionState.CONNECTED_RECEIVED
                            statusDetailText = "Connected to ${studentConfig.teacherIp}:${studentConfig.teacherPort} (Latest active)"
                        }
                        ClassroomMessageResult.WRONG_GRADE, ClassroomMessageResult.WRONG_SECTION -> {
                            connectionState = LocalConnectionState.SECTION_MISMATCH
                            statusDetailText = "Ignored broadcast for Grade ${msg.grade} Section ${msg.section}"
                        }
                        ClassroomMessageResult.INVALID_MESSAGE -> {
                            statusDetailText = "Ignored invalid classroom payload"
                        }
                        ClassroomMessageResult.NO_MESSAGE -> {
                            connectionState = LocalConnectionState.CONNECTED_WAITING
                            statusDetailText = "Connected to ${studentConfig.teacherIp}:${studentConfig.teacherPort}"
                        }
                    }
                }
                is ClassroomFetchResult.NoMessage -> {
                    connectionState = LocalConnectionState.CONNECTED_WAITING
                    statusDetailText = "Connected to ${studentConfig.teacherIp}:${studentConfig.teacherPort} (Waiting for teacher)"
                }
                is ClassroomFetchResult.NetworkError, is ClassroomFetchResult.ServerError -> {
                    connectionState = LocalConnectionState.TEACHER_UNAVAILABLE
                    statusDetailText = "Teacher server unavailable at ${studentConfig.teacherIp}:${studentConfig.teacherPort}"
                }
            }
            delay(2000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
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
                PalashBrandHeader(
                    showOfflineBadge = true,
                    subtitle = "ᱪᱮᱛᱮᱫᱤᱭᱟᱹ (Student Mode)"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Assigned Student Configuration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFFFCC80))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Assigned Device Configuration",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            val badgeColor = when (connectionState) {
                                LocalConnectionState.CONNECTED_RECEIVED, LocalConnectionState.CONNECTED_WAITING -> Color(0xFF2E7D32)
                                LocalConnectionState.CONNECTING -> Color(0xFFF57C00)
                                else -> Color(0xFFC62828)
                            }
                            Text(
                                text = when (connectionState) {
                                    LocalConnectionState.CONNECTED_RECEIVED, LocalConnectionState.CONNECTED_WAITING -> "🟢 कनेक्टेड"
                                    LocalConnectionState.CONNECTING -> "🟡 कनेक्ट हो रहा है..."
                                    LocalConnectionState.SECTION_MISMATCH -> "🟠 वर्ग भिन्न है"
                                    LocalConnectionState.TEACHER_UNAVAILABLE -> "🔴 सर्वर ऑफ़लाइन"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "कक्षा ${studentConfig.grade} (ᱪᱟᱱᱟᱪ $gradeDigitOlChiki)${if (studentConfig.section.isNotBlank()) " — वर्ग ${studentConfig.section}" else ""}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFBF360C)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "शिक्षक IP: ${studentConfig.teacherIp}:${studentConfig.teacherPort}",
                            fontSize = 13.sp,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusDetailText,
                            fontSize = 12.sp,
                            color = Color(0xFF795548)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Self-Paced Learning Entry Card (Grade 2 MVP Content)
                if (studentConfig.grade == 2) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOpenLesson("Grade 2", "Mathematics", "Counting", studentConfig.section.ifBlank { "A" })
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                        ),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📚 स्व-अध्ययन पाठ (ᱪᱟᱱᱟᱪ ᱒)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "कक्षा २",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "कक्षा २ - गणित (गिनती)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    onOpenLesson("Grade 2", "Mathematics", "Counting", studentConfig.section.ifBlank { "A" })
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "▶ पाठ शुरू करें",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (studentConfig.grade == 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "📚 स्व-अध्ययन पाठ (ᱪᱟᱱᱟᱪ ᱑)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "कक्षा १ शिक्षण सामग्री",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "कक्षा १ के लिए शिक्षण सामग्री शीघ्र उपलब्ध होगी। वर्तमान पाठ्यसामग्री कक्षा २ के लिए उपलब्ध है।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val currentMsg = latestAcceptedMessage
                if (currentMsg != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(2.dp, Color(0xFFD84315))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📢 शिक्षक पाठ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFD84315)
                                )
                                Text(
                                    text = "कक्षा ${currentMsg.grade}${if (!currentMsg.section.isNullOrBlank()) " - वर्ग ${currentMsg.section}" else ""}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "ᱥᱟᱱᱛᱟᱲᱤ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00897B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentMsg.santaliText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF004D40)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            val audioId = currentMsg.audioId
                            val assetPath = if (!audioId.isNullOrBlank()) audioRepo.getAssetPathForAudio(audioId) else null
                            if (assetPath != null && audioRepo.hasAudioAsset(audioId!!)) {
                                val isMsgAudioPlaying = currentPlayingAudioId == audioId
                                Button(
                                    onClick = {
                                        playbackEngine.playAsset(assetPath, audioId = audioId) { _, err ->
                                            audioError = err
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isMsgAudioPlaying) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                    )
                                ) {
                                    Text(
                                        text = if (isMsgAudioPlaying) "⏸ ᱥᱟᱲᱮ ᱟᱸᱡᱚᱢᱚᱜ ᱠᱟᱱᱟ..." else "▶ ᱥᱟᱲᱮ ᱟᱸᱡᱚᱢ ᱢᱮ",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "ध्वनि उपलब्ध नहीं है",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📡 शिक्षक प्रसारण की प्रतीक्षा...",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "जब शिक्षक कक्षा ${studentConfig.grade}${if (studentConfig.section.isNotBlank()) " वर्ग ${studentConfig.section}" else ""} में बोलेंगे, तब स्थानीय नेटवर्क के माध्यम से यहाँ संताली पाठ प्रदर्शित होंगे।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        configManager.resetConfig()
                        onReconfigure()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "⚙️ डिवाइस भूमिका / शिक्षक IP पुनः बदलें",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
