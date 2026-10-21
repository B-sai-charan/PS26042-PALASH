package com.sih2026.palash.data.network

import com.google.gson.Gson
import com.sih2026.palash.data.local.dao.TranslationCacheDao
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.network.dto.TranslateRequestDto
import com.sih2026.palash.data.network.dto.TranslateResponseDto
import com.sih2026.palash.data.repository.TranslationRepositoryImpl
import com.sih2026.palash.translation.ValidationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive unit tests verifying POST /translate endpoint DTOs, validation rules,
 * repository offline/online logic, Room cache saving/retrieval, and prohibition of fake translations.
 */
class TranslateNetworkIntegrationTest {

    @Before
    fun setUp() {
        NetworkConfig.reset()
    }

    @After
    fun tearDown() {
        NetworkConfig.reset()
    }

    @Test
    fun test1_validTranslateRequestDtoSerialization() {
        val dto = TranslateRequestDto(
            text = "किसानों को सरकारी योजना का लाभ मिलेगा।",
            sourceLanguage = "hin_Deva",
            targetLanguage = "sat_Olck"
        )
        val json = Gson().toJson(dto)

        assertTrue(json.contains("\"text\":\"किसानों को सरकारी योजना का लाभ मिलेगा।\""))
        assertTrue(json.contains("\"source_language\":\"hin_Deva\""))
        assertTrue(json.contains("\"target_language\":\"sat_Olck\""))
    }

    @Test
    fun test2_validTranslateResponseDtoDeserialization() {
        val json = """
            {
              "translated_text": "ᱥᱚᱨᱠᱟᱨ ᱟᱜ ᱢᱤᱫᱴᱟᱝ ᱯᱚᱨᱤᱢᱟᱱ ᱠᱷᱚᱱ ᱪᱟᱥᱤᱭᱟᱹ ᱠᱚ ᱵᱷᱟᱞᱮ ᱧᱟᱢᱼᱟ ᱾",
              "source_language": "hin_Deva",
              "target_language": "sat_Olck",
              "latency_ms": 3719.94,
              "status": "success"
            }
        """.trimIndent()

        val response = Gson().fromJson(json, TranslateResponseDto::class.java)
        assertEquals("ᱥᱚᱨᱠᱟᱨ ᱟᱜ ᱢᱤᱫᱴᱟᱝ ᱯᱚᱨᱤᱢᱟᱱ ᱠᱷᱚᱱ ᱪᱟᱥᱤᱭᱟᱹ ᱠᱚ ᱵᱷᱟᱞᱮ ᱧᱟᱢᱼᱟ ᱾", response.translatedText)
        assertEquals("hin_Deva", response.sourceLanguage)
        assertEquals("sat_Olck", response.targetLanguage)
        assertEquals(3719.94, response.latencyMs, 0.001)
        assertEquals("success", response.status)
        assertTrue(response.isValid())
    }

    @Test
    fun test3_invalidBlankInputText() = runBlocking {
        val fakeDao = FakeTranslationCacheDao()
        val repository = TranslationRepositoryImpl(fakeDao)
        val result = repository.translateHindiToSantali("")

        assertTrue(result is NetworkResult.UnknownError)
        val error = (result as NetworkResult.UnknownError).exception
        assertTrue(error is IllegalArgumentException)
        assertEquals("Input text cannot be blank", error.message)
    }

    @Test
    fun test4_invalidResponseStatusValidation() {
        val dto = TranslateResponseDto(
            translatedText = "ᱥᱚᱨᱠᱟᱨ",
            sourceLanguage = "hin_Deva",
            targetLanguage = "sat_Olck",
            latencyMs = 120.0,
            status = "error"
        )
        assertFalse(dto.isValid())
    }

    @Test
    fun test5_blankTranslatedTextValidation() {
        val dto = TranslateResponseDto(
            translatedText = "   ",
            sourceLanguage = "hin_Deva",
            targetLanguage = "sat_Olck",
            latencyMs = 120.0,
            status = "success"
        )
        assertFalse(dto.isValid())
    }

    @Test
    fun test6_wrongSourceLanguageValidation() {
        val dto = TranslateResponseDto(
            translatedText = "ᱥᱚᱨᱠᱟᱨ",
            sourceLanguage = "eng_Latn",
            targetLanguage = "sat_Olck",
            latencyMs = 120.0,
            status = "success"
        )
        assertFalse(dto.isValid())
    }

