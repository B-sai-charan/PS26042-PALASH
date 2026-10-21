package com.sih2026.palash.ui.screens.worksheet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.palash.data.adapter.M5RoomAdapter
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.model.m5.M5WorksheetContent
import com.sih2026.palash.data.model.m5.M5WorksheetQuestion
import com.sih2026.palash.data.repository.M5ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorksheetUiState(
    val isLoading: Boolean = true,
    val worksheetContent: M5WorksheetContent? = null,
    val questions: List<M5WorksheetQuestion> = emptyList(),
    val selectedOptions: Map<String, String> = emptyMap(), // questionId -> selectedOption
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val validationError: String? = null,
    val errorMessage: String? = null,
    val provenanceNotice: String = ""
)

class WorksheetViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = M5ContentRepository(application)
    private val worksheetDao = DatabaseProvider.getDatabase(application).worksheetDao()

    private val _uiState = MutableStateFlow(WorksheetUiState())
    val uiState: StateFlow<WorksheetUiState> = _uiState.asStateFlow()

    init {
        loadWorksheet("Grade 2")
    }

    fun loadWorksheet(gradeName: String = "Grade 2") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.loadGrade2Worksheet()

            result.onSuccess { rawWorksheet ->
                val worksheet = com.sih2026.palash.data.config.LogicalGradeFilter.filterWorksheetForGrade(rawWorksheet, gradeName)
                // Cache into Room
                try {
                    worksheetDao.insertWorksheet(M5RoomAdapter.toWorksheetEntity(worksheet))
                } catch (e: Exception) {
                    // Non-fatal cache write
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        worksheetContent = worksheet,
                        questions = worksheet.questions,
                        totalQuestions = worksheet.questions.size,
                        provenanceNotice = worksheet.validationStatus ?: ""
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Failed to load worksheet"
                    )
                }
            }
        }
    }

    fun selectOption(questionId: String, option: String) {
        if (_uiState.value.isSubmitted) return // Selection locked after Submit
        _uiState.update { state ->
            val updatedMap = state.selectedOptions.toMutableMap()
            if (updatedMap[questionId] == option) {
                updatedMap.remove(questionId)
            } else {
                updatedMap[questionId] = option
            }
            state.copy(
                selectedOptions = updatedMap,
                validationError = null
            )
        }
    }

    fun submitWorksheet() {
        val state = _uiState.value
        val questions = state.questions
        if (questions.isEmpty()) return

        // Evaluate all questions (unanswered questions are evaluated as incorrect)
        var correctCount = 0
        questions.forEach { q ->
            val selected = state.selectedOptions[q.questionId]?.trim()
            if (selected != null && selected == q.correctAnswer.trim()) {
                correctCount++
            }
        }

        _uiState.update {
            it.copy(
                isSubmitted = true,
                score = correctCount,
                validationError = null
            )
        }
    }

    fun resetWorksheet() {
        _uiState.update {
            it.copy(
                selectedOptions = emptyMap(),
                isSubmitted = false,
                score = 0,
                validationError = null
            )
        }
    }
}
