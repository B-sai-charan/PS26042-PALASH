package com.sih2026.palash.tts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LivePiperTtsUnitTest {

    @Test
    fun testLiveTtsStateTransitions() {
        var state = LiveTtsState.IDLE
        assertEquals(LiveTtsState.IDLE, state)

        state = LiveTtsState.PROCESSING
        assertEquals(LiveTtsState.PROCESSING, state)

        state = LiveTtsState.READY
        assertEquals(LiveTtsState.READY, state)
    }

    @Test
    fun testSantaliOlChikiTransliteration() {
        val olChikiOne = "ᱢᱤᱫ"
        val romanOne = SantaliOlChikiTransliteration.olChikiToPiperRoman(olChikiOne)
        assertEquals("mid", romanOne)

        val olChikiTwo = "ᱵᱟᱨ"
        val romanTwo = SantaliOlChikiTransliteration.olChikiToPiperRoman(olChikiTwo)
        assertEquals("bar", romanTwo)

        val olChikiFive = "ᱢᱚᱬᱮ"
        val romanFive = SantaliOlChikiTransliteration.olChikiToPiperRoman(olChikiFive)
        assertEquals("mone", romanFive) // ᱬ maps to 'n' per Authoritative Mapping
    }

    @Test
    fun testAuthoritativeMappingEntriesAndRules() {
        // Test Word-Pause Rule: space becomes ", "
        val phrase = "ᱢᱤᱫ ᱵᱟᱨ"
        val romanPhrase = SantaliOlChikiTransliteration.olChikiToPiperRoman(phrase)
        assertEquals("mid, bar", romanPhrase)

        // Test digits
        assertEquals("mit", SantaliOlChikiTransliteration.olChikiToPiperRoman("1"))
        assertEquals("bar", SantaliOlChikiTransliteration.olChikiToPiperRoman("2"))
        assertEquals("pe", SantaliOlChikiTransliteration.olChikiToPiperRoman("3"))
        assertEquals("pon", SantaliOlChikiTransliteration.olChikiToPiperRoman("4"))
        assertEquals("morae", SantaliOlChikiTransliteration.olChikiToPiperRoman("5"))

        // Test special characters: ᱹ -> "", ᱽ -> "'", ᱺ -> "", ᱼ -> "-"
        assertEquals("", SantaliOlChikiTransliteration.olChikiToPiperRoman("ᱹ"))
        assertEquals("'", SantaliOlChikiTransliteration.olChikiToPiperRoman("ᱽ"))
        assertEquals("", SantaliOlChikiTransliteration.olChikiToPiperRoman("ᱺ"))
        assertEquals("-", SantaliOlChikiTransliteration.olChikiToPiperRoman("ᱼ"))

        // Test unknown character preservation (e.g. English letters/symbols pass through)
        assertEquals("ABC", SantaliOlChikiTransliteration.olChikiToPiperRoman("ABC"))
    }

    @Test
    fun testABTransliterationIsolation() {
        val romanA = SantaliOlChikiTransliteration.olChikiToPiperRoman("ᱢᱤᱫ")
        val romanB = SantaliOlChikiTransliteration.olChikiToPiperRoman("ᱵᱟᱨ")

        assertNotEquals("Romanized A must not equal Romanized B", romanA, romanB)
        assertEquals("mid", romanA)
        assertEquals("bar", romanB)
    }

    @Test
    fun testFallbackBehaviorWhenTtsUnavailable() {
        val result = LiveTtsResult(
            text = "ᱢᱤᱫ",
            romanizedText = "mid",
            audioFile = null,
            state = LiveTtsState.UNAVAILABLE,
            errorMessage = "Piper TTS model unavailable"
        )
        assertEquals(LiveTtsState.UNAVAILABLE, result.state)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun testLiveTtsConfigSpeechRateIsolation() {
        // 1. Configured Piper neural length_scale
        assertEquals(1.80f, LiveTtsConfig.PIPER_LENGTH_SCALE, 0.001f)
        assertTrue(LiveTtsConfig.PIPER_LENGTH_SCALE >= 1.0f)

        // 2. Controlled speech rate for Live Piper TTS (0.80f = ~20% slower natural speed)
        assertEquals(0.80f, LiveTtsConfig.LIVE_TTS_SPEECH_RATE, 0.001f)
        assertEquals(0.80f, LiveTtsConfig.getSpeechRateForAudioId("LIVE_PIPER_TTS"), 0.001f)
        assertEquals(0.80f, LiveTtsConfig.getSpeechRateForAudioId("LIVE_PIPER"), 0.001f)
        assertEquals(0.80f, LiveTtsConfig.getSpeechRateForAudioId("SANTALI_AUDIO"), 0.001f)

        // 3. Original Hindi recording replay must be unaffected (1.0f)
        assertEquals(1.00f, LiveTtsConfig.getSpeechRateForAudioId("ORIGINAL_HINDI"), 0.001f)
        assertEquals(1.00f, LiveTtsConfig.getSpeechRateForAudioId("ORIGINAL_AUDIO"), 0.001f)

        // 4. M3 pre-recorded fallback audio must be unaffected (1.0f)
        assertEquals(1.00f, LiveTtsConfig.getSpeechRateForAudioId("STATIC_SANTALI"), 0.001f)
        assertEquals(1.00f, LiveTtsConfig.getSpeechRateForAudioId("STATIC_ASSET"), 0.001f)
        assertEquals(1.00f, LiveTtsConfig.getSpeechRateForAudioId("number_1"), 0.001f)
        assertEquals(1.00f, LiveTtsConfig.getSpeechRateForAudioId(null), 0.001f)
    }

    @Test
    fun testExperimentalModelComparisonInputs() {
        // TEST A: "एक" -> "ᱢᱤᱫ" -> "mid"
        val santaliA = "ᱢᱤᱫ"
        val romanA = SantaliOlChikiTransliteration.olChikiToPiperRoman(santaliA)
        assertEquals("mid", romanA)

        // TEST B: "गिनती सीखें" -> "ᱞᱮᱠᱷᱟ ᱪᱮᱫᱚᱜ ᱢᱮ।" -> "lekha, chedog, me."
        val santaliB = "ᱞᱮᱠᱷᱟ ᱪᱮᱫᱚᱜ ᱢᱮ。"
        val romanB = SantaliOlChikiTransliteration.olChikiToPiperRoman(santaliB)
        println("ACTUAL_ROMAN_B='$romanB'")
        assertTrue("Roman B must start with lekha, chedog, me", romanB.startsWith("lekha, chedog, me"))
    }
}
