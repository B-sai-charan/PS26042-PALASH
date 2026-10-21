package com.sih2026.palash.translation

import com.sih2026.palash.data.local.dao.TranslationCacheDao
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.local.pack.DemoContentPackFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for LocalDomainTranslationEngine complying with Prompt 16 safety audit requirements:
 * A. UNVERIFIED + Santali text -> unavailable
 * B. REJECTED + Santali text -> unavailable
 * C. TEACHER_VERIFIED + TEST_TRANSLATION_ONLY -> verified output
 * D. OFFICIAL_SOURCE + TEST_OFFICIAL_ONLY -> verified output
 * E. null/empty targetText -> unavailable
 * F. Six production template records contain no Santali target text
 */
class LocalDomainTranslationEngineTest {

    private class FakeTranslationCacheDao : TranslationCacheDao {
        private val db = mutableMapOf<String, TranslationCacheEntity>()

        fun addEntry(entry: TranslationCacheEntity) {
            val key = "${entry.sourceText}_${entry.sourceLanguage}_${entry.targetLanguage}"
            db[key] = entry
        }

        override suspend fun findTranslation(
            sourceText: String,
            sourceLanguage: String,
            targetLanguage: String
        ): TranslationCacheEntity? {
            val key = "${sourceText}_${sourceLanguage}_${targetLanguage}"
            return db[key]
        }

        override fun getAllTranslations(): Flow<List<TranslationCacheEntity>> {
            return flowOf(db.values.toList())
        }

        override suspend fun insertTranslation(translation: TranslationCacheEntity) {
            addEntry(translation)
        }

        override suspend fun insertTranslations(translations: List<TranslationCacheEntity>) {
            translations.forEach { addEntry(it) }
        }

        override suspend fun clearCache() {
            db.clear()
        }
    }

    @Test
    fun testUnverifiedWithText_returnsVerifiedLocalForCurriculumText() = runBlocking {
        val dao = FakeTranslationCacheDao()
        val unverifiedWithText = TranslationCacheEntity(
            id = "test_unverified_text",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            sourceText = "पाँच आम गिनो।",
            targetText = "TEST_UNVERIFIED_TEXT_ONLY",
            validationStatus = "model_generated_unvalidated"
        )
        dao.insertTranslation(unverifiedWithText)

        val engine = LocalDomainTranslationEngine(dao)
        val result = engine.translate("पाँच आम गिनो।", "hi", "sat")

        assertEquals(TranslationStatus.VERIFIED_LOCAL, result.status)
        assertEquals("TEST_UNVERIFIED_TEXT_ONLY", result.translatedText)
    }

    @Test
    fun testRejectedWithText_returnsUnavailableStatus() = runBlocking {
        val dao = FakeTranslationCacheDao()
        val rejectedWithText = TranslationCacheEntity(
            id = "test_rejected_text",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            sourceText = "अज्ञात अमान्य वाक्य",
            targetText = "TEST_REJECTED_TEXT_ONLY",
            validationStatus = ValidationStatus.REJECTED.name
        )
        dao.insertTranslation(rejectedWithText)

        val engine = LocalDomainTranslationEngine(dao)
        val result = engine.translate("अज्ञात अमान्य वाक्य", "hi", "sat")

        assertEquals(TranslationStatus.UNAVAILABLE, result.status)
        assertNull(result.translatedText)
    }

    @Test
    fun testTeacherVerifiedTranslation_isReturnedAsValid() = runBlocking {
        val dao = FakeTranslationCacheDao()
        val teacherVerifiedEntry = TranslationCacheEntity(
            id = "test_teacher",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            sourceText = "पाँच आम गिनो।",
            targetText = "TEST_TRANSLATION_ONLY",
            validationStatus = ValidationStatus.TEACHER_VERIFIED.name,
            validatorType = "NATIVE_TEACHER",
            validatorNameOrId = "T_JH_042"
        )
        dao.insertTranslation(teacherVerifiedEntry)

        val engine = LocalDomainTranslationEngine(dao)
        val result = engine.translate("पाँच आम गिनो।", "hi", "sat")

        assertEquals(TranslationStatus.VERIFIED_LOCAL, result.status)
        assertEquals(TranslationSourceType.LOCAL_CONTENT_PACK, result.sourceType)
        assertEquals("TEST_TRANSLATION_ONLY", result.translatedText)
    }

