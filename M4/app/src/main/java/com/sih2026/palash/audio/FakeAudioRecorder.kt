package com.sih2026.palash.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class FakeAudioRecorder : AudioRecorder {

    @Volatile
    override var currentState: RecordingState = RecordingState.IDLE

    @Volatile
    override var currentDurationMs: Long = 0L

    private var targetFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun startRecording(
        outputFile: File,
        onStateChanged: (RecordingState, String?) -> Unit
    ) {
        targetFile = outputFile
        outputFile.parentFile?.mkdirs()
        if (!outputFile.exists()) {
            outputFile.writeText("fake-pcm-header-data")
        }

        currentState = RecordingState.RECORDING
        currentDurationMs = 0L
        onStateChanged(RecordingState.RECORDING, null)

        timerJob = scope.launch {
            while (isActive && currentState == RecordingState.RECORDING) {
                delay(100)
                currentDurationMs += 100
                if (currentDurationMs >= 10000L) {
                    stopRecording { }
                    break
                }
            }
        }
    }

    override fun stopRecording(onCompleted: (AudioRecordingResult?) -> Unit) {
        if (currentState != RecordingState.RECORDING) {
            onCompleted(null)
            return
        }

        currentState = RecordingState.STOPPING
        timerJob?.cancel()
        currentState = RecordingState.RECORDED

        val file = targetFile ?: File.createTempFile("fake_rec", ".pcm")
        val result = AudioRecordingResult(
            audioFile = file,
            durationMs = currentDurationMs.coerceAtLeast(1000L),
            sampleRate = 16000,
            channels = 1,
            bitsPerSample = 16
        )
        onCompleted(result)
    }

    override fun cancelRecording() {
        timerJob?.cancel()
        targetFile?.delete()
        targetFile = null
        currentDurationMs = 0L
        currentState = RecordingState.IDLE
    }

    override fun release() {
        cancelRecording()
    }
}
