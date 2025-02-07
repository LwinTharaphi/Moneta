package com.example.moneta.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen, // Primary buttons, main theme
    secondary = CoolGray, // Secondary UI elements
    tertiary = SoftYellow, // Highlights
    background = DeepBlack, // Background
    surface = DeepBlack,
    onPrimary = Color.White, // Text on primary color
    onSecondary = LightGray, // Text on secondary
    onBackground = LightGray, // Text on background
    onSurface = LightGray // General text
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue, // Main action buttons
    secondary = EmeraldGreen, // Positive indicators
    tertiary = CrimsonRed, // Alerts, delete actions
    background = CoolGray, // App background
    surface = Color.White, // Card backgrounds
    onPrimary = Color.White, // Text on primary buttons
    onSecondary = Color.White, // Secondary text
    onBackground = DarkGray, // Text on background
    onSurface = DarkGray // General text
)

@Composable
fun MonetaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
