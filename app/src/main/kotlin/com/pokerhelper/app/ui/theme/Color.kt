package com.pokerhelper.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Палитра приложения. Все цвета собраны в одном месте, чтобы тема была
 * легко настраиваемой и поддерживала dark mode.
 *
 * Используется естественная для покера палитра: зелёное сукно как базовый
 * акцент, золотой как highlight (ауты, победы), приглушённые тона для фона.
 */
object PokerColors {

    // === Светлая тема ===
    object Light {
        val Primary = Color(0xFF1B5E20)            // тёмно-зелёный (сукно)
        val OnPrimary = Color(0xFFFFFFFF)
        val PrimaryContainer = Color(0xFFA5D6A7)   // светло-зелёный (фон выделения)
        val OnPrimaryContainer = Color(0xFF002106)

        val Secondary = Color(0xFFFF8F00)          // тёплый янтарь (для аутов)
        val OnSecondary = Color(0xFFFFFFFF)
        val SecondaryContainer = Color(0xFFFFE0B2)
        val OnSecondaryContainer = Color(0xFF2A1800)

        val Tertiary = Color(0xFFB71C1C)           // глубокий красный (фолды, ошибки)
        val OnTertiary = Color(0xFFFFFFFF)

        val Background = Color(0xFFF5F5F0)         // тёплый светло-серый
        val OnBackground = Color(0xFF1A1C18)

        val Surface = Color(0xFFFFFFFF)
        val OnSurface = Color(0xFF1A1C18)
        val SurfaceVariant = Color(0xFFE0E4DB)
        val OnSurfaceVariant = Color(0xFF43483F)

        val Outline = Color(0xFF73796E)
        val Error = Color(0xFFBA1A1A)
        val OnError = Color(0xFFFFFFFF)

        // Семантические цвета приложения
        val CardRed = Color(0xFFD32F2F)             // черви/бубны
        val CardBlack = Color(0xFF212121)           // пики/трефы
        val OutAccent = Color(0xFFFFB74D)           // фон карт-аутов
        val OutBorder = Color(0xFFE65100)           // граница карт-аутов
        val Win = Color(0xFF2E7D32)
        val Fold = Color(0xFF9E9E9E)
    }

    // === Тёмная тема ===
    object Dark {
        val Primary = Color(0xFF8BC34A)             // светло-зелёный (на тёмном фоне)
        val OnPrimary = Color(0xFF003912)
        val PrimaryContainer = Color(0xFF1B5E20)
        val OnPrimaryContainer = Color(0xFFC1F4C2)

        val Secondary = Color(0xFFFFB74D)
        val OnSecondary = Color(0xFF422B00)
        val SecondaryContainer = Color(0xFFE65100)
        val OnSecondaryContainer = Color(0xFFFFE0B2)

        val Tertiary = Color(0xFFFFB4AB)
        val OnTertiary = Color(0xFF690005)

        val Background = Color(0xFF121212)
        val OnBackground = Color(0xFFE3E3DC)

        val Surface = Color(0xFF1E1E1E)
        val OnSurface = Color(0xFFE3E3DC)
        val SurfaceVariant = Color(0xFF2A2D27)
        val OnSurfaceVariant = Color(0xFFC3C8BC)

        val Outline = Color(0xFF8D9387)
        val Error = Color(0xFFFFB4AB)
        val OnError = Color(0xFF690005)

        // Семантические — на тёмном фоне карт сами карты остаются светлыми
        // (как обычные игральные карты), но обводка и оттенки меняются
        val CardRed = Color(0xFFEF5350)
        val CardBlack = Color(0xFF212121)
        val OutAccent = Color(0xFFFFA726)           // ярче на тёмном
        val OutBorder = Color(0xFFFF6F00)
        val Win = Color(0xFF66BB6A)
        val Fold = Color(0xFF757575)
    }
}
