package com.example.nekoeco.ai

import android.util.Base64
import com.example.nekoeco.BuildConfig
import com.example.nekoeco.data.auth.supabaseSecret.GEMINI_API_KEY
import com.google.gson.Gson
import java.io.File

class GeminiRepository(
    private val apiService: GeminiApiService
) {

    private val gson = Gson()

    suspend fun analyzeImage(
        imageFile: File
    ): AiScanResponse {

        if (!imageFile.exists()) {
            throw Exception(
                "Image file does not exist."
            )
        }

        if (GEMINI_API_KEY.isBlank()) {
            throw Exception(
                "Gemini API key is missing."
            )
        }

        val imageBytes = imageFile.readBytes()

        if (imageBytes.isEmpty()) {
            throw Exception(
                "Image file is empty."
            )
        }

        val base64Image =
            Base64.encodeToString(
                imageBytes,
                Base64.NO_WRAP
            )

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(

                        Part(
                            text =
                                GeminiPrompt.DEVICE_IDENTIFICATION
                        ),

                        Part(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = base64Image
                            )
                        )
                    )
                )
            )
        )

        try {

            val response =
                apiService.generateContent(
                    apiKey = GEMINI_API_KEY,
                    request = request
                )

            if (!response.isSuccessful) {

                val errorBody =
                    response.errorBody()
                        ?.string()

                throw Exception(
                    "Gemini API Error ${response.code()}\n" +
                            errorBody
                )
            }

            val geminiResponse =
                response.body()
                    ?: throw Exception(
                        "Gemini returned an empty response."
                    )

            val jsonText =
                geminiResponse
                    .candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?: throw Exception(
                        "Gemini returned no text."
                    )

            return parseAiResponse(jsonText)

        } catch (e: Exception) {

            throw Exception(
                when {
                    e.message?.contains(
                        "Unable to resolve host",
                        ignoreCase = true
                    ) == true ->
                        "No Internet connection or DNS problem. " +
                                "Please check your device Internet connection."

                    e.message?.contains(
                        "timeout",
                        ignoreCase = true
                    ) == true ->
                        "Gemini connection timed out. " +
                                "Please try again."

                    else ->
                        e.message ?: "Gemini AI scan failed."
                },
                e
            )
        }
    }

    private fun parseAiResponse(
        rawText: String
    ): AiScanResponse {

        var jsonText = rawText.trim()

        // Remove ```json ... ``` if Gemini accidentally returns Markdown.
        if (jsonText.startsWith("```")) {

            jsonText = jsonText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        }

        return try {

            gson.fromJson(
                jsonText,
                AiScanResponse::class.java
            )

        } catch (e: Exception) {

            throw Exception(
                "Invalid JSON returned by Gemini:\n$rawText",
                e
            )
        }
    }
}