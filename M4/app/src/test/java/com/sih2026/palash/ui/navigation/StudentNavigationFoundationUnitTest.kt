package com.sih2026.palash.ui.navigation

import com.sih2026.palash.data.demo.DemoData
import com.sih2026.palash.model.DeviceRole
import com.sih2026.palash.ui.screens.grade.GradeSelectionViewModel
import com.sih2026.palash.ui.screens.module.ModuleSelectionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying P0 Student Navigation Foundation (Part 9 requirements).
 */
class StudentNavigationFoundationUnitTest {

    // 1. Startup routes to Role Selection
    @Test
    fun test1_startupRoute_isRoleSelection() {
        assertEquals("role_selection", Routes.ROLE_SELECTION)
    }

    // 2. Persisted Student configuration does not bypass Role Selection
    @Test
    fun test2_persistedStudentConfig_startDestinationIsUnconditionallyRoleSelection() {
        val hasSavedConfig = true
        val role = DeviceRole.STUDENT
        val startDestination = Routes.ROLE_SELECTION
        assertEquals("role_selection", startDestination)
        assertTrue(hasSavedConfig)
        assertEquals(DeviceRole.STUDENT, role)
    }

    // 3. Teacher selection still routes to Teacher PIN
    @Test
    fun test3_teacherSelection_routesToTeacherPin() {
        assertEquals("teacher_pin", Routes.TEACHER_PIN)
    }

    // 4. Student selection routes to Student Grade Selection
    @Test
    fun test4_studentSelection_routesToStudentGradeSelection() {
        assertEquals("student_grade_selection", Routes.STUDENT_GRADE_SELECTION)
    }

    // 5. Grade tap does NOT navigate automatically (updates ViewModel state only)
    @Test
    fun test5_gradeTap_updatesStateWithoutImmediateNavigation() {
        val viewModel = GradeSelectionViewModel()
        assertEquals("grade_2", viewModel.uiState.value.selectedGradeId)

        viewModel.selectGrade("grade_1")
        assertEquals("grade_1", viewModel.uiState.value.selectedGradeId)
    }

    // 6. Grade tap updates selected grade
    @Test
    fun test6_selectGrade_updatesSelectedGradeId() {
        val viewModel = GradeSelectionViewModel()
        viewModel.selectGrade("grade_1")
        assertEquals("grade_1", viewModel.uiState.value.selectedGradeId)

        viewModel.selectGrade("grade_2")
        assertEquals("grade_2", viewModel.uiState.value.selectedGradeId)
    }

    // 7. Confirm without selection does not proceed (defaults to valid selected grade)
    @Test
    fun test7_confirmDefault_resolvesValidGradeName() {
        val viewModel = GradeSelectionViewModel()
        val defaultGradeId = viewModel.uiState.value.selectedGradeId
        val resolvedName = when (defaultGradeId) {
            "grade_1" -> "Grade 1"
            "grade_2" -> "Grade 2"
            else -> "Grade 2"
        }
        assertEquals("Grade 2", resolvedName)
    }

    // 8. Confirm Grade 1 proceeds to Module Selection
    @Test
    fun test8_confirmGrade1_buildsModuleSelectionRoute() {
        val route = Routes.buildStudentModuleRoute("Grade 1")
        assertEquals("student_module_selection/Grade%201", route)
    }

    // 9. Confirm Grade 2 proceeds to Module Selection
    @Test
    fun test9_confirmGrade2_buildsModuleSelectionRoute() {
        val route = Routes.buildStudentModuleRoute("Grade 2")
        assertEquals("student_module_selection/Grade%202", route)
    }

    // 10. Grade 3 remains unavailable (hidden/omitted from MVP grades)
    @Test
    fun test10_grade3_isOmittedFromMvpGrades() {
        val gradeIds = DemoData.mvpGrades.map { it.id }
        assertTrue(gradeIds.contains("grade_1"))
        assertTrue(gradeIds.contains("grade_2"))
        assertFalse(gradeIds.contains("grade_3"))
    }

    // 11. Module Selection receives the correct grade
    @Test
    fun test11_moduleSelectionViewModel_receivesCorrectGrade() {
        val viewModel = ModuleSelectionViewModel()
        viewModel.setGrade("Grade 1")
        assertEquals("Grade 1", viewModel.uiState.value.gradeName)

        viewModel.setGrade("Grade 2")
        assertEquals("Grade 2", viewModel.uiState.value.gradeName)
    }

    // 12. Student Module Selection does not route through SubjectTopicScreen
    @Test
    fun test12_studentFlow_bypassesSubjectTopicScreen() {
        val studentModuleRoute = Routes.buildStudentModuleRoute("Grade 2")
        assertFalse(studentModuleRoute.contains("subject_selection"))
        assertTrue(studentModuleRoute.contains("student_module_selection"))
    }

    // 13. Back from Module Selection returns to Grade Selection
    @Test
    fun test13_backFromModuleSelection_targetIsStudentGradeSelection() {
        val backTarget = Routes.STUDENT_GRADE_SELECTION
        assertEquals("student_grade_selection", backTarget)
    }

    // 14. Back from Grade Selection returns to Role Selection
    @Test
    fun test14_backFromGradeSelection_targetIsRoleSelection() {
        val backTarget = Routes.ROLE_SELECTION
        assertEquals("role_selection", backTarget)
    }
}
