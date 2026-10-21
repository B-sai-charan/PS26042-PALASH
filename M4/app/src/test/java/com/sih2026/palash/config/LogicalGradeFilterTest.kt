package com.sih2026.palash.config

import com.sih2026.palash.data.config.LogicalGradeFilter
import com.sih2026.palash.data.model.m5.M5AssessmentContent
import com.sih2026.palash.data.model.m5.M5AssessmentQuestion
import com.sih2026.palash.data.model.m5.M5Flashcard
import com.sih2026.palash.data.model.m5.M5FlashcardContent
import com.sih2026.palash.data.model.m5.M5LessonContent
import com.sih2026.palash.data.model.m5.M5Section
import com.sih2026.palash.data.model.m5.M5WorksheetContent
import com.sih2026.palash.data.model.m5.M5WorksheetQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying logical Grade 1 content mapping rules and regression protection for Grade 2/3.
 */
class LogicalGradeFilterTest {

    private fun createSampleLesson(): M5LessonContent {
        val sections = listOf(
            M5Section(sectionId = "SEC01", type = "section", heading = "Section 1"),
            M5Section(sectionId = "SEC02", type = "section", heading = "Section 2"),
            M5Section(sectionId = "SEC03", type = "section", heading = "Section 3"),
            M5Section(sectionId = "SEC04", type = "section", heading = "Section 4"),
            M5Section(sectionId = "SEC05", type = "section", heading = "Section 5"),
            M5Section(sectionId = "SEC06", type = "section", heading = "Section 6"),
            M5Section(sectionId = "SEC07", type = "section", heading = "Section 7")
        )
        return M5LessonContent(
            lessonId = "L2_MTH_C01",
            title = "Grade 2 Math",
            grade = 2,
            subject = "Mathematics",
            topic = "Counting",
            sections = sections
        )
    }

    private fun createSampleFlashcards(): M5FlashcardContent {
        val cards = listOf(
            M5Flashcard(cardId = "FC001", type = "number", hindi = "एक", santali = "ᱢᱤᱫ"),
            M5Flashcard(cardId = "FC002", type = "number", hindi = "दो", santali = "ᱵᱟᱨ"),
            M5Flashcard(cardId = "FC003", type = "number", hindi = "तीन", santali = "ᱯᱮ"),
            M5Flashcard(cardId = "FC004", type = "number", hindi = "चार", santali = "ᱯᱳᱱ"),
            M5Flashcard(cardId = "FC005", type = "number", hindi = "पाँच", santali = "ᱢᱚᱬᱮ"),
            M5Flashcard(cardId = "FC006", type = "number", hindi = "छह", santali = "ᱛᱩᱨᱩᱭ"),
            M5Flashcard(cardId = "FC011", type = "operation", hindi = "जोड़", santali = "ᱡᱳᱲ"),
            M5Flashcard(cardId = "FC012", type = "operation", hindi = "जोड़", santali = "ᱡᱳᱲ"),
            M5Flashcard(cardId = "FC013", type = "operation", hindi = "घटाव", santali = "ᱜᱷᱟᱴᱟᱣ"),
            M5Flashcard(cardId = "FC014", type = "operation", hindi = "घटाव", santali = "ᱜᱷᱟᱴᱟᱣ")
        )
        return M5FlashcardContent(
            flashcardSetId = "FC_SET_1",
            title = "Grade 2 Flashcards",
            grade = 2,
            subject = "Mathematics",
            topic = "Counting",
            cards = cards
        )
    }

    private fun createSampleWorksheet(): M5WorksheetContent {
        val items = listOf(
            M5WorksheetQuestion(questionId = "WSQ001", questionType = "mcq", correctAnswer = "1"),
            M5WorksheetQuestion(questionId = "WSQ002", questionType = "mcq", correctAnswer = "2"),
            M5WorksheetQuestion(questionId = "WSQ003", questionType = "mcq", correctAnswer = "1"),
            M5WorksheetQuestion(questionId = "WSQ004", questionType = "mcq", correctAnswer = "2"),
            M5WorksheetQuestion(questionId = "WSQ005", questionType = "mcq", correctAnswer = "1"),
            M5WorksheetQuestion(questionId = "WSQ006", questionType = "mcq", correctAnswer = "2")
        )
        return M5WorksheetContent(
            worksheetId = "WS_1",
            title = "Grade 2 Worksheet",
            grade = 2,
            subject = "Mathematics",
            topic = "Counting",
            questions = items
        )
    }

