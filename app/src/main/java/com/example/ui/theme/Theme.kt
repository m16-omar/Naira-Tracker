package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreenLight,
    onPrimary = CharcoalDark,
    primaryContainer = EmeraldGreenDark,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = MintAccent,
    onSecondary = CharcoalDark,
    secondaryContainer = CharcoalCard,
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = WarningAmber,
    background = CharcoalDark,
    onBackground = TextPrimaryDark,
    surface = CharcoalSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = CharcoalBorder,
    error = ExpenseRed
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = EmeraldGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF1B5E20),
    tertiary = WarningAmber,
    background = OffWhiteBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEDF4EE),
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    error = ExpenseRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
