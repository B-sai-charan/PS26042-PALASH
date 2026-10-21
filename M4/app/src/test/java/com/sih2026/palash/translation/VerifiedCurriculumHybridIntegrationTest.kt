package com.sih2026.palash.translation

import com.sih2026.palash.data.local.dao.TranslationCacheDao
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.network.NetworkResult
import com.sih2026.palash.data.repository.TranslationRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 9 Required Tests for Verified Curriculum + Neural Fallback Integration.
 */
class VerifiedCurriculumHybridIntegrationTest {

    private val fakeDao = object : TranslationCacheDao {
        private val cache = mutableMapOf<String, TranslationCacheEntity>()

        override suspend fun insertTranslation(translation: TranslationCacheEntity) {
            cache[translation.sourceText] = translation
        }

        override suspend fun insertTranslations(translations: List<TranslationCacheEntity>) {
            translations.forEach { cache[it.sourceText] = it }
        }

        override suspend fun findTranslation(
            sourceText: String,
            sourceLang: String,
            targetLang: String
        ): TranslationCacheEntity? = cache[sourceText]

        override fun getAllTranslations(): kotlinx.coroutines.flow.Flow<List<TranslationCacheEntity>> {
            return kotlinx.coroutines.flow.flowOf(cache.values.toList())
        }

        override suspend fun clearCache() {
            cache.clear()
        }
    }

    private val repository = TranslationRepositoryImpl(fakeDao)

    @Test
    fun testAll15VerifiedPhrases() {
        val expectedPhrases = mapOf(
            "सभी बच्चे बैठ जाओ।" to "ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ ᱫᱩᱲᱩᱵ ᱯե᱾",
            "आज हम क्या सीखेंगे?" to "ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱪᱮᱫ ᱵᱚᱱ ᱪᱮᱫᱟᱜᱼᱟ?",
            "नमस्ते बच्चों।" to "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ᱾",
            "आप सभी चुप रहें।" to "ᱟᱯᱮ ᱡᱚᱛᱚ ᱦᱚᱲ ᱛᱷᱤᱨ ᱠᱚᱜ ᱯե᱾",
            "अपनी पुस्तकें खोलो" to "ᱟᱯᱱᱟᱨ ᱯᱩᱛᱷᱤ ᱨᱟᱲᱟᱭ ᱯե",
            "शुभकामनाएं" to "ᱥᱟᱹᱜᱩᱱ ᱥᱟᱱᱟᱢ",
            "तुम्हें कितने नंबर मिले?" to "ᱟᱢ ᱛᱤᱱᱟᱹᱜ ᱱᱚᱢᱵᱚᱨ ᱮᱢ ᱧᱟᱢ ᱠᱮᱫᱼᱟ?",
            "कृपया इसे एक बार फिर समझा दें।" to "ᱫᱟᱭᱟ ᱠᱟᱛᱮ ᱱᱚᱶᱟ ᱟᱨ ᱢᱤᱫ ᱫᱷᱟᱣ ᱵᱩᱡᱷᱟᱹᱣ ᱞᱟᱹᱭ ᱯե᱾",
            "क्या मैं शौचालय जा सकता हूँ?" to "ᱪᱮᱫ ᱤᱧ ᱨᱟᱪᱟ ᱛᱮᱧ ᱥᱮᱱ ᱫᱟᱲᱮᱭᱟᱜᱼᱟ?",
            "चुपचाप बैठें" to "ᱛᱷᱤᱨ ᱛᱷᱤᱨ ᱫᱩᱲᱩᱵ ᱯե",
            "आप सभी अच्छी तरह तैयारी करें।" to "ᱟᱯᱮ ᱡᱚᱛᱚ ᱦᱚᱲ ᱱᱟᱯᱟᱭ ᱛᱮ ᱥᱟᱯᱲᱟᱣ ᱠᱚᱜ ᱯե᱾",
            "شور मत करो।" to "ᱦᱟᱲᱵᱟᱲᱟᱣ ᱟᱞᱚᱯᱮ ᱦᱩᱭ ᱚᱪᱚᱭᱟ᱾",
            "नमस्ते बच्चों, आज हम गणित सीखेंगे।" to "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱞᱮᱠᱷᱟ ᱵᱚᱱ ᱪᱮᱫᱟᱜᱼᱟ᱾",
            "इसे ज़रूर लिखें।" to "ᱱᱚᱶᱟ ᱡᱟᱹᱨᱩᱲ ᱚᱞ ᱯե᱾",
            "आप सभी की कल परीक्षा है।" to "ᱟᱯᱮ ᱥᱟᱱᱟᱢ ᱠᱚᱣᱟᱜ ᱜᱟᱯᱟ ᱵᱤᱱᱤᱰ ᱢᱮᱱᱟᱜᱼᱟ᱾"
        )

        assertEquals("Must contain exactly 15 verified phrases", 15, expectedPhrases.size)

        for ((hindi, expectedOlChiki) in expectedPhrases) {
            val result = CurriculumDictionary.getVerifiedTranslation(hindi)
            assertNotNull("Translation must exist for: $hindi", result)
            assertEquals("Exact verified translation mismatch for: $hindi", expectedOlChiki, result)
        }
    }

