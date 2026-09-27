package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Persona
import com.example.ui.CallViewModel
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.ApiKeyDialog
import com.example.ui.screens.PersonaListScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Permission Launcher for RECORD_AUDIO
                var pendingPersonaToCall by remember { mutableStateOf<Persona?>(null) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        pendingPersonaToCall?.let { persona ->
                            viewModel.startCall(persona)
                            pendingPersonaToCall = null
                        }
                    } else {
                        // Even if permission denied or unavailable on emulator, still allow call with text/TTS
                        pendingPersonaToCall?.let { persona ->
                            viewModel.startCall(persona)
                            pendingPersonaToCall = null
                        }
                    }
                }

                fun initiateCallWithPermission(persona: Persona) {
                    val hasAudioPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasAudioPermission) {
                        viewModel.startCall(persona)
                    } else {
                        pendingPersonaToCall = persona
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (uiState.activePersona == null) {
                        PersonaListScreen(
                            currentApiKey = uiState.apiKey,
                            onPersonaSelected = { persona ->
                                initiateCallWithPermission(persona)
                            },
                            onOpenApiKeyDialog = {
                                viewModel.openApiKeyDialog()
                            }
                        )
                    } else {
                        ActiveCallScreen(
                            uiState = uiState,
                            onEndCall = {
                                viewModel.endCall()
                            },
                            onToggleMute = {
                                viewModel.toggleMute()
                            },
                            onToggleSpeaker = {
                                viewModel.toggleSpeaker()
                            },
                            onSendManualText = { text ->
                                viewModel.handleUserSpokenInput(text)
                            }
                        )
                    }

                    if (uiState.showApiKeyDialog) {
                        ApiKeyDialog(
                            initialKey = uiState.apiKey,
                            onSave = { newKey ->
                                viewModel.saveApiKey(newKey)
                            },
                            onDismiss = {
                                viewModel.closeApiKeyDialog()
                            }
                        )
                    }
                }
            }
        }
    }
}
