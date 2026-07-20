package com.company.selvabooking.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = TropicalGreen,
    onPrimaryContainer = Color.White,
    secondary = DarkTeal,
    onSecondary = Color.White,
    secondaryContainer = SkyBlue,
    onSecondaryContainer = DarkTeal,
    tertiary = ToucanOrange,
    onTertiary = Color.White,
    background = LogoBackground,
    onBackground = DarkTeal,
    surface = LogoBackground,
    onSurface = DarkTeal,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = DarkTeal,
    error = ErrorRed,
    onError = Color.White,
    outline = WoodBrown
)

@Composable
fun SelvaBookingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
