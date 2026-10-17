package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SystemBlue,
    onPrimary = Label,
    primaryContainer = SecondarySystemBackground,
    onPrimaryContainer = Label,
    secondary = SystemBlue,
    onSecondary = Label,
    background = SystemBackground,
    onBackground = Label,
    surface = SecondarySystemBackground,
    onSurface = Label,
    surfaceVariant = TertiarySystemBackground,
    onSurfaceVariant = SecondaryLabel,
    outline = Separator,
    outlineVariant = OpaqueSeparator,
    error = SystemRed,
    onError = Label
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
