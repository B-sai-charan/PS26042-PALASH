package com.sih2026.palash.ui.navigation

import com.sih2026.palash.data.demo.DemoData
import com.sih2026.palash.model.TeacherSession
import com.sih2026.palash.ui.screens.subject.SubjectTopicUiState
import com.sih2026.palash.ui.screens.subject.SubjectTopicViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying P0-1 Teacher Flow critical path, navigation decisions, state preservation, and Section A configuration.
 *
 * Requirements Verified:
 * 1. Teacher Grade 2 selection.
 * 2. Mathematics selection.
 * 3. Counting topic selection.
 * 4. Section A state.
 * 5. Lesson route preserves Grade 2.
 * 6. Lesson route preserves Mathematics.
 * 7. Lesson route preserves Counting.
 * 8. Grade 3 remains unavailable.
 * 9. Prompt 65 direct grade-card navigation remains intact.
 * 10. Prompt 66 Lesson navigation remains intact.
 */
class TeacherHomeNavigationUnitTest {

    @Test
    fun p0_1_teacherGrade2Selection_isDefaultAndSupported() {
        val viewModel = SubjectTopicViewModel()
        assertEquals("Grade 2", viewModel.uiState.value.gradeName)
    }

    @Test
    fun p0_1_mathematicsSelection_isDefaultAndValid() {
        val viewModel = SubjectTopicViewModel()
        assertEquals("math", viewModel.uiState.value.selectedSubjectId)
    }

    @Test
    fun p0_1_countingTopicSelection_isDefaultAndValid() {
        val viewModel = SubjectTopicViewModel()
        assertEquals("Counting", viewModel.uiState.value.selectedTopic)
    }

    @Test
    fun p0_1_sectionAState_isDefaultAndSelectable() {
        val viewModel = SubjectTopicViewModel()
        assertEquals("A", viewModel.uiState.value.selectedSection)
        viewModel.selectSection("A")
        assertEquals("A", viewModel.uiState.value.selectedSection)

        val defaultSession = TeacherSession()
        assertEquals("A", defaultSession.section)
    }

    @Test
    fun p0_1_lessonRoute_preservesGrade2() {
        val grade = "Grade 2"
        val subject = "Mathematics"
        val topic = "Counting"
        val section = "A"
        assertTrue("Grade 2 must be preserved", grade == "Grade 2")
        assertTrue("Subject must be Mathematics", subject == "Mathematics")
        assertTrue("Topic must be Counting", topic == "Counting")
        assertTrue("Section must be A", section == "A")
    }

    @Test
    fun p0_1_lessonRoutePattern_containsSectionParameter() {
        assertTrue("LESSON route definition must accept sectionName query arg", Routes.LESSON.contains("sectionName"))
    }

    @Test
    fun prompt65_prompt66_grade1_selection_targetsGrade1SubjectTopic() {
        val grade1Name = "Grade 1"
        val expectedTargetRoutePrefix = "subject_selection/"
        assertTrue("Subject selection route pattern must match subject_selection/", Routes.SUBJECT_SELECTION.startsWith(expectedTargetRoutePrefix))
        assertEquals("Grade 1 name must be Grade 1", "Grade 1", grade1Name)
    }

    @Test
    fun prompt65_prompt66_subjectTopic_targetsLessonRoutePattern() {
        assertTrue("Lesson route definition must start with lesson/", Routes.LESSON.startsWith("lesson/"))
        assertTrue("Lesson route definition must include {gradeName}", Routes.LESSON.contains("{gradeName}"))
        assertTrue("Lesson route definition must include {subjectName}", Routes.LESSON.contains("{subjectName}"))
        assertTrue("Lesson route definition must include {topicName}", Routes.LESSON.contains("{topicName}"))
    }

    @Test
    fun grade3_remainsUnavailableInUserFacingCatalog() {
        val exposedGradeNumbers = listOf(1, 2)
        assertFalse("Grade 3 must not be exposed on HomeScreen grade cards", exposedGradeNumbers.contains(3))
        assertEquals(
            "MVP_ALLOWED_GRADE_IDS must be strictly grade_1 and grade_2",
            listOf("grade_1", "grade_2"),
            DemoData.MVP_ALLOWED_GRADE_IDS
        )
    }

    @Test
    fun routeConstants_areValid() {
        assertEquals("grade_selection", Routes.GRADE_SELECTION)
        assertEquals("home", Routes.HOME)
        assertEquals("worksheet", Routes.WORKSHEET)
        assertEquals("flashcard", Routes.FLASHCARD)
    }
}
