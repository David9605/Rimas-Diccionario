package com.davidgonzalez.rimasdiccionario

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta tomada de los logos de Rimas Diccionario y Versometro (azul marino + dorado).
private val Gold = Color(0xFFE8B94A)
private val Navy = Color(0xFF0A1A45)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1A1200),
    secondary = Color(0xFF9FB4E0),
    background = Color(0xFF061432),
    surface = Color(0xFF0A1A45),
    surfaceVariant = Color(0xFF12296B),
    onBackground = Color(0xFFEDEFF5),
    onSurface = Color(0xFFEDEFF5),
)

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = Color(0xFF8A6A1F),
    background = Color(0xFFFAF7F0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFE8D6),
)

@Composable
fun RimarioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
