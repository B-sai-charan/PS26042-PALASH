package com.sih2026.palash.ui.screens.voice

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sih2026.palash.asr.AsrEngine
import com.sih2026.palash.asr.AsrResult
import com.sih2026.palash.asr.AsrState
import com.sih2026.palash.asr.VoskHindiAsrEngine
import com.sih2026.palash.audio.AndroidAudioRecorderImpl
import com.sih2026.palash.audio.AndroidPcmAudioPlayerImpl
import com.sih2026.palash.audio.AudioRecorder
import com.sih2026.palash.audio.PcmAudioPlayer
import com.sih2026.palash.audio.PcmPlaybackState
import com.sih2026.palash.audio.RecordingState
import com.sih2026.palash.audio.SantaliAudioRepository
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import com.sih2026.palash.classroom.ClassroomNetworkUtils
import com.sih2026.palash.classroom.ClassroomTranslationMessage
import com.sih2026.palash.classroom.TeacherClassroomServer
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.network.NetworkResult
import com.sih2026.palash.data.repository.TranslationRepository
import com.sih2026.palash.data.repository.TranslationRepositoryImpl
import com.sih2026.palash.translation.LocalDomainTranslationEngine
import com.sih2026.palash.translation.TranslationEngine
import com.sih2026.palash.translation.TranslationResult
import com.sih2026.palash.translation.TranslationSourceType
import com.sih2026.palash.translation.TranslationStatus
import com.sih2026.palash.tts.LivePiperTtsEngine
import com.sih2026.palash.tts.LiveTtsState
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch




data class VoiceTranslationUiState(
    val gradeName: String = "Grade 2",
    val subjectTopic: String = "Mathematics > Counting",
    val languagePair: String = "Hindi → Santali",
    val recordingState: RecordingState = RecordingState.IDLE,
    val recordedDurationSeconds: Float = 0f,
    val recordedFilePath: String? = null,
    val isPlayingOriginalAudio: Boolean = false,
    val originalAudioState: PcmPlaybackState = PcmPlaybackState.IDLE,
    val errorMessage: String? = null,
    val statusText: String = "माइक दबाएं और हिंदी में बोलें",
    val asrState: AsrState = AsrState.ASR_NOT_READY,
    val recognizedHindiText: String? = null,
    val recordingDurationMs: Long = 0L,
    val asrProcessingTimeMs: Long = 0L,
    val totalAsrTimeMs: Long = 0L,
    val asrNotice: String = "Vosk Hindi ASR",
    val isTranslating: Boolean = false,
    val translationStatus: TranslationStatus = TranslationStatus.UNAVAILABLE,
    val translatedSantaliText: String? = null,
    val translationSourceType: TranslationSourceType = TranslationSourceType.UNKNOWN,
    val backendLatencyMs: Double? = null,
    val androidRoundTripMs: Long? = null,
    val translationNotice: String = "माइक दबाएं और हिंदी में बोलें",
    val isClassroomSharing: Boolean = false,
    val classroomServerIp: String? = null,
    val classroomServerPort: Int = 8080,
    val lastPublishedMessageId: String? = null,
    val isLiveTtsAvailable: Boolean = false,
    val liveTtsAudioFile: File? = null,
    val liveTtsInferenceTimeMs: Long = 0L,
    val isPlayingSantaliAudio: Boolean = false,
    val santaliAudioState: PcmPlaybackState = PcmPlaybackState.IDLE,
    val activeAudioSource: String = "FALLBACK_M3_WAV"

)

