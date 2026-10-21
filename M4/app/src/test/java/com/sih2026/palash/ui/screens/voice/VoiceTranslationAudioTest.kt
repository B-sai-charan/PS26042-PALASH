package com.sih2026.palash.ui.screens.voice

import com.sih2026.palash.audio.FakeAudioRecorder
import com.sih2026.palash.audio.RecordingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class VoiceTranslationAudioTest {

    private lateinit var fakeRecorder: FakeAudioRecorder
    private lateinit var tempFile: File

    @Before
    fun setUp() {
        fakeRecorder = FakeAudioRecorder()
        tempFile = File.createTempFile("test_rec", ".pcm")
    }

    @Test
    fun testInitialRecorderStateIsIdle() {
        assertEquals(RecordingState.IDLE, fakeRecorder.currentState)
        assertEquals(0L, fakeRecorder.currentDurationMs)
    }

    @Test
    fun testStartRecordingTransitionsToRecording() {
        fakeRecorder.startRecording(tempFile) { state, _ ->
            assertEquals(RecordingState.RECORDING, state)
        }
        assertEquals(RecordingState.RECORDING, fakeRecorder.currentState)
    }

    @Test
    fun testStopRecordingProducesResult() {
        fakeRecorder.startRecording(tempFile) { _, _ -> }
        fakeRecorder.stopRecording { result ->
            assertNotNull(result)
            assertEquals(16000, result?.sampleRate)
            assertEquals(1, result?.channels)
            assertEquals(16, result?.bitsPerSample)
        }
        assertEquals(RecordingState.RECORDED, fakeRecorder.currentState)
    }

    @Test
    fun testCancelRecordingResetsState() {
        fakeRecorder.startRecording(tempFile) { _, _ -> }
        fakeRecorder.cancelRecording()
        assertEquals(RecordingState.IDLE, fakeRecorder.currentState)
        assertEquals(0L, fakeRecorder.currentDurationMs)
    }

    @Test
    fun testUiStateDefaultsNoFakeTranscription() {
        val state = VoiceTranslationUiState()
        assertEquals(RecordingState.IDLE, state.recordingState)
        assertFalse(state.isTranslating)
        assertEquals("माइक दबाएं और हिंदी में बोलें", state.translationNotice)
        assertNull(state.recordedFilePath)
    }
}
