package com.smashedtatoes.stepcount.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaporwaveColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = OnVaporwavePrimary,
    primaryContainer = NeonPurple,
    onPrimaryContainer = Color.White,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = VaporwaveSurfaceVariant,
    onSecondaryContainer = NeonCyan,
    tertiary = NeonYellow,
    onTertiary = Color.Black,
    background = VaporwaveBackground,
    onBackground = OnVaporwaveBackground,
    surface = VaporwaveSurface,
    onSurface = OnVaporwaveSurface,
    surfaceVariant = VaporwaveSurfaceVariant,
    onSurfaceVariant = OnVaporwaveBackground,
    outline = VaporwaveOutline
)

@Composable
fun StepCountTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VaporwaveColorScheme,
        typography = Typography,
        content = content
    )
}