    private fun createSampleAssessment(): M5AssessmentContent {
        val items = listOf(
            M5AssessmentQuestion(assessmentQuestionId = "AS001", questionType = "mcq", correctAnswer = "1"),
            M5AssessmentQuestion(assessmentQuestionId = "AS002", questionType = "mcq", correctAnswer = "2"),
            M5AssessmentQuestion(assessmentQuestionId = "AS003", questionType = "mcq", correctAnswer = "1"),
            M5AssessmentQuestion(assessmentQuestionId = "AS004", questionType = "mcq", correctAnswer = "2"),
            M5AssessmentQuestion(assessmentQuestionId = "AS005", questionType = "mcq", correctAnswer = "1"),
            M5AssessmentQuestion(assessmentQuestionId = "AS006", questionType = "mcq", correctAnswer = "2")
        )
        return M5AssessmentContent(
            assessmentId = "AS_1",
            title = "Grade 2 Assessment",
            grade = 2,
            subject = "Mathematics",
            topic = "Counting",
            questions = items
        )
    }

    @Test
    fun testGrade1ReturnsExactly6Lessons() {
        val filtered = LogicalGradeFilter.filterLessonForGrade(createSampleLesson(), "Grade 1")
        assertEquals(6, filtered.sections.size)
        val ids = filtered.sections.map { it.sectionId }
        assertEquals(listOf("SEC01", "SEC02", "SEC03", "SEC04", "SEC05", "SEC06"), ids)
    }

    @Test
    fun testGrade1ReturnsExactly3Flashcards() {
        val filtered = LogicalGradeFilter.filterFlashcardsForGrade(createSampleFlashcards(), "Grade 1")
        assertEquals(3, filtered.cards.size)
    }

    @Test
    fun testGrade1FlashcardsContainFC001_FC003_FC006() {
        val filtered = LogicalGradeFilter.filterFlashcardsForGrade(createSampleFlashcards(), "Grade 1")
        val ids = filtered.cards.map { it.cardId }
        assertTrue(ids.contains("FC001"))
        assertTrue(ids.contains("FC003"))
        assertTrue(ids.contains("FC006"))
        assertEquals(listOf("FC001", "FC003", "FC006"), ids)
    }

    @Test
    fun testGrade1FlashcardsDoNotContainFC011ToFC014() {
        val filtered = LogicalGradeFilter.filterFlashcardsForGrade(createSampleFlashcards(), "Grade 1")
        val ids = filtered.cards.map { it.cardId }
        assertFalse(ids.contains("FC011"))
        assertFalse(ids.contains("FC012"))
        assertFalse(ids.contains("FC013"))
        assertFalse(ids.contains("FC014"))
    }

    @Test
    fun testGrade1ReturnsExactly5Worksheets() {
        val filtered = LogicalGradeFilter.filterWorksheetForGrade(createSampleWorksheet(), "Grade 1")
        assertEquals(5, filtered.questions.size)
        val ids = filtered.questions.map { it.questionId }
        assertEquals(listOf("WSQ001", "WSQ002", "WSQ003", "WSQ004", "WSQ005"), ids)
    }

    @Test
    fun testGrade1ReturnsExactly5Assessments() {
        val filtered = LogicalGradeFilter.filterAssessmentForGrade(createSampleAssessment(), "Grade 1")
        assertEquals(5, filtered.questions.size)
        val ids = filtered.questions.map { it.assessmentQuestionId }
        assertEquals(listOf("AS001", "AS002", "AS003", "AS004", "AS005"), ids)
    }

    @Test
    fun testGrade2ReturnsAllExistingFlashcards() {
        val original = createSampleFlashcards()
        val filtered = LogicalGradeFilter.filterFlashcardsForGrade(original, "Grade 2")
        assertEquals(original.cards.size, filtered.cards.size)
    }

    @Test
    fun testGrade2StillReturnsFC011ToFC014() {
        val filtered = LogicalGradeFilter.filterFlashcardsForGrade(createSampleFlashcards(), "Grade 2")
        val ids = filtered.cards.map { it.cardId }
        assertTrue(ids.contains("FC011"))
        assertTrue(ids.contains("FC012"))
        assertTrue(ids.contains("FC013"))
        assertTrue(ids.contains("FC014"))
    }

    @Test
    fun testGrade3RemainsUnavailable() {
        assertTrue(LogicalGradeFilter.isGrade1("Grade 1"))
        assertFalse(LogicalGradeFilter.isGrade1("Grade 2"))
        assertFalse(LogicalGradeFilter.isGrade1("Grade 3"))
    }
}