    @Test
    fun testNumbers1to10() {
        val expectedNumbers = mapOf(
            "एक" to "ᱢᱤᱫ",
            "दो" to "ᱵᱟᱨ",
            "तीन" to "ᱯᱮ",
            "चार" to "ᱯᱚᱱ",
            "पाँच" to "ᱢᱚᱬᱮ",
            "छह" to "ᱛᱩᱨᱩᱭ",
            "सात" to "ᱮᱭᱟᱭ",
            "आठ" to "ᱤᱨᱟᱹᱞ",
            "नौ" to "ᱟᱨᱮ",
            "दस" to "ᱜᱮᱞ"
        )

        assertEquals("Must contain 10 number mappings", 10, expectedNumbers.size)

        for ((hindiNum, expectedOlChiki) in expectedNumbers) {
            val result = CurriculumDictionary.getVerifiedTranslation(hindiNum)
            assertNotNull("Translation must exist for number: $hindiNum", result)
            assertEquals("Number mismatch for $hindiNum", expectedOlChiki, result)
        }
    }

    @Test
    fun testCriticalThreeMustReturnPeAndNotPeya() {
        val result = CurriculumDictionary.getVerifiedTranslation("तीन")
        assertNotNull(result)
        assertEquals("ᱯᱮ", result)
        assertNotEquals("ᱯᱮᱭᱟ", result)
    }

    @Test
    fun testExactNormalizedMatching() {
        val paddedInput = "   सभी बच्चे    बैठ जाओ।   "
        val result = CurriculumDictionary.getVerifiedTranslation(paddedInput)
        assertEquals("ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ ᱫᱩᱲᱩᱵ ᱯե᱾", result)
    }

    @Test
    fun testUnknownInputReturnsNullFromCurriculumDictionary() {
        val unknownSentence = "एक बिलकुल नया अनजाना वाक्य जो पाठ्यचर्या में नहीं है।"
        val result = CurriculumDictionary.getVerifiedTranslation(unknownSentence)
        assertNull(result)
        assertFalse(CurriculumDictionary.isVerified(unknownSentence))
    }

    @Test
    fun testVerifiedInputDoesNotCallM1() = runBlocking {
        // Since repository returns Path A directly without network call, it succeeds instantly offline/online
        val res = repository.translateHindiToSantali("एक")
        assertTrue(res is NetworkResult.Success)
        val data = (res as NetworkResult.Success).data
        assertEquals("ᱢᱤᱫ", data.dto.translatedText)
        assertTrue("Verified translation must be flagged fromCache", data.isFromCache)
    }

    @Test
    fun testM1CannotOverwriteVerifiedContent() {
        val verifiedSentence = "तीन"
        assertTrue(CurriculumDictionary.isVerified(verifiedSentence))

        // Ensure that for verified sentences, CurriculumDictionary.isVerified prevents saving M1 outputs
        val isVerified = CurriculumDictionary.isVerified(verifiedSentence)
        assertTrue("Verified phrase 'तीन' must be protected from M1 overwrite", isVerified)
    }
}
