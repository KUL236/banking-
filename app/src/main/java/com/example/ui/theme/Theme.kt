package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PolishColorScheme = lightColorScheme(
    primary = PrimaryPolish,
    onPrimary = Color.White,
    secondary = SecondaryPolish,
    onSecondary = Color.White,
    tertiary = TertiaryPolish,
    onTertiary = Color.White,
    background = BackgroundPolish,
    onBackground = TextPolishPrimary,
    surface = SurfacePolish,
    onSurface = TextPolishPrimary,
    surfaceVariant = SurfacePolishVariant,
    onSurfaceVariant = TextPolishPrimary,
    outline = BorderPolish
)

private val LightColorScheme = PolishColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Use the customized professional polish theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Force modern bright professional polish color scheme
    val colorScheme = PolishColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
