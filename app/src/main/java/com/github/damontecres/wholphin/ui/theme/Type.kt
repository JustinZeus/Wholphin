package com.github.damontecres.wholphin.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isSpecified
import com.github.damontecres.wholphin.preferences.DisplaySizeLevel
import com.github.damontecres.wholphin.preferences.textPercent
import androidx.compose.material3.Typography as MaterialTypography
import androidx.tv.material3.Typography as TvTypography

val AppTypography: TvTypography = TvTypography()

fun TvTypography.scaledBy(level: DisplaySizeLevel): TvTypography {
    val scale = level.textPercent() / 100f
    if (scale == 1f) return this
    return TvTypography(
        displayLarge = displayLarge.scaledBy(scale),
        displayMedium = displayMedium.scaledBy(scale),
        displaySmall = displaySmall.scaledBy(scale),
        headlineLarge = headlineLarge.scaledBy(scale),
        headlineMedium = headlineMedium.scaledBy(scale),
        headlineSmall = headlineSmall.scaledBy(scale),
        titleLarge = titleLarge.scaledBy(scale),
        titleMedium = titleMedium.scaledBy(scale),
        titleSmall = titleSmall.scaledBy(scale),
        bodyLarge = bodyLarge.scaledBy(scale),
        bodyMedium = bodyMedium.scaledBy(scale),
        bodySmall = bodySmall.scaledBy(scale),
        labelLarge = labelLarge.scaledBy(scale),
        labelMedium = labelMedium.scaledBy(scale),
        labelSmall = labelSmall.scaledBy(scale),
    )
}

fun TvTypography.toMaterialTypography(): MaterialTypography =
    MaterialTypography(
        displayLarge = displayLarge,
        displayMedium = displayMedium,
        displaySmall = displaySmall,
        headlineLarge = headlineLarge,
        headlineMedium = headlineMedium,
        headlineSmall = headlineSmall,
        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = titleSmall,
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall,
        labelLarge = labelLarge,
        labelMedium = labelMedium,
        labelSmall = labelSmall,
    )

private fun TextStyle.scaledBy(scale: Float): TextStyle {
    if (scale == 1f) return this
    return copy(
        fontSize = if (fontSize.isSpecified) fontSize * scale else fontSize,
        lineHeight = if (lineHeight.isSpecified) lineHeight * scale else lineHeight,
    )
}
