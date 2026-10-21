package com.sih2026.palash.classroom

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive unit tests verifying Phase 2 Classroom Message Contract,
 * validation rules, JSON serialization/deserialization, and SectionMatcher integration.
 */
class ClassroomMessageContractUnitTest {

    private val gson = Gson()

    private fun createValidMessage(
        messageId: String = "msg_test_001",
        grade: Int = 2,
        section: String = "A",
        subject: String = "mathematics",
        lessonId: Int? = 1,
        timestamp: Long = 1725624000000L,
        hindiText: String = "TEST_HINDI",
        santaliText: String = "TEST_SANTALI",
        audioId: String? = "number_5"
    ): ClassroomTranslationMessage {
        return ClassroomTranslationMessage(
            messageId = messageId,
            type = ClassroomMessageType.CLASSROOM_TRANSLATION,
            grade = grade,
            section = section,
            subject = subject,
            lessonId = lessonId,
            timestamp = timestamp,
            hindiText = hindiText,
            santaliText = santaliText,
            audioId = audioId
        )
    }

    // 1. Valid message accepted
    @Test
    fun test01_validMessage_accepted() {
        val msg = createValidMessage()
        val result = ClassroomMessageValidator.validate(msg)
        assertTrue("Valid classroom message must be accepted", result is MessageValidationResult.Valid)
        assertTrue(ClassroomMessageValidator.isValid(msg))
    }

    // 2. Blank messageId rejected
    @Test
    fun test02_blankMessageId_rejected() {
        val msg = createValidMessage(messageId = "   ")
        val result = ClassroomMessageValidator.validate(msg)
        assertTrue("Blank messageId must be rejected", result is MessageValidationResult.Invalid)
        assertEquals("messageId must not be blank", (result as MessageValidationResult.Invalid).reason)
    }

    // 3. Invalid message type rejected
    @Test
    fun test03_invalidMessageType_rejected() {
        val json = """
            {
              "messageId": "msg_test_002",
              "type": "UNKNOWN_TYPE",
              "grade": 2,
              "section": "A",
              "subject": "mathematics",
              "timestamp": 1725624000000,
              "hindiText": "TEST_HINDI",
              "santaliText": "TEST_SANTALI"
            }
        """.trimIndent()
        val deserialized = ClassroomTranslationMessage.fromJson(json, gson)
        val isValid = deserialized != null && ClassroomMessageValidator.isValid(deserialized)
        assertFalse("Unknown enum message type must be rejected by validator or deserializer", isValid)
    }

    // 4. Invalid grade (<= 0) rejected
    @Test
    fun test04_invalidGrade_rejected() {
        val msgZero = createValidMessage(grade = 0)
        val msgNegative = createValidMessage(grade = -1)
        assertFalse("Grade 0 must be rejected", ClassroomMessageValidator.isValid(msgZero))
        assertFalse("Grade -1 must be rejected", ClassroomMessageValidator.isValid(msgNegative))
    }

    // 5. Blank/null section accepted as optional field
    @Test
    fun test05_blankSection_acceptedAsOptional() {
        val msg = createValidMessage(section = "")
        assertTrue("Blank section is optional and must be accepted", ClassroomMessageValidator.isValid(msg))
    }

    // 6. Blank subject rejected
    @Test
    fun test06_blankSubject_rejected() {
        val msg = createValidMessage(subject = "  ")
        assertFalse("Blank subject must be rejected", ClassroomMessageValidator.isValid(msg))
    }

    // 7. Blank/null Hindi text accepted as optional field
    @Test
    fun test07_blankHindiText_acceptedAsOptional() {
        val msg = createValidMessage(hindiText = "")
        assertTrue("Blank hindiText is optional and must be accepted", ClassroomMessageValidator.isValid(msg))
    }

    // 8. Blank Santali text rejected
    @Test
    fun test08_blankSantaliText_rejected() {
        val msg = createValidMessage(santaliText = "  ")
        assertFalse("Blank santaliText must be rejected", ClassroomMessageValidator.isValid(msg))
    }

