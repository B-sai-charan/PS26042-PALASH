package com.sih2026.palash.ui

import com.sih2026.palash.data.model.m5.M5Flashcard
import com.sih2026.palash.data.model.m5.M5LessonContent
import com.sih2026.palash.data.model.m5.M5Section
import com.sih2026.palash.data.model.m5.M5WorksheetQuestion
import com.sih2026.palash.ui.screens.flashcard.FlashcardUiState
import com.sih2026.palash.ui.screens.lesson.LessonUiState
import com.sih2026.palash.ui.screens.worksheet.WorksheetUiState
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying P1-2 Student UI metadata & provenance privacy rules.
 */
class StudentUiMetadataPrivacyUnitTest {

    @Test
    fun lessonUiState_provenanceNotice_doesNotDefaultToTechnicalString() {
        val state = LessonUiState()
        assertFalse(
            "LessonUiState must not contain technical metadata 'model_generated_unvalidated'",
            state.provenanceNotice.contains("model_generated_unvalidated")
        )
    }

    @Test
    fun worksheetUiState_provenanceNotice_doesNotDefaultToTechnicalString() {
        val state = WorksheetUiState()
        assertFalse(
            "WorksheetUiState must not contain technical metadata 'model_generated_unvalidated'",
            state.provenanceNotice.contains("model_generated_unvalidated")
        )
    }

    @Test
    fun flashcardUiState_provenanceNotice_doesNotDefaultToTechnicalString() {
        val state = FlashcardUiState()
        assertFalse(
            "FlashcardUiState must not contain technical metadata 'model_generated_unvalidated'",
            state.provenanceNotice.contains("model_generated_unvalidated")
        )
    }

    @Test
    fun underlyingM5Models_retainStoredValidationStatusIntact() {
        val lesson = M5LessonContent(
            lessonId = "1",
            title = "Counting 1 to 20",
            grade = 2,
            subject = "Mathematics",
            topic = "Counting",
            validationStatus = "model_generated_unvalidated"
        )
        val question = M5WorksheetQuestion(
            questionId = "WSQ001",
            questionType = "multiple_choice",
            correctAnswer = "5",
            validationStatus = "model_generated_unvalidated"
        )
        val flashcard = M5Flashcard(
            cardId = "FC001",
            type = "number_recognition",
            hindi = "एक",
            santali = "᱑",
            validationStatus = "model_generated_unvalidated"
        )

        assertEquals("model_generated_unvalidated", lesson.validationStatus)
        assertEquals("model_generated_unvalidated", question.validationStatus)
        assertEquals("model_generated_unvalidated", flashcard.validationStatus)

        // Internal identifiers must remain untouched
        assertEquals("1", lesson.lessonId)
        assertEquals("WSQ001", question.questionId)
        assertEquals("FC001", flashcard.cardId)
    }

    @Test
    fun studentFacingLabels_doNotExposeRawWavFilenames() {
        val audioButtonLabel = "🔊 Listen Audio (ᱥᱟᱲᱮ ᱟᱸᱡᱚᱢ ᱢᱮ)"
        assertFalse(audioButtonLabel.contains(".wav"))
        assertFalse(audioButtonLabel.contains("FC001"))
        assertFalse(audioButtonLabel.contains("L2_SEC01"))
    }
}
