package com.universalwallet.loyalty.core.cards

import androidx.compose.ui.graphics.Color

/**
 * The catalogue of premium loyalty-card visual styles. A card stores its chosen
 * style id in [com.universalwallet.loyalty.domain.model.LoyaltyCard.colorThemeId];
 * [fromId] resolves it back, defaulting to [CLASSIC].
 */
enum class CardStyle(val id: String, val displayName: String) {
    CLASSIC("classic", "Classic"),
    GLASS("glass", "Glass"),
    MINIMAL("minimal", "Minimal"),
    GRADIENT("gradient", "Gradient"),
    MODERN("modern", "Modern"),
    BUSINESS("business", "Business"),
    DARK_PREMIUM("dark_premium", "Dark Premium"),
    NEON("neon", "Neon"),
    SOFT("soft", "Soft"),
    LUXURY("luxury", "Luxury");

    companion object {
        fun fromId(id: String?): CardStyle =
            entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

/**
 * Resolved visual attributes for a card face: the gradient stops, the colour to
 * draw content in, and a muted variant for secondary text. Kept as plain data
 * (no Compose `Brush`) so it is cheap, stable, and previewable.
 */
data class CardVisual(
    val gradient: List<Color>,
    val content: Color,
    val contentMuted: Color,
    val isDark: Boolean,
)

private fun light(content: Color) = content.copy(alpha = 0.72f)

/**
 * Maps a [CardStyle] to its [CardVisual]. An optional [seed] (a store's brand
 * colour) tints the styles that are designed to adapt to the retailer.
 */
fun cardVisual(style: CardStyle, seed: Color? = null): CardVisual {
    val onDark = Color(0xFFFFFFFF)
    val onLight = Color(0xFF1A1C1E)
    return when (style) {
        CardStyle.CLASSIC -> CardVisual(
            gradient = listOf(seed ?: Color(0xFF0A4DA2), (seed ?: Color(0xFF0A4DA2)).darken()),
            content = onDark, contentMuted = light(onDark), isDark = true,
        )
        CardStyle.GLASS -> CardVisual(
            gradient = listOf(Color(0xCCFFFFFF), Color(0x99E3E6EE)),
            content = onLight, contentMuted = light(onLight), isDark = false,
        )
        CardStyle.MINIMAL -> CardVisual(
            gradient = listOf(Color(0xFFF7F8FB), Color(0xFFEDEFF4)),
            content = onLight, contentMuted = light(onLight), isDark = false,
        )
        CardStyle.GRADIENT -> CardVisual(
            gradient = listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
            content = onDark, contentMuted = light(onDark), isDark = true,
        )
        CardStyle.MODERN -> CardVisual(
            gradient = listOf(Color(0xFF111827), Color(0xFF0F766E)),
            content = onDark, contentMuted = light(onDark), isDark = true,
        )
        CardStyle.BUSINESS -> CardVisual(
            gradient = listOf(Color(0xFF1F2A44), Color(0xFF3A4A6B)),
            content = onDark, contentMuted = light(onDark), isDark = true,
        )
        CardStyle.DARK_PREMIUM -> CardVisual(
            gradient = listOf(Color(0xFF0B0B0D), Color(0xFF24262B)),
            content = Color(0xFFE9C46A), contentMuted = Color(0xFFE9C46A).copy(alpha = 0.7f), isDark = true,
        )
        CardStyle.NEON -> CardVisual(
            gradient = listOf(Color(0xFFFF0080), Color(0xFF7928CA), Color(0xFF00D4FF)),
            content = onDark, contentMuted = light(onDark), isDark = true,
        )
        CardStyle.SOFT -> CardVisual(
            gradient = listOf(Color(0xFFFFE0EC), Color(0xFFE0F0FF)),
            content = onLight, contentMuted = light(onLight), isDark = false,
        )
        CardStyle.LUXURY -> CardVisual(
            gradient = listOf(Color(0xFF2D0B59), Color(0xFF7B2FF7)),
            content = Color(0xFFF5D061), contentMuted = Color(0xFFF5D061).copy(alpha = 0.75f), isDark = true,
        )
    }
}

/** Darkens a colour toward black for the second gradient stop. */
private fun Color.darken(factor: Float = 0.72f): Color =
    Color(red * factor, green * factor, blue * factor, alpha)

/** Parses a "#RRGGBB" / "#AARRGGBB" string into a [Color], or null if invalid. */
fun hexToColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    val cleaned = hex.removePrefix("#")
    return runCatching {
        when (cleaned.length) {
            6 -> Color(("FF$cleaned").toLong(16))
            8 -> Color(cleaned.toLong(16))
            else -> null
        }
    }.getOrNull()
}
