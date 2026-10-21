package com.sih2026.palash.ui.screens.result

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.sih2026.palash.audio.AndroidAudioPlaybackEngineImpl
import com.sih2026.palash.audio.AndroidPcmAudioPlayerImpl
import com.sih2026.palash.audio.AudioPlaybackState
import com.sih2026.palash.audio.PcmAudioPlayer
import com.sih2026.palash.audio.PcmPlaybackState
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import com.sih2026.palash.data.demo.DemoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

data class TranslationResultUiState(
    val hindiText: String = DemoData.sampleHindiInput,
    val santaliNotice: String = DemoData.sampleSantaliOutputNotice,
    val liveAudioPath: String? = null,
    val originalAudioPath: String? = null,
    val confidenceLabel: String = "Verified",
    val latencyLabel: String = "Realtime",
    val snackbarMessage: String? = null,
    val isPlayingAudio: Boolean = false,
    val isPlayingOriginalAudio: Boolean = false,
    val activeAudioSource: String = "NONE" // LIVE_PIPER, STATIC_ASSET, ORIGINAL_HINDI, NONE
)

class TranslationResultViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "PALASH_RESULT_VM"
    }

    private val _uiState = MutableStateFlow(TranslationResultUiState())
    val uiState: StateFlow<TranslationResultUiState> = _uiState.asStateFlow()

    private val audioRepo = SantaliAudioRepositoryImpl(application)
    private val playbackEngine = AndroidAudioPlaybackEngineImpl(application)
    private val pcmAudioPlayer: PcmAudioPlayer = AndroidPcmAudioPlayerImpl()
    private val livePiperTtsEngine = com.sih2026.palash.voice.VoiceModelManager.getPiperEngine(application)

    fun setTranslation(
        hindi: String,
        santaliNotice: String,
        liveAudioPath: String? = null,
        originalAudioPath: String? = null
    ) {
        _uiState.update {
            it.copy(
                hindiText = if (hindi.isNotBlank()) hindi else DemoData.sampleHindiInput,
                santaliNotice = if (santaliNotice.isNotBlank()) santaliNotice else DemoData.sampleSantaliOutputNotice,
                liveAudioPath = liveAudioPath?.takeIf { p -> p.isNotBlank() },
                originalAudioPath = originalAudioPath?.takeIf { p -> p.isNotBlank() }
            )
        }
        Log.i(TAG, "setTranslation loaded: liveAudioPath='$liveAudioPath', originalAudioPath='$originalAudioPath'")
        playAudioClicked()
    }

    fun playAudioClicked() {
        val state = _uiState.value
        val livePath = state.liveAudioPath
        val currentSantali = state.santaliNotice
        val currentHindi = state.hindiText

        // Stop any active original Hindi audio state
        _uiState.update { it.copy(isPlayingOriginalAudio = false) }

        // PRIORITY 1: EXISTING LIVE PIPER AUDIO FILE
        if (!livePath.isNullOrBlank()) {
            val liveFile = File(livePath)
            if (liveFile.exists() && liveFile.length() > 0) {
                Log.i(TAG, "Audio source selected: LIVE_PIPER, File: '${liveFile.name}', Path: '$livePath', Size: ${liveFile.length()} bytes")
                _uiState.update { it.copy(isPlayingAudio = true, activeAudioSource = "LIVE_PIPER", snackbarMessage = null) }

                playbackEngine.playAsset(livePath, audioId = "SANTALI_AUDIO") { playbackState, errorMsg ->
                    _uiState.update {
                        it.copy(
                            isPlayingAudio = (playbackState == AudioPlaybackState.PLAYING),
                            snackbarMessage = if (playbackState == AudioPlaybackState.ERROR) (errorMsg ?: "Audio playback error") else it.snackbarMessage
                        )
                    }
                }
                return
            } else {
                Log.w(TAG, "LIVE AUDIO FILE MISSING at path: '$livePath'")
            }
        }

        // PRIORITY 1B: ON-DEMAND PIPER TTS SYNTHESIS FOR VALID SANTALI TEXT
        val isUnavailableNotice = currentSantali.contains("उपलब्ध नहीं है", ignoreCase = true) ||
                currentSantali.contains("unavailable", ignoreCase = true)
        if (currentSantali.isNotBlank() && !isUnavailableNotice) {
            viewModelScope.launch {
                Log.i(TAG, "Synthesizing live Piper TTS on demand for: '$currentSantali'")
                val liveResult = livePiperTtsEngine.synthesizeLive(currentSantali)
                if (liveResult.state == com.sih2026.palash.tts.LiveTtsState.READY && liveResult.audioFile != null && liveResult.audioFile.exists()) {
                    val newPath = liveResult.audioFile.absolutePath
                    _uiState.update { it.copy(liveAudioPath = newPath, isPlayingAudio = true, activeAudioSource = "LIVE_PIPER", snackbarMessage = null) }
                    playbackEngine.playAsset(newPath, audioId = "SANTALI_AUDIO") { playbackState, errorMsg ->
                        _uiState.update {
                            it.copy(
                                isPlayingAudio = (playbackState == AudioPlaybackState.PLAYING),
                                snackbarMessage = if (playbackState == AudioPlaybackState.ERROR) (errorMsg ?: "Audio playback error") else it.snackbarMessage
                            )
                        }
                    }
                    return@launch
                }

                // PRIORITY 2 FALLBACK IF LIVE SYNTHESIS FAILS
                playFallbackAudio(currentHindi, currentSantali)
            }
            return
        }

        // PRIORITY 2: APPROVED PRERECORDED M3 AUDIO FALLBACK
        playFallbackAudio(currentHindi, currentSantali)
    }

    private fun playFallbackAudio(currentHindi: String, currentSantali: String) {
        val resolvedAudioRef = resolveAudioKey(currentHindi, currentSantali)
        val assetPath = if (resolvedAudioRef != null) audioRepo.getAssetPathForAudio(resolvedAudioRef) else null

        if (assetPath != null) {
            Log.i(TAG, "Audio source selected: STATIC_ASSET, Ref: '$resolvedAudioRef', AssetPath: '$assetPath'")
            _uiState.update { it.copy(isPlayingAudio = true, activeAudioSource = "STATIC_ASSET", snackbarMessage = null) }
            playbackEngine.playAsset(assetPath, audioId = "SANTALI_AUDIO") { playbackState, errorMsg ->
                _uiState.update {
                    it.copy(
                        isPlayingAudio = (playbackState == AudioPlaybackState.PLAYING),
                        snackbarMessage = if (playbackState == AudioPlaybackState.ERROR) (errorMsg ?: "Audio playback error") else it.snackbarMessage
                    )
                }
            }
            return
        }

        // PRIORITY 3: AUDIO UNAVAILABLE
        Log.w(TAG, "Audio source selected: NONE (Neither Live Piper WAV nor static asset available for '$currentHindi')")
        _uiState.update {
            it.copy(
                activeAudioSource = "NONE",
                snackbarMessage = "ध्वनि उपलब्ध नहीं है"
            )
        }
    }

    fun playOriginalAudioClicked() {
        val origPath = _uiState.value.originalAudioPath
        if (origPath.isNullOrBlank()) {
            _uiState.update { it.copy(snackbarMessage = "मूल रिकॉर्डिंग उपलब्ध नहीं है") }
            return
        }
        val origFile = File(origPath)
        if (!origFile.exists() || origFile.length() == 0L) {
            _uiState.update { it.copy(snackbarMessage = "मूल रिकॉर्डिंग फ़ाइल missing") }
            return
        }

        // Stop any active Santali audio state
        _uiState.update { it.copy(isPlayingAudio = false) }

        Log.i(TAG, "Audio source selected: ORIGINAL_HINDI, File: '${origFile.name}', Size: ${origFile.length()} bytes")
        _uiState.update { it.copy(isPlayingOriginalAudio = true, activeAudioSource = "ORIGINAL_HINDI") }

        if (origPath.endsWith(".pcm", ignoreCase = true)) {
            pcmAudioPlayer.playPcmFileWithId(origFile, audioId = "ORIGINAL_HINDI", sampleRate = 16000) { state, errorMsg ->
                _uiState.update {
                    it.copy(
                        isPlayingOriginalAudio = (state == PcmPlaybackState.PLAYING),
                        snackbarMessage = if (state == PcmPlaybackState.ERROR) (errorMsg ?: "Original audio playback error") else it.snackbarMessage
                    )
                }
            }
        } else {
            playbackEngine.playAsset(origPath, audioId = "ORIGINAL_HINDI") { state, errorMsg ->
                _uiState.update {
                    it.copy(
                        isPlayingOriginalAudio = (state == AudioPlaybackState.PLAYING),
                        snackbarMessage = if (state == AudioPlaybackState.ERROR) (errorMsg ?: "Original audio playback error") else it.snackbarMessage
                    )
                }
            }
        }
    }

    private fun resolveAudioKey(hindi: String, santali: String): String? {
        val cleanSantali = santali.trim()
        val cleanHindi = hindi.trim()

        if (audioRepo.hasAudioAsset(cleanSantali)) return cleanSantali

        val audioMap = mapOf(
            "पाँच आम गिनो।" to "AS001",
            "पाँच आम गिनो" to "AS001",
            "1" to "number_1", "एक" to "number_1", "ᱢᱤᱫ" to "number_1",
            "2" to "number_2", "दो" to "number_2", "ᱵᱟᱨ" to "number_2",
            "3" to "number_3", "तीन" to "number_3", "ᱯᱮ" to "number_3",
            "4" to "number_4", "चार" to "number_4", "ᱯᱩᱱ" to "number_4",
            "5" to "number_5", "पाँच" to "number_5", "ᱢᱚᱬᱮ" to "number_5",
            "6" to "number_6", "छह" to "number_6", "ᱛᱩᱨᱩᱭ" to "number_6"
        )

        return audioMap[cleanHindi] ?: audioMap[cleanSantali]
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        playbackEngine.release()
        pcmAudioPlayer.release()
    }
}
