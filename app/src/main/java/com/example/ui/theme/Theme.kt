package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = DeepCharcoal,
    secondary = BrandSecondary,
    onSecondary = PureWhite,
    tertiary = SageGreen,
    background = Color(0xFF0E1012),
    onBackground = Color(0xFFE2E4E8),
    surface = Color(0xFF16181A),
    onSurface = Color(0xFFE2E4E8),
    surfaceVariant = Color(0xFF202326),
    onSurfaceVariant = BrandTertiary,
    outline = BrandSecondary,
    error = SunsetOrange
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = PureWhite,
    secondary = BrandSecondary,
    onSecondary = PureWhite,
    tertiary = SageGreen,
    background = AppleLightGray,
    onBackground = DeepCharcoal,
    surface = PureWhite,
    onSurface = DeepCharcoal,
    surfaceVariant = Color(0xFFEBECEF),
    onSurfaceVariant = BrandSecondary,
    outline = LightGrayBorder,
    error = SunsetOrange
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamicColor by default to preserve custom branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
