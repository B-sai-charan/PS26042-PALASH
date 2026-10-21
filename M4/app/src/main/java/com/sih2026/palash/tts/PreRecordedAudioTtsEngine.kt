package com.sih2026.palash.tts

import com.sih2026.palash.audio.AudioFileValidator
import com.sih2026.palash.data.local.dao.AudioMetadataDao
import com.sih2026.palash.data.local.entity.AudioMetadataEntity
import java.io.File

/**
 * TTS Engine implementation backed by native educator audio packs.
 * Performs lookup of verified AudioMetadataEntity records in Room database
 * and validates physical file existence, checksums, and verification status.
 * If audio is unverified or missing, safely returns TtsState.UNAVAILABLE without error.
 */
class PreRecordedAudioTtsEngine(
    private val audioDao: AudioMetadataDao?
) : TtsEngine {

    override suspend fun synthesize(
        text: String,
        language: String,
        script: String
    ): TtsResult {
        val startTime = System.currentTimeMillis()
        val dao = audioDao

        if (dao == null || text.isBlank()) {
            return TtsResult(
                text = text,
                language = language,
                script = script,
                audioFile = null,
                durationMs = 0L,
                processingTimeMs = System.currentTimeMillis() - startTime,
                state = TtsState.UNAVAILABLE,
                errorMessage = "Audio playback unavailable"
            )
        }

        try {
            val audioMetadata: AudioMetadataEntity? = dao.findAudioForTranslation(text)
            val isValid = AudioFileValidator.isValidAudioFile(audioMetadata, text)
            val elapsedTime = System.currentTimeMillis() - startTime

            if (isValid && audioMetadata?.audioPath != null) {
                val file = File(audioMetadata.audioPath)
                return TtsResult(
                    text = text,
                    language = language,
                    script = script,
                    audioFile = file,
                    durationMs = audioMetadata.durationMs,
                    processingTimeMs = elapsedTime,
                    state = TtsState.READY
                )
            } else {
                return TtsResult(
                    text = text,
                    language = language,
                    script = script,
                    audioFile = null,
                    durationMs = 0L,
                    processingTimeMs = elapsedTime,
                    state = TtsState.UNAVAILABLE,
                    errorMessage = "Verified audio recording not available"
                )
            }
        } catch (e: Exception) {
            return TtsResult(
                text = text,
                language = language,
                script = script,
                audioFile = null,
                durationMs = 0L,
                processingTimeMs = System.currentTimeMillis() - startTime,
                state = TtsState.ERROR,
                errorMessage = "Audio lookup error: ${e.localizedMessage}"
            )
        }
    }

    override fun release() {
        // No native C++ resources to clean up for pre-recorded audio packs
    }
}
