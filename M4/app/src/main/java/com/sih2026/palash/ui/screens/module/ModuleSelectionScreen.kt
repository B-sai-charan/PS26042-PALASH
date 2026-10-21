package com.sih2026.palash.ui.screens.module

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih2026.palash.R
import com.sih2026.palash.ui.components.M6BackgroundArtwork
import com.sih2026.palash.ui.components.M6StudentModuleCard
import com.sih2026.palash.ui.components.NetworkStatus
import com.sih2026.palash.ui.components.OfflineStatusIndicator
import com.sih2026.palash.ui.components.StudentTranslationButton
import com.sih2026.palash.ui.localization.teacherStringResource
import com.sih2026.palash.ui.theme.PalashTerracotta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ModuleSelectionUiState(
    val gradeName: String = "Grade 2"
)

class ModuleSelectionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ModuleSelectionUiState())
    val uiState: StateFlow<ModuleSelectionUiState> = _uiState.asStateFlow()

    fun setGrade(gradeName: String) {
        _uiState.update {
            it.copy(
                gradeName = if (gradeName.isNotBlank()) gradeName else "Grade 2"
            )
        }
    }
}

@Composable
fun ModuleSelectionScreen(
    gradeName: String,
    onSelectLessons: () -> Unit,
    onSelectWorksheets: () -> Unit,
    onSelectFlashcards: () -> Unit,
    onSelectAssessments: () -> Unit,
    onOpenTranslation: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: ModuleSelectionViewModel = viewModel()
) {
    androidx.compose.runtime.LaunchedEffect(gradeName) {
        viewModel.setGrade(gradeName)
    }

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val isG1 = com.sih2026.palash.data.config.LogicalGradeFilter.isGrade1(uiState.gradeName)
    val gradeBadgeText = if (isG1) "कक्षा १ | गणित" else "कक्षा २ | गणित"

    M6BackgroundArtwork(onBack = onBack) {
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
                // Top Header with Brand and Translation Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "पलाश",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PalashTerracotta
                        )
                        Text(
                            text = "ᱯᱚᱞᱟᱥ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudentTranslationButton(onOpenTranslation = onOpenTranslation)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grade Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE3F2FD),
                    border = BorderStroke(1.dp, Color(0xFF90CAF9))
                ) {
                    Text(
                        text = gradeBadgeText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "शिक्षण मॉड्यूल",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF3E2723)
                )

                Text(
                    text = "ᱥᱮᱪᱮᱫ ᱢᱳᱰᱩᱞ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF795548)
                )



                // 4 Vertical Stacked Module Cards matching M6 reference 11 stu 03.png
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // 1. Flashcards Module Card
                    M6StudentModuleCard(
                        iconText = "🎴",
                        titleHindi = "फ्लैशकार्ड",
                        titleSantali = "ᱯᱷᱞᱮᱥ ᱠᱟᱨᱰ",
                        subtitle = "संख्या और कार्ड",
                        containerBg = Color(0xFFF3E5F5),
                        borderColor = Color(0xFFCE93D8),
                        textColor = Color(0xFF4A148C),
                        onClick = onSelectFlashcards
                    )

                    // 2. Worksheets Module Card
                    M6StudentModuleCard(
                        iconText = "📝",
                        titleHindi = "कार्यपत्रक",
                        titleSantali = "ᱠᱟᱹᱢᱤ ᱥᱟᱠᱟᱢ",
                        subtitle = "अभ्यास प्रश्न",
                        containerBg = Color(0xFFE1F5FE),
                        borderColor = Color(0xFF81D4FA),
                        textColor = Color(0xFF01579B),
                        onClick = onSelectWorksheets
                    )

                    // 3. Assessments Module Card
                    M6StudentModuleCard(
                        iconText = "🖊️",
                        titleHindi = "मूल्यांकन",
                        titleSantali = "ᱢᱩᱞᱭᱟᱝᱠᱚᱱ",
                        subtitle = "प्रश्नोत्तरी एवं जांच",
                        containerBg = Color(0xFFFCE4EC),
                        borderColor = Color(0xFFF48FB1),
                        textColor = Color(0xFF880E4F),
                        onClick = onSelectAssessments
                    )

                    // 4. Lessons Module Card
                    M6StudentModuleCard(
                        iconText = "📚",
                        titleHindi = "पाठ",
                        titleSantali = "ᱯᱟᱴ",
                        subtitle = "पाठ एवं ऑडियो",
                        containerBg = Color(0xFFFFF8E1),
                        borderColor = Color(0xFFFFE082),
                        textColor = Color(0xFFE65100),
                        onClick = onSelectLessons
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "← " + teacherStringResource(R.string.nav_previous),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

