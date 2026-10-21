package com.sih2026.palash.data.model.m5

import com.google.gson.annotations.SerializedName

/**
 * M5 Content Data Models representing Grade 2 vernacular learning assets.
 * Matches M5 JSON schemas without altering field names or inventing data.
 */

data class M5NumberContent(
    @SerializedName("number") val number: Int,
    @SerializedName("hindi") val hindi: String,
    @SerializedName("santali") val santali: String,
    @SerializedName("visual") val visual: String? = null,
    @SerializedName("validation_status") val validationStatus: String? = null
)

data class M5Section(
    @SerializedName("section_id") val sectionId: String,
    @SerializedName("type") val type: String,
    @SerializedName("heading") val heading: String? = null,
    @SerializedName("hindi") val hindi: String? = null,
    @SerializedName("santali") val santali: String? = null,
    @SerializedName("validation_status") val validationStatus: String? = null,
    @SerializedName("audio_file") val audioFile: String? = null,
    @SerializedName("numbers") val numbers: List<M5NumberContent>? = null,
    @SerializedName("visual") val visual: String? = null
)

data class M5LessonContent(
    @SerializedName("lesson_id") val lessonId: String,
    @SerializedName("title") val title: String,
    @SerializedName("grade") val grade: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("source_language") val sourceLanguage: String? = null,
    @SerializedName("target_language") val targetLanguage: String? = null,
    @SerializedName("learning_objective") val learningObjective: String? = null,
    @SerializedName("prerequisite") val prerequisite: String? = null,
    @SerializedName("fln_alignment_basis") val flnAlignmentBasis: String? = null,
    @SerializedName("translation_source") val translationSource: String? = null,
    @SerializedName("validation_status") val validationStatus: String? = null,
    @SerializedName("sections") val sections: List<M5Section> = emptyList(),
    @SerializedName("expected_learning_outcome") val expectedLearningOutcome: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class M5WorksheetQuestion(
    @SerializedName("question_id") val questionId: String,
    @SerializedName("content_id") val contentId: String? = null,
    @SerializedName("question_type") val questionType: String,
    @SerializedName("hindi") val hindi: String? = null,
    @SerializedName("santali") val santali: String? = null,
    @SerializedName("options") val options: List<String> = emptyList(),
    @SerializedName("numbers") val numbers: List<String>? = null,
    @SerializedName("answer_format") val answerFormat: String? = null,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("validation_status") val validationStatus: String? = null
)

data class M5WorksheetContent(
    @SerializedName("worksheet_id") val worksheetId: String,
    @SerializedName("title") val title: String,
    @SerializedName("grade") val grade: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("source_language") val sourceLanguage: String? = null,
    @SerializedName("target_language") val targetLanguage: String? = null,
    @SerializedName("validation_status") val validationStatus: String? = null,
    @SerializedName("questions") val questions: List<M5WorksheetQuestion> = emptyList()
)

data class M5Flashcard(
    @SerializedName("card_id") val cardId: String,
    @SerializedName("type") val type: String,
    @SerializedName("number") val number: Any? = null,
    @SerializedName("hindi") val hindi: String,
    @SerializedName("santali") val santali: String,
    @SerializedName("visual") val visual: String? = null,
    @SerializedName("visual_layout") val visualLayout: String? = null,
    @SerializedName("visual_rows") val visualRows: List<Int>? = null,
    @SerializedName("operator") val operator: String? = null,
    @SerializedName("operand1") val operand1: Int? = null,
    @SerializedName("operand2") val operand2: Int? = null,
    @SerializedName("result") val result: Int? = null,
    @SerializedName("validation_status") val validationStatus: String? = null,
    @SerializedName("notes") val notes: String? = null
) {
    /** Helper property to get string representation of 'number' (works for Int 1 or String "1 + 2 = 3") */
    val numberDisplay: String
        get() = when (number) {
            is Double -> if (number % 1.0 == 0.0) number.toLong().toString() else number.toString()
            is Number -> number.toString()
            else -> number?.toString() ?: ""
        }
}

data class M5FlashcardContent(
    @SerializedName("flashcard_set_id") val flashcardSetId: String,
    @SerializedName("title") val title: String,
    @SerializedName("grade") val grade: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("source_language") val sourceLanguage: String? = null,
    @SerializedName("target_language") val targetLanguage: String? = null,
    @SerializedName("validation_status") val validationStatus: String? = null,
    @SerializedName("cards") val cards: List<M5Flashcard> = emptyList()
)

data class M5AssessmentQuestion(
    @SerializedName("assessment_question_id") val assessmentQuestionId: String,
    @SerializedName("content_id") val contentId: String? = null,
    @SerializedName("grade") val grade: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("topic") val topic: String? = null,
    @SerializedName("question_type") val questionType: String,
    @SerializedName("hindi") val hindi: String? = null,
    @SerializedName("santali") val santali: String? = null,
    @SerializedName("options") val options: List<String> = emptyList(),
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("validation_status") val validationStatus: String? = null
)

data class M5AssessmentContent(
    @SerializedName("assessment_id") val assessmentId: String,
    @SerializedName("title") val title: String,
    @SerializedName("grade") val grade: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("source_language") val sourceLanguage: String? = null,
    @SerializedName("target_language") val targetLanguage: String? = null,
    @SerializedName("validation_status") val validationStatus: String? = null,
    @SerializedName("questions") val questions: List<M5AssessmentQuestion> = emptyList()
)
