package com.sih2026.palash.ui.screens.grade

import com.sih2026.palash.data.demo.DemoData
import org.junit.Assert.*
import org.junit.Test

/**
 * Regression tests verifying the SIH MVP grade scope restriction.
 * Grade 1 and Grade 2 are the only user-selectable grades in the current MVP.
 * Grade 3 must NOT appear in user-facing selection lists.
 */
class MvpGradeScopeUnitTest {

    // ── DemoData scope ──────────────────────────────────────────────────────

    @Test
    fun mvpGrades_containsGrade1() {
        val ids = DemoData.mvpGrades.map { it.id }
        assertTrue("MVP grade list must include grade_1", "grade_1" in ids)
    }

    @Test
    fun mvpGrades_containsGrade2() {
        val ids = DemoData.mvpGrades.map { it.id }
        assertTrue("MVP grade list must include grade_2", "grade_2" in ids)
    }

    @Test
    fun mvpGrades_doesNotContainGrade3() {
        val ids = DemoData.mvpGrades.map { it.id }
        assertFalse("MVP grade list must NOT include grade_3", "grade_3" in ids)
    }

    @Test
    fun mvpAllowedGradeIds_isExactlyGrade1AndGrade2() {
        assertEquals(
            "MVP_ALLOWED_GRADE_IDS must be exactly [grade_1, grade_2]",
            listOf("grade_1", "grade_2"),
            DemoData.MVP_ALLOWED_GRADE_IDS
        )
    }

    @Test
    fun fullGradesCatalog_stillContainsGrade3() {
        val ids = DemoData.grades.map { it.id }
        assertTrue(
            "Full grades catalog must retain grade_3 for future integration",
            "grade_3" in ids
        )
    }

    @Test
    fun mvpGrades_hasExactly2Entries() {
        assertEquals("MVP should have exactly 2 grades", 2, DemoData.mvpGrades.size)
    }

    // ── ViewModel scope ─────────────────────────────────────────────────────

    @Test
    fun gradeSelectionViewModel_initialisesWithMvpGradesOnly() {
        val viewModel = GradeSelectionViewModel()
        val availableIds = viewModel.uiState.value.availableGrades.map { it.id }
        assertFalse("ViewModel availableGrades must NOT include grade_3", "grade_3" in availableIds)
        assertTrue("ViewModel must include grade_1", "grade_1" in availableIds)
        assertTrue("ViewModel must include grade_2", "grade_2" in availableIds)
    }

    @Test
    fun gradeSelectionViewModel_defaultSelectedGradeIsGrade2() {
        val viewModel = GradeSelectionViewModel()
        assertEquals("grade_2", viewModel.uiState.value.selectedGradeId)
    }

    @Test
    fun teacher_canSelectGrade1() {
        val viewModel = GradeSelectionViewModel()
        viewModel.selectGrade("grade_1")
        assertEquals("grade_1", viewModel.uiState.value.selectedGradeId)
    }

    @Test
    fun teacher_canSelectGrade2() {
        val viewModel = GradeSelectionViewModel()
        viewModel.selectGrade("grade_2")
        assertEquals("grade_2", viewModel.uiState.value.selectedGradeId)
    }

    @Test
    fun teacher_canSwitchGrade2ToGrade1AndBack() {
        val viewModel = GradeSelectionViewModel()
        assertEquals("grade_2", viewModel.uiState.value.selectedGradeId)
        viewModel.selectGrade("grade_1")
        assertEquals("grade_1", viewModel.uiState.value.selectedGradeId)
        viewModel.selectGrade("grade_2")
        assertEquals("grade_2", viewModel.uiState.value.selectedGradeId)
    }

    @Test
    fun availableGrades_doesNotChangeWhenGrade3IsAttempted() {
        val viewModel = GradeSelectionViewModel()
        viewModel.selectGrade("grade_3")
        val availableIds = viewModel.uiState.value.availableGrades.map { it.id }
        assertFalse(
            "Calling selectGrade(grade_3) must not add grade_3 to availableGrades",
            "grade_3" in availableIds
        )
    }
}
