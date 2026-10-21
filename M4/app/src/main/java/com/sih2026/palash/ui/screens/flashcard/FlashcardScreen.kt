package com.sih2026.palash.ui.screens.flashcard

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih2026.palash.R
import com.sih2026.palash.audio.AndroidAudioPlaybackEngineImpl
import com.sih2026.palash.audio.AudioPlaybackState
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import com.sih2026.palash.ui.components.M6BackgroundArtwork
import com.sih2026.palash.ui.components.NetworkStatus
import com.sih2026.palash.ui.components.OfflineStatusIndicator
import com.sih2026.palash.ui.components.PalashBrandHeader
import com.sih2026.palash.ui.components.StudentTranslationButton
import com.sih2026.palash.ui.localization.teacherStringResource

/**
 * Safe HTML-stripping utility.
 * Removes any HTML tags from the visual string so they never appear as raw markup.
 * Only used as last-resort fallback — prefer visual_rows rendering via [FlashcardVisualRows].
 */
fun stripHtmlTags(input: String): String {
    return input
        .replace(Regex("<[^>]*>"), "")   // remove all HTML tags
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .trim()
}

/**
 * Determines whether a given visual string contains raw HTML markup that must NOT
 * be displayed directly as text.
 */
fun containsRawHtml(visual: String): Boolean {
    return visual.contains('<') && visual.contains('>')
}

/**
 * Renders a visual using the structured [visualRows] data from M5 JSON.
 * Each element in [visualRows] defines the count of [emoji] icons on that row.
 * Rows are left-aligned to form a right-angle triangle arrangement.
 *
 * Example: visual_rows=[1,2] → row 1: 🍎, row 2: 🍎🍎 (Number 3, right-angle triangle)
 *          visual_rows=[1,2,3] → rows: 🍎 / 🍎🍎 / 🍎🍎🍎 (Number 6, right-angle triangle)
 */
