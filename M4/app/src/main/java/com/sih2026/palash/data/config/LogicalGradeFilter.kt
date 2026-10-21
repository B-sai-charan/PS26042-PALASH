package com.sih2026.palash.data.config

import com.sih2026.palash.data.model.m5.M5AssessmentContent
import com.sih2026.palash.data.model.m5.M5FlashcardContent
import com.sih2026.palash.data.model.m5.M5LessonContent
import com.sih2026.palash.data.model.m5.M5WorksheetContent

/**
 * Centralized Single Source of Truth for Logical Grade Content Filtering.
 * Filters existing M5 assets (stored under content/grade_2/) to safely expose Grade 1 content
 * without renaming, moving, duplicating, or modifying M5 JSON source files.
 */
object LogicalGradeFilter {

    val GRADE_1_LESSON_SECTIONS = setOf("SEC01", "SEC02", "SEC03", "SEC04", "SEC05", "SEC06")
    val GRADE_1_FLASHCARD_IDS = setOf("FC001", "FC003", "FC006")
    val GRADE_1_WORKSHEET_QUESTION_IDS = setOf("WSQ001", "WSQ002", "WSQ003", "WSQ004", "WSQ005")
    val GRADE_1_ASSESSMENT_QUESTION_IDS = setOf("AS001", "AS002", "AS003", "AS004", "AS005")

    fun isGrade1(gradeName: String?): Boolean {
        if (gradeName.isNullOrBlank()) return false
        return gradeName.equals("Grade 1", ignoreCase = true) ||
                gradeName.equals("grade_1", ignoreCase = true) ||
                gradeName == "1"
    }

    fun filterFlashcardsForGrade(content: M5FlashcardContent, gradeName: String?): M5FlashcardContent {
        if (!isGrade1(gradeName)) return content
        val filteredCards = content.cards.filter { it.cardId in GRADE_1_FLASHCARD_IDS }
        return content.copy(
            grade = 1,
            title = "PALASH Grade 1 Mathematics - Counting",
            cards = filteredCards
        )
    }

    fun filterLessonForGrade(content: M5LessonContent, gradeName: String?): M5LessonContent {
        if (!isGrade1(gradeName)) return content
        val filteredSections = content.sections.filter { it.sectionId in GRADE_1_LESSON_SECTIONS }
        return content.copy(
            grade = 1,
            title = "PALASH Grade 1 Mathematics - Counting and Numbers",
            sections = filteredSections
        )
    }

    fun filterWorksheetForGrade(content: M5WorksheetContent, gradeName: String?): M5WorksheetContent {
        if (!isGrade1(gradeName)) return content
        val filteredQuestions = content.questions.filter { it.questionId in GRADE_1_WORKSHEET_QUESTION_IDS }
        return content.copy(
            grade = 1,
            title = "PALASH - Grade 1 Mathematics: Counting and Numbers",
            questions = filteredQuestions
        )
    }

    fun filterAssessmentForGrade(content: M5AssessmentContent, gradeName: String?): M5AssessmentContent {
        if (!isGrade1(gradeName)) return content
        val filteredQuestions = content.questions.filter { it.assessmentQuestionId in GRADE_1_ASSESSMENT_QUESTION_IDS }
        return content.copy(
            grade = 1,
            title = "PALASH Grade 1 Mathematics Assessment - Counting and Numbers",
            questions = filteredQuestions
        )
    }
}