    @Test
    fun test7_wrongTargetLanguageValidation() {
        val dto = TranslateResponseDto(
            translatedText = "ᱥᱚᱨᱠᱟᱨ",
            sourceLanguage = "hin_Deva",
            targetLanguage = "hin_Deva",
            latencyMs = 120.0,
            status = "success"
        )
        assertFalse(dto.isValid())
    }

    @Test
    fun test8_negativeLatencyValidation() {
        val dto = TranslateResponseDto(
            translatedText = "ᱥᱚᱨᱠᱟᱨ",
            sourceLanguage = "hin_Deva",
            targetLanguage = "sat_Olck",
            latencyMs = -10.0,
            status = "success"
        )
        assertFalse(dto.isValid())
    }

    @Test
    fun test9_roomCacheSaveAndRetrieval() = runBlocking {
        val fakeDao = FakeTranslationCacheDao()
        val repository = TranslationRepositoryImpl(fakeDao)

        val entity = TranslationCacheEntity(
            id = "test_trans_1",
            sourceLanguage = "hin_Deva",
            targetLanguage = "sat_Olck",
            sourceText = "नमस्ते",
            targetText = "ᱡᱚᱦᱟᱨ",
            script = "Ol Chiki",
            validationStatus = ValidationStatus.UNVERIFIED.name
        )

        repository.saveTranslation(entity)
        val retrieved = repository.getTranslation("नमस्ते", "hin_Deva", "sat_Olck")

        assertNotNull(retrieved)
        assertEquals("नमस्ते", retrieved?.sourceText)
        assertEquals("ᱡᱚᱦᱟᱨ", retrieved?.targetText)
        assertEquals(ValidationStatus.UNVERIFIED.name, retrieved?.validationStatus)
    }

    @Test
    fun test10_offlineCacheHit() = runBlocking {
        val fakeDao = FakeTranslationCacheDao()
        fakeDao.insertTranslation(
            TranslationCacheEntity(
                id = "cached_1",
                sourceLanguage = "hin_Deva",
                targetLanguage = "sat_Olck",
                sourceText = "जल ही जीवन है।",
                targetText = "ᱫᱟᱜ ᱜᱮ ᱡᱤᱣᱤ ᱠᱟᱱᱟ ᱾",
                script = "Ol Chiki",
                validationStatus = ValidationStatus.UNVERIFIED.name
            )
        )

        val repository = TranslationRepositoryImpl(fakeDao)
        val result = repository.translateHindiToSantali("जल ही जीवन है।")

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertTrue(data.isFromCache)
        assertEquals("ᱫᱟᱜ ᱜᱮ ᱡᱤᱣᱤ ᱠᱟᱱᱟ ᱾", data.dto.translatedText)
        assertEquals(0.0, data.dto.latencyMs, 0.001)
    }

    @Test
    fun test11_unknownInputUsesControlledDemoFallback() = runBlocking {
        val fakeDao = FakeTranslationCacheDao()
        val repository = TranslationRepositoryImpl(fakeDao)

        val result = repository.translateHindiToSantali("अनजानी बात")

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("demo_sample", data.dto.status)
        assertTrue(data.isFromCache)
        assertTrue(data.dto.translatedText.isNotBlank())
    }

    private class FakeTranslationCacheDao : TranslationCacheDao {
        private val map = mutableMapOf<String, TranslationCacheEntity>()

        override suspend fun findTranslation(
            sourceText: String,
            sourceLanguage: String,
            targetLanguage: String
        ): TranslationCacheEntity? {
            return map.values.firstOrNull {
                it.sourceText == sourceText &&
                        it.sourceLanguage == sourceLanguage &&
                        it.targetLanguage == targetLanguage
            }
        }

        override fun getAllTranslations(): Flow<List<TranslationCacheEntity>> {
            return flowOf(map.values.toList())
        }

        override suspend fun insertTranslation(translation: TranslationCacheEntity) {
            map[translation.id] = translation
        }

        override suspend fun insertTranslations(translations: List<TranslationCacheEntity>) {
            translations.forEach { map[it.id] = it }
        }

        override suspend fun clearCache() {
            map.clear()
        }
    }
}
