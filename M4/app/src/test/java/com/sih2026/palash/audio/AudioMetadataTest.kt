package com.sih2026.palash.audio

import com.sih2026.palash.data.local.entity.AudioMetadataEntity
import com.sih2026.palash.data.local.pack.DemoContentPackFactory
import com.sih2026.palash.translation.ValidationStatus
import com.sih2026.palash.tts.PreRecordedAudioTtsEngine
import com.sih2026.palash.tts.TtsState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests verifying Prompt 19 Native Audio Data Architecture & Security Rules:
 * 1. Missing audio returns unavailable.
 * 2. UNVERIFIED audio cannot be played.
 * 3. REJECTED audio cannot be played.
 * 4. TEACHER_VERIFIED audio with valid metadata can be selected.
 * 5. OFFICIAL_SOURCE audio with valid metadata can be selected.
 * 6. Invalid/mismatched translationId cannot be selected.
 * 7. Invalid checksum is rejected.
 * 8. Six production template records have no audio attached.
 * 9. Existing tests continue to pass.
 */
class AudioMetadataTest {

    private lateinit var tempAudioFile: File
    private lateinit var validSha: String

    @Before
    fun setUp() {
        tempAudioFile = File.createTempFile("test_fixture", ".wav")
        tempAudioFile.writeBytes(byteArrayOf(0x52, 0x49, 0x46, 0x46, 0x24, 0x00, 0x00, 0x00)) // 8 synthetic test bytes
        validSha = AudioFileValidator.calculateSha256(tempAudioFile)
    }

    @Test
    fun testMissingAudio_returnsUnavailable() = runBlocking {
        val isValid = AudioFileValidator.isValidAudioFile(null, "trans_1")
        assertFalse(isValid)

        val engine = PreRecordedAudioTtsEngine(null)
        val res = engine.synthesize("trans_1")
        assertEquals(TtsState.UNAVAILABLE, res.state)
        assertNull(res.audioFile)
    }

    @Test
    fun testUnverifiedAudio_cannotBePlayed() {
        val metadata = AudioMetadataEntity(
            id = "audio_1",
            translationId = "trans_1",
            audioPath = tempAudioFile.absolutePath,
            validationStatus = ValidationStatus.UNVERIFIED.name,
            sha256 = validSha
        )
        assertFalse(AudioFileValidator.isValidAudioFile(metadata, "trans_1"))
    }

    @Test
    fun testRejectedAudio_cannotBePlayed() {
        val metadata = AudioMetadataEntity(
            id = "audio_1",
            translationId = "trans_1",
            audioPath = tempAudioFile.absolutePath,
            validationStatus = ValidationStatus.REJECTED.name,
            sha256 = validSha
        )
        assertFalse(AudioFileValidator.isValidAudioFile(metadata, "trans_1"))
    }

    @Test
    fun testTeacherVerifiedAudio_canBeSelected() {
        val metadata = AudioMetadataEntity(
            id = "audio_1",
            translationId = "trans_1",
            audioPath = tempAudioFile.absolutePath,
            validationStatus = ValidationStatus.TEACHER_VERIFIED.name,
            validatorType = "NATIVE_TEACHER",
            validatorNameOrId = "T_JH_042",
            sha256 = validSha
        )
        assertTrue(AudioFileValidator.isValidAudioFile(metadata, "trans_1"))
    }

    @Test
    fun testOfficialSourceAudio_canBeSelected() {
        val metadata = AudioMetadataEntity(
            id = "audio_1",
            translationId = "trans_1",
            audioPath = tempAudioFile.absolutePath,
            validationStatus = ValidationStatus.OFFICIAL_SOURCE.name,
            sourceReference = "JCERT FLN Audio Guide",
            sha256 = validSha
        )
        assertTrue(AudioFileValidator.isValidAudioFile(metadata, "trans_1"))
    }

    @Test
    fun testMismatchedTranslationId_isRejected() {
        val metadata = AudioMetadataEntity(
            id = "audio_1",
            translationId = "trans_1",
            audioPath = tempAudioFile.absolutePath,
            validationStatus = ValidationStatus.TEACHER_VERIFIED.name,
            sha256 = validSha
        )
        assertFalse(AudioFileValidator.isValidAudioFile(metadata, "trans_DIFFERENT_99"))
    }

    @Test
    fun testInvalidChecksum_isRejected() {
        val metadata = AudioMetadataEntity(
            id = "audio_1",
            translationId = "trans_1",
            audioPath = tempAudioFile.absolutePath,
            validationStatus = ValidationStatus.TEACHER_VERIFIED.name,
            sha256 = "INVALID_CORRUPTED_SHA_256_STRING"
        )
        assertFalse(AudioFileValidator.isValidAudioFile(metadata, "trans_1"))
    }

    @Test
    fun testProductionTemplateRecords_haveNoAudioAttached() {
        val pack = DemoContentPackFactory.createSantaliDemoPack()
        assertTrue("Production demo pack audios list must be empty", pack.audios.isEmpty())
        assertTrue("Production demo pack translations must contain curriculum entries", pack.translations.size >= 28)
    }
}
