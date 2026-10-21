package com.sih2026.palash.data.validator

import com.sih2026.palash.data.model.m5.M5AssessmentContent
import com.sih2026.palash.data.model.m5.M5FlashcardContent
import com.sih2026.palash.data.model.m5.M5LessonContent
import com.sih2026.palash.data.model.m5.M5WorksheetContent

/**
 * Validator verifying that Grade 2 JSON audio references resolve to exact filenames in the audio manifest.
 * Performs exact string matching (no fuzzy matching).
 */
object M5AudioReferenceValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val verifiedReferences: List<String>,
        val missingReferences: List<String>
    )

    fun validateLessonAudio(lesson: M5LessonContent, availableAudioFilenames: Set<String>): ValidationResult {
        val verified = mutableListOf<String>()
        val missing = mutableListOf<String>()

        for (section in lesson.sections) {
            val audioName = section.audioFile
            if (!audioName.isNullOrEmpty()) {
                if (availableAudioFilenames.contains(audioName)) {
                    verified.add(audioName)
                } else {
                    missing.add("Section ${section.sectionId}: $audioName")
                }
            }
        }

        return ValidationResult(
            isValid = missing.isEmpty(),
            verifiedReferences = verified,
            missingReferences = missing
        )
    }


    fun validateFlashcardAudio(flashcards: M5FlashcardContent, availableAudioFilenames: Set<String>): ValidationResult {
        val verified = mutableListOf<String>()
        val missing = mutableListOf<String>()

        for (card in flashcards.cards) {
            val expectedFilename = "${card.cardId}.wav"
            if (availableAudioFilenames.contains(expectedFilename)) {
                verified.add(expectedFilename)
            } else {
                missing.add("Card ${card.cardId}: $expectedFilename")
            }
        }

        return ValidationResult(
            isValid = missing.isEmpty(),
            verifiedReferences = verified,
            missingReferences = missing
        )
    }

    fun validateWorksheetAudio(worksheet: M5WorksheetContent, availableAudioFilenames: Set<String>): ValidationResult {
        val verified = mutableListOf<String>()
        val missing = mutableListOf<String>()

        for (question in worksheet.questions) {
            val expectedFilename = "${question.questionId}_question.wav"
            if (availableAudioFilenames.contains(expectedFilename)) {
                verified.add(expectedFilename)
            } else {
                missing.add("Question ${question.questionId}: $expectedFilename")
            }
        }

        return ValidationResult(
            isValid = missing.isEmpty(),
            verifiedReferences = verified,
            missingReferences = missing
        )
    }

    fun validateAssessmentAudio(assessment: M5AssessmentContent, availableAudioFilenames: Set<String>): ValidationResult {
        val verified = mutableListOf<String>()
        val missing = mutableListOf<String>()

        for (question in assessment.questions) {
            val expectedFilename = "${question.assessmentQuestionId}_question.wav"
            if (availableAudioFilenames.contains(expectedFilename)) {
                verified.add(expectedFilename)
            } else {
                missing.add("Assessment Question ${question.assessmentQuestionId}: $expectedFilename")
            }
        }

        return ValidationResult(
            isValid = missing.isEmpty(),
            verifiedReferences = verified,
            missingReferences = missing
        )
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
