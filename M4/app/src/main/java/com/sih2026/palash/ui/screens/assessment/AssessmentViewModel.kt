package com.sih2026.palash.ui.screens.assessment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.palash.data.model.m5.M5AssessmentContent
import com.sih2026.palash.data.model.m5.M5AssessmentQuestion
import com.sih2026.palash.data.repository.M5ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssessmentUiState(
    val gradeName: String = "Grade 2",
    val isLoading: Boolean = true,
    val isGrade1Notice: Boolean = false,
    val assessmentContent: M5AssessmentContent? = null,
    val questions: List<M5AssessmentQuestion> = emptyList(),
    val selectedAnswers: Map<String, String> = emptyMap(), // questionId -> answer
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val validationError: String? = null,
    val errorMessage: String? = null,
    val provenanceNotice: String = ""
)

class AssessmentViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = M5ContentRepository(application)

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState: StateFlow<AssessmentUiState> = _uiState.asStateFlow()

    init {
        loadAssessment("Grade 2")
    }

    fun setGrade(gradeName: String) {
        loadAssessment(gradeName)
    }

    fun loadAssessment(gradeName: String = "Grade 2") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isGrade1Notice = false) }
            val result = repository.loadGrade2Assessment()

            result.onSuccess { rawAssessment ->
                val assessment = com.sih2026.palash.data.config.LogicalGradeFilter.filterAssessmentForGrade(rawAssessment, gradeName)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        gradeName = gradeName,
                        assessmentContent = assessment,
                        questions = assessment.questions,
                        totalQuestions = assessment.questions.size,
                        provenanceNotice = assessment.validationStatus ?: ""
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Failed to load assessment content"
                    )
                }
            }
        }
    }

    fun selectAnswer(questionId: String, answer: String) {
        if (_uiState.value.isSubmitted) return // Locked after submission
        _uiState.update { state ->
            val updatedMap = state.selectedAnswers.toMutableMap()
            if (updatedMap[questionId] == answer) {
                updatedMap.remove(questionId)
            } else {
                updatedMap[questionId] = answer
            }
            state.copy(
                selectedAnswers = updatedMap,
                validationError = null
            )
        }
    }

    fun submitAssessment() {
        val state = _uiState.value
        val questions = state.questions
        if (questions.isEmpty()) return

        // Check if all questions are answered
        val unansweredCount = questions.count { !state.selectedAnswers.containsKey(it.assessmentQuestionId) }
        if (unansweredCount > 0) {
            _uiState.update {
                it.copy(
                    validationError = "कृपया मूल्यांकन जमा करने से पहले सभी $unansweredCount प्रश्नों के उत्तर दें।"
                )
            }
            return
        }

        // Evaluate all questions against correct answers
        var correctCount = 0
        questions.forEach { q ->
            val selected = state.selectedAnswers[q.assessmentQuestionId]?.trim()
            if (selected != null && selected.equals(q.correctAnswer.trim(), ignoreCase = true)) {
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

    fun resetAssessment() {
        _uiState.update {
            it.copy(
                selectedAnswers = emptyMap(),
                isSubmitted = false,
                score = 0,
                validationError = null
            )
        }
    }
}
