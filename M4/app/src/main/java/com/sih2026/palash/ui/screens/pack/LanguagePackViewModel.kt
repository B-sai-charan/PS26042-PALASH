package com.sih2026.palash.ui.screens.pack

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.palash.audio.AndroidAudioPlaybackEngineImpl
import com.sih2026.palash.audio.AudioPlaybackEngine
import com.sih2026.palash.audio.AudioPlaybackState
import com.sih2026.palash.audio.SantaliAudioRepository
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.local.entity.LanguagePackMetadataEntity
import com.sih2026.palash.data.local.pack.ContentPackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LanguagePackUiState(
    val packs: List<LanguagePackMetadataEntity> = emptyList(),
    val isOperating: Boolean = false,
    val statusMessage: String? = null,
    val playbackState: AudioPlaybackState = AudioPlaybackState.IDLE,
    val audioNotice: String = "Unverified audio asset",
    val testAudioFilename: String = "number_1.wav"
)

class LanguagePackViewModel @JvmOverloads constructor(
    application: Application,
    private val audioRepository: SantaliAudioRepository = SantaliAudioRepositoryImpl(application),
    private val playbackEngine: AudioPlaybackEngine = AndroidAudioPlaybackEngineImpl(application)
) : AndroidViewModel(application) {

    private val packManager = ContentPackManager(DatabaseProvider.getDatabase(application))

    private val _uiState = MutableStateFlow(LanguagePackUiState())
    val uiState: StateFlow<LanguagePackUiState> = _uiState.asStateFlow()

    init {
        observePacks()
    }

    private fun observePacks() {
        viewModelScope.launch {
            packManager.getAllPacksFlow().collect { packList ->
                _uiState.update { it.copy(packs = packList) }
            }
        }
    }

    fun playTestAudio() {
        val assetPath = audioRepository.getAssetPathForAudio("number_1")
        if (assetPath == null) {
            _uiState.update {
                it.copy(statusMessage = "Test audio asset number_1.wav not found in assets.")
            }
            return
        }

        playbackEngine.playAsset(assetPath) { state, error ->
            _uiState.update {
                it.copy(
                    playbackState = state,
                    statusMessage = when (state) {
                        AudioPlaybackState.PLAYING -> "Playing M3 asset: number_1.wav (Unverified audio asset)"
                        AudioPlaybackState.COMPLETED -> "Playback completed successfully."
                        AudioPlaybackState.ERROR -> error ?: "Audio playback error"
                        else -> it.statusMessage
                    }
                )
            }
        }
    }

    fun stopTestAudio() {
        playbackEngine.stop()
        _uiState.update { it.copy(playbackState = AudioPlaybackState.IDLE) }
    }

    fun resetDemoPack() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true) }
            val success = packManager.resetDemoPack()
            _uiState.update {
                it.copy(
                    isOperating = false,
                    statusMessage = if (success) "Santali Demo Pack (v0.1-demo) reinstalled successfully." else "Failed to reset demo pack."
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        playbackEngine.release()
    }
}
