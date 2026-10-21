package com.sih2026.palash.data.m5

import com.sih2026.palash.data.adapter.M5RoomAdapter
import com.sih2026.palash.data.repository.M5ContentRepository
import com.sih2026.palash.data.validator.M5AudioReferenceValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class M5ContentAdapterUnitTest {

    private val repository = M5ContentRepository()

    private val lessonFile = File("src/main/assets/content/grade_2/lesson_g2_counting.json")
    private val worksheetFile = File("src/main/assets/content/grade_2/worksheet_g2_counting.json")
    private val flashcardsFile = File("src/main/assets/content/grade_2/flashcards_g2_counting.json")
    private val assessmentsFile = File("src/main/assets/content/grade_2/assessments_g2_counting.json")

    // 1. Grade 2 lesson JSON parsing
    @Test
    fun test01_parseGrade2Lesson_success() {
        assertTrue("Lesson file must exist in assets", lessonFile.exists())
        val json = lessonFile.readText(Charsets.UTF_8)
        val result = repository.parseLesson(json)

        assertTrue("Lesson parsing should succeed", result.isSuccess)
        val lesson = result.getOrThrow()
        assertEquals("LESSON_G2_COUNTING_001", lesson.lessonId)
        assertEquals(2, lesson.grade)
        assertEquals("mathematics", lesson.subject)
        assertEquals("counting_numbers_and_operations", lesson.topic)
    }

    // 2. Six lesson sections are parsed & 3. Section order is preserved
    @Test
    fun test02_lessonSections_parsedInExactOrder() {
        val json = lessonFile.readText(Charsets.UTF_8)
        val lesson = repository.parseLesson(json).getOrThrow()

        assertEquals("Must parse exactly 6 sections", 6, lesson.sections.size)

        val expectedSectionIds = listOf("SEC01", "SEC02", "SEC03", "SEC04", "SEC05", "SEC06")
        val actualSectionIds = lesson.sections.map { it.sectionId }

        assertEquals("Section order must be strictly preserved", expectedSectionIds, actualSectionIds)

        val expectedTypes = listOf(
            "introduction",
            "teacher_instruction",
            "number_learning",
            "example",
            "student_activity",
            "assessment_prompt"
        )
        val actualTypes = lesson.sections.map { it.type }
        assertEquals("Section types must match M5 specification", expectedTypes, actualTypes)
    }

    // 4. Grade 2 worksheet parsing
    @Test
    fun test03_parseGrade2Worksheet_success() {
        assertTrue("Worksheet file must exist in assets", worksheetFile.exists())
        val json = worksheetFile.readText(Charsets.UTF_8)
        val result = repository.parseWorksheet(json)

        assertTrue("Worksheet parsing should succeed", result.isSuccess)
        val worksheet = result.getOrThrow()
        assertEquals("WS_G2_COUNTING_001", worksheet.worksheetId)
        assertEquals(2, worksheet.grade)
        assertEquals(5, worksheet.questions.size)

        val firstQ = worksheet.questions.first()
        assertEquals("WSQ001", firstQ.questionId)
        assertEquals("count_objects", firstQ.questionType)
    }

    // 5. Grade 2 flashcard parsing
    @Test
    fun test04_parseGrade2Flashcards_success() {
        assertTrue("Flashcards file must exist in assets", flashcardsFile.exists())
        val json = flashcardsFile.readText(Charsets.UTF_8)
        val result = repository.parseFlashcards(json)

        assertTrue("Flashcards parsing should succeed", result.isSuccess)
        val flashcards = result.getOrThrow()
        assertEquals("FC_G2_COUNTING_OPS_001", flashcards.flashcardSetId)
        assertEquals(2, flashcards.grade)
        assertEquals(7, flashcards.cards.size)

        val card1 = flashcards.cards.first()
        assertEquals("FC001", card1.cardId)
        assertEquals("1", card1.numberDisplay)

        val cardOperation = flashcards.cards.last()
        assertEquals("FC014", cardOperation.cardId)
        assertEquals("6 ÷ 2 = 3", cardOperation.numberDisplay)
    }

    // 6. Grade 2 assessment parsing
    @Test
    fun test05_parseGrade2Assessment_success() {
        assertTrue("Assessments file must exist in assets", assessmentsFile.exists())
        val json = assessmentsFile.readText(Charsets.UTF_8)
        val result = repository.parseAssessment(json)

        assertTrue("Assessment parsing should succeed", result.isSuccess)
        val assessment = result.getOrThrow()
        assertEquals("AS_G2_COUNTING_001", assessment.assessmentId)
        assertEquals(2, assessment.grade)
        assertEquals(5, assessment.questions.size)
    }

    // 7. Required-field validation
    @Test
    fun test06_missingRequiredField_failsValidation() {
        val missingIdJson = """{"title": "No ID", "grade": 2, "subject": "math", "topic": "counting", "sections": [{"section_id":"SEC01", "type":"intro"}]}"""
        val result = repository.parseLesson(missingIdJson)

        assertTrue("Parsing should fail when lesson_id is missing", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("lesson_id") == true)
    }

    // 8. Invalid JSON handling
    @Test
    fun test07_invalidJson_failsGracefully() {
        val malformedJson = "{ lesson_id: 'LESSON1', grade: 2, bad_json: }"
        val result = repository.parseLesson(malformedJson)

        assertTrue("Parsing should fail gracefully on malformed JSON", result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    // 9. Missing asset handling
    @Test
    fun test08_missingAsset_returnsFailureResult() {
        val result = repository.loadJsonFromAsset("content/grade_2/non_existent_file.json")
        assertTrue("Loading non-existent asset should return failure Result", result.isFailure)
    }

    // 10. Validation status preservation
    @Test
    fun test09_validationStatus_isPreservedExact() {
        val lesson = repository.parseLesson(lessonFile.readText(Charsets.UTF_8)).getOrThrow()
        assertEquals("model_generated_unvalidated", lesson.validationStatus)

        for (section in lesson.sections) {
            assertEquals("Section validation status must remain model_generated_unvalidated", "model_generated_unvalidated", section.validationStatus)
        }
    }

    // 11. Translation source preservation
    @Test
    fun test10_translationSource_isPreservedExact() {
        val lesson = repository.parseLesson(lessonFile.readText(Charsets.UTF_8)).getOrThrow()
        assertEquals("AI/model-generated content", lesson.translationSource)
        assertTrue(lesson.notes?.contains("model-generated/unvalidated") == true)
    }

    // 12. Exact audio filename reference validation & 14. No fuzzy matching
    @Test
    fun test11_audioFilenameReferences_validateExact() {
        val lesson = repository.parseLesson(lessonFile.readText(Charsets.UTF_8)).getOrThrow()
        val manifestFilenames = setOf(
            "L2_SEC01.wav",
            "L2_SEC02.wav",
            "L2_SEC03.wav",
            "L2_SEC04.wav",
            "L2_SEC05.wav",
            "L2_SEC06.wav"
        )

        val validation = M5AudioReferenceValidator.validateLessonAudio(lesson, manifestFilenames)
        assertTrue("All 6 section audio references must resolve exactly", validation.isValid)
        assertEquals(6, validation.verifiedReferences.size)
        assertTrue("No missing audio references", validation.missingReferences.isEmpty())
    }

    // 13. Missing audio reference detection
    @Test
    fun test12_missingAudioReference_isDetected() {
        val lesson = repository.parseLesson(lessonFile.readText(Charsets.UTF_8)).getOrThrow()
        val incompleteManifest = setOf("L2_SEC01.wav", "L2_SEC02.wav") // missing SEC03..SEC06

        val validation = M5AudioReferenceValidator.validateLessonAudio(lesson, incompleteManifest)
        assertFalse("Validation must fail when audio references are missing from manifest", validation.isValid)
        assertEquals(4, validation.missingReferences.size)
    }

    // 15. Grade 3 isolation
    @Test
    fun test13_grade3Content_isRejectedByGrade2Repository() {
        val grade3Json = """
            {
                "lesson_id": "LESSON_G3_001",
                "title": "Grade 3 Lesson",
                "grade": 3,
                "subject": "mathematics",
                "topic": "counting",
                "sections": [{"section_id": "SEC01", "type": "intro"}]
            }
        """.trimIndent()

        val result = repository.parseLesson(grade3Json)
        assertTrue("Repository must reject Grade 3 content", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Grade 2") == true)
    }

    // 16. Room Adapter Mapping Verification
    @Test
    fun test14_roomAdapter_mapsEntitiesCorrectly() {
        val lesson = repository.parseLesson(lessonFile.readText(Charsets.UTF_8)).getOrThrow()
        val lessonEntity = M5RoomAdapter.toLessonEntity(lesson)

        assertEquals("LESSON_G2_COUNTING_001", lessonEntity.id)
        assertEquals("2", lessonEntity.grade)
        assertEquals("mathematics", lessonEntity.subject)
        assertEquals("sat_Olck", lessonEntity.languageCode)
        assertTrue(lessonEntity.contentJson.contains("SEC01"))

        val worksheet = repository.parseWorksheet(worksheetFile.readText(Charsets.UTF_8)).getOrThrow()
        val worksheetEntity = M5RoomAdapter.toWorksheetEntity(worksheet)
        assertEquals("WS_G2_COUNTING_001", worksheetEntity.id)
        assertEquals("2", worksheetEntity.grade)

        val flashcards = repository.parseFlashcards(flashcardsFile.readText(Charsets.UTF_8)).getOrThrow()
        val flashcardEntities = M5RoomAdapter.toFlashcardEntities(flashcards)
        assertEquals(7, flashcardEntities.size)
        assertEquals("FC001", flashcardEntities.first().id)
    }

    // 17. Offline Content Loading Verification
    @Test
    fun test15_offlineContentLoading_worksWithoutNetwork() {
        val lesson = repository.parseLesson(lessonFile.readText(Charsets.UTF_8)).getOrThrow()
        val worksheet = repository.parseWorksheet(worksheetFile.readText(Charsets.UTF_8)).getOrThrow()
        val flashcards = repository.parseFlashcards(flashcardsFile.readText(Charsets.UTF_8)).getOrThrow()

        assertNotNull(lesson)
        assertEquals(6, lesson.sections.size)
        assertNotNull(worksheet)
        assertEquals(5, worksheet.questions.size)
        assertNotNull(flashcards)
        assertEquals(7, flashcards.cards.size)
        assertEquals("model_generated_unvalidated", lesson.validationStatus)
        assertEquals("model_generated", worksheet.validationStatus)
        assertEquals("model_generated_unvalidated", flashcards.validationStatus)
    }
}
