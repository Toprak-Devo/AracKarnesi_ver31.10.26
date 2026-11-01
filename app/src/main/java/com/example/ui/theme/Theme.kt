package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryVibrant,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimarySoft,
    onPrimaryContainer = PrimaryVibrantDark,
    secondary = SecondaryVibrant,
    onSecondary = SurfaceWhite,
    secondaryContainer = SecondarySoft,
    onSecondaryContainer = InsuranceBlueDark,
    tertiary = FuelColor,
    onTertiary = SurfaceWhite,
    tertiaryContainer = FuelSoft,
    onTertiaryContainer = FuelColorDark,
    error = AccentRose,
    onError = SurfaceWhite,
    errorContainer = AccentRoseSoft,
    onErrorContainer = MaintenanceRedDark,
    background = BgLight,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMedium,
    outline = BorderLight,
    outlineVariant = BorderMedium
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
