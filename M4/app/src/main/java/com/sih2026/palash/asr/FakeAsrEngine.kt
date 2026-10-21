package com.sih2026.palash.asr

import android.content.Context
import java.io.File

/**
 * Mock AsrEngine for JVM testing without native Android/Vosk dependencies.
 */
class FakeAsrEngine(
    var simulatedResultText: String = "नमस्ते बच्चों आज हम गणित पढ़ेंगे",
    var shouldFail: Boolean = false,
    var failureMessage: String = "Vosk model files are not present; integration cannot be runtime-verified yet."
) : AsrEngine {

    override var currentState: AsrState = AsrState.ASR_NOT_READY
        private set

    override var lastError: String? = null
        private set

    override fun initialize(
        context: Context,
        modelName: String,
        onStateChanged: (AsrState) -> Unit
    ) {
        currentState = AsrState.ASR_LOADING
        onStateChanged(currentState)

        if (shouldFail) {
            currentState = AsrState.ASR_ERROR
            lastError = failureMessage
            onStateChanged(currentState)
        } else {
            currentState = AsrState.ASR_READY
            lastError = null
            onStateChanged(currentState)
        }
    }

    override fun processAudio(pcmFile: File, onResult: (AsrResult) -> Unit) {
        if (shouldFail) {
            currentState = AsrState.ASR_ERROR
            onResult(AsrResult(text = "", isFinal = true, errorMessage = failureMessage))
            return
        }

        currentState = AsrState.ASR_PROCESSING
        val startTime = System.currentTimeMillis()

        // Simulate fast execution
        currentState = AsrState.ASR_RESULT
        onResult(
            AsrResult(
                text = simulatedResultText,
                isFinal = true,
                processingTimeMs = System.currentTimeMillis() - startTime + 150L
            )
        )
    }

    override fun release() {
        currentState = AsrState.ASR_NOT_READY
        lastError = null
    }
}
