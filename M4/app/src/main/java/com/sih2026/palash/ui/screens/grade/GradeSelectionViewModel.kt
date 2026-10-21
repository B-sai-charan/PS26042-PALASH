package com.sih2026.palash.ui.screens.grade

import androidx.lifecycle.ViewModel
import com.sih2026.palash.data.demo.DemoData
import com.sih2026.palash.data.demo.GradeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GradeSelectionUiState(
    // MVP scope: Grade 1 and Grade 2 only. Grade 3 will be added when M5 Grade 3 content is integrated.
    val availableGrades: List<GradeItem> = DemoData.mvpGrades,
    val selectedGradeId: String = "grade_2"
)

class GradeSelectionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GradeSelectionUiState())
    val uiState: StateFlow<GradeSelectionUiState> = _uiState.asStateFlow()

    fun selectGrade(gradeId: String) {
        _uiState.update { it.copy(selectedGradeId = gradeId) }
    }
}
