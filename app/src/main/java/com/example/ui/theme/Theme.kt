package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,              // Lavender: #D0BCFF
    onPrimary = VioletAccent,          // Deep violet: #381E72
    primaryContainer = VioletAccent,   // Deep violet container
    onPrimaryContainer = TextPrimary,  // Cool light gray text
    secondary = InfoBlue,              // Ice Blue: #7FCFFF
    secondaryContainer = DarkSlateCard,// Deep slate card: #2D3033
    onSecondaryContainer = TextPrimary,// Cool light text
    tertiary = LightPinkAccent,        // Soft pink highlight: #EFB8C8
    background = DarkSlateBackground,  // Cozy near-black slate: #1A1C1E
    surface = DarkSlateSurface,        // Mid-tone surface: #242629
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = DarkSlateCard,    // Card body: #2D3033
    onSurfaceVariant = TextSecondary,  // Cool gray secondary text: #C4C6CF
    outline = BorderSlate,             // Outline separator border: #45474F
    error = CrimsonAlert
)

// We serve the premium dark theme universally for the ultimate elegant fintech feel
private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // disabled to enforce premium curated brand colors
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

