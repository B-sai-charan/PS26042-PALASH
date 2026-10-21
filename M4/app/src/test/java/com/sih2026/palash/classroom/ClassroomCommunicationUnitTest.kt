package com.sih2026.palash.classroom

import com.google.gson.Gson
import com.sih2026.palash.model.StudentConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ClassroomCommunicationUnitTest {

    private val gson = Gson()
    private val client = StudentClassroomClient(gson)

    private fun createValidMessage(
        messageId: String = UUID.randomUUID().toString(),
        grade: Int = 2,
        section: String = "A",
        subject: String = "Mathematics",
        hindiText: String = "एक",
        santaliText: String = "ᱢᱤᱫ",
        audioId: String? = "num_001_1"
    ): ClassroomTranslationMessage {
        return ClassroomTranslationMessage(
            messageId = messageId,
            type = ClassroomMessageType.CLASSROOM_TRANSLATION,
            grade = grade,
            section = section,
            subject = subject,
            timestamp = System.currentTimeMillis(),
            hindiText = hindiText,
            santaliText = santaliText,
            audioId = audioId
        )
    }

    // 1. Grade 2 -> Grade 2 ACCEPT
    @Test
    fun test01_grade2ToGrade2_accepted() {
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "",
            messageGrade = 2,
            messageSection = ""
        )
        assertTrue("Grade 2 -> Grade 2 must be accepted", accepted)
    }

    // 2. Grade 2 -> Grade 3 REJECT
    @Test
    fun test02_grade2ToGrade3_rejected() {
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 3,
            messageSection = "A"
        )
        assertFalse("Grade 2 -> Grade 3 must be rejected", accepted)
    }

    // 3. Grade 2 Section A -> Grade 2 Section A ACCEPT
    @Test
    fun test03_grade2SectionAToGrade2SectionA_accepted() {
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 2,
            messageSection = "A"
        )
        assertTrue("Grade 2 Section A -> Grade 2 Section A must be accepted", accepted)
    }

    // 4. Grade 2 Section A -> Grade 2 Section B REJECT when both explicitly specify sections
    @Test
    fun test04_grade2SectionAToGrade2SectionB_rejected() {
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 2,
            messageSection = "B"
        )
        assertFalse("Grade 2 Section A -> Grade 2 Section B must be rejected", accepted)
    }

    // 5. Grade 2 with no section -> Grade 2 with no section ACCEPT
    @Test
    fun test05_grade2NoSectionToGrade2NoSection_accepted() {
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "",
            messageGrade = 2,
            messageSection = null
        )
        assertTrue("Grade 2 no section -> Grade 2 no section must be accepted", accepted)
    }

    // 6. Grade 2 with no section -> Grade 2 Section A ACCEPT for MVP Grade-only routing
    @Test
    fun test06_grade2NoSectionToGrade2SectionA_accepted() {
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "",
            messageGrade = 2,
            messageSection = "A"
        )
        assertTrue("Grade 2 no section -> Grade 2 Section A must be accepted for MVP Grade-only routing", accepted)
    }

    // 7. Grade 2 Section A -> Grade 2 with no section ACCEPT for MVP Grade-only routing
    @Test
    fun test07_grade2SectionAToGrade2NoSection_accepted() {
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = 2,
            messageSection = ""
        )
        assertTrue("Grade 2 Section A -> Grade 2 no section must be accepted for MVP Grade-only routing", accepted)
    }

    // 8. Duplicate Message Detection
    @Test
    fun test08_duplicateMessageId_detectedAsDuplicate() {
        val id = "MSG-1001"
        val msg1 = createValidMessage(messageId = id)
        val msg2 = createValidMessage(messageId = id)

        var lastMessageId: String? = null
        val process1 = if (msg1.messageId != lastMessageId) {
            lastMessageId = msg1.messageId
            true
        } else false

        val process2 = if (msg2.messageId != lastMessageId) {
            lastMessageId = msg2.messageId
            true
        } else false

        assertTrue("First message should be processed", process1)
        assertFalse("Duplicate messageId should be ignored", process2)
    }

    // 9. Null audioId
    @Test
    fun test09_nullAudioId_isValidMessage() {
        val msg = createValidMessage(audioId = null)
        assertTrue("Message with null audioId must be valid", ClassroomMessageValidator.isValid(msg))
        assertNull(msg.audioId)
    }

    // 10. Valid audioId
    @Test
    fun test10_validAudioId_isValidMessage() {
        val msg = createValidMessage(audioId = "num_001_1")
        assertTrue("Message with valid audioId must be valid", ClassroomMessageValidator.isValid(msg))
        assertEquals("num_001_1", msg.audioId)
    }

    // 11. Blank Hindi text does NOT invalidate student message
    @Test
    fun test11_blankHindiText_doesNotInvalidateStudentMessage() {
        val msg1 = createValidMessage(hindiText = "")
        val msg2 = createValidMessage(hindiText = "   ")
        assertTrue("Blank Hindi text must NOT invalidate student message", ClassroomMessageValidator.isValid(msg1))
        assertTrue("Whitespace Hindi text must NOT invalidate student message", ClassroomMessageValidator.isValid(msg2))
    }

    // 12. Blank Santali text invalidates message
    @Test
    fun test12_blankSantaliText_isRejectedByValidator() {
        val msg = createValidMessage(santaliText = "")
        assertFalse("Blank Santali text must be rejected by validator", ClassroomMessageValidator.isValid(msg))
    }

    // 13. Student client response parsing
    @Test
    fun test13_jsonParsing_deserializesCleanly() {
        val original = createValidMessage(hindiText = "गिनती", santaliText = "ᱮᱞ")
        val json = original.toJson(gson)
        val deserialized = ClassroomTranslationMessage.fromJson(json, gson)

        assertNotNull("Deserialized message must not be null", deserialized)
        assertEquals(original.messageId, deserialized?.messageId)
        assertEquals("गिनती", deserialized?.hindiText)
        assertEquals("ᱮᱞ", deserialized?.santaliText)
    }

    // 14. Connection failure handling
    @Test
    fun test14_invalidIpPort_returnsServerErrorOrNetworkError() {
        val result = client.fetchLatestMessage("invalid_ip_format_999", 99999)
        assertTrue(
            "Invalid IP/port must return NetworkError or ServerError",
            result is ClassroomFetchResult.NetworkError || result is ClassroomFetchResult.ServerError
        )
    }

    // 15. Teacher server unavailable
    @Test
    fun test15_unreachablePort_returnsNetworkErrorOrServerError() {
        val result = client.fetchLatestMessage("127.0.0.1", 59999)
        assertTrue(
            "Unreachable port must fail gracefully",
            result is ClassroomFetchResult.NetworkError || result is ClassroomFetchResult.ServerError
        )
    }

    // 16. New message detection
    @Test
    fun test16_newMessageId_updatesLastMessageId() {
        var lastId: String? = "MSG-1"
        val newMsg = createValidMessage(messageId = "MSG-2")

        val isNew = newMsg.messageId != lastId
        if (isNew) {
            lastId = newMsg.messageId
        }

        assertTrue("MSG-2 should be detected as new", isNew)
        assertEquals("MSG-2", lastId)
    }

    // 17. Same message ignored
    @Test
    fun test17_sameMessageId_isIgnored() {
        val lastId = "MSG-SAME"
        val sameMsg = createValidMessage(messageId = "MSG-SAME")

        val isNew = sameMsg.messageId != lastId
        assertFalse("Same messageId should not be flagged as new", isNew)
    }

    // 18. Configuration persistence (StudentConfig parameters)
    @Test
    fun test18_studentConfigDefaults_areCorrect() {
        val config = StudentConfig(grade = 2, section = "A", teacherIp = "192.168.43.1", teacherPort = 8080)
        assertEquals(2, config.grade)
        assertEquals("A", config.section)
        assertEquals("192.168.43.1", config.teacherIp)
        assertEquals(8080, config.teacherPort)
    }

    // 19. Evaluate message -> ACCEPTED
    @Test
    fun test19_evaluateMessage_returnsAccepted() {
        val msg = createValidMessage(grade = 2, section = "A", messageId = "MSG-100")
        val res = SectionMatcher.evaluateMessage(2, "A", null, msg)
        assertEquals(ClassroomMessageResult.ACCEPTED, res)
    }

    // 20. Evaluate message -> WRONG_GRADE
    @Test
    fun test20_evaluateMessage_returnsWrongGrade() {
        val msg = createValidMessage(grade = 1, section = "A", messageId = "MSG-101")
        val res = SectionMatcher.evaluateMessage(2, "A", null, msg)
        assertEquals(ClassroomMessageResult.WRONG_GRADE, res)
    }

    // 21. Evaluate message -> WRONG_SECTION when both sections differ
    @Test
    fun test21_evaluateMessage_returnsWrongSection() {
        val msg = createValidMessage(grade = 2, section = "B", messageId = "MSG-102")
        val res = SectionMatcher.evaluateMessage(2, "A", null, msg)
        assertEquals(ClassroomMessageResult.WRONG_SECTION, res)
    }

    // 22. Evaluate message -> DUPLICATE
    @Test
    fun test22_evaluateMessage_returnsDuplicate() {
        val msg = createValidMessage(grade = 2, section = "A", messageId = "MSG-103")
        val res = SectionMatcher.evaluateMessage(2, "A", "MSG-103", msg)
        assertEquals(ClassroomMessageResult.DUPLICATE, res)
    }

    // 23. Evaluate message -> INVALID_MESSAGE
    @Test
    fun test23_evaluateMessage_returnsInvalidMessage() {
        val msg = createValidMessage(santaliText = "", messageId = "MSG-104")
        val res = SectionMatcher.evaluateMessage(2, "A", null, msg)
        assertEquals(ClassroomMessageResult.INVALID_MESSAGE, res)
    }
}
