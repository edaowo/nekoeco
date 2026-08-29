package com.example.nekoeco.ai

/**
 * Converts a 0.0–1.0 confidence score into the High / Medium / Low
 * label shown as a badge in the UI (matches the wireframe).
 */
fun confidenceLabel(confidence: Double): String {
    // Gemini sometimes returns confidence as a 0-100 style number
    // (e.g. 50) instead of the requested 0.0-1.0 decimal (e.g. 0.5).
    // Normalize so the badge always maps correctly either way.
    val normalized = if (confidence > 1.0) confidence / 100.0 else confidence

    return when {
        normalized >= 0.75 -> "High"
        normalized >= 0.4 -> "Medium"
        else -> "Low"
    }
}