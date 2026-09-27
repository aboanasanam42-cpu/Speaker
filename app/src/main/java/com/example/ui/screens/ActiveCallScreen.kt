package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Persona
import com.example.network.CallChatMessage
import com.example.ui.CallStatus
import com.example.ui.CallUiState
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import kotlinx.coroutines.launch

@Composable
fun ActiveCallScreen(
    uiState: CallUiState,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSendManualText: (String) -> Unit
) {
    val persona = uiState.activePersona ?: return

    BackHandler {
        onEndCall()
    }

    var showTextInputDialog by remember { mutableStateOf(false) }

    // Format duration e.g. 01:23
    val minutes = uiState.callDurationSeconds / 60
    val seconds = uiState.callDurationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    // Call Screen Dark Surface for authentic modern phone call feel
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Deep Slate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP HEADER ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language badge
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = persona.language,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Call Duration
                    Text(
                        text = formattedDuration,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Audio output indicator
                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isSpeakerOn) Color.White.copy(alpha = 0.2f) else Color.Transparent)
                            .testTag("speaker_toggle_header")
                    ) {
                        Icon(
                            imageVector = if (uiState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.Hearing,
                            contentDescription = "Speaker Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Caller Avatar with radar pulse rings
                CallAvatarWithRadar(
                    persona = persona,
                    isActive = uiState.callStatus == CallStatus.SPEAKING || uiState.callStatus == CallStatus.LISTENING
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Persona Name & Title
                Text(
                    text = persona.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = persona.title,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Indicator Pill
                StatusPill(
                    callStatus = uiState.callStatus,
                    statusMessage = uiState.statusMessage,
                    isMuted = uiState.isMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Audio Waveform Visualization
                AudioWaveform(
                    isSpeaking = uiState.callStatus == CallStatus.SPEAKING,
                    isListening = uiState.callStatus == CallStatus.LISTENING && !uiState.isMuted,
                    rms = uiState.rmsLevel
                )
            }

            // --- CENTER: TRANSCRIPTION & REPLIES ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                TranscriptCard(
                    transcript = uiState.transcript,
                    personaName = persona.name,
                    userLiveText = uiState.userLiveTranscript
                )
            }

            // --- QUICK SUGGESTION CHIPS (TAP TO SPEAK / EMULATOR HELPER) ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اقتراحات للحديث • Quick Topics:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(persona.sampleQuestions) { question ->
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = question,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("suggestion_chip_${question.take(6)}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTTOM IN-CALL CONTROLS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Button
                CallControlButton(
                    icon = if (uiState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (uiState.isMuted) "مكتوم" else "كتم",
                    active = uiState.isMuted,
                    onClick = onToggleMute,
                    testTag = "call_mute_button"
                )

                // Large Red End Call Button
                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = CallRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "إنهاء المكالمة",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Keyboard / Manual Speech Input
                CallControlButton(
                    icon = Icons.Default.Keyboard,
                    label = "كتابة",
                    active = false,
                    onClick = { showTextInputDialog = true },
                    testTag = "call_keyboard_button"
                )
            }
        }
    }

    if (showTextInputDialog) {
        ManualTextInputDialog(
            personaName = persona.name,
            onSend = { text ->
                onSendManualText(text)
                showTextInputDialog = false
            },
            onDismiss = { showTextInputDialog = false }
        )
    }
}

@Composable
fun CallAvatarWithRadar(
    persona: Persona,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
        // Glowing Outer Wave
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(persona.avatarGradient.first().copy(alpha = pulseAlpha))
        )

        // Inner Avatar Circle
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(persona.avatarGradient)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = persona.name.take(1),
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusPill(
    callStatus: CallStatus,
    statusMessage: String,
    isMuted: Boolean
) {
    val (bgColor, textColor, icon) = when {
        isMuted -> Triple(CallRed.copy(alpha = 0.2f), CallRed, Icons.Default.MicOff)
        callStatus == CallStatus.SPEAKING -> Triple(CallGreen.copy(alpha = 0.2f), CallGreen, Icons.Default.RecordVoiceOver)
        callStatus == CallStatus.LISTENING -> Triple(Color(0xFF38BDF8).copy(alpha = 0.2f), Color(0xFF38BDF8), Icons.Default.Hearing)
        callStatus == CallStatus.PROCESSING -> Triple(Color(0xFFA855F7).copy(alpha = 0.2f), Color(0xFFA855F7), Icons.Default.GraphicEq)
        else -> Triple(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.9f), Icons.Default.Call)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusMessage.ifBlank { "متصل..." },
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AudioWaveform(
    isSpeaking: Boolean,
    isListening: Boolean,
    rms: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 32.dp)
    ) {
        val barsCount = 21
        val barWidth = 4.dp.toPx()
        val spacing = (size.width - (barsCount * barWidth)) / (barsCount - 1)
        val centerY = size.height / 2

        for (i in 0 until barsCount) {
            val factor = if (isSpeaking) {
                // Wave pulsing
                val offset = kotlin.math.sin((i.toDouble() / barsCount) * Math.PI)
                (offset * waveAnim * size.height * 0.9f).toFloat().coerceAtLeast(6f)
            } else if (isListening) {
                // RMS reactive
                val rmsFactor = (rms / 10f).coerceIn(0.1f, 1f)
                val variation = kotlin.math.sin(i.toDouble())
                ((variation + 1) * 0.5f * rmsFactor * size.height * 0.8f).toFloat().coerceAtLeast(6f)
            } else {
                6f
            }

            val x = i * (barWidth + spacing)
            drawLine(
                color = if (isSpeaking) Color(0xFF38BDF8) else if (isListening) CallGreen else Color.White.copy(alpha = 0.3f),
                start = Offset(x, centerY - factor / 2),
                end = Offset(x, centerY + factor / 2),
                strokeWidth = barWidth
            )
        }
    }
}

@Composable
fun TranscriptCard(
    transcript: List<CallChatMessage>,
    personaName: String,
    userLiveText: String
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(transcript.size, userLiveText) {
        if (transcript.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(transcript.size - 1)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White.copy(alpha = 0.06f),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (transcript.isEmpty() && userLiveText.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "المحادثة الصوتية المباشرة ستظهر هنا...",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transcript) { message ->
                    val isModel = message.role == "model"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
                    ) {
                        Surface(
                            color = if (isModel) Color(0xFF1E293B) else Color(0xFF4F46E5),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isModel) personaName else "أنت",
                                    color = if (isModel) Color(0xFF38BDF8) else Color(0xFFA5B4FC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = message.text,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                if (userLiveText.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                color = Color(0xFF4F46E5).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "أنت (تتحدث الآن...)",
                                        color = Color(0xFFA5B4FC),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = userLiveText,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(testTag)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (active) CallRed else Color.White.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ManualTextInputDialog(
    personaName: String,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تحدث مع $personaName كتابياً")
        },
        text = {
            Column {
                Text(
                    text = "يمكنك كتابة رسالتك وسيقوم الذكاء الاصطناعي بالرد ونطق الجواب فوراً عبر محرك الصوت:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("اكتب رسالتك هنا...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_call_text_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSend(inputText)
                    }
                },
                modifier = Modifier.testTag("send_manual_call_text_button")
            ) {
                Text("إرسال ونطق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
