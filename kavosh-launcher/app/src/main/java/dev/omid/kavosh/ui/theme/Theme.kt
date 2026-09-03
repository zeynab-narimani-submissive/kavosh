package dev.omid.kavosh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KavoshColorScheme = darkColorScheme(
    primary = KavoshAccent,
    background = KavoshBackground,
    surface = KavoshSurface,
    surfaceVariant = KavoshSurfaceVariant,
    onBackground = KavoshOnSurface,
    onSurface = KavoshOnSurface,
    onSurfaceVariant = KavoshOnSurfaceMuted,
)

@Composable
fun KavoshTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KavoshColorScheme,
        typography = KavoshTypography,
        content = content,
    )
}
