package com.github.damontecres.wholphin.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.damontecres.wholphin.data.model.HomeRowViewOptions
import com.github.damontecres.wholphin.preferences.AppSliderPreference
import com.github.damontecres.wholphin.ui.AspectRatio
import com.github.damontecres.wholphin.ui.Cards

@Composable
internal fun HomeRowViewOptions.resolvedCardHeight(): Dp {
    val custom = LocalInterfaceCustomization.current
    val naturalBase = naturalCardBaseDp(aspectRatio)
    val globalForThisRow =
        if (naturalBase == Cards.HEIGHT_EPISODE) custom.episodeCardHeightDp else custom.cardHeightDp
    return resolveCardHeight(
        heightDp = heightDp,
        cardSizeMultiplier = cardSizeMultiplier,
        globalCardHeightDp = globalForThisRow,
        baseCardHeightDp = naturalBase,
    ).dp
}

// SQUARE / FOUR_THREE rows piggyback on the poster (TALL) global; they either store an
// explicit override (Music preset rows at 100dp, etc.) or compose well enough at poster size.
internal fun naturalCardBaseDp(aspectRatio: AspectRatio): Int =
    when (aspectRatio) {
        AspectRatio.WIDE -> Cards.HEIGHT_EPISODE
        AspectRatio.TALL, AspectRatio.SQUARE, AspectRatio.FOUR_THREE -> Cards.HEIGHT_2X3_DP
    }

internal fun resolveCardHeight(
    heightDp: Int,
    cardSizeMultiplier: Int,
    globalCardHeightDp: Int,
    baseCardHeightDp: Int,
): Int {
    val isOverride = heightDp > 0 && heightDp != baseCardHeightDp
    val base = if (isOverride) heightDp else globalCardHeightDp
    return base * cardSizeMultiplier / 100
}

// Scales density (and therefore both dp layouts and sp text) linearly by uiScalePercent.
// fontScale is left at the user's system value so Android's accessibility setting still
// applies on top - scaling both would render text at factor^2 instead of factor.
internal fun scaledDensity(
    base: Density,
    uiScalePercent: Int,
): Density {
    val factor = uiScalePercent / 100f
    return Density(
        density = base.density * factor,
        fontScale = base.fontScale,
    )
}

internal fun Int.withinBoundsOrDefault(preference: AppSliderPreference<*>): Int {
    val min = preference.min.toInt()
    val max = preference.max.toInt()
    return if (this in min..max) this else preference.defaultValue.toInt()
}
