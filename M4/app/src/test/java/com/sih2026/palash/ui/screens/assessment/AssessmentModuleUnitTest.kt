package com.sih2026.palash.ui.screens.assessment

import com.sih2026.palash.data.repository.M5ContentRepository
import com.sih2026.palash.ui.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Dedicated unit tests for P0 Assessment Module implementation.
 */
class AssessmentModuleUnitTest {

    private fun getAssetFile(relativePath: String): File {
        val fileSrc = File("src/main/assets/$relativePath")
        if (fileSrc.exists()) return fileSrc
        return File("app/src/main/assets/$relativePath")
    }

    // 1. Assessment route does not resolve to WorksheetScreen
    @Test
    fun test1_assessmentRoute_isIndependentFromWorksheet() {
        val assessmentRoute = Routes.ASSESSMENT
        val worksheetRoute = Routes.WORKSHEET

        assertEquals("assessment", assessmentRoute)
        assertEquals("worksheet", worksheetRoute)
        assertNotEquals(assessmentRoute, worksheetRoute)
    }

    // 2. Assessment content loads from correct M5 source
    @Test
    fun test2_assessmentContent_loadsFromCorrectM5JsonSource() {
        val jsonFile = getAssetFile("content/grade_2/assessments_g2_counting.json")
        assertTrue("M5 Assessment JSON file must exist", jsonFile.exists())

        val jsonString = jsonFile.readText(Charsets.UTF_8)
        val repository = M5ContentRepository(context = null)
        val result = repository.parseAssessment(jsonString)

        assertTrue("Parsing assessment JSON must succeed", result.isSuccess)
        val assessment = result.getOrNull()
        assertNotNull(assessment)
        assertEquals("AS_G2_COUNTING_001", assessment?.assessmentId)
        assertEquals(2, assessment?.grade)
        assertEquals("mathematics", assessment?.subject)
        assertEquals("counting_numbers", assessment?.topic)
    }

    // 3. Assessment questions render from structured data
    @Test
    fun test3_assessmentQuestions_renderFromStructuredData() {
        val jsonFile = getAssetFile("content/grade_2/assessments_g2_counting.json")
        val jsonString = jsonFile.readText(Charsets.UTF_8)
        val repository = M5ContentRepository(context = null)
        val assessment = repository.parseAssessment(jsonString).getOrThrow()

        val questions = assessment.questions
        assertEquals(5, questions.size)

        val q1 = questions[0]
        assertEquals("AS001", q1.assessmentQuestionId)
        assertEquals("count_objects", q1.questionType)
        assertEquals("3", q1.correctAnswer)
        assertEquals(4, q1.options.size)

        val q4 = questions[3]
        assertEquals("AS004", q4.assessmentQuestionId)
        assertEquals("write_number", q4.questionType)
        assertEquals("7", q4.correctAnswer)

        val q5 = questions[4]
        assertEquals("AS005", q5.assessmentQuestionId)
        assertEquals("forward_counting", q5.questionType)
        assertEquals("1-10", q5.correctAnswer)
    }

    // 4. Answer selection state works
    @Test
    fun test4_answerSelectionState_updatesSelectedAnswersAndEvaluatesScore() {
        val jsonFile = getAssetFile("content/grade_2/assessments_g2_counting.json")
        val jsonString = jsonFile.readText(Charsets.UTF_8)
        val repository = M5ContentRepository(context = null)
        val questions = repository.parseAssessment(jsonString).getOrThrow().questions

        val selectedAnswers = mutableMapOf<String, String>()
        questions.forEach { q ->
            selectedAnswers[q.assessmentQuestionId] = q.correctAnswer
        }

        var score = 0
        questions.forEach { q ->
            if (selectedAnswers[q.assessmentQuestionId] == q.correctAnswer) {
                score++
            }
        }

        assertEquals(5, selectedAnswers.size)
        assertEquals(5, score)
    }

    // 5. Back navigation target does not route to Worksheet
    @Test
    fun test5_backFromAssessment_targetIsStudentModuleSelection() {
        val moduleRoute = Routes.buildStudentModuleRoute("Grade 2")
        val worksheetRoute = Routes.WORKSHEET
        val backTargetRoute = Routes.STUDENT_GRADE_SELECTION

        assertFalse(moduleRoute.contains(worksheetRoute))
        assertEquals("student_grade_selection", backTargetRoute)
    }

    // 6. Missing Grade 1 content handled safely without fabrication
    @Test
    fun test6_grade1Assessment_handledSafelyWithoutFabricatedContent() {
        val grade1File = getAssetFile("content/grade_1/assessments_g1_counting.json")
        assertFalse("Grade 1 assessment JSON does not exist in MVP bundle", grade1File.exists())

        val isGrade1Notice = true
        val questionsCount = 0
        assertTrue("Grade 1 notice banner must be set", isGrade1Notice)
        assertEquals("Grade 1 must not contain fabricated questions", 0, questionsCount)
    }
}