    // 9. Invalid timestamp (<= 0) rejected
    @Test
    fun test09_invalidTimestamp_rejected() {
        val msgZero = createValidMessage(timestamp = 0L)
        val msgNegative = createValidMessage(timestamp = -100L)
        assertFalse("Timestamp 0 must be rejected", ClassroomMessageValidator.isValid(msgZero))
        assertFalse("Negative timestamp must be rejected", ClassroomMessageValidator.isValid(msgNegative))
    }

    // 10. Null lessonId accepted
    @Test
    fun test10_nullLessonId_accepted() {
        val msg = createValidMessage(lessonId = null)
        assertTrue("Null lessonId must be accepted", ClassroomMessageValidator.isValid(msg))
        assertNull(msg.lessonId)
    }

    // 11. Null audioId accepted
    @Test
    fun test11_nullAudioId_accepted() {
        val msg = createValidMessage(audioId = null)
        assertTrue("Null audioId must be accepted", ClassroomMessageValidator.isValid(msg))
        assertNull(msg.audioId)
    }

    // 12. Valid audioId accepted
    @Test
    fun test12_validAudioId_accepted() {
        val msg = createValidMessage(audioId = "number_5")
        assertTrue("Valid audioId 'number_5' must be accepted", ClassroomMessageValidator.isValid(msg))
        assertEquals("number_5", msg.audioId)
    }

    // 13. JSON serialization contains expected keys
    @Test
    fun test13_jsonSerialization_containsAllFields() {
        val msg = createValidMessage()
        val json = msg.toJson(gson)

        assertTrue(json.contains("\"messageId\":\"msg_test_001\""))
        assertTrue(json.contains("\"type\":\"CLASSROOM_TRANSLATION\""))
        assertTrue(json.contains("\"grade\":2"))
        assertTrue(json.contains("\"section\":\"A\""))
        assertTrue(json.contains("\"subject\":\"mathematics\""))
        assertTrue(json.contains("\"lessonId\":1"))
        assertTrue(json.contains("\"timestamp\":1725624000000"))
        assertTrue(json.contains("\"hindiText\":\"TEST_HINDI\""))
        assertTrue(json.contains("\"santaliText\":\"TEST_SANTALI\""))
        assertTrue(json.contains("\"audioId\":\"number_5\""))
    }

    // 14. JSON deserialization reconstructs identical object
    @Test
    fun test14_jsonDeserialization_reconstructsIdenticalObject() {
        val original = createValidMessage()
        val json = original.toJson(gson)
        val restored = ClassroomTranslationMessage.fromJson(json, gson)

        assertNotNull("Deserialized message must not be null", restored)
        assertEquals(original, restored)
    }

    // 15. SectionMatcher accepts matching grade + section
    @Test
    fun test15_sectionMatcher_acceptsMatchingGradeAndSection() {
        val msg = createValidMessage(grade = 2, section = "A")
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = msg.grade,
            messageSection = msg.section
        )
        assertTrue("Student Grade 2 Section A must accept Grade 2 Section A message", accepted)
    }

    // 16. SectionMatcher rejects wrong section
    @Test
    fun test16_sectionMatcher_rejectsWrongSection() {
        val msg = createValidMessage(grade = 2, section = "B")
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = msg.grade,
            messageSection = msg.section
        )
        assertFalse("Student Grade 2 Section A must reject Grade 2 Section B message", accepted)
    }

    // 17. SectionMatcher rejects wrong grade
    @Test
    fun test17_sectionMatcher_rejectsWrongGrade() {
        val msg = createValidMessage(grade = 3, section = "A")
        val accepted = SectionMatcher.shouldAcceptClassroomMessage(
            studentGrade = 2,
            studentSection = "A",
            messageGrade = msg.grade,
            messageSection = msg.section
        )
        assertFalse("Student Grade 2 Section A must reject Grade 3 Section A message", accepted)
    }
}
