package com.example.nekoeco.ai

data class AiScanResponse(
    val device: ScanResult,
    val parts: List<SellablePart>,
    val tips: List<String> = emptyList()
) {
    val totalRecoveryValue: Double
        get() = parts.sumOf { it.price }

    val totalWeight: Double
        get() = if (device.estimatedWeight > 0) {
            device.estimatedWeight
        } else {
            parts.sumOf { it.weight }
        }
}