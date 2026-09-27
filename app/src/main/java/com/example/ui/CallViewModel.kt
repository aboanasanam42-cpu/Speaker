package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.audio.VoiceManager
import com.example.data.Persona
import com.example.network.CallChatMessage
import com.example.network.GeminiCaller
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class CallStatus {
    IDLE,
    CONNECTING,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ENDED
}

data class CallUiState(
    val activePersona: Persona? = null,
    val callStatus: CallStatus = CallStatus.IDLE,
    val callDurationSeconds: Long = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val userLiveTranscript: String = "",
    val lastAiSpokenText: String = "",
    val transcript: List<CallChatMessage> = emptyList(),
    val rmsLevel: Float = 0f,
    val statusMessage: String = "",
    val apiKey: String = "",
    val showApiKeyDialog: Boolean = false
)

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ai_call_prefs", Context.MODE_PRIVATE)
    private val geminiCaller = GeminiCaller()

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private var voiceManager: VoiceManager? = null
    private var timerJob: Job? = null

    init {
        // Resolve initial API key: SharedPreferences -> BuildConfig.GEMINI_API_KEY -> BuildConfig.GEMINI_API_KEY_FALLBACK
        val savedKey = prefs.getString("gemini_api_key", "") ?: ""
        val buildKey = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" } ?: ""
        val fallbackKey = BuildConfig.GEMINI_API_KEY_FALLBACK.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" } ?: ""

        val effectiveKey = when {
            savedKey.isNotBlank() -> savedKey
            buildKey.isNotBlank() -> buildKey
            fallbackKey.isNotBlank() -> fallbackKey
            else -> ""
        }

        _uiState.update { it.copy(apiKey = effectiveKey) }

        initVoiceManager()
    }

    private fun initVoiceManager() {
        voiceManager = VoiceManager(
            context = getApplication(),
            onSpeechRecognized = { text -> handleUserSpokenInput(text) },
            onPartialSpeech = { partial ->
                _uiState.update { it.copy(userLiveTranscript = partial) }
            },
            onRmsChanged = { rms ->
                _uiState.update { it.copy(rmsLevel = rms) }
            },
            onSpeechError = { errorCode ->
                handleSpeechRecognitionError(errorCode)
            },
            onTtsStart = {
                _uiState.update {
                    it.copy(
                        callStatus = CallStatus.SPEAKING,
                        statusMessage = if (it.activePersona?.langTag == "ar")
                            "${it.activePersona?.name} تتحدث..."
                        else
                            "${it.activePersona?.name} is speaking..."
                    )
                }
            },
            onTtsDone = {
                handleTtsFinished()
            }
        )
    }

    fun startCall(persona: Persona) {
        val vm = voiceManager ?: return
        vm.configureForPersona(persona)
        vm.setSpeakerphoneOn(_uiState.value.isSpeakerOn)

        _uiState.update {
            it.copy(
                activePersona = persona,
                callStatus = CallStatus.CONNECTING,
                callDurationSeconds = 0,
                userLiveTranscript = "",
                lastAiSpokenText = persona.initialGreeting,
                transcript = listOf(
                    CallChatMessage(role = "model", text = persona.initialGreeting)
                ),
                statusMessage = if (persona.langTag == "ar") "جارٍ الاتصال..." else "Connecting..."
            )
        }

        startTimer()

        // Wait a brief moment to simulate realistic phone pickup connection, then speak greeting
        viewModelScope.launch {
            delay(1200)
            if (_uiState.value.callStatus != CallStatus.ENDED && _uiState.value.activePersona != null) {
                vm.speak(persona.initialGreeting, "call_greeting")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
            }
        }
    }

    private fun handleTtsFinished() {
        val currentState = _uiState.value
        if (currentState.callStatus == CallStatus.ENDED || currentState.activePersona == null) return

        if (currentState.isMuted) {
            _uiState.update {
                it.copy(
                    callStatus = CallStatus.LISTENING,
                    statusMessage = if (it.activePersona?.langTag == "ar") "الميكروفون مكتوم" else "Microphone is muted"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                callStatus = CallStatus.LISTENING,
                statusMessage = if (it.activePersona?.langTag == "ar")
                    "أنا أسمعك الآن، تفضل بالتحدث..."
                else
                    "Listening to you now, speak freely..."
            )
        }

        currentState.activePersona?.let { persona ->
            voiceManager?.startListening(persona.langTag)
        }
    }

    fun handleUserSpokenInput(userText: String) {
        if (userText.isBlank()) return
        val currentPersona = _uiState.value.activePersona ?: return

        voiceManager?.stopListening()

        // Add user text to transcript and update state to PROCESSING
        _uiState.update {
            it.copy(
                callStatus = CallStatus.PROCESSING,
                userLiveTranscript = userText,
                transcript = it.transcript + CallChatMessage(role = "user", text = userText),
                statusMessage = if (currentPersona.langTag == "ar")
                    "لحظة أفكر في الرد..."
                else
                    "Thinking of a reply..."
            )
        }

        // Call Gemini
        viewModelScope.launch {
            val responseResult = geminiCaller.getPersonaResponse(
                persona = currentPersona,
                apiKey = _uiState.value.apiKey,
                userQuery = userText,
                recentHistory = _uiState.value.transcript
            )

            val replyText = responseResult.getOrElse {
                if (currentPersona.langTag == "ar")
                    "أنا معك، هل يمكنك تكرار ذلك بلطف؟"
                else
                    "I'm here with you, could you please repeat that?"
            }

            if (_uiState.value.callStatus != CallStatus.ENDED && _uiState.value.activePersona != null) {
                _uiState.update {
                    it.copy(
                        lastAiSpokenText = replyText,
                        transcript = it.transcript + CallChatMessage(role = "model", text = replyText),
                        callStatus = CallStatus.SPEAKING,
                        statusMessage = if (currentPersona.langTag == "ar")
                            "${currentPersona.name} تتحدث..."
                        else
                            "${currentPersona.name} is speaking..."
                    )
                }
                voiceManager?.speak(replyText, "ai_reply_${System.currentTimeMillis()}")
            }
        }
    }

    private fun handleSpeechRecognitionError(errorCode: Int) {
        val currentState = _uiState.value
        if (currentState.callStatus == CallStatus.ENDED || currentState.activePersona == null || currentState.isMuted) return

        // If listening timed out or no speech detected, resume listening seamlessly after short delay
        viewModelScope.launch {
            delay(1000)
            if (_uiState.value.callStatus == CallStatus.LISTENING && !_uiState.value.isMuted) {
                _uiState.value.activePersona?.let { persona ->
                    voiceManager?.startListening(persona.langTag)
                }
            }
        }
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        _uiState.update { it.copy(isMuted = newMuted) }
        if (newMuted) {
            voiceManager?.stopListening()
            _uiState.update {
                it.copy(
                    statusMessage = if (it.activePersona?.langTag == "ar") "الميكروفون مكتوم" else "Microphone is muted"
                )
            }
        } else {
            if (_uiState.value.callStatus == CallStatus.LISTENING) {
                _uiState.value.activePersona?.let { persona ->
                    voiceManager?.startListening(persona.langTag)
                }
            }
        }
    }

    fun toggleSpeaker() {
        val newSpeaker = !_uiState.value.isSpeakerOn
        _uiState.update { it.copy(isSpeakerOn = newSpeaker) }
        voiceManager?.setSpeakerphoneOn(newSpeaker)
    }

    fun endCall() {
        timerJob?.cancel()
        timerJob = null
        voiceManager?.stopSpeaking()
        voiceManager?.stopListening()

        _uiState.update {
            it.copy(
                callStatus = CallStatus.ENDED,
                statusMessage = if (it.activePersona?.langTag == "ar") "انتهت المكالمة" else "Call ended"
            )
        }

        viewModelScope.launch {
            delay(600)
            _uiState.update {
                it.copy(
                    activePersona = null,
                    callStatus = CallStatus.IDLE,
                    userLiveTranscript = "",
                    transcript = emptyList(),
                    callDurationSeconds = 0
                )
            }
        }
    }

    fun saveApiKey(key: String) {
        val clean = key.trim()
        prefs.edit().putString("gemini_api_key", clean).apply()
        _uiState.update { it.copy(apiKey = clean, showApiKeyDialog = false) }
    }

    fun openApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = true) }
    }

    fun closeApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = false) }
    }

    override fun onCleared() {
        timerJob?.cancel()
        voiceManager?.release()
        voiceManager = null
        super.onCleared()
    }
}
