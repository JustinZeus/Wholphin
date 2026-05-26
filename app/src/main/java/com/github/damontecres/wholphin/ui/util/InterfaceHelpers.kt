package com.github.damontecres.wholphin.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.damontecres.wholphin.data.model.HomeRowViewOptions
import com.github.damontecres.wholphin.preferences.AppSliderPreference
import com.github.damontecres.wholphin.ui.AspectRatio
import com.github.damontecres.wholphin.ui.Cards

@Composable
internal fun HomeRowViewOptions.resolvedCardHeight(): Dp {
    val custom = LocalInterfaceCustomization.current
    val isWide = aspectRatio == AspectRatio.WIDE
    val naturalBase = if (isWide) Cards.HEIGHT_EPISODE else Cards.HEIGHT_2X3_DP
    val globalForThisRow = if (isWide) custom.episodeCardHeightDp else custom.cardHeightDp
    return resolveCardHeight(
        heightDp = heightDp,
        cardSizeMultiplier = cardSizeMultiplier,
        globalCardHeightDp = globalForThisRow,
        baseCardHeightDp = naturalBase,
    ).dp
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

internal fun Int.withinBoundsOrDefault(preference: AppSliderPreference<*>): Int {
    val min = preference.min.toInt()
    val max = preference.max.toInt()
    return if (this in min..max) this else preference.defaultValue.toInt()
}
