package com.sih2026.palash.asr

import android.content.Context
import java.io.File

/**
 * State machine for ASR processing lifecycle.
 */
enum class AsrState {
    ASR_NOT_READY,
    ASR_LOADING,
    ASR_READY,
    ASR_LISTENING,
    ASR_PROCESSING,
    ASR_RESULT,
    ASR_ERROR
}

/**
 * Result returned by ASR Engine containing recognized text and timing metadata.
 */
data class AsrResult(
    val text: String,
    val isFinal: Boolean = true,
    val processingTimeMs: Long = 0L,
    val errorMessage: String? = null
)

/**
 * Clean abstraction interface for Speech-To-Text / ASR.
 */
interface AsrEngine {
    val currentState: AsrState
    val lastError: String?

    fun initialize(context: Context, modelName: String = "vosk-model-small-hi-0.22", onStateChanged: (AsrState) -> Unit)
    fun processAudio(pcmFile: File, onResult: (AsrResult) -> Unit)
    fun release()
}
