package com.sih2026.palash.tts

import java.io.File

/**
 * Speech synthesis state lifecycle enum.
 */
enum class TtsState {
    READY,
    LOADING,
    SYNTHESIZING,
    COMPLETED,
    UNAVAILABLE,
    ERROR
}

/**
 * Result returned by speech synthesis engine.
 */
data class TtsResult(
    val text: String,
    val language: String = "sat",
    val script: String = "Ol Chiki",
    val audioFile: File? = null,
    val durationMs: Long = 0L,
    val processingTimeMs: Long = 0L,
    val state: TtsState,
    val errorMessage: String? = null
)

/**
 * Interface contract for vernacular Text-To-Speech / Pre-recorded audio playback.
 */
interface TtsEngine {
    suspend fun synthesize(
        text: String,
        language: String = "sat",
        script: String = "Ol Chiki"
    ): TtsResult

    fun release()
}
