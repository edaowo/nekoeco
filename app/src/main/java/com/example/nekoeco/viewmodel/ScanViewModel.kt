package com.example.nekoeco.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nekoeco.ai.AiScanResponse
import com.example.nekoeco.ai.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ScanViewModel(
    private val repository: GeminiRepository
) : ViewModel() {

    // =========================================================
    // AI RESULT
    // =========================================================

    private val _result =
        MutableStateFlow<AiScanResponse?>(null)

    val result: StateFlow<AiScanResponse?> =
        _result


    // =========================================================
    // LOADING
    // =========================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading


    // =========================================================
    // ERROR
    // =========================================================

    private val _error =
        MutableStateFlow<String?>(null)

    val error: StateFlow<String?> =
        _error


    // =========================================================
    // SCAN IMAGE
    // =========================================================

    fun scanImage(imageFile: File) {

        viewModelScope.launch {

            // -----------------------------------------------
            // IMPORTANT:
            // Clear previous result BEFORE starting
            // a new scan.
            // -----------------------------------------------

            _result.value = null

            _error.value = null

            _isLoading.value = true


            try {

                // -------------------------------------------
                // SEND IMAGE TO GEMINI
                // -------------------------------------------

                val response =
                    repository.analyzeImage(imageFile)


                // -------------------------------------------
                // SAVE NEW RESULT
                // -------------------------------------------

                _result.value =
                    response

            } catch (e: Exception) {

                _error.value =
                    e.message
                        ?: "AI scan failed"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // =========================================================
    // CLEAR RESULT
    // =========================================================
    //
    // Call this when leaving the Result screen.
    //
    // This prevents the previous AI result from appearing
    // automatically when opening Camera again.
    //
    // =========================================================

    fun clearResult() {

        _result.value = null

        _error.value = null

        _isLoading.value = false
    }
}