@Composable
fun FlashcardVisualRows(
    visualRows: List<Int>,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics { testTag = "flashcard_visual_rows" },
        horizontalAlignment = Alignment.Start
    ) {
        visualRows.forEach { count ->
            Row(
                horizontalArrangement = Arrangement.Start
            ) {
                repeat(count) {
                    Text(
                        text = emoji,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Renders the front-side visual content of a flashcard card safely.
 *
 * Priority:
 * 1. If [visualRows] is non-null and non-empty → use native [FlashcardVisualRows] (no HTML)
 * 2. Else if [visual] is non-null and does NOT contain HTML → render as plain Text
 * 3. Else if [visual] contains HTML → strip tags and render as plain Text
 *
 * RAW HTML MUST NEVER BE PASSED TO [Text] DIRECTLY.
 */
@Composable
fun FlashcardSafeVisual(
    visual: String?,
    visualRows: List<Int>?,
    modifier: Modifier = Modifier
) {
    if (!visualRows.isNullOrEmpty()) {
        val emoji = if (!visual.isNullOrBlank()) {
            val stripped = stripHtmlTags(visual)
            if (stripped.isNotBlank()) stripped.trim().take(2) else "🍎"
        } else "🍎"
        FlashcardVisualRows(
            visualRows = visualRows,
            emoji = emoji,
            modifier = modifier
        )
    } else if (!visual.isNullOrBlank()) {
        val safeText = if (containsRawHtml(visual)) stripHtmlTags(visual) else visual
        Text(
            text = safeText,
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = modifier
        )
    }
}

@Composable
fun FlashcardScreen(
    gradeName: String = "Grade 2",
    onBackToClassroom: () -> Unit,
    onOpenTranslation: () -> Unit = {},
    viewModel: FlashcardViewModel = viewModel()
) {
    androidx.compose.runtime.LaunchedEffect(gradeName) {
        viewModel.loadFlashcards(gradeName)
    }

    val context = LocalContext.current
    val audioRepo = remember { SantaliAudioRepositoryImpl(context) }
    val playbackEngine = remember { AndroidAudioPlaybackEngineImpl(context) }
    val currentPlayingAudioId by com.sih2026.palash.audio.GlobalAudioPlayer.currentPlayingAudioId.collectAsState()
    var audioError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val card = uiState.currentCard
    val totalCount = uiState.flashcards.size

    DisposableEffect(Unit) {
        onDispose {
            playbackEngine.release()
        }
    }

    LaunchedEffect(uiState.currentIndex) {
        playbackEngine.stop()
        audioError = null
    }

    var showTranslationDialog by remember(card?.cardId) { mutableStateOf(false) }

    M6BackgroundArtwork(onBack = onBackToClassroom) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Right Controls (Student Translation & Offline Indicator)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentTranslationButton(onOpenTranslation = {
                        showTranslationDialog = true
                    })
                    Spacer(modifier = Modifier.width(8.dp))
                    OfflineStatusIndicator(status = NetworkStatus.OFFLINE)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Centered Brand Header
                PalashBrandHeader(
                    showOfflineBadge = false,
                    subtitle = "भाषाओं को जोड़ें, सीखने का उजाला फैलाएं"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // M6 Sub-header Banner Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFCC80)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFE0B2),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🎴", fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "फ्लैशकार्ड / ᱯᱷᱞᱮᱥᱠᱟᱨᱰ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A2E19)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFE0B2)
                            ) {
                                Text(
                                    text = "कक्षा 2 | गणित | 1 से 10 तक संख्या",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color(0xFFFFB74D))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFD84315))
                        }
                    }
                } else if (totalCount > 0 && card != null) {

                    // Flashcard Physical Card View
                    // Uses heightIn(min = 260.dp) to avoid any clipping of 6-apple visual structure for Number 6
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp)
                            .clickable { viewModel.flipCard() }
                            .semantics { testTag = "flashcard_card" },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isFlipped) Color(0xFFFFFDE7) else Color.White
                        ),
                        border = BorderStroke(
                            2.dp,
                            if (uiState.isFlipped) Color(0xFFFBC02D) else Color(0xFFFFB74D)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Side Tag
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (uiState.isFlipped) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                border = BorderStroke(
                                    1.dp,
                                    if (uiState.isFlipped) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
                                )
                            ) {
                                Text(
                                    text = if (uiState.isFlipped) "ᱥᱟᱱᱛᱟᱲᱤ (OL CHIKI)" else "हिंदी / संख्या",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isFlipped) Color(0xFF2E7D32) else Color(0xFFD84315),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (uiState.isFlipped) {
                                // REVERSE SIDE: Santali / Ol Chiki Content
                                Text(
                                    text = card.santali,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF5D4037),
                                    modifier = Modifier.semantics { testTag = "flashcard_santali_text" }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Hindi Reference
                                Text(
                                    text = card.hindi,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF616161)
                                )
                            } else {
                                // FRONT SIDE: Large Number + Unclipped Visual + Hindi Text
                                if (card.numberDisplay.isNotBlank()) {
                                    Text(
                                        text = card.numberDisplay,
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4A2E19)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // Unclipped Visual Representation (1+2 for Number 3, 1+2+3 for Number 6)
                                FlashcardSafeVisual(
                                    visual = card.visual,
                                    visualRows = card.visualRows,
                                    modifier = Modifier.semantics { testTag = "flashcard_visual" }
                                )

                                if (card.visual != null || card.visualRows != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                if (!card.hindi.isNullOrBlank()) {
                                    Text(
                                        text = card.hindi,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF2C3E50)
                                    )
                                }

                                if (!card.santali.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = card.santali,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF5D4037)
                                    )
                                }

                                if (card.operator != null && card.operand1 != null && card.operand2 != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${card.operand1} ${card.operator} ${card.operand2} = ${card.result}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Audio Playback Action Button (if asset exists)
                            val cardId = card.cardId
                            val assetPath = if (!cardId.isNullOrBlank()) audioRepo.getAssetPathForAudio(cardId) else null
                            val hasAudio = assetPath != null && audioRepo.hasAudioAsset(cardId!!)

                            if (hasAudio) {
                                val isCardPlaying = (currentPlayingAudioId == cardId)
                                Button(
                                    onClick = {
                                        playbackEngine.playAsset(assetPath!!, audioId = cardId) { _, err ->
                                            audioError = err
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCardPlaying) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                    )
                                ) {
                                    Text(
                                        text = if (isCardPlaying) "⏸ ध्वनि चल रही है" else "▶ ध्वनि सुनें",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Card Footer Info (Counter & Flip Instruction)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "कार्ड ${uiState.currentIndex + 1} / $totalCount",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315)
                                )

                                Text(
                                    text = "कार्ड पलटें 🔄",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "कोई फ़्लैशकार्ड उपलब्ध नहीं है।",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Controls (Previous / Next / Back)
            Column(modifier = Modifier.fillMaxWidth()) {
                if (totalCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Previous Button
                        Button(
                            onClick = { viewModel.previousCard() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFF3E0),
                                contentColor = Color(0xFFD84315)
                            ),
                            border = BorderStroke(1.5.dp, Color(0xFFFFCC80))
                        ) {
                            Text(
                                text = teacherStringResource(R.string.nav_previous),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Next Button
                        Button(
                            onClick = { viewModel.nextCard() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8F5E9),
                                contentColor = Color(0xFF2E7D32)
                            ),
                            border = BorderStroke(1.5.dp, Color(0xFFA5D6A7))
                        ) {
                            Text(
                                text = teacherStringResource(R.string.nav_next),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Back to Classroom Button
                OutlinedButton(
                    onClick = onBackToClassroom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFBDBDBD))
                ) {
                    Text(
                        text = teacherStringResource(R.string.flashcard_back_to_classroom),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    )
                }
            }
        }

        if (showTranslationDialog && card != null) {
            AlertDialog(
                onDismissRequest = { showTranslationDialog = false },
                title = {
                    Text(
                        text = "अनुवाद",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(text = "ᱥᱟᱱᱛᱟᱲᱤ (Ol Chiki):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = card.santali, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "हिंदी अर्थ:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = card.hindi, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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

