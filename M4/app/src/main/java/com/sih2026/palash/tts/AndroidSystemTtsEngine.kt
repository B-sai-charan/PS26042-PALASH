package com.sih2026.palash.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.util.Locale
import java.util.UUID

/**
 * Experimental Candidate TTS Engine utilizing Android OS Native TextToSpeech framework.
 * Synthesizes exact Romanized Santali text to local WAV files offline on device.
 */
class AndroidSystemTtsEngine(
    private val context: Context
) : TtsEngine {

    companion object {
        private const val TAG = "AndroidSystemTtsEngine"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var initErrorMessage: String? = null
    private val initDeferred = CompletableDeferred<Boolean>()

    init {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val res = tts?.setLanguage(Locale("en", "IN"))
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.language = Locale.ENGLISH
                    }
                    isInitialized = true
                    initDeferred.complete(true)
                    Log.i(TAG, "Android System TTS initialized successfully.")
                } else {
                    initErrorMessage = "Android System TTS init failed with status: $status"
                    isInitialized = false
                    initDeferred.complete(false)
                    Log.e(TAG, initErrorMessage!!)
                }
            }
        } catch (e: Exception) {
            initErrorMessage = "Error instantiating TextToSpeech: ${e.message}"
            isInitialized = false
            initDeferred.complete(false)
        }
    }

    suspend fun synthesizeCandidate(santaliText: String): LiveTtsResult {
        val start = System.currentTimeMillis()

        if (!isInitialized) {
            initDeferred.await()
        }

        if (!isInitialized || tts == null) {
            return LiveTtsResult(
                text = santaliText,
                romanizedText = "",
                state = LiveTtsState.UNAVAILABLE,
                errorMessage = initErrorMessage ?: "System TTS unavailable",
                totalTtsTimeMs = System.currentTimeMillis() - start
            )
        }

        val romanText = SantaliOlChikiTransliteration.olChikiToPiperRoman(santaliText)
        try {
            Log.d(TAG, "INPUT_OL_CHIKI=$santaliText")
            Log.d(TAG, "TTS_ROMAN_INPUT=$romanText")
        } catch (_: Throwable) {
            println("INPUT_OL_CHIKI=$santaliText")
            println("TTS_ROMAN_INPUT=$romanText")
        }

        val outputFile = File(context.cacheDir, "system_tts_candidate_${UUID.randomUUID()}.wav")
        val utteranceId = "system_tts_${UUID.randomUUID()}"
        val synthesisDeferred = CompletableDeferred<Boolean>()

        val params = Bundle()

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(uttId: String?) {}
            override fun onDone(uttId: String?) {
                if (uttId == utteranceId) synthesisDeferred.complete(true)
            }
            @Deprecated("Deprecated in Java")
            override fun onError(uttId: String?) {
                if (uttId == utteranceId) synthesisDeferred.complete(false)
            }
            override fun onError(uttId: String?, errorCode: Int) {
                if (uttId == utteranceId) synthesisDeferred.complete(false)
            }
        })

        val infStart = System.currentTimeMillis()
        val result = tts?.synthesizeToFile(romanText, params, outputFile, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            return LiveTtsResult(
                text = santaliText,
                romanizedText = romanText,
                state = LiveTtsState.ERROR,
                errorMessage = "synthesizeToFile call failed with error code $result",
                totalTtsTimeMs = System.currentTimeMillis() - start
            )
        }

        val success = synthesisDeferred.await()
        val infTime = System.currentTimeMillis() - infStart
        val totalTime = System.currentTimeMillis() - start

        if (success && outputFile.exists() && outputFile.length() > 0) {
            return LiveTtsResult(
                text = santaliText,
                romanizedText = romanText,
                audioFile = outputFile,
                durationMs = 0L,
                inferenceTimeMs = infTime,
                totalTtsTimeMs = totalTime,
                state = LiveTtsState.READY
            )
        } else {
            return LiveTtsResult(
                text = santaliText,
                romanizedText = romanText,
                state = LiveTtsState.ERROR,
                errorMessage = "System TTS failed to output valid audio file",
                totalTtsTimeMs = totalTime
            )
        }
    }

    override suspend fun synthesize(text: String, language: String, script: String): TtsResult {
        val candResult = synthesizeCandidate(text)
        return TtsResult(
            text = text,
            language = language,
            script = script,
            audioFile = candResult.audioFile,
            durationMs = candResult.durationMs,
            processingTimeMs = candResult.totalTtsTimeMs,
            state = when (candResult.state) {
                LiveTtsState.READY -> TtsState.READY
                else -> TtsState.UNAVAILABLE
            },
            errorMessage = candResult.errorMessage
        )
    }

    override fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down System TTS: ${e.message}")
        }
    }
}
