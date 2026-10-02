package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ImageAnalysisResult(
    val success: Boolean,
    val markdownAnalysis: String,
    val thinkingProcess: String? = null,
    val errorMessage: String? = null
)

object GeminiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize if too large to ensure quick transmission
        val maxDimension = 1024
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = minOf(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Multimodal Image Analysis with gemini-3.1-pro-preview and High Thinking Level.
     * Note: Does NOT set maxOutputTokens, as mandated.
     */
    suspend fun analyzeImageWithThinking(
        bitmap: Bitmap,
        customPrompt: String = "Perform a thorough, expert-level inspection of this image or document. If it contains a QR code, barcode, receipt, link, product badge, or poster, extract all raw identifiers, summarize the contents, check for potential security or phishing hazards, and provide practical next steps."
    ): ImageAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ImageAnalysisResult(
                success = false,
                markdownAnalysis = "",
                errorMessage = "Gemini API key is not configured. Please add your GEMINI_API_KEY in the AI Studio Secrets panel."
            )
        }

        try {
            val base64Image = bitmapToBase64(bitmap)

            val rootJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            // Text part
                            put(JSONObject().apply { put("text", customPrompt) })
                            // Image part
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                // High Thinking Mode configuration
                put("generationConfig", JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                })
            }

            val requestUrl = "${BASE_URL}gemini-3.1-pro-preview:generateContent?key=$apiKey"
            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext ImageAnalysisResult(
                    success = false,
                    markdownAnalysis = "",
                    errorMessage = "Analysis error (${response.code}): $responseBody"
                )
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")

            var extractedText = ""
            var thinkingText: String? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("thought")) {
                        thinkingText = part.optString("thought")
                    }
                    if (part.has("text")) {
                        extractedText += part.optString("text")
                    }
                }
            }

            if (extractedText.isBlank()) {
                extractedText = "Inspection complete. No textual or code data recognized in the provided image."
            }

            ImageAnalysisResult(
                success = true,
                markdownAnalysis = extractedText,
                thinkingProcess = thinkingText
            )
        } catch (e: Exception) {
            ImageAnalysisResult(
                success = false,
                markdownAnalysis = "",
                errorMessage = "Network or analysis failure: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    /**
     * Multi-turn chat interface using gemini-3.5-flash with system instruction to guide users.
     */
    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel.")
            )
        }

        try {
            val rootJson = JSONObject().apply {
                // System instruction for Aura Guide
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "You are 'Aura Guide', the personal AI assistant inside AuraQR. You provide friendly, concise, and expert guidance on how to use every feature of AuraQR: live camera QR/barcode scanner, gallery image scanner, batch mode for warehouses/events, custom QR Studio (WiFi, vCard, URLs), local link security inspection, and privacy-first local processing. Keep answers crisp, iOS-styled, and helpful. Do not use generic filler.")
                        })
                    })
                })

                val contentsArray = JSONArray()

                // Append conversation history
                for (msg in history.takeLast(10)) {
                    contentsArray.put(JSONObject().apply {
                        put("role", if (msg.role == "user") "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", msg.content) })
                        })
                    })
                }

                // Append current user message
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })

                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val requestUrl = "${BASE_URL}gemini-3.5-flash:generateContent?key=$apiKey"
            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Assistant response failed (${response.code}): $responseBody")
                )
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")

            val reply = parts?.optJSONObject(0)?.optString("text") ?: "I'm here to help with your QR codes and scans. What would you like to know?"
            Result.success(reply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
