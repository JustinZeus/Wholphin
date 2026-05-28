package com.github.damontecres.wholphin.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.github.damontecres.wholphin.data.model.HomeRowViewOptions
import com.github.damontecres.wholphin.preferences.AppSliderPreference
import com.github.damontecres.wholphin.preferences.scaledSp
import com.github.damontecres.wholphin.ui.AspectRatio
import com.github.damontecres.wholphin.ui.Cards

@Composable
internal fun HomeRowViewOptions.resolvedCardHeight(): Dp =
    resolveCardHeight(
        heightDp = heightDp,
        cardSizeMultiplier = cardSizeMultiplier,
        globalCardSizePercent = LocalInterfaceCustomization.current.cardSizePercent,
        naturalCardHeightDp = naturalCardBaseDp(aspectRatio),
    ).dp

// SQUARE / FOUR_THREE rows piggyback on the poster (TALL) global; they either store an
// explicit override (Music preset rows at 100dp, etc.) or compose well enough at poster size.
internal fun naturalCardBaseDp(aspectRatio: AspectRatio): Int =
    when (aspectRatio) {
        AspectRatio.WIDE -> Cards.HEIGHT_EPISODE
        AspectRatio.TALL, AspectRatio.SQUARE, AspectRatio.FOUR_THREE -> Cards.HEIGHT_2X3_DP
    }

// The global Card size, the per-row multiplier, and any preset-set absolute heightDp
// all stack. Preset 148dp + global 116% + per-row 110% = ~189dp. The base is the row's
// preset if present, otherwise the natural height for its aspect.
internal fun resolveCardHeight(
    heightDp: Int,
    cardSizeMultiplier: Int,
    globalCardSizePercent: Int,
    naturalCardHeightDp: Int,
): Int {
    val isOverride = heightDp > 0 && heightDp != naturalCardHeightDp
    val base = if (isOverride) heightDp else naturalCardHeightDp
    return base * globalCardSizePercent / 100 * cardSizeMultiplier / 100
}

@Composable
@ReadOnlyComposable
internal fun scaledSp(baseSp: Int): TextUnit = LocalInterfaceCustomization.current.textSizeLevel.scaledSp(baseSp)

internal fun Int.withinBoundsOrDefault(preference: AppSliderPreference<*>): Int {
    val min = preference.min.toInt()
    val max = preference.max.toInt()
    return if (this in min..max) this else preference.defaultValue.toInt()
}