class VoiceTranslationViewModel @JvmOverloads constructor(
    application: Application,
    private val audioRecorder: AudioRecorder = AndroidAudioRecorderImpl(application),
    private val asrEngine: AsrEngine = com.sih2026.palash.voice.VoiceModelManager.getVoskEngine(),
    private val translationEngine: TranslationEngine = LocalDomainTranslationEngine(application),
    private val translationRepository: TranslationRepository = TranslationRepositoryImpl(
        DatabaseProvider.getDatabase(application).translationCacheDao()
    ),
    private val classroomServer: TeacherClassroomServer = com.sih2026.palash.TeacherClassroomServerHolder.server,
    private val santaliAudioRepo: SantaliAudioRepository = SantaliAudioRepositoryImpl(application),
    private val pcmAudioPlayer: PcmAudioPlayer = AndroidPcmAudioPlayerImpl(),
    private val livePiperTtsEngine: LivePiperTtsEngine = com.sih2026.palash.voice.VoiceModelManager.getPiperEngine(application)
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PALASH_VOICE_VM"
    }

    private val _uiState = MutableStateFlow(VoiceTranslationUiState())
    val uiState: StateFlow<VoiceTranslationUiState> = _uiState.asStateFlow()

    private var durationMonitorJob: Job? = null
    private var recordingStartTime: Long = 0L

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            initializeAsr()
            startClassroomServer()
        }
    }

    fun startClassroomServer() {
        viewModelScope.launch {
            val started = classroomServer.start()
            val ip = ClassroomNetworkUtils.getLocalIPv4Address()
            _uiState.update {
                it.copy(
                    isClassroomSharing = started,
                    classroomServerIp = ip ?: "127.0.0.1",
                    classroomServerPort = classroomServer.activePort
                )
            }
        }
    }

    fun initializeAsr() {
        Log.i(TAG, "Initializing VoiceTranslationViewModel ASR engine...")
        asrEngine.initialize(getApplication()) { state ->
            _uiState.update { it.copy(asrState = state) }
        }
    }

    fun setContext(grade: String, subject: String, topic: String) {
        _uiState.update {
            it.copy(
                gradeName = if (grade.isNotBlank()) grade else "Grade 2",
                subjectTopic = "${if (subject.isNotBlank()) subject else "Mathematics"} > ${if (topic.isNotBlank()) topic else "Counting"}"
            )
        }
    }

    fun startRecording(outputDir: File) {
        if (_uiState.value.recordingState == RecordingState.RECORDING) return

        pcmAudioPlayer.stop()
        val outputFile = File(outputDir, "recorded_hindi_${System.currentTimeMillis()}.pcm")

        _uiState.update {
            it.copy(
                recordedFilePath = null,
                isPlayingOriginalAudio = false,
                originalAudioState = PcmPlaybackState.IDLE,
                recognizedHindiText = null,
                translatedSantaliText = null,
                translationStatus = TranslationStatus.UNAVAILABLE,
                recordingDurationMs = 0L,
                asrProcessingTimeMs = 0L,
                backendLatencyMs = null,
                androidRoundTripMs = null,
                totalAsrTimeMs = 0L,
                isTranslating = false,
                translationNotice = "माइक दबाएं और हिंदी में बोलें"
            )
        }

        recordingStartTime = SystemClock.elapsedRealtime()
        Log.i(TAG, "Starting audio recording to file: ${outputFile.absolutePath}")

        audioRecorder.startRecording(outputFile) { state, errorMsg ->
            _uiState.update {
                it.copy(
                    recordingState = state,
                    errorMessage = errorMsg,
                    statusText = when (state) {
                        RecordingState.RECORDING -> "सुन रहे हैं..."
                        RecordingState.ERROR -> errorMsg ?: "रिकॉर्डिंग विफल हुई"
                        else -> it.statusText
                    }
                )
            }
        }

        durationMonitorJob?.cancel()
        durationMonitorJob = viewModelScope.launch {
            while (_uiState.value.recordingState == RecordingState.RECORDING) {
                val currentMs = audioRecorder.currentDurationMs
                val sec = currentMs / 1000f
                _uiState.update { it.copy(recordedDurationSeconds = sec) }
                delay(100)
            }
        }
    }

    fun stopRecording() {
        if (_uiState.value.recordingState != RecordingState.RECORDING) return

        val recDuration = SystemClock.elapsedRealtime() - recordingStartTime
        durationMonitorJob?.cancel()
        Log.i(TAG, "Stopping recording. Duration: ${recDuration}ms")

        _uiState.update { it.copy(recordingState = RecordingState.STOPPING, statusText = "प्रसंस्करण हो रहा है...") }

        audioRecorder.stopRecording { result ->
            if (result != null) {
                val sec = result.durationMs / 1000f
                val audioFile = result.audioFile
                Log.i(TAG, "Audio recorded successfully. Path: ${audioFile.absolutePath}, Size: ${audioFile.length()} bytes")

                _uiState.update {
                    it.copy(
                        recordingState = RecordingState.RECORDED,
                        recordedDurationSeconds = sec,
                        recordingDurationMs = result.durationMs,
                        recordedFilePath = audioFile.absolutePath,
                        statusText = "प्रसंस्करण हो रहा है...",
                        errorMessage = null
                    )
                }
                processAsr(audioFile, result.durationMs)
            } else {
                Log.e(TAG, "Recording stop returned null result.")
                _uiState.update {
                    it.copy(
                        recordingState = RecordingState.ERROR,
                        errorMessage = "ध्वनि फ़ाइल सहेजने में विफल",
                        statusText = "रिकॉर्डिंग त्रुटि"
                    )
                }
            }
        }
    }

    fun processAsr(audioFile: File, recordingMs: Long) {
        val pipelineStart = SystemClock.elapsedRealtime()
        Log.i(TAG, "Processing ASR pipeline for audio file size ${audioFile.length()} bytes...")

        _uiState.update {
            it.copy(
                asrState = AsrState.ASR_PROCESSING,
                statusText = "प्रसंस्करण हो रहा है..."
            )
        }

        asrEngine.processAudio(audioFile) { asrResult ->
            val totalAsrDuration = SystemClock.elapsedRealtime() - pipelineStart
            Log.i(TAG, "ASR result returned: text=\"${asrResult.text}\", processingTime=${asrResult.processingTimeMs}ms")

            val isNoSpeech = asrResult.errorMessage != null ||
                    asrResult.text.isBlank() ||
                    asrResult.text == "कोई आवाज़ नहीं पहचानी गई" ||
                    asrResult.text.contains("No speech", ignoreCase = true) ||
                    asrResult.text.contains("Speech not", ignoreCase = true) ||
                    asrResult.text.contains("ASR_", ignoreCase = true) ||
                    asrResult.text.contains("ERROR", ignoreCase = true) ||
                    asrResult.text.contains("failed", ignoreCase = true)

            _uiState.update {
                if (isNoSpeech) {
                    it.copy(
                        asrState = if (asrResult.errorMessage != null) AsrState.ASR_ERROR else AsrState.ASR_RESULT,
                        recognizedHindiText = "कोई आवाज़ नहीं पहचानी गई",
                        asrProcessingTimeMs = asrResult.processingTimeMs,
                        totalAsrTimeMs = recordingMs + asrResult.processingTimeMs,
                        statusText = "कोई आवाज़ नहीं पहचानी गई",
                        errorMessage = null
                    )
                } else {
                    it.copy(
                        asrState = AsrState.ASR_RESULT,
                        recognizedHindiText = asrResult.text,
                        asrProcessingTimeMs = asrResult.processingTimeMs,
                        totalAsrTimeMs = recordingMs + asrResult.processingTimeMs,
                        statusText = "पहचाना गया हिंदी वाक्य"
                    )
                }
            }

            // Trigger M1 POST /translate ONLY for valid recognized Hindi text
            if (!isNoSpeech && asrResult.text.isNotBlank()) {
                translateText(asrResult.text)
            }
        }
    }

    fun translateText(hindiText: String) {
        if (hindiText.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTranslating = true,
                    statusText = "Translating..."
                )
            }

            val result = translationRepository.translateHindiToSantali(hindiText)
            Log.i(TAG, "Translation repository result: $result")

            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    val isCache = data.isFromCache
                    val santaliText = data.dto.translatedText

                    val matchedAudioId = resolveAudioId(hindiText, santaliText)
                    val liveResult = livePiperTtsEngine.synthesizeLive(santaliText)
                    val isPiperReady = liveResult.state == LiveTtsState.READY && liveResult.audioFile != null

                    val gradeVal = try {
                        _uiState.value.gradeName.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 2
                    } catch (_: Exception) { 2 }

                    val msg = ClassroomTranslationMessage(
                        messageId = UUID.randomUUID().toString(),
                        grade = gradeVal,
                        section = "A",
                        subject = "Mathematics",
                        timestamp = System.currentTimeMillis(),
                        hindiText = hindiText,
                        santaliText = santaliText,
                        audioId = matchedAudioId
                    )
                    val published = classroomServer.publishMessage(msg)

                    _uiState.update {
                        it.copy(
                            isTranslating = false,
                            translationStatus = TranslationStatus.SUCCESS,
                            translatedSantaliText = santaliText,
                            translationSourceType = if (isCache) TranslationSourceType.LOCAL_CACHE else TranslationSourceType.BACKEND,
                            isLiveTtsAvailable = isPiperReady,
                            liveTtsAudioFile = if (isPiperReady) liveResult.audioFile else null,
                            liveTtsInferenceTimeMs = if (isPiperReady) liveResult.inferenceTimeMs else 0L,
                            activeAudioSource = if (isPiperReady) "LIVE_PIPER_TTS" else "FALLBACK_M3_WAV",
                            backendLatencyMs = if (isCache) null else data.dto.latencyMs,
                            androidRoundTripMs = data.androidRoundTripMs,
                            translationNotice = if (isCache) "ऑफ़लाइन कैश अनुवाद (0 ms)" else "एम1 अनुवाद (सफल)",
                            statusText = if (published) "अनुवादित एवं कक्षा में प्रसारित" else "एम1 द्वारा अनुवादित",
                            lastPublishedMessageId = if (published) msg.messageId else null
                        )
                    }
                }
                else -> {
                    val cleanInput = LocalDomainTranslationEngine.normalizeForMatching(hindiText)
                    val localResult = translationEngine.translate(hindiText, "hi", "sat")
                    val localTranslation = localResult.translatedText

                    if (localTranslation != null) {
                        val matchedAudioId = resolveAudioId(cleanInput, localTranslation)
                        val liveResult = livePiperTtsEngine.synthesizeLive(localTranslation)
                        val isPiperReady = liveResult.state == LiveTtsState.READY && liveResult.audioFile != null

                        val gradeVal = try {
                            _uiState.value.gradeName.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 2
                        } catch (_: Exception) { 2 }

                        val msg = ClassroomTranslationMessage(
                            messageId = UUID.randomUUID().toString(),
                            grade = gradeVal,
                            section = "A",
                            subject = "Mathematics",
                            timestamp = System.currentTimeMillis(),
                            hindiText = cleanInput,
                            santaliText = localTranslation,
                            audioId = matchedAudioId
                        )
                        val published = classroomServer.publishMessage(msg)

                        _uiState.update {
                            it.copy(
                                isTranslating = false,
                                translationStatus = TranslationStatus.SUCCESS,
                                translatedSantaliText = localTranslation,
                                translationSourceType = TranslationSourceType.LOCAL_CONTENT_PACK,
                                isLiveTtsAvailable = isPiperReady,
                                liveTtsAudioFile = if (isPiperReady) liveResult.audioFile else null,
                                liveTtsInferenceTimeMs = if (isPiperReady) liveResult.inferenceTimeMs else 0L,
                                activeAudioSource = if (isPiperReady) "LIVE_PIPER_TTS" else "FALLBACK_M3_WAV",
                                backendLatencyMs = null,
                                androidRoundTripMs = 0L,
                                translationNotice = "ऑफ़लाइन पाठ्यचर्या अनुवाद (0 ms)",
                                statusText = if (published) "ऑफ़लाइन अनुवाद कक्षा में प्रसारित" else "ऑफ़लाइन अनुवाद तैयार",
                                lastPublishedMessageId = if (published) msg.messageId else null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isTranslating = false,
                                translationStatus = TranslationStatus.UNAVAILABLE,
                                translatedSantaliText = null,
                                translationSourceType = TranslationSourceType.UNKNOWN,
                                isLiveTtsAvailable = false,
                                liveTtsAudioFile = null,
                                activeAudioSource = "NONE",
                                backendLatencyMs = null,
                                androidRoundTripMs = null,
                                translationNotice = "अनुवाद उपलब्ध नहीं हो पाया। फिर से बोलें।",
                                statusText = "अनुवाद उपलब्ध नहीं हो पाया। फिर से बोलें।"
                            )
                        }
                    }
                }
            }
        }
    }

    fun playOriginalAudio() {
        val path = _uiState.value.recordedFilePath
        if (path.isNullOrBlank()) {
            _uiState.update { it.copy(originalAudioState = PcmPlaybackState.ERROR, errorMessage = "Audio file not available") }
            return
        }
        val file = File(path)
        if (!file.exists() || file.length() == 0L) {
            _uiState.update { it.copy(originalAudioState = PcmPlaybackState.ERROR, errorMessage = "Audio file not available") }
            return
        }

        _uiState.update { it.copy(isPlayingOriginalAudio = true, originalAudioState = PcmPlaybackState.PLAYING, isPlayingSantaliAudio = false, santaliAudioState = PcmPlaybackState.IDLE) }
        pcmAudioPlayer.playPcmFileWithId(file, audioId = "ORIGINAL_HINDI", sampleRate = 16000) { state, errorMsg ->
            _uiState.update {
                it.copy(
                    originalAudioState = state,
                    isPlayingOriginalAudio = (state == PcmPlaybackState.PLAYING),
                    errorMessage = errorMsg ?: it.errorMessage
                )
            }
        }
    }

    fun stopOriginalAudio() {
        pcmAudioPlayer.stop()
        _uiState.update {
            it.copy(
                isPlayingOriginalAudio = false,
                originalAudioState = PcmPlaybackState.IDLE
            )
        }
    }

    fun cancelRecording() {
        durationMonitorJob?.cancel()
        audioRecorder.cancelRecording()
        stopOriginalAudio()
        _uiState.update {
            it.copy(
                recordingState = RecordingState.IDLE,
                recordedDurationSeconds = 0f,
                recordedFilePath = null,
                isPlayingOriginalAudio = false,
                originalAudioState = PcmPlaybackState.IDLE,
                recognizedHindiText = null,
                translatedSantaliText = null,
                translationStatus = TranslationStatus.UNAVAILABLE,
                recordingDurationMs = 0L,
                asrProcessingTimeMs = 0L,
                backendLatencyMs = null,
                androidRoundTripMs = null,
                totalAsrTimeMs = 0L,
                isTranslating = false,
                errorMessage = null,
                statusText = "माइक दबाएं और हिंदी में बोलें",
                translationNotice = "माइक दबाएं और हिंदी में बोलें"
            )
        }
    }

    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                recordingState = RecordingState.ERROR,
                errorMessage = "Microphone permission required to record Hindi classroom phrases.",
                statusText = "Microphone permission required"
            )
        }
    }

    fun onPermissionPermanentlyDenied() {
        _uiState.update {
            it.copy(
                recordingState = RecordingState.ERROR,
                errorMessage = "Microphone permission permanently denied. Please enable it in Settings.",
                statusText = "Microphone permission permanently denied"
            )
        }
    }

    private fun resolveAudioId(hindiText: String, santaliText: String): String? {
        val cleanHindi = hindiText.trim()
        val cleanSantali = santaliText.trim()
        if (santaliAudioRepo.hasAudioAsset(cleanSantali)) return cleanSantali

        val curriculumMap = mapOf(
            "1" to "number_1",
            "2" to "number_2",
            "3" to "number_3",
            "4" to "number_4",
            "5" to "number_5",
            "6" to "number_6"
        )
        val mappedKey = curriculumMap[cleanHindi] ?: curriculumMap[cleanSantali]
        if (mappedKey != null && santaliAudioRepo.hasAudioAsset(mappedKey)) {
            return mappedKey
        }
        return null
    }

    
    fun playSantaliAudio() {
        val liveFile = _uiState.value.liveTtsAudioFile
        if (liveFile != null && liveFile.exists() && liveFile.length() > 0) {
            _uiState.update { it.copy(isPlayingSantaliAudio = true, santaliAudioState = PcmPlaybackState.PLAYING, isPlayingOriginalAudio = false, originalAudioState = PcmPlaybackState.IDLE) }
            pcmAudioPlayer.playPcmFileWithId(liveFile, audioId = "LIVE_PIPER_TTS") { state, errorMsg ->
                _uiState.update {
                    it.copy(
                        santaliAudioState = state,
                        isPlayingSantaliAudio = (state == PcmPlaybackState.PLAYING),
                        errorMessage = errorMsg ?: it.errorMessage
                    )
                }
            }
        } else {
            // Fallback to pre-recorded M3 WAV asset
            val audioId = _uiState.value.recognizedHindiText?.let { hindi ->
                _uiState.value.translatedSantaliText?.let { santali ->
                    resolveAudioId(hindi, santali)
                }
            }
            if (audioId != null && santaliAudioRepo.hasAudioAsset(audioId)) {
                val assetPath = santaliAudioRepo.getAssetPathForAudio(audioId)
                if (assetPath != null) {
                    _uiState.update { it.copy(isPlayingSantaliAudio = true, santaliAudioState = PcmPlaybackState.PLAYING, isPlayingOriginalAudio = false, originalAudioState = PcmPlaybackState.IDLE) }
                    com.sih2026.palash.audio.GlobalAudioPlayer.playAudio(getApplication(), "STATIC_SANTALI", assetPath) { state, errorMsg ->
                        _uiState.update {
                            it.copy(
                                isPlayingSantaliAudio = (state == com.sih2026.palash.audio.AudioPlaybackState.PLAYING),
                                santaliAudioState = if (state == com.sih2026.palash.audio.AudioPlaybackState.PLAYING) PcmPlaybackState.PLAYING else PcmPlaybackState.COMPLETED,
                                errorMessage = errorMsg ?: it.errorMessage
                            )
                        }
                    }
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Santali audio recording unavailable") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        durationMonitorJob?.cancel()
        pcmAudioPlayer.release()
        audioRecorder.release()
    }
}

class VoiceTranslationViewModelFactory @JvmOverloads constructor(
    private val application: Application,
    private val audioRecorder: AudioRecorder = AndroidAudioRecorderImpl(application),
    private val asrEngine: AsrEngine = VoskHindiAsrEngine(),
    private val translationEngine: TranslationEngine = LocalDomainTranslationEngine(application),
    private val translationRepository: TranslationRepository = TranslationRepositoryImpl(
        DatabaseProvider.getDatabase(application).translationCacheDao()
    )
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoiceTranslationViewModel::class.java)) {
            return VoiceTranslationViewModel(application, audioRecorder, asrEngine, translationEngine, translationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}