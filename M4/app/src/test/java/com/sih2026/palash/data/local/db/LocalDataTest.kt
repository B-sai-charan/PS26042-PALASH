package com.sih2026.palash.data.local.db

import com.sih2026.palash.data.demo.DemoData
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.entity.LessonEntity
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.local.entity.WorksheetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDataTest {

    @Test
    fun testLessonEntityCreation() {
        val lesson = LessonEntity(
            id = "lesson_1",
            grade = "Grade 2",
            subject = "Mathematics",
            topic = "Counting",
            title = "Test Lesson",
            languageCode = "hi",
            contentJson = "{}"
        )

        assertEquals("lesson_1", lesson.id)
        assertEquals("Grade 2", lesson.grade)
        assertEquals("Mathematics", lesson.subject)
        assertEquals("Counting", lesson.topic)
    }

    @Test
    fun testTranslationCacheEntityCreation() {
        val cache = TranslationCacheEntity(
            id = "trans_1",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            sourceText = "पाँच आम गिनो।",
            targetText = null,
            validationStatus = com.sih2026.palash.translation.ValidationStatus.UNVERIFIED.name
        )

        assertEquals("trans_1", cache.id)
        assertEquals("hi", cache.sourceLanguage)
        assertEquals("sat", cache.targetLanguage)
        assertEquals("UNVERIFIED", cache.validationStatus)
        org.junit.Assert.assertNull(cache.targetText)
    }

    @Test
    fun testWorksheetEntityCreation() {
        val worksheet = WorksheetEntity(
            id = "worksheet_1",
            grade = "Grade 2",
            subject = "Mathematics",
            topic = "Counting",
            title = "Counting Worksheet",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            contentJson = "{}",
            version = "1.0"
        )

        assertEquals("worksheet_1", worksheet.id)
        assertEquals("Grade 2", worksheet.grade)
        assertEquals("1.0", worksheet.version)
    }

    @Test
    fun testLanguagePackMetadataEntityCreation() {
        val metadata = LanguagePackMetadataEntity(
            languageCode = "sat",
            languageName = "Santali",
            version = "0.1-demo",
            status = "INSTALLED_SAMPLE",
            checksum = "test-checksum"
        )

        assertEquals("sat", metadata.languageCode)
        assertEquals("Santali", metadata.languageName)
        assertEquals("INSTALLED_SAMPLE", metadata.status)
    }

    @Test
    fun testDemoDataIntegrity() {
        assertNotNull(DemoData.sampleHindiInput)
        assertEquals("Verified Santali translation not available", DemoData.sampleSantaliOutputNotice)
        assertEquals(3, DemoData.grades.size)
        assertEquals(2, DemoData.subjects.size)
    }
}
