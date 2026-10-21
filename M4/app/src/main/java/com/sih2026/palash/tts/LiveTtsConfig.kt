package com.sih2026.palash.tts

/**
 * Centralized configuration for Live Piper TTS rendering and audio playback speed.
 */
object LiveTtsConfig {
    /**
     * Controlled speech rate multiplier for Live Piper TTS.
     * Approximately 15-20% slower than normal speed for clear, natural classroom listening.
     * Value: 0.80f (tunable between 0.75f and 1.00f).
     */
    const val LIVE_TTS_SPEECH_RATE: Float = 0.80f

    /**
     * Piper ONNX neural duration scale multiplier (length_scale).
     * Controls phoneme duration natively inside ONNX synthesis with 100% pitch preservation and zero DSP artifacts.
     * Calibrated: 1.80f (~0.56f baseline rate).
     */
    const val PIPER_LENGTH_SCALE: Float = 1.80f

    /**
     * Determines whether controlled speech-rate adjustment should apply to the specified audio stream ID.
     * Returns LIVE_TTS_SPEECH_RATE for Live Piper TTS streams and 1.0f for original Hindi / pre-recorded M3 assets.
     */
    fun getSpeechRateForAudioId(audioId: String?): Float {
        return if (audioId == "LIVE_PIPER_TTS" || audioId == "LIVE_PIPER" || audioId == "SANTALI_AUDIO") {
            LIVE_TTS_SPEECH_RATE
        } else {
            1.0f
        }
    }
}
