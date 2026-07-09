package com.stillness.focus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StillnessColorScheme = darkColorScheme(
    background = Background,
    surface = Background,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    primary = SecondaryTeal,
    onPrimary = OnSecondary,
    secondary = SecondaryTeal,
    onSecondary = OnSecondary,
    tertiary = TertiaryLavender,
    outlineVariant = OutlineVariant,
)

@Composable
fun StillnessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StillnessColorScheme,
        typography = StillnessTypography,
        content = content,
    )
}
