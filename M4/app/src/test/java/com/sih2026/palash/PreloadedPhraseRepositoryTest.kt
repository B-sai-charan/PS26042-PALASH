package com.sih2026.palash

import com.sih2026.palash.data.local.dao.TranslationCacheDao
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.network.NetworkResult
import com.sih2026.palash.data.repository.PreloadedPhraseRepository
import com.sih2026.palash.data.repository.TranslationRepositoryImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreloadedPhraseRepositoryTest {

    @Test
    fun testAll15PhrasesExistInPreloadedRepository() {
        assertEquals(15, PreloadedPhraseRepository.getPhraseCount())
        val all = PreloadedPhraseRepository.getAllPhrases()
        assertEquals(15, all.size)

        val ids = all.map { it.id }
        for (i in 1..15) {
            val expectedId = String.format("P%03d", i)
            assertTrue("Expected ID $expectedId to exist", ids.contains(expectedId))
        }
    }

    @Test
    fun testExactMatchingOfSpecificPhrases() {
        // P001
        val p1 = PreloadedPhraseRepository.findExactHindiPhrase("सभी बच्चे बैठ जाओ।")
        assertNotNull(p1)
        assertEquals("P001", p1?.id)
        assertEquals("ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ, ᱫᱩᱲᱩᱵ ᱢᱮ᱾", p1?.santaliText)

        // P003
        val p3 = PreloadedPhraseRepository.findExactHindiPhrase("नमस्ते बच्चों।")
        assertNotNull(p3)
        assertEquals("P003", p3?.id)
        assertEquals("ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ ᱾", p3?.santaliText)

        // P005
        val p5 = PreloadedPhraseRepository.findExactHindiPhrase("अपनी पुस्तकें खोलो")
        assertNotNull(p5)
        assertEquals("P005", p5?.id)
        assertEquals("ᱟᱢᱟᱜ ᱯᱚᱛᱚᱵ ᱡᱷᱤᱡᱽ ᱢᱮ", p5?.santaliText)

        // P006
        val p6 = PreloadedPhraseRepository.findExactHindiPhrase("शुभकामनाएं")
        assertNotNull(p6)
        assertEquals("P006", p6?.id)
        assertEquals("ᱥᱟᱹᱜᱩᱱ ᱥᱟᱱᱟᱝ", p6?.santaliText)

        // P012
        val p12 = PreloadedPhraseRepository.findExactHindiPhrase("शोर मत करो.")
        assertNotNull(p12)
        assertEquals("P012", p12?.id)
        assertEquals("ᱠᱟᱞᱠᱟᱞᱟᱣ ᱟᱞᱚᱢ ᱛᱮᱭᱟᱨᱟ᱾", p12?.santaliText)

        // P015
        val p15 = PreloadedPhraseRepository.findExactHindiPhrase("आप सभी की कल परीक्षा है।")
        assertNotNull(p15)
        assertEquals("P015", p15?.id)
        assertEquals("ᱟᱯᱮ ᱥᱟᱱᱟᱢ ᱠᱚᱣᱟᱜ ᱜᱟᱯᱟ ᱵᱤᱱᱤᱰ ᱢᱮᱱᱟᱜᱼᱟ ᱾", p15?.santaliText)
    }

    @Test
    fun testPunctuationNormalization() {
        // With danda
        val matchWithDanda = PreloadedPhraseRepository.findExactHindiPhrase("सभी बच्चे बैठ जाओ।")
        // Without danda
        val matchWithoutDanda = PreloadedPhraseRepository.findExactHindiPhrase("सभी बच्चे बैठ जाओ")
        // With period
        val matchWithDot = PreloadedPhraseRepository.findExactHindiPhrase("सभी बच्चे बैठ जाओ.")
        // With exclamation
        val matchWithExcl = PreloadedPhraseRepository.findExactHindiPhrase("सभी बच्चे बैठ जाओ!")

        assertNotNull(matchWithDanda)
        assertNotNull(matchWithoutDanda)
        assertNotNull(matchWithDot)
        assertNotNull(matchWithExcl)

        assertEquals("P001", matchWithDanda?.id)
        assertEquals("P001", matchWithoutDanda?.id)
        assertEquals("P001", matchWithDot?.id)
        assertEquals("P001", matchWithExcl?.id)
    }

    @Test
    fun testWhitespaceAndZeroWidthNormalization() {
        // Extra spaces and zero-width spaces
        val messyText = "  नमस्ते \u200B  बच्चों  \uFEFF ।  "
        val match = PreloadedPhraseRepository.findExactHindiPhrase(messyText)
        assertNotNull(match)
        assertEquals("P003", match?.id)
        assertEquals("ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ ᱾", match?.santaliText)
    }

    @Test
    fun testUnknownPhraseReturnsNull() {
        val unknown = PreloadedPhraseRepository.findExactHindiPhrase("यह वाक्य एक्सेल शीट में नहीं है")
        assertNull(unknown)
        assertFalse(PreloadedPhraseRepository.isPreloaded("यह वाक्य एक्सेल शीट में नहीं है"))
    }

    @Test
    fun testBlankOrNullReturnsNull() {
        assertNull(PreloadedPhraseRepository.findExactHindiPhrase(""))
        assertNull(PreloadedPhraseRepository.findExactHindiPhrase("   "))
        assertNull(PreloadedPhraseRepository.findExactHindiPhrase(null))
    }

    @Test
    fun testTranslationRepositoryPriorityPreloadedOverM1() = runBlocking {
        // Mock fake DAO
        val fakeDao = object : TranslationCacheDao {
            override suspend fun findTranslation(sourceText: String, sourceLanguage: String, targetLanguage: String): TranslationCacheEntity? = null
            override fun getAllTranslations() = flowOf(emptyList<TranslationCacheEntity>())
            override suspend fun insertTranslation(translation: TranslationCacheEntity) {}
            override suspend fun insertTranslations(translations: List<TranslationCacheEntity>) {}
            override suspend fun clearCache() {}
        }

        val repository = TranslationRepositoryImpl(fakeDao)

        // Translate P006 "शुभकामनाएं" -> Must succeed from Excel without M1 or DAO
        val result = repository.translateHindiToSantali("शुभकामनाएं")
        assertTrue("Expected NetworkResult.Success", result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("ᱥᱟᱹᱜᱩᱱ ᱥᱟᱱᱟᱝ", data.dto.translatedText)
        assertEquals("success", data.dto.status)
        assertTrue(data.isFromCache)
    }

    @Test
    fun testUnknownPhraseTriggersControlledP001ThroughP015Fallback() {
        val unknown = "यह एक पूरी तरह से अज्ञात वाक्य है"
        val result = PreloadedPhraseRepository.findOrFallbackPhrase(unknown)
        assertFalse("Must not be flagged as exact match", result.isExactMatch)
        assertNotNull(result.entity)
        assertTrue("Entity ID must be between P001 and P015", result.entity.id.matches(Regex("P0\\d\\d")))
        assertTrue("Candidate must be in preloaded list", PreloadedPhraseRepository.getAllPhrases().contains(result.entity))
    }

    @Test
    fun testAll15CandidatesAreEligibleForRandomSelection() {
        // Test deterministic selection across all indices 0..14
        val selectedIds = mutableSetOf<String>()
        for (i in 0 until 15) {
            val fakeRandom = object : kotlin.random.Random() {
                override fun nextBits(bitCount: Int): Int = 0
                override fun nextInt(until: Int): Int = i % until
            }
            val record = PreloadedPhraseRepository.getRandomPhrase(fakeRandom)
            selectedIds.add(record.id)
        }
        assertEquals("All 15 IDs must be selectable", 15, selectedIds.size)
    }

    @Test
    fun testUnknownPhraseInTranslationRepositoryReturnsDemoSampleWithoutM1() = runBlocking {
        val fakeDao = object : TranslationCacheDao {
            override suspend fun findTranslation(sourceText: String, sourceLanguage: String, targetLanguage: String): TranslationCacheEntity? = null
            override fun getAllTranslations() = flowOf(emptyList<TranslationCacheEntity>())
            override suspend fun insertTranslation(translation: TranslationCacheEntity) {}
            override suspend fun insertTranslations(translations: List<TranslationCacheEntity>) {}
            override suspend fun clearCache() {}
        }

        val repository = TranslationRepositoryImpl(fakeDao)

        val res = repository.translateHindiToSantali("अज्ञात वाक्य")
        assertTrue("Expected NetworkResult.Success", res is NetworkResult.Success)
        val data = (res as NetworkResult.Success).data
        assertEquals("demo_sample", data.dto.status)
        assertTrue("Fallback translation must be flagged fromCache", data.isFromCache)
        assertTrue("Returned Santali must not be blank", data.dto.translatedText.isNotBlank())
    }
}
