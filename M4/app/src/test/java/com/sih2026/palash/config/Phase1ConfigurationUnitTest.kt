package com.sih2026.palash.config

import com.sih2026.palash.classroom.SectionMatcher
import com.sih2026.palash.model.DeviceRole
import com.sih2026.palash.model.StudentConfig
import com.sih2026.palash.model.TeacherSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Phase 1 User Role, Student Section Configuration,
 * Teacher Session state, and Section Matching Logic.
 */
class Phase1ConfigurationUnitTest {

    @Test
    fun testDeviceRoleEnumValues() {
        assertEquals("TEACHER", DeviceRole.TEACHER.name)
        assertEquals("STUDENT", DeviceRole.STUDENT.name)
        assertEquals(2, DeviceRole.values().size)
    }

    @Test
    fun testTeacherSessionDefaultValues() {
        val session = TeacherSession()
        assertEquals(2, session.grade)
        assertEquals("Mathematics", session.subject)
        assertEquals("A", session.section)
    }

    @Test
    fun testStudentConfigDefaultValues() {
        val config = StudentConfig(grade = 2, section = "B")
        assertEquals(2, config.grade)
        assertEquals("B", config.section)
    }

    @Test
    fun testSectionMatching_grade2A_receives_grade2A_shouldPASS() {
        val result = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 2,
            messageSection = "A"
        )
        assertTrue("Grade 2 Section A must accept Grade 2 Section A message", result)
    }

    @Test
    fun testSectionMatching_grade2A_receives_grade2B_shouldREJECT() {
        val result = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 2,
            messageSection = "B"
        )
        assertFalse("Grade 2 Section A must reject Grade 2 Section B message", result)
    }

    @Test
    fun testSectionMatching_grade2B_receives_grade2A_shouldREJECT() {
        val result = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "B",
            messageGrade = 2,
            messageSection = "A"
        )
        assertFalse("Grade 2 Section B must reject Grade 2 Section A message", result)
    }

    @Test
    fun testSectionMatching_grade2A_receives_grade3A_shouldREJECT() {
        val result = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 3,
            messageSection = "A"
        )
        assertFalse("Grade 2 Section A must reject Grade 3 Section A message", result)
    }

    @Test
    fun testSectionMatching_caseInsensitivity_A_and_a_shouldACCEPT() {
        val resultUpperLower = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 2,
            messageSection = "a"
        )
        assertTrue("Section matching must be case-insensitive (A vs a)", resultUpperLower)

        val resultLowerUpper = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "a",
            messageGrade = 2,
            messageSection = "A"
        )
        assertTrue("Section matching must be case-insensitive (a vs A)", resultLowerUpper)
    }

    @Test
    fun testSectionMatching_blankSection_shouldACCEPTForMVPGradeOnly() {
        val studentBlank = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "",
            messageGrade = 2,
            messageSection = "A"
        )
        assertTrue("Blank student section accepts message when Grade matches for MVP", studentBlank)

        val messageBlank = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 2,
            messageSection = "   "
        )
        assertTrue("Blank message section accepts message when Grade matches for MVP", messageBlank)
    }
}
