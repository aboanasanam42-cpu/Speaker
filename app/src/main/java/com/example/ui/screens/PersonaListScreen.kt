package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Persona
import com.example.data.PersonaRepository
import com.example.ui.theme.CallGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaListScreen(
    currentApiKey: String,
    onPersonaSelected: (Persona) -> Unit,
    onOpenApiKeyDialog: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") } // "all", "ar", "en"

    val filteredPersonas = remember(selectedFilter) {
        when (selectedFilter) {
            "ar" -> PersonaRepository.personas.filter { it.langTag == "ar" }
            "en" -> PersonaRepository.personas.filter { it.langTag == "en" }
            else -> PersonaRepository.personas
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "المتحدثات الذكيات",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text("10", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Text(
                            text = "AI Phone Call Simulator • Voice & Audio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenApiKeyDialog,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("api_key_settings_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (currentApiKey.isNotBlank()) {
                                    Badge(
                                        containerColor = CallGreen,
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Gemini API Key Settings",
                                tint = if (currentApiKey.isNotBlank()) CallGreen else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // API Key Status Banner if not configured yet
            AnimatedVisibility(
                visible = currentApiKey.isBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onOpenApiKeyDialog() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مفتاح Gemini API (جاهز للربط)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "انقر هنا لتخصيص المفتاح أو استخدام المفتاح المدمج من Secrets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Language Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text("الكل • All (10)") },
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "ar",
                        onClick = { selectedFilter = "ar" },
                        label = { Text("العربية (5 شخصيات)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_ar")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "en",
                        onClick = { selectedFilter = "en" },
                        label = { Text("English (5 Personas)") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_en")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Personas List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("persona_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPersonas, key = { it.id }) { persona ->
                    PersonaCard(
                        persona = persona,
                        onCallClick = { onPersonaSelected(persona) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PersonaCard(
    persona: Persona,
    onCallClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallClick() }
            .testTag("persona_card_${persona.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle with Gradient
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(persona.avatarGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = persona.name.take(1),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = persona.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = persona.language,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (persona.langTag == "ar")
                                CallGreen.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            labelColor = if (persona.langTag == "ar")
                                CallGreen
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        border = null,
                        modifier = Modifier.height(24.dp)
                    )
                }

                Text(
                    text = persona.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = persona.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Call Action Button
            IconButton(
                onClick = onCallClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CallGreen)
                    .testTag("call_action_${persona.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "اتصال بـ ${persona.name}",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ApiKeyDialog(
    initialKey: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var keyText by remember { mutableStateOf(initialKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعداد مفتاح Gemini API", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "يتم قراءة المفتاح تلقائياً من GitHub Secrets (GEMINI_API_KEY) أو ملف .env. يمكنك أيضاً إدخاله يدوياً هنا للتجربة الفورية:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_text_field"),
                    shape = RoundedCornerShape(10.dp)
                )
                if (initialKey.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CallGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "المفتاح مفعّل ونشط حالياً",
                            fontSize = 12.sp,
                            color = CallGreen
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(keyText) },
                modifier = Modifier.testTag("save_api_key_button")
            ) {
                Text("حفظ المفتاح")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_api_key_button")
            ) {
                Text("إلغاء")
            }
        }
    )
}
