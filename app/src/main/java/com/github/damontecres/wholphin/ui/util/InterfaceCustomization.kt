package com.github.damontecres.wholphin.ui.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.github.damontecres.wholphin.preferences.AppPreferences
import com.github.damontecres.wholphin.preferences.DisplaySizeLevel
import com.github.damontecres.wholphin.preferences.DisplayToggle
import com.github.damontecres.wholphin.preferences.cardPercent
import com.github.damontecres.wholphin.preferences.spacingPercent
import com.github.damontecres.wholphin.ui.BASE_SPACING_DP
import com.github.damontecres.wholphin.ui.Cards
import java.util.EnumSet

/**
 * Represents various UI preferences made available via [LocalInterfaceCustomization]
 */
data class InterfaceCustomization(
    val enabledDisplayToggles: EnumSet<DisplayToggle>,
    val textSizeLevel: DisplaySizeLevel = DisplaySizeLevel.DEFAULT,
    val cardSizeLevel: DisplaySizeLevel = DisplaySizeLevel.DEFAULT,
    val spacingLevel: DisplaySizeLevel = DisplaySizeLevel.DEFAULT,
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
        textSizeLevel = prefs.interfacePreferences.textSizeLevel,
        cardSizeLevel = prefs.interfacePreferences.cardSizeLevel,
        spacingLevel = prefs.interfacePreferences.spacingLevel,
    )

    val cardSizePercent: Int
        get() = cardSizeLevel.cardPercent()

    val spacingPercent: Int
        get() = spacingLevel.spacingPercent()

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
