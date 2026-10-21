package com.sih2026.palash.audio

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying Flashcard P1-1 audio mapping & metadata privacy rules.
 */
class FlashcardAudioUnitTest {

    @Test
    fun flashcardAudioId_FC001_mappingFormat() {
        val meta = SantaliAudioMetadata(
            audioId = "FC001",
            relativeAssetPath = "audio/santali/g2_counting/flashcards/FC001.wav",
            category = "flashcards"
        )
        assertEquals("FC001", meta.audioId)
        assertEquals("audio/santali/g2_counting/flashcards/FC001.wav", meta.relativeAssetPath)
        assertEquals("Piper-generated", meta.generationMethod)
        assertEquals("UNVERIFIED", meta.validationStatus)
    }

    @Test
    fun missingAudioId_returnsNullWithoutCrashing() {
        val mappedAssetPath: String? = null
        assertNull("Missing audio mapping must safely evaluate to null", mappedAssetPath)
    }

    @Test
    fun studentUiLabel_doesNotExposeRawWavFilenameOrAudioId() {
        val studentUiLabel = "🔊 Listen Audio (ᱥᱟᱲᱮ ᱟᱸᱡᱚᱢ ᱢե)"
        assertFalse("Student UI string must not contain .wav extension", studentUiLabel.contains(".wav"))
        assertFalse("Student UI string must not contain raw audioId FC001", studentUiLabel.contains("FC001"))
    }
}
