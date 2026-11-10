package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val DarkNavigationColorScheme = darkColorScheme(
    primary = TigrisTurquoise,
    onPrimary = Color(0xFF04221E),
    primaryContainer = Color(0xFF0C3B43),
    onPrimaryContainer = Color(0xFF98F8EC),
    secondary = BabylonGold,
    onSecondary = Color(0xFF281800),
    secondaryContainer = Color(0xFF3E2C0A),
    onSecondaryContainer = BabylonGoldLight,
    tertiary = EuphratesBlue,
    background = MesopotamiaNightBg,
    onBackground = Color(0xFFEAF4FC),
    surface = MesopotamiaNightSurface,
    onSurface = Color(0xFFEAF4FC),
    surfaceVariant = MesopotamiaNightCard,
    onSurfaceVariant = Color(0xFFAEC6DC),
    outline = MesopotamiaNightBorder,
    error = AlertCrimson
)

private val LightNavigationColorScheme = lightColorScheme(
    primary = TigrisTurquoiseDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F6F0),
    onPrimaryContainer = Color(0xFF002B25),
    secondary = Color(0xFFD98200),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECC7),
    onSecondaryContainer = Color(0xFF3A2000),
    tertiary = Color(0xFF0284C7),
    background = DaySandBg,
    onBackground = DayDeepText,
    surface = DaySandSurface,
    onSurface = DayDeepText,
    surfaceVariant = DaySandCard,
    onSurfaceVariant = Color(0xFF3F586E),
    outline = Color(0xFFBBD0E2),
    error = AlertCrimson
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkNavigationColorScheme else LightNavigationColorScheme

    // فرض اتجاه الواجهة من اليمين إلى اليسار (RTL) لدعم اللغة العربية بالكامل
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
