package com.example.nekoeco.ai

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: ResponseContent?
)

data class ResponseContent(
    val parts: List<ResponsePart>?
)

data class ResponsePart(
    val text: String?
)