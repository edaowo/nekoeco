package com.example.nekoeco.ai

data class ScanResult(
    val brand: String,
    val model: String,
    val device: String,
    val condition: String,
    val confidence: Double,
    val estimatedWeight: Double = 0.0, // kg, optional (device-level total weight)
    val recyclability: String = "Medium", // High / Medium / Low — how recyclable the device's materials are

    // Whether the object in the image is an electronic /
    // electrical device at all. Non-electrical objects
    // (food, furniture, plants, general waste) cannot be
    // booked for pickup. Defaults to true so legacy
    // responses without the flag stay bookable.
    val isElectronic: Boolean = true
)