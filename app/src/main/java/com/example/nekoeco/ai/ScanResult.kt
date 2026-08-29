package com.example.nekoeco.ai

data class ScanResult(
    val brand: String,
    val model: String,
    val device: String,
    val condition: String,
    val confidence: Double,
    val estimatedWeight: Double = 0.0, // kg, optional (device-level total weight)
    val recyclability: String = "Medium" // High / Medium / Low — how recyclable the device's materials are
)