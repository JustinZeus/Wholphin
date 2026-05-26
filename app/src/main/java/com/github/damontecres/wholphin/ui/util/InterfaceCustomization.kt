package com.github.damontecres.wholphin.ui.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.github.damontecres.wholphin.preferences.AppPreference
import com.github.damontecres.wholphin.preferences.AppPreferences
import com.github.damontecres.wholphin.preferences.DisplayToggle
import com.github.damontecres.wholphin.ui.BASE_SPACING_DP
import com.github.damontecres.wholphin.ui.Cards
import java.util.EnumSet

/**
 * Represents various UI preferences made available via [LocalInterfaceCustomization]
 */
data class InterfaceCustomization(
    val enabledDisplayToggles: EnumSet<DisplayToggle>,
    val uiScalePercent: Int = AppPreference.UiScale.defaultValue.toInt(),
    val cardSizePercent: Int = AppPreference.CardSize.defaultValue.toInt(),
    val spacingPercent: Int = AppPreference.Spacing.defaultValue.toInt(),
) {
    constructor(prefs: AppPreferences) : this(
        enabledDisplayToggles =
            prefs.interfacePreferences.displayTogglesList.let {
                if (it.isEmpty()) {
                    EnumSet.noneOf(DisplayToggle::class.java)
                } else {
                    EnumSet.copyOf(it)
                }
            },
        uiScalePercent =
            prefs.interfacePreferences.uiScalePercent.withinBoundsOrDefault(AppPreference.UiScale),
        cardSizePercent =
            prefs.interfacePreferences.cardSizePercent.withinBoundsOrDefault(AppPreference.CardSize),
        spacingPercent =
            prefs.interfacePreferences.spacingPercent.withinBoundsOrDefault(AppPreference.Spacing),
    )

    val cardHeightDp: Int
        get() = Cards.HEIGHT_2X3_DP * cardSizePercent / 100

    val episodeCardHeightDp: Int
        get() = Cards.HEIGHT_EPISODE * cardSizePercent / 100

    val spacingDp: Int
        get() = BASE_SPACING_DP * spacingPercent / 100
}

val LocalInterfaceCustomization =
    staticCompositionLocalOf<InterfaceCustomization> {
        InterfaceCustomization(EnumSet.allOf(DisplayToggle::class.java))
    }
