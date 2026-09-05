package com.shuangpinshooter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppColors = lightColorScheme(
    primary = Primary,
    onPrimary = Card,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = Card,
    secondary = Accent,
    onSecondary = Card,
    background = BgTop,
    onBackground = Dark,
    surface = Card,
    onSurface = Dark,
    surfaceVariant = BgBot,
    onSurfaceVariant = Muted,
    error = Danger,
    onError = Card
)

private val AppTypography = Typography(
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
)

@Composable
fun ShuangpinShooterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        content = content
    )
}