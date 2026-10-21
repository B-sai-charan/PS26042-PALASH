package com.sih2026.palash.ui.screens.grade

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih2026.palash.R
import com.sih2026.palash.ui.components.M6BackgroundArtwork
import com.sih2026.palash.ui.components.M6GradeCard
import com.sih2026.palash.ui.components.PalashBrandHeader
import com.sih2026.palash.ui.localization.teacherStringResource

@Composable
fun GradeSelectionScreen(
    onContinue: (selectedGradeName: String) -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: GradeSelectionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

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
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PalashBrandHeader(showOfflineBadge = true)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "अपनी कक्षा चुनें",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF3E2723),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "ᱟᱢᱟᱜ ᱪᱟᱱᱟᱪ ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF795548),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    M6GradeCard(
                        gradeNumber = 1,
                        devanagariTitle = teacherStringResource(R.string.grade_1_name),
                        olChikiTitle = "ᱪᱟᱱᱟᱪ ᱑",
                        subtitle = teacherStringResource(R.string.grade_1_desc),
                        isSelected = uiState.selectedGradeId == "grade_1",
                        onClick = { viewModel.selectGrade("grade_1") },
                        modifier = Modifier.weight(1f)
                    )

                    M6GradeCard(
                        gradeNumber = 2,
                        devanagariTitle = teacherStringResource(R.string.grade_2_name),
                        olChikiTitle = "ᱪᱟᱱᱟᱪ ᱒",
                        subtitle = teacherStringResource(R.string.grade_2_desc),
                        isSelected = uiState.selectedGradeId == "grade_2",
                        onClick = { viewModel.selectGrade("grade_2") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Grade 3 card intentionally omitted for SIH MVP scope.
            }

            Button(
                onClick = {
                    val selectedGradeName = when (uiState.selectedGradeId) {
                        "grade_1" -> "Grade 1"
                        "grade_2" -> "Grade 2"
                        else -> "Grade 2" // safe fallback
                    }
                    onContinue(selectedGradeName)
                },
                enabled = uiState.selectedGradeId == "grade_2" || uiState.selectedGradeId == "grade_1",
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.sih2026.palash.ui.theme.M6DarkAmber,
                    contentColor = Color.White,
                    disabledContainerColor = com.sih2026.palash.ui.theme.M6Disabled,
                    disabledContentColor = Color(0xFF9E9E9E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "आगे बढ़ें ➔",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

