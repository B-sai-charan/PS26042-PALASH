package com.sih2026.palash.ui.navigation

import com.sih2026.palash.classroom.ClassroomTranslationMessage
import com.sih2026.palash.classroom.ClassroomMessageResult
import com.sih2026.palash.classroom.SectionMatcher
import com.sih2026.palash.model.StudentConfig
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying Student P0-1 self-paced navigation & classroom broadcast isolation.
 */
class StudentSelfPacedNavigationUnitTest {

    @Test
    fun lessonRouteConstant_definesExpectedStructure() {
        assertEquals("lesson/{gradeName}/{subjectName}/{topicName}?section={sectionName}", Routes.LESSON)
    }

    @Test
    fun studentHomeRouteConstant_isDefined() {
        assertEquals("student_home", Routes.STUDENT_HOME)
    }

    @Test
    fun studentConfig_grade2DefaultSection_isValid() {
        val config = StudentConfig(grade = 2, section = "A")
        assertEquals(2, config.grade)
        assertEquals("A", config.section)
    }

    @Test
    fun studentConfig_grade1_isValid() {
        val config = StudentConfig(grade = 1, section = "A")
        assertEquals(1, config.grade)
    }

    @Test
    fun classroomBroadcast_evaluationAcceptsMatchingGradeAndSection() {
        val msg = ClassroomTranslationMessage(
            messageId = "msg_001",
            grade = 2,
            section = "A",
            subject = "Mathematics",
            timestamp = System.currentTimeMillis(),
            santaliText = "᱑ ᱠᱷᱚᱱ ᱒᱐ ᱛᱟᱠ ᱮᱞ ᱥᱮᱫ ᱢᱮ"
        )
        val result = SectionMatcher.evaluateMessage(
            studentGrade = 2,
            studentSection = "A",
            lastMessageId = null,
            message = msg
        )
        assertEquals(ClassroomMessageResult.ACCEPTED, result)
    }

    @Test
    fun classroomBroadcast_ignoresWrongGrade() {
        val msg = ClassroomTranslationMessage(
            messageId = "msg_002",
            grade = 3,
            section = "A",
            subject = "Mathematics",
            timestamp = System.currentTimeMillis(),
            santaliText = "Wrong Grade"
        )
        val result = SectionMatcher.evaluateMessage(
            studentGrade = 2,
            studentSection = "A",
            lastMessageId = null,
            message = msg
        )
        assertEquals(ClassroomMessageResult.WRONG_GRADE, result)
    }
}
