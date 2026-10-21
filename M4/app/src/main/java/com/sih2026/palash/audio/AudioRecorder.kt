package com.sih2026.palash.audio

import java.io.File

enum class RecordingState {
    IDLE,
    RECORDING,
    STOPPING,
    RECORDED,
    ERROR
}

data class AudioRecordingResult(
    val audioFile: File,
    val durationMs: Long,
    val sampleRate: Int = 16000,
    val channels: Int = 1,
    val bitsPerSample: Int = 16
)

interface AudioRecorder {
    val currentState: RecordingState
    val currentDurationMs: Long

    fun startRecording(outputFile: File, onStateChanged: (RecordingState, String?) -> Unit)
    fun stopRecording(onCompleted: (AudioRecordingResult?) -> Unit)
    fun cancelRecording()
    fun release()
}
