package com.github.damontecres.wholphin.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.damontecres.wholphin.data.model.HomeRowViewOptions
import com.github.damontecres.wholphin.preferences.AppSliderPreference
import com.github.damontecres.wholphin.ui.Cards

@Composable
internal fun HomeRowViewOptions.resolvedCardHeight(): Dp {
    val global = LocalInterfaceCustomization.current.cardHeightDp
    val isOverride = heightDp > 0 && heightDp != Cards.HEIGHT_2X3_DP
    return (if (isOverride) heightDp else global).dp
}

internal fun Int.withinBoundsOrDefault(preference: AppSliderPreference<*>): Int {
    val min = preference.min.toInt()
    val max = preference.max.toInt()
    return if (this in min..max) this else preference.defaultValue.toInt()
}