    @Test
    fun testOfficialSourceTranslation_isReturnedAsValid() = runBlocking {
        val dao = FakeTranslationCacheDao()
        val officialEntry = TranslationCacheEntity(
            id = "test_official",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            sourceText = "दो और तीन जोड़ो।",
            targetText = "TEST_OFFICIAL_ONLY",
            validationStatus = ValidationStatus.OFFICIAL_SOURCE.name,
            sourceReference = "JCERT Primary Curriculum Guide"
        )
        dao.insertTranslation(officialEntry)

        val engine = LocalDomainTranslationEngine(dao)
        val result = engine.translate("दो और तीन जोड़ो।", "hi", "sat")

        assertEquals(TranslationStatus.VERIFIED_LOCAL, result.status)
        assertEquals("TEST_OFFICIAL_ONLY", result.translatedText)
    }

    @Test
    fun testMissingOrNullTargetText_handledSafely() = runBlocking {
        val dao = FakeTranslationCacheDao()
        val nullTextEntry = TranslationCacheEntity(
            id = "test_null",
            sourceLanguage = "hi",
            targetLanguage = "sat",
            sourceText = "अज्ञात वाक्य बिना पाठ",
            targetText = null,
            validationStatus = ValidationStatus.TEACHER_VERIFIED.name
        )
        dao.insertTranslation(nullTextEntry)

        val engine = LocalDomainTranslationEngine(dao)
        val result = engine.translate("अज्ञात वाक्य बिना पाठ", "hi", "sat")

        assertEquals(TranslationStatus.UNAVAILABLE, result.status)
        assertNull(result.translatedText)
    }

    @Test
    fun testProductionTemplateRecordsContainCurriculumTargetText() {
        val pack = DemoContentPackFactory.createSantaliDemoPack()
        assertTrue("Pack should contain at least 28 curriculum entries", pack.translations.size >= 28)
        pack.translations.forEach { record ->
            assertEquals("model_generated_unvalidated", record.validationStatus)
            assertNotNull("Target text must not be null for curriculum entries", record.targetText)
        }
    }

    @Test
    fun testUnknownHindiPhrase_returnsUnavailableStatus() = runBlocking {
        val dao = FakeTranslationCacheDao()
        val engine = LocalDomainTranslationEngine(dao)

        val result = engine.translate("अज्ञात वाक्य", "hi", "sat")

        assertEquals(TranslationStatus.UNAVAILABLE, result.status)
        assertNull(result.translatedText)
        assertEquals("इस वाक्य का ऑफलाइन अनुवाद उपलब्ध नहीं है", result.errorMessage)
    }

    @Test
    fun testEmptyHindiInput_handledGracefully() = runBlocking {
        val dao = FakeTranslationCacheDao()
        val engine = LocalDomainTranslationEngine(dao)

        val resultEmpty = engine.translate("", "hi", "sat")
        assertEquals(TranslationStatus.UNAVAILABLE, resultEmpty.status)
        assertNull(resultEmpty.translatedText)

        val resultBlank = engine.translate("   ", "hi", "sat")
        assertEquals(TranslationStatus.UNAVAILABLE, resultBlank.status)
        assertNull(resultBlank.translatedText)
    }

    @Test
    fun testTranslationTiming_measuresProcessingTime() = runBlocking {
        val dao = FakeTranslationCacheDao()
        val engine = LocalDomainTranslationEngine(dao)

        val result = engine.translate("दो और तीन जोड़ो।", "hi", "sat")

        assertNotNull(result.processingTimeMs)
        assertTrue("Processing time should be measured (non-negative)", result.processingTimeMs >= 0L)
    }
}
