package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.data.Persona
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onPartialSpeech: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onSpeechError: (Int) -> Unit,
    private val onTtsStart: () -> Unit,
    private val onTtsDone: () -> Unit
) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    var isTtsInitialized = false
        private set

    var isListening = false
        private set

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    mainHandler.post { onTtsStart() }
                }

                override fun onDone(utteranceId: String?) {
                    mainHandler.post { onTtsDone() }
                }

                override fun onError(utteranceId: String?) {
                    mainHandler.post { onTtsDone() }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?, errorCode: Int) {
                    mainHandler.post { onTtsDone() }
                }
            })
        } else {
            Log.e("VoiceManager", "TTS initialization failed with status: $status")
        }
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                setupRecognitionListener()
            } else {
                Log.w("VoiceManager", "Speech recognition not available on this device")
            }
        }
    }

    private fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() {
                // User started speaking
            }

            override fun onRmsChanged(rmsdB: Float) {
                mainHandler.post { onRmsChanged(rmsdB) }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                mainHandler.post { onSpeechError(error) }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull().orEmpty().trim()
                if (recognized.isNotEmpty()) {
                    mainHandler.post { onSpeechRecognized(recognized) }
                } else {
                    mainHandler.post { onSpeechError(SpeechRecognizer.ERROR_NO_MATCH) }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = partials?.firstOrNull().orEmpty()
                if (text.isNotEmpty()) {
                    mainHandler.post { onPartialSpeech(text) }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun configureForPersona(persona: Persona) {
        val tts = textToSpeech ?: return
        val targetLocale = if (persona.langTag == "ar") Locale("ar", "SA") else Locale.US
        tts.language = targetLocale

        // Select best matching female voice if available
        try {
            val voices = tts.voices
            if (!voices.isNullOrEmpty()) {
                val femaleVoice = voices.firstOrNull { voice ->
                    val matchesLang = voice.locale.language.equals(targetLocale.language, ignoreCase = true)
                    val isFemale = voice.name.contains("female", ignoreCase = true) ||
                            voice.name.contains("#female", ignoreCase = true) ||
                            voice.features.any { it.contains("female", ignoreCase = true) }
                    matchesLang && isFemale
                } ?: voices.firstOrNull { it.locale.language.equals(targetLocale.language, ignoreCase = true) }

                if (femaleVoice != null) {
                    tts.voice = femaleVoice
                }
            }
        } catch (e: Exception) {
            Log.w("VoiceManager", "Error selecting custom voice: ${e.message}")
        }

        tts.setPitch(persona.voicePitch)
        tts.setSpeechRate(persona.voiceRate)
    }

    fun speak(text: String, utteranceId: String = "call_ai_reply") {
        if (!isTtsInitialized || text.isBlank()) {
            mainHandler.post { onTtsDone() }
            return
        }
        stopListening()
        textToSpeech?.stop()
        val params = Bundle()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    fun startListening(langTag: String) {
        mainHandler.post {
            if (speechRecognizer == null) {
                initSpeechRecognizer()
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                val localeString = if (langTag == "ar") "ar-SA" else "en-US"
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeString)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeString)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeString)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            try {
                speechRecognizer?.startListening(intent)
                isListening = true
            } catch (e: Exception) {
                Log.e("VoiceManager", "Failed to start listening: ${e.message}")
                isListening = false
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                isListening = false
            } catch (e: Exception) {
                Log.w("VoiceManager", "Error stopping listening: ${e.message}")
            }
        }
    }

    fun setSpeakerphoneOn(on: Boolean) {
        try {
            audioManager?.mode = if (on) AudioManager.MODE_IN_COMMUNICATION else AudioManager.MODE_NORMAL
            audioManager?.isSpeakerphoneOn = on
        } catch (e: Exception) {
            Log.w("VoiceManager", "Failed to toggle speakerphone: ${e.message}")
        }
    }

    fun release() {
        try {
            stopSpeaking()
            textToSpeech?.shutdown()
            textToSpeech = null
            speechRecognizer?.destroy()
            speechRecognizer = null
            setSpeakerphoneOn(false)
        } catch (e: Exception) {
            Log.w("VoiceManager", "Error releasing VoiceManager: ${e.message}")
        }
    }
}
