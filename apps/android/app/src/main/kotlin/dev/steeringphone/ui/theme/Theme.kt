package dev.steeringphone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RacingPrimaryRed,
    secondary = RacingAccentCyan,
    background = RacingDarkBg,
    surface = RacingCardBg,
    onPrimary = RacingTextPrimary,
    onBackground = RacingTextPrimary,
    onSurface = RacingTextPrimary
)

@Composable
fun SteeringPhoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
