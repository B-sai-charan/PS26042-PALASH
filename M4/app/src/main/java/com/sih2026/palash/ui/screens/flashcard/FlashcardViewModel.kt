package com.sih2026.palash.ui.screens.flashcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.palash.data.adapter.M5RoomAdapter
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.model.m5.M5Flashcard
import com.sih2026.palash.data.model.m5.M5FlashcardContent
import com.sih2026.palash.data.repository.M5ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlashcardUiState(
    val isLoading: Boolean = true,
    val flashcardContent: M5FlashcardContent? = null,
    val flashcards: List<M5Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val errorMessage: String? = null,
    val provenanceNotice: String = ""
) {
    val currentCard: M5Flashcard?
        get() = if (flashcards.isNotEmpty() && currentIndex in flashcards.indices) flashcards[currentIndex] else null
}

class FlashcardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = M5ContentRepository(application)
    private val flashcardDao = DatabaseProvider.getDatabase(application).flashcardDao()

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        loadFlashcards("Grade 2")
    }

    fun loadFlashcards(gradeName: String = "Grade 2") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.loadGrade2Flashcards()

            result.onSuccess { rawFlashcards ->
                val flashcards = com.sih2026.palash.data.config.LogicalGradeFilter.filterFlashcardsForGrade(rawFlashcards, gradeName)
                // Cache into Room
                try {
                    flashcardDao.insertFlashcards(M5RoomAdapter.toFlashcardEntities(flashcards))
                } catch (e: Exception) {
                    // Non-fatal cache write
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        flashcardContent = flashcards,
                        flashcards = flashcards.cards,
                        provenanceNotice = flashcards.validationStatus ?: ""
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Failed to load flashcards"
                    )
                }
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun nextCard() {
        val count = _uiState.value.flashcards.size
        if (count == 0) return
        val nextIdx = (_uiState.value.currentIndex + 1) % count
        _uiState.update {
            it.copy(
                currentIndex = nextIdx,
                isFlipped = false
            )
        }
    }

    fun previousCard() {
        val count = _uiState.value.flashcards.size
        if (count == 0) return
        val prevIdx = if (_uiState.value.currentIndex - 1 < 0) count - 1 else _uiState.value.currentIndex - 1
        _uiState.update {
            it.copy(
                currentIndex = prevIdx,
                isFlipped = false
            )
        }
    }
}
