package com.sih2026.palash.ui.screens.lesson

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.palash.data.adapter.M5RoomAdapter
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.model.m5.M5LessonContent
import com.sih2026.palash.data.repository.M5ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LessonUiState(
    val isLoading: Boolean = true,
    val lessonContent: M5LessonContent? = null,
    val errorMessage: String? = null,
    val provenanceNotice: String = ""
)

class LessonViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = M5ContentRepository(application)
    private val lessonDao = DatabaseProvider.getDatabase(application).lessonDao()

    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        loadLesson("Grade 2")
    }

    fun loadLesson(gradeName: String = "Grade 2") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.loadGrade2Lesson()

            result.onSuccess { rawLesson ->
                val lesson = com.sih2026.palash.data.config.LogicalGradeFilter.filterLessonForGrade(rawLesson, gradeName)
                // Cache into Room using existing Room schema
                try {
                    lessonDao.insertLesson(M5RoomAdapter.toLessonEntity(lesson))
                } catch (e: Exception) {
                    // Non-fatal cache write fallback
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lessonContent = lesson,
                        provenanceNotice = lesson.validationStatus ?: ""
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Failed to load lesson"
                    )
                }
            }
        }
    }
}
