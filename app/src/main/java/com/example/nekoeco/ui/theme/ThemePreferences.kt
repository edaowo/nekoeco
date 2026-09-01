package com.example.nekoeco.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


// ============================================================
// THEME PREFERENCES
//
// Persists the dark-mode toggle across app restarts using
// SharedPreferences. Exposes a StateFlow so Compose can react
// to changes from any screen.
// ============================================================

object ThemePreferences {

    private const val PREFS_NAME = "neko_eco_prefs"
    private const val KEY_DARK_MODE = "dark_mode"

    private lateinit var prefs: SharedPreferences

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _darkMode.value = prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(value: Boolean) {
        if (!::prefs.isInitialized) return
        prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
        _darkMode.value = value
    }
}