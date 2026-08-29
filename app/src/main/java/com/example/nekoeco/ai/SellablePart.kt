package com.example.nekoeco.ai

data class SellablePart(
    val name: String,
    val price: Double,
    val weight: Double = 0.0,      // kg
    val confidence: Double = 0.0   // 0.0 - 1.0, drives the High/Medium/Low badge
)