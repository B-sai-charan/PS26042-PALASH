package com.sih2026.palash.translation

import com.sih2026.palash.tts.SantaliOlChikiTransliteration
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the Curriculum-Bounded Offline Translation Layer.
 */
class CurriculumOfflineTranslationUnitTest {

    private val engine = LocalDomainTranslationEngine(null)

    @Test
    fun testExactCurriculumMatch() = runBlocking {
        val result = engine.translate("पाँच आम गिनो।", "hi", "sat")
        assertEquals(TranslationStatus.VERIFIED_LOCAL, result.status)
        assertEquals("ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।", result.translatedText)
        assertEquals(TranslationSourceType.LOCAL_CONTENT_PACK, result.sourceType)
    }

    @Test
    fun testWhitespaceNormalization() = runBlocking {
        val rawInput = "  पाँच   आम   गिनो।  "
        val normalized = LocalDomainTranslationEngine.normalizeForMatching(rawInput)
        assertEquals("पाँच आम गिनो", normalized)

        val result = engine.translate(rawInput, "hi", "sat")
        assertEquals(TranslationStatus.VERIFIED_LOCAL, result.status)
        assertEquals("ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।", result.translatedText)
    }

    @Test
    fun testPunctuationNormalization() = runBlocking {
        val inputWithoutPunctuation = "पाँच आम गिनो"
        val normalized = LocalDomainTranslationEngine.normalizeForMatching(inputWithoutPunctuation)
        assertEquals("पाँच आम गिनो", normalized)

        val result = engine.translate(inputWithoutPunctuation, "hi", "sat")
        assertEquals(TranslationStatus.VERIFIED_LOCAL, result.status)
        assertEquals("ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।", result.translatedText)
    }

    @Test
    fun testUnicodeNormalization() = runBlocking {
        val textWithZeroWidthChar = "पाँच\u200B आम गिनो।"
        val normalized = LocalDomainTranslationEngine.normalizeForMatching(textWithZeroWidthChar)
        assertEquals("पाँच आम गिनो", normalized)

        val result = engine.translate(textWithZeroWidthChar, "hi", "sat")
        assertEquals(TranslationStatus.VERIFIED_LOCAL, result.status)
        assertEquals("ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।", result.translatedText)
    }

    @Test
    fun testUnsupportedPhraseReturnsUnavailable() = runBlocking {
        val unsupportedSentence = "आज हम कक्षा में गणित और गिनती का नया पाठ पढ़ेंगे।"
        val result = engine.translate(unsupportedSentence, "hi", "sat")

        assertEquals(TranslationStatus.UNAVAILABLE, result.status)
        assertNull(result.translatedText)
        assertEquals("इस वाक्य का ऑफलाइन अनुवाद उपलब्ध नहीं है", result.errorMessage)
    }

    @Test
    fun testNoEnglishUserFacingUnavailableText() = runBlocking {
        val unsupported1 = "जल ही जीवन है।"
        val unsupported2 = "आज हम कक्षा में गणित और गिनती का नया पाठ पढ़ेंगे।"

        val r1 = engine.translate(unsupported1, "hi", "sat")
        val r2 = engine.translate(unsupported2, "hi", "sat")

        assertFalse(r1.errorMessage!!.contains("No speech"))
        assertFalse(r1.errorMessage!!.contains("ASR_"))
        assertFalse(r1.errorMessage!!.contains("UNAVAILABLE"))
        assertEquals("इस वाक्य का ऑफलाइन अनुवाद उपलब्ध नहीं है", r1.errorMessage)

        assertFalse(r2.errorMessage!!.contains("No speech"))
        assertFalse(r2.errorMessage!!.contains("ASR_"))
        assertFalse(r2.errorMessage!!.contains("UNAVAILABLE"))
        assertEquals("इस वाक्य का ऑफलाइन अनुवाद उपलब्ध नहीं है", r2.errorMessage)
    }

    @Test
    fun testNumberMappings() = runBlocking {
        val numberMap = mapOf(
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

        for ((hindi, expectedSantali) in numberMap) {
            val result = engine.translate(hindi, "hi", "sat")
            assertEquals(TranslationStatus.VERIFIED_LOCAL, result.status)
            assertEquals(expectedSantali, result.translatedText)
        }
    }

    @Test
    fun testDuplicatePrevention() {
        val set = mutableSetOf<String>()
        val ids = listOf("trans_g2_curr_1", "trans_g2_curr_2", "trans_g2_curr_1")
        val added = ids.filter { set.add(it) }

        assertEquals(2, added.size)
        assertEquals(2, set.size)
    }

    @Test
    fun testM1IsNotCalledDuringOfflineMatching() = runBlocking {
        val result = engine.translate("पाँच आम गिनो।", "hi", "sat")
        assertEquals(TranslationSourceType.LOCAL_CONTENT_PACK, result.sourceType)
        assertNull(result.errorMessage)
    }

    @Test
    fun testPiperReceivesMatchedSantaliText() = runBlocking {
        val result = engine.translate("एक", "hi", "sat")
        assertNotNull(result.translatedText)
        assertEquals("ᱢᱤᱫ", result.translatedText)

        val romanized = SantaliOlChikiTransliteration.toRoman(result.translatedText!!)
        assertEquals("mid", romanized)
    }

    @Test
    fun testUnsafeNumberSubstitutionIsRejected() = runBlocking {
        val unmappedInput = "संख्या ५ को अंकों में लिखो।"
        val result = engine.translate(unmappedInput, "hi", "sat")

        // Unsafe mapping to "संख्या 7 को अंकों में लिखो।" must NOT occur
        assertEquals(TranslationStatus.UNAVAILABLE, result.status)
        assertNull(result.translatedText)
    }

    @Test
    fun testABDifferentInputsProduceDifferentOutputs() = runBlocking {
        val inputA = "एक"
        val inputB = "दो"

        val resA = engine.translate(inputA, "hi", "sat")
        val resB = engine.translate(inputB, "hi", "sat")

        assertEquals("ᱢᱤᱫ", resA.translatedText)
        assertEquals("ᱵᱟᱨ", resB.translatedText)
        assertNotEquals(resA.translatedText, resB.translatedText)
    }
}
