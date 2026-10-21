package com.sih2026.palash.classroom

/**
 * Pure, testable utility for enforcing section identity matching rules.
 * Determines whether a student device assigned to a specific grade and section
 * should accept a classroom message broadcast from a teacher session.
 */
object SectionMatcher {

    /**
     * Verifies if a student device (studentGrade, studentSection) matches
     * a broadcast message target (messageGrade, messageSection).
     *
     * Rules:
     * 1. Primary match: studentGrade MUST equal messageGrade.
     * 2. Optional secondary match: if BOTH sides specify non-blank sections,
     *    studentSection MUST equal messageSection (case-insensitive).
     * 3. If either side has blank or null section, Grade match is sufficient for MVP acceptance.
     */
    fun shouldAcceptClassroomMessage(
        studentGrade: Int,
        studentSection: String?,
        messageGrade: Int,
        messageSection: String?
    ): Boolean {
        if (studentGrade != messageGrade) {
            return false
        }

        val cleanStudentSection = studentSection?.trim() ?: ""
        val cleanMessageSection = messageSection?.trim() ?: ""

        if (cleanStudentSection.isNotEmpty() && cleanMessageSection.isNotEmpty()) {
            return cleanStudentSection.equals(cleanMessageSection, ignoreCase = true)
        }

        return true
    }

    /**
     * Authoritative single location for evaluating incoming classroom messages.
     * Checks validity, Grade (primary), Section (optional secondary), and deduplication against lastMessageId.
     */
    fun evaluateMessage(
        studentGrade: Int,
        studentSection: String?,
        lastMessageId: String?,
        message: ClassroomTranslationMessage?
    ): ClassroomMessageResult {
        if (message == null) {
            return ClassroomMessageResult.NO_MESSAGE
        }

        if (!ClassroomMessageValidator.isValid(message)) {
            return ClassroomMessageResult.INVALID_MESSAGE
        }

        if (studentGrade != message.grade) {
            return ClassroomMessageResult.WRONG_GRADE
        }

        val cleanStudentSec = studentSection?.trim() ?: ""
        val cleanMsgSec = message.section?.trim() ?: ""
        if (cleanStudentSec.isNotEmpty() && cleanMsgSec.isNotEmpty() && !cleanStudentSec.equals(cleanMsgSec, ignoreCase = true)) {
            return ClassroomMessageResult.WRONG_SECTION
        }

        if (lastMessageId != null && lastMessageId == message.messageId) {
            return ClassroomMessageResult.DUPLICATE
        }

        return ClassroomMessageResult.ACCEPTED
    }
}
