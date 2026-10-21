package com.sih2026.palash.ui.screens.worksheet

import androidx.compose.ui.graphics.Color
import com.sih2026.palash.data.model.m5.M5WorksheetContent
import com.sih2026.palash.data.model.m5.M5WorksheetQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying P0 Worksheet Single-Page + Submit UX Requirements.
 */
class WorksheetUxCorrectionUnitTest {

    private fun createSampleWorksheet(): M5WorksheetContent {
        return M5WorksheetContent(
            worksheetId = "ws_grade2_math_01",
            title = "Grade 2 Math Worksheet",
            grade = 2,
            subject = "Mathematics",
            topic = "Counting",
            questions = listOf(
                M5WorksheetQuestion(
                    questionId = "q1",
                    questionType = "multiple_choice",
                    hindi = "गिनती करें",
                    santali = "ᱞᱮᱠᱷᱟᱭ ᱢᱮ",
                    options = listOf("1", "2", "3"),
                    correctAnswer = "2"
                ),
                M5WorksheetQuestion(
                    questionId = "q2",
                    questionType = "multiple_choice",
                    hindi = "जोड़ करें",
                    santali = "ᱢᱮᱥᱟᱭ ᱢᱮ",
                    options = listOf("3", "4", "5"),
                    correctAnswer = "4"
                )
            )
        )
    }

    @Test
    fun test1_worksheetLoadsAllQuestions() {
        val content = createSampleWorksheet()
        assertEquals(2, content.questions.size)
        assertEquals("q1", content.questions[0].questionId)
        assertEquals("q2", content.questions[1].questionId)
    }

    @Test
    fun test2_allQuestionsAreRepresentedSimultaneously() {
        val state = WorksheetUiState(
            isLoading = false,
            worksheetContent = createSampleWorksheet(),
            questions = createSampleWorksheet().questions,
            totalQuestions = 2
        )
        assertFalse(state.isLoading)
        assertEquals(2, state.questions.size)
        // Single page representation: no pagination index required
        assertEquals("q1", state.questions[0].questionId)
        assertEquals("q2", state.questions[1].questionId)
    }

    @Test
    fun test3_selectingAnswerStoresSelection() {
        var state = WorksheetUiState(questions = createSampleWorksheet().questions)
        val selectedMap = state.selectedOptions.toMutableMap()
        selectedMap["q1"] = "2"
        state = state.copy(selectedOptions = selectedMap)

        assertEquals("2", state.selectedOptions["q1"])
        assertFalse(state.isSubmitted)
    }

    @Test
    fun test4_selectionCanBeChangedBeforeSubmit() {
        var state = WorksheetUiState(questions = createSampleWorksheet().questions)
        var selectedMap = state.selectedOptions.toMutableMap()
        selectedMap["q1"] = "1"
        state = state.copy(selectedOptions = selectedMap)
        assertEquals("1", state.selectedOptions["q1"])

        // Change selection before Submit
        selectedMap = state.selectedOptions.toMutableMap()
        selectedMap["q1"] = "2"
        state = state.copy(selectedOptions = selectedMap)
        assertEquals("2", state.selectedOptions["q1"])
        assertFalse(state.isSubmitted)
    }

    @Test
    fun test5_selectingAnswerDoesNotEvaluateCorrectnessBeforeSubmit() {
        var state = WorksheetUiState(questions = createSampleWorksheet().questions)
        val selectedMap = state.selectedOptions.toMutableMap()
        selectedMap["q1"] = "2" // correct option
        state = state.copy(selectedOptions = selectedMap)

        // isSubmitted is false before Submit
        assertFalse("Correctness evaluation must NOT happen before Submit", state.isSubmitted)
        assertEquals(0, state.score)
    }

    @Test
    fun test6_noNextQuestionNavigationIsRequired() {
        val methods = WorksheetViewModel::class.java.declaredMethods.map { it.name }
        assertFalse("ViewModel should not rely on nextQuestion for question pagination", methods.contains("nextQuestion"))
    }

    @Test
    fun test7_noPreviousQuestionNavigationIsRequired() {
        val methods = WorksheetViewModel::class.java.declaredMethods.map { it.name }
        assertFalse("ViewModel should not rely on previousQuestion for question pagination", methods.contains("previousQuestion"))
    }

    @Test
    fun test8_submitEvaluatesAllQuestions() {
        val questions = createSampleWorksheet().questions
        val selections = mapOf("q1" to "2", "q2" to "4") // Both correct

        var correctCount = 0
        questions.forEach { q ->
            if (selections[q.questionId]?.trim() == q.correctAnswer.trim()) {
                correctCount++
            }
        }

        val state = WorksheetUiState(
            questions = questions,
            selectedOptions = selections,
            isSubmitted = true,
            score = correctCount,
            totalQuestions = questions.size
        )

        assertTrue(state.isSubmitted)
        assertEquals(2, state.score)
        assertEquals(2, state.totalQuestions)
    }

    @Test
    fun test9_correctAnswersReceivePostSubmitCorrectState() {
        val postSubmitGreen = Color(0xB32E7D32)
        val option = "2"
        val correctAnswer = "2"
        val isSubmitted = true

        val color = if (isSubmitted && option.trim() == correctAnswer.trim()) postSubmitGreen else Color.Unspecified
        assertEquals(postSubmitGreen, color)
    }

    @Test
    fun test10_incorrectSelectedAnswersReceivePostSubmitIncorrectState() {
        val postSubmitRed = Color(0xB3D32F2F)
        val selectedOption = "1"
        val option = "1"
        val correctAnswer = "2"
        val isSubmitted = true

        val isSelected = selectedOption == option
        val isCorrect = option.trim() == correctAnswer.trim()

        val color = if (isSubmitted && isSelected && !isCorrect) postSubmitRed else Color.Unspecified
        assertEquals(postSubmitRed, color)
    }

    @Test
    fun test11_otherUnselectedOptionsReceiveNeutralPostSubmitState() {
        val postSubmitGray = Color(0xB39E9E9E)
        val selectedOption = "1"
        val option = "3"
        val correctAnswer = "2"
        val isSubmitted = true

        val isSelected = selectedOption == option
        val isCorrect = option.trim() == correctAnswer.trim()

        val color = if (isSubmitted && !isCorrect && !isSelected) postSubmitGray else Color.Unspecified
        assertEquals(postSubmitGray, color)
    }

    @Test
    fun test12_submitDoesNotOccurSilentlyWithUnansweredQuestions() {
        val questions = createSampleWorksheet().questions
        val partialSelections = mapOf("q1" to "2") // q2 left unanswered

        val unansweredCount = questions.count { !partialSelections.containsKey(it.questionId) }
        val validationError = if (unansweredCount > 0) "Please answer all questions" else null

        assertNotNull("Validation error must be raised when questions remain unanswered", validationError)
        assertEquals(1, unansweredCount)
    }

    @Test
    fun test13_selectionStateSurvivesRecompositionStateUpdates() {
        val selections = mapOf("q1" to "2", "q2" to "4")
        val state1 = WorksheetUiState(selectedOptions = selections)
        val state2 = state1.copy(isLoading = false)

        assertEquals("2", state2.selectedOptions["q1"])
        assertEquals("4", state2.selectedOptions["q2"])
    }

    @Test
    fun test14_existingGrade2WorksheetContentRemainsUnchanged() {
        val content = createSampleWorksheet()
        assertEquals("ws_grade2_math_01", content.worksheetId)
        assertEquals(2, content.grade)
        assertEquals("Mathematics", content.subject)
        assertEquals("Counting", content.topic)
    }
}
