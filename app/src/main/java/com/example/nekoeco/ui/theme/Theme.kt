package com.example.nekoeco.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ============================================================
// LIGHT COLOR SCHEME
// ============================================================

private val LightColorScheme = lightColorScheme(

    primary = EcoDarkGreen,
    secondary = EcoGreen,

    background = EcoBackground,
    surface = EcoWhite,

    onPrimary = EcoWhite,
    onSecondary = EcoWhite,

    onBackground = DarkText,
    onSurface = DarkText
)


// ============================================================
// DARK COLOR SCHEME
// ============================================================

private val DarkColorScheme = darkColorScheme(

    primary = EcoGreenDark,
    secondary = EcoGreenDarkSecondary,

    background = DarkBackground,
    surface = DarkSurface,

    onPrimary = DarkBackground,
    onSecondary = EcoWhite,

    onBackground = LightText,
    onSurface = LightText
)


// ============================================================
// NEKO ECO THEME
// ============================================================

@Composable
fun NekoEcoTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {

    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}