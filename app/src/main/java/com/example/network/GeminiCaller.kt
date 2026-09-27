package com.example.network

import android.util.Log
import com.example.data.Persona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CallChatMessage(
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiCaller {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getPersonaResponse(
        persona: Persona,
        apiKey: String,
        userQuery: String,
        recentHistory: List<CallChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide a graceful fallback response informing the user
            val fallback = if (persona.langTag == "ar") {
                "أسمعك بوضوح! لتفعيل إجاباتي الحية بواسطة الذكاء الاصطناعي، يرجى إدخال مفتاح Gemini API في إعدادات التطبيق."
            } else {
                "I can hear you clearly! To enable full live AI responses, please configure your Gemini API key in the app settings."
            }
            return@withContext Result.success(fallback)
        }

        // Try primary model requested (gemini-2.5-flash) and fallback if needed
        val models = listOf("gemini-2.5-flash", "gemini-3.5-flash", "gemini-flash-latest")

        var lastException: Exception? = null

        for (model in models) {
            try {
                val responseText = executeGeminiRequest(model, apiKey, persona, userQuery, recentHistory)
                if (responseText.isNotBlank()) {
                    val cleanText = sanitizeForTts(responseText)
                    return@withContext Result.success(cleanText)
                }
            } catch (e: Exception) {
                Log.w("GeminiCaller", "Model $model call failed: ${e.message}")
                lastException = e
            }
        }

        val errorMessage = if (persona.langTag == "ar") {
            "عذراً، انقطع الصوت للحظة، هل يمكنك إعادة ما قلت؟"
        } else {
            "Pardon me, the line broke for a moment, could you repeat that?"
        }
        Result.failure(lastException ?: Exception(errorMessage))
    }

    private fun executeGeminiRequest(
        model: String,
        apiKey: String,
        persona: Persona,
        userQuery: String,
        recentHistory: List<CallChatMessage>
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val rootJson = JSONObject()

        // System Instruction
        val systemInstructionJson = JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().put("text", persona.personalityPrompt))
            })
        }
        rootJson.put("systemInstruction", systemInstructionJson)

        // Contents (Turns)
        val contentsArray = JSONArray()

        // Include last 4 turns for context
        val contextHistory = recentHistory.takeLast(4)
        for (msg in contextHistory) {
            val contentTurn = JSONObject().apply {
                put("role", if (msg.role == "model") "model" else "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", msg.text))
                })
            }
            contentsArray.put(contentTurn)
        }

        // Current turn
        val currentTurn = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().put("text", userQuery))
            })
        }
        contentsArray.put(currentTurn)

        rootJson.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", 120) // Keep answers concise for phone call
        }
        rootJson.put("generationConfig", genConfig)

        val requestBody = rootJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBodyString = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBodyString")
        }

        val json = JSONObject(responseBodyString)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }

        return ""
    }

    private fun sanitizeForTts(rawText: String): String {
        return rawText
            // Remove markdown bold/italics
            .replace(Regex("\\*+"), "")
            // Remove markdown headings
            .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
            // Remove markdown bullets
            .replace(Regex("^[-*•]\\s*", RegexOption.MULTILINE), "")
            // Remove parenthesized stage directions e.g. (chuckles) or [smiles]
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("\\(.*?\\)"), "")
            // Remove backticks
            .replace("`", "")
            // Clean extra spaces
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
