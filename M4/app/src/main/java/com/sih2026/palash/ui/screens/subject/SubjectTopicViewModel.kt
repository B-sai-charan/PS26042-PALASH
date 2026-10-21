package com.sih2026.palash.ui.screens.subject

import androidx.lifecycle.ViewModel
import com.sih2026.palash.data.demo.DemoData
import com.sih2026.palash.data.demo.SubjectItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SubjectTopicUiState(
    val gradeName: String = "Grade 2",
    val subjects: List<SubjectItem> = DemoData.subjects,
    val selectedSubjectId: String = "math",
    val selectedTopic: String = "Counting",
    val selectedSection: String = "A"
)

class SubjectTopicViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SubjectTopicUiState())
    val uiState: StateFlow<SubjectTopicUiState> = _uiState.asStateFlow()

    fun setGrade(gradeName: String) {
        _uiState.update { it.copy(gradeName = gradeName) }
    }

    fun selectSubject(subjectId: String) {
        val subject = _uiState.value.subjects.find { it.id == subjectId }
        val defaultTopic = subject?.topics?.firstOrNull() ?: ""
        _uiState.update {
            it.copy(
                selectedSubjectId = subjectId,
                selectedTopic = defaultTopic
            )
        }
    }

    fun selectTopic(topic: String) {
        _uiState.update { it.copy(selectedTopic = topic) }
    }

    fun selectSection(section: String) {
        _uiState.update { it.copy(selectedSection = section) }
    }
}
