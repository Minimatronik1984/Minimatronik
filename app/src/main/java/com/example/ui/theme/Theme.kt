package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    secondary = AccentTeal,
    tertiary = WarmAmber,
    background = MidnightBlue,
    surface = CardNavy,
    onPrimary = MidnightBlue,
    onSecondary = MidnightBlue,
    onBackground = TextLight,
    onSurface = TextLight,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    secondary = FinancialTeal,
    tertiary = WarmAmber,
    background = SoftGrayBg,
    surface = SoftSurface,
    onPrimary = SoftSurface,
    onSecondary = SoftSurface,
    onBackground = TextDark,
    onSurface = TextDark,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
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
