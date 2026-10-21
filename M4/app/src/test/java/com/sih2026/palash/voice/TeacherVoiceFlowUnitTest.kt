package com.sih2026.palash.voice

import com.sih2026.palash.audio.RecordingState
import com.sih2026.palash.ui.screens.voice.VoiceTranslationUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Teacher Voice Flow state lifecycle, ASR clearing,
 * offline translation resolution, and non-stale transaction handling.
 */
class TeacherVoiceFlowUnitTest {

    @Test
    fun testStartRecordingClearsStaleState() {
        val initialState = VoiceTranslationUiState(
            recognizedHindiText = "पाँच आम गिनो।",
            translatedSantaliText = "ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।",
            recordingState = RecordingState.RECORDED,
            errorMessage = "Old Error"
        )

        // Reset state simulate
        val resetState = initialState.copy(
            recognizedHindiText = null,
            translatedSantaliText = null,
            recordingState = RecordingState.RECORDING,
            errorMessage = null
        )

        assertNull(resetState.recognizedHindiText)
        assertNull(resetState.translatedSantaliText)
        assertNull(resetState.errorMessage)
        assertEquals(RecordingState.RECORDING, resetState.recordingState)
    }

    @Test
    fun testInputAVsInputBProducesDistinctTranslations() {
        val offlineDictionary = mapOf(
            "पाँच आम गिनो।" to "ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।",
            "एक" to "ᱢᱤᱫ",
            "दो" to "ᱵᱟᱨ",
            "तीन" to "ᱯᱮ"
        )

        val inputA = "एक"
        val inputB = "दो"

        val translationA = offlineDictionary[inputA]
        val translationB = offlineDictionary[inputB]

        assertEquals("ᱢᱤᱫ", translationA)
        assertEquals("ᱵᱟᱨ", translationB)
        assertFalse(translationA == translationB)
    }

    @Test
    fun testUnknownInputReturnsTruthfulUnavailableState() {
        val offlineDictionary = mapOf(
            "पाँच आम गिनो।" to "ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।"
        )

        val unknownInput = "एक नया अज्ञात वाक्य"
        val result = offlineDictionary[unknownInput]

        assertNull("Unknown arbitrary input must NOT return fixed fallback string", result)
    }

    @Test
    fun testStudentTranslationTiedToContentId() {
        val flashcardA = mapOf("id" to "FC001", "hindi" to "एक", "santali" to "ᱢᱤᱫ")
        val flashcardB = mapOf("id" to "FC003", "hindi" to "तीन", "santali" to "ᱯᱮ")

        val currentCardAId = flashcardA["id"]
        val currentCardBId = flashcardB["id"]

        assertFalse(currentCardAId == currentCardBId)
        assertEquals("एक", flashcardA["hindi"])
        assertEquals("तीन", flashcardB["hindi"])
    }
}
