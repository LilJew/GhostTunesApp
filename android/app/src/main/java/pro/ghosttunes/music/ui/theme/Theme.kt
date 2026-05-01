package pro.ghosttunes.music.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9C72F5),
    onPrimary = Color(0xFF1A0040),
    primaryContainer = Color(0xFF3A1878),
    onPrimaryContainer = Color(0xFFE6DAFF),
    secondary = Color(0xFF6EA8FE),
    background = Color(0xFF0D0D14),
    onBackground = Color(0xFFEAE8F0),
    surface = Color(0xFF141420),
    onSurface = Color(0xFFEAE8F0),
    surfaceVariant = Color(0xFF1E1E2E),
    onSurfaceVariant = Color(0xFFB0AECA),
    error = Color(0xFFFF6B6B),
)

@Composable
fun MusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}