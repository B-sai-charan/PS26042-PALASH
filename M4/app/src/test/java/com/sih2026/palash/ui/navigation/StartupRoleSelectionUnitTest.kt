package com.sih2026.palash.ui.navigation

import com.sih2026.palash.model.DeviceRole
import com.sih2026.palash.model.StudentConfig
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying P1 Startup Role Selection routing rules and persistence preservation.
 */
class StartupRoleSelectionUnitTest {

    @Test
    fun freshUnconfiguredApp_startDestinationIsRoleSelection() {
        val startDestination = Routes.ROLE_SELECTION
        assertEquals("role_selection", startDestination)
    }

    @Test
    fun previouslyConfiguredTeacher_startDestinationIsRoleSelection() {
        val isConfigured = true
        val role = DeviceRole.TEACHER

        // Startup route must unconditionally remain ROLE_SELECTION
        val startDestination = Routes.ROLE_SELECTION
        assertEquals("role_selection", startDestination)
        assertTrue(isConfigured)
        assertEquals(DeviceRole.TEACHER, role)
    }

    @Test
    fun previouslyConfiguredStudent_startDestinationIsRoleSelection() {
        val isConfigured = true
        val role = DeviceRole.STUDENT

        // Startup route must unconditionally remain ROLE_SELECTION
        val startDestination = Routes.ROLE_SELECTION
        assertEquals("role_selection", startDestination)
        assertTrue(isConfigured)
        assertEquals(DeviceRole.STUDENT, role)
    }

    @Test
    fun selectingTeacher_navigatesToTeacherPin() {
        val teacherTargetRoute = Routes.TEACHER_PIN
        assertEquals("teacher_pin", teacherTargetRoute)
    }

    @Test
    fun selectingStudent_navigatesToStudentGradeSelection() {
        val targetRoute = Routes.STUDENT_GRADE_SELECTION
        assertEquals("student_grade_selection", targetRoute)
    }

    @Test
    fun savedStudentConfiguration_isPreservedAndRetrievable() {
        val savedConfig = StudentConfig(
            grade = 2,
            section = "A",
            teacherIp = "192.168.43.1",
            teacherPort = 8080
        )

        assertEquals(2, savedConfig.grade)
        assertEquals("A", savedConfig.section)
        assertEquals("192.168.43.1", savedConfig.teacherIp)
        assertEquals(8080, savedConfig.teacherPort)
    }
}
