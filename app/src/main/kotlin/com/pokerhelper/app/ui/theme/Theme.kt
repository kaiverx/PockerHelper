package com.pokerhelper.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Семантические цвета приложения, которых нет в стандартной MaterialTheme.
 * Используются через PokerHelperTheme.semantic.cardRed и т.д. в Composable.
 */
data class SemanticColors(
    val cardRed: Color,
    val cardBlack: Color,
    val outAccent: Color,
    val outBorder: Color,
    val win: Color,
    val fold: Color
)

private val LocalSemanticColors = staticCompositionLocalOf<SemanticColors> {
    error("SemanticColors not provided")
}

/**
 * Доступ к семантическим цветам и тёмной теме внутри Composable:
 *   val win = PokerHelperTheme.semantic.win
 */
object PokerHelperTheme {
    val semantic: SemanticColors
        @Composable @ReadOnlyComposable get() = LocalSemanticColors.current
}

private val LightScheme = lightColorScheme(
    primary = PokerColors.Light.Primary,
    onPrimary = PokerColors.Light.OnPrimary,
    primaryContainer = PokerColors.Light.PrimaryContainer,
    onPrimaryContainer = PokerColors.Light.OnPrimaryContainer,
    secondary = PokerColors.Light.Secondary,
    onSecondary = PokerColors.Light.OnSecondary,
    secondaryContainer = PokerColors.Light.SecondaryContainer,
    onSecondaryContainer = PokerColors.Light.OnSecondaryContainer,
    tertiary = PokerColors.Light.Tertiary,
    onTertiary = PokerColors.Light.OnTertiary,
    background = PokerColors.Light.Background,
    onBackground = PokerColors.Light.OnBackground,
    surface = PokerColors.Light.Surface,
    onSurface = PokerColors.Light.OnSurface,
    surfaceVariant = PokerColors.Light.SurfaceVariant,
    onSurfaceVariant = PokerColors.Light.OnSurfaceVariant,
    outline = PokerColors.Light.Outline,
    error = PokerColors.Light.Error,
    onError = PokerColors.Light.OnError
)

private val DarkScheme = darkColorScheme(
    primary = PokerColors.Dark.Primary,
    onPrimary = PokerColors.Dark.OnPrimary,
    primaryContainer = PokerColors.Dark.PrimaryContainer,
    onPrimaryContainer = PokerColors.Dark.OnPrimaryContainer,
    secondary = PokerColors.Dark.Secondary,
    onSecondary = PokerColors.Dark.OnSecondary,
    secondaryContainer = PokerColors.Dark.SecondaryContainer,
    onSecondaryContainer = PokerColors.Dark.OnSecondaryContainer,
    tertiary = PokerColors.Dark.Tertiary,
    onTertiary = PokerColors.Dark.OnTertiary,
    background = PokerColors.Dark.Background,
    onBackground = PokerColors.Dark.OnBackground,
    surface = PokerColors.Dark.Surface,
    onSurface = PokerColors.Dark.OnSurface,
    surfaceVariant = PokerColors.Dark.SurfaceVariant,
    onSurfaceVariant = PokerColors.Dark.OnSurfaceVariant,
    outline = PokerColors.Dark.Outline,
    error = PokerColors.Dark.Error,
    onError = PokerColors.Dark.OnError
)

private val LightSemantic = SemanticColors(
    cardRed = PokerColors.Light.CardRed,
    cardBlack = PokerColors.Light.CardBlack,
    outAccent = PokerColors.Light.OutAccent,
    outBorder = PokerColors.Light.OutBorder,
    win = PokerColors.Light.Win,
    fold = PokerColors.Light.Fold
)

private val DarkSemantic = SemanticColors(
    cardRed = PokerColors.Dark.CardRed,
    cardBlack = PokerColors.Dark.CardBlack,
    outAccent = PokerColors.Dark.OutAccent,
    outBorder = PokerColors.Dark.OutBorder,
    win = PokerColors.Dark.Win,
    fold = PokerColors.Dark.Fold
)

/**
 * Корневая тема приложения. По умолчанию следует системной настройке
 * (Settings → Display → Dark theme на устройстве).
 */
@Composable
fun PokerHelperTheme(
    useDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDark) DarkScheme else LightScheme
    val semantic = if (useDark) DarkSemantic else LightSemantic

    androidx.compose.runtime.CompositionLocalProvider(
        LocalSemanticColors provides semantic
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
