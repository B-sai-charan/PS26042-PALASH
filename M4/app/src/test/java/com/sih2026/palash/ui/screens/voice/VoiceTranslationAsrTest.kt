package com.sih2026.palash.ui.screens.voice

import com.sih2026.palash.asr.AsrState
import com.sih2026.palash.asr.FakeAsrEngine
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

class VoiceTranslationAsrTest {

    private lateinit var fakeRecorder: FakeAudioRecorder
    private lateinit var fakeAsrEngine: FakeAsrEngine
    private lateinit var tempFile: File

    @Before
    fun setUp() {
        fakeRecorder = FakeAudioRecorder()
        fakeAsrEngine = FakeAsrEngine()
        tempFile = File.createTempFile("test_asr", ".pcm")
    }

    @Test
    fun testInitialAsrStateIsNotReady() {
        val asr = FakeAsrEngine(shouldFail = false)
        assertEquals(AsrState.ASR_NOT_READY, asr.currentState)
        assertNull(asr.lastError)
    }

    @Test
    fun testFakeAsrEngineProcessAudioReturnsResult() {
        fakeAsrEngine.processAudio(tempFile) { result ->
            assertNotNull(result)
            assertEquals("नमस्ते बच्चों आज हम गणित पढ़ेंगे", result.text)
            assertTrue(result.isFinal)
            assertTrue(result.processingTimeMs >= 0L)
            assertNull(result.errorMessage)
        }
        assertEquals(AsrState.ASR_RESULT, fakeAsrEngine.currentState)
    }

    @Test
    fun testAsrEngineFailureWhenModelMissing() {
        val failingAsr = FakeAsrEngine(shouldFail = true)
        failingAsr.processAudio(tempFile) { result ->
            assertNotNull(result)
            assertEquals("", result.text)
            assertNotNull(result.errorMessage)
            assertTrue(result.errorMessage!!.contains("Vosk model files are not present"))
        }
        assertEquals(AsrState.ASR_ERROR, failingAsr.currentState)
    }

    @Test
    fun testUiStateDefaultsNoFakeTranslation() {
        val state = VoiceTranslationUiState()
        assertEquals(RecordingState.IDLE, state.recordingState)
        assertEquals(AsrState.ASR_NOT_READY, state.asrState)
        assertNull(state.recognizedHindiText)
        assertFalse(state.isTranslating)
        assertEquals("माइक दबाएं और हिंदी में बोलें", state.translationNotice)
    }

    @Test
    fun testViewModelFactoryCanInstantiateViewModel() {
        val fakeRepo = object : com.sih2026.palash.data.repository.TranslationRepository {
            override suspend fun getTranslation(sourceText: String, sourceLang: String, targetLang: String) = null
            override fun getAllTranslations() = kotlinx.coroutines.flow.flowOf(emptyList<com.sih2026.palash.data.local.entity.TranslationCacheEntity>())
            override suspend fun saveTranslation(translation: com.sih2026.palash.data.local.entity.TranslationCacheEntity) {}
            override suspend fun saveTranslations(translations: List<com.sih2026.palash.data.local.entity.TranslationCacheEntity>) {}
            override suspend fun translateHindiToSantali(text: String) = com.sih2026.palash.data.network.NetworkResult.NetworkUnavailable()
        }
        val factory = VoiceTranslationViewModelFactory(
            application = android.app.Application(),
            audioRecorder = fakeRecorder,
            asrEngine = fakeAsrEngine,
            translationRepository = fakeRepo
        )
        assertNotNull(factory)
    }

    @Test
    fun testRepeatedAsrExecution_secondAndThirdAttemptsSucceed() {
        val asr = FakeAsrEngine(shouldFail = false)

        // Attempt 1
        asr.processAudio(tempFile) { res1 ->
            assertEquals("नमस्ते बच्चों आज हम गणित पढ़ेंगे", res1.text)
        }
        assertEquals(AsrState.ASR_RESULT, asr.currentState)

        // Attempt 2
        asr.simulatedResultText = "पाँच आम गिनो"
        asr.processAudio(tempFile) { res2 ->
            assertEquals("पाँच आम गिनो", res2.text)
        }
        assertEquals(AsrState.ASR_RESULT, asr.currentState)

        // Attempt 3
        asr.simulatedResultText = "दो सेब जोड़ो"
        asr.processAudio(tempFile) { res3 ->
            assertEquals("दो सेब जोड़ो", res3.text)
        }
        assertEquals(AsrState.ASR_RESULT, asr.currentState)
    }

    @Test
    fun testAsrErrorRecovery_recoversOnNextAttempt() {
        val asr = FakeAsrEngine(shouldFail = true)

        // Attempt 1 fails
        asr.processAudio(tempFile) { res1 ->
            assertNotNull(res1.errorMessage)
        }
        assertEquals(AsrState.ASR_ERROR, asr.currentState)

        // Reset fail flag and retry
        asr.shouldFail = false
        asr.simulatedResultText = "फिर से प्रयास करें"
        asr.processAudio(tempFile) { res2 ->
            assertEquals("फिर से प्रयास करें", res2.text)
            assertNull(res2.errorMessage)
        }
        assertEquals(AsrState.ASR_RESULT, asr.currentState)
    }
}
