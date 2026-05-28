package com.github.damontecres.wholphin.ui.util

import com.github.damontecres.wholphin.data.model.HomeRowConfig
import com.github.damontecres.wholphin.data.model.HomeRowViewOptions
import com.github.damontecres.wholphin.preferences.AppPreference
import com.github.damontecres.wholphin.preferences.AppPreferences
import com.github.damontecres.wholphin.preferences.DisplaySizeLevel
import com.github.damontecres.wholphin.services.migrateLegacyRowSpacing
import com.github.damontecres.wholphin.ui.AspectRatio
import com.github.damontecres.wholphin.ui.Cards
import org.jellyfin.sdk.model.UUID
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.EnumSet

class InterfaceHelpersTest {
    @Test
    fun `withinBoundsOrDefault returns value when in range`() {
        assertEquals(30, 30.withinBoundsOrDefault(AppPreference.SkipForward))
        assertEquals(60, 60.withinBoundsOrDefault(AppPreference.SkipForward))
        assertEquals(120, 120.withinBoundsOrDefault(AppPreference.SkipForward))
    }

    @Test
    fun `withinBoundsOrDefault returns default when below min`() {
        assertEquals(30, 4.withinBoundsOrDefault(AppPreference.SkipForward))
        assertEquals(30, 0.withinBoundsOrDefault(AppPreference.SkipForward))
    }

    @Test
    fun `withinBoundsOrDefault returns default when above max`() {
        assertEquals(30, 301.withinBoundsOrDefault(AppPreference.SkipForward))
        assertEquals(30, 1000.withinBoundsOrDefault(AppPreference.SkipForward))
    }

    @Test
    fun `withinBoundsOrDefault includes both bounds inclusively`() {
        // Sanity: min and max themselves are accepted, NOT replaced with default.
        assertEquals(5, 5.withinBoundsOrDefault(AppPreference.SkipForward))
        assertEquals(300, 300.withinBoundsOrDefault(AppPreference.SkipForward))
    }

    @Test
    fun `resolveCardHeight uses natural base when heightDp is sentinel 0`() {
        // Sentinel 0 falls back to the natural base; global percent scales it.
        assertEquals(172, resolveCardHeight(0, 100, 100, Cards.HEIGHT_2X3_DP))
        assertEquals(258, resolveCardHeight(0, 100, 150, Cards.HEIGHT_2X3_DP))
    }

    @Test
    fun `resolveCardHeight treats stored heightDp matching natural base as no override`() {
        // Pre-PR builds wrote heightDp=172 (the poster natural base) into JSON. Episode rows
        // wrote 128. Both must be transparent to the global slider so existing users get
        // immediate scaling without a reset.
        assertEquals(172, resolveCardHeight(Cards.HEIGHT_2X3_DP, 100, 100, Cards.HEIGHT_2X3_DP))
        assertEquals(258, resolveCardHeight(Cards.HEIGHT_2X3_DP, 100, 150, Cards.HEIGHT_2X3_DP))
        assertEquals(128, resolveCardHeight(Cards.HEIGHT_EPISODE, 100, 100, Cards.HEIGHT_EPISODE))
        assertEquals(192, resolveCardHeight(Cards.HEIGHT_EPISODE, 100, 150, Cards.HEIGHT_EPISODE))
    }

    @Test
    fun `resolveCardHeight honours per-row override`() {
        // Preset rows (148) and live-TV (96) take precedence over the natural base.
        assertEquals(148, resolveCardHeight(148, 100, 100, Cards.HEIGHT_2X3_DP))
        assertEquals(96, resolveCardHeight(96, 100, 100, Cards.HEIGHT_EPISODE))
    }

    @Test
    fun `resolveCardHeight applies global percent on top of per-row override`() {
        // Preset 148 with global 150% renders at 222dp. The user moves one slider; every
        // row, including their preset-set ones, follows.
        assertEquals(222, resolveCardHeight(148, 100, 150, Cards.HEIGHT_2X3_DP))
        // Same at 50%: preset 148 * 0.5 = 74.
        assertEquals(74, resolveCardHeight(148, 100, 50, Cards.HEIGHT_2X3_DP))
        // Live-TV 96 * 125% = 120.
        assertEquals(120, resolveCardHeight(96, 100, 125, Cards.HEIGHT_EPISODE))
    }

    @Test
    fun `resolveCardHeight applies cardSizeMultiplier and global percent together`() {
        // Natural 172 * global 120% * per-row 110% = 226.
        assertEquals(226, resolveCardHeight(0, 110, 120, Cards.HEIGHT_2X3_DP))
        // Preset 148 * global 150% * per-row 110% = 244.
        assertEquals(244, resolveCardHeight(148, 110, 150, Cards.HEIGHT_2X3_DP))
        // Per-row alone (global at 100%) still works: preset 148 * 120% = 177.
        assertEquals(177, resolveCardHeight(148, 120, 100, Cards.HEIGHT_2X3_DP))
    }

    @Test
    fun `naturalCardBaseDp maps WIDE to episode base`() {
        assertEquals(Cards.HEIGHT_EPISODE, naturalCardBaseDp(AspectRatio.WIDE))
    }

    @Test
    fun `naturalCardBaseDp maps TALL, SQUARE, FOUR_THREE to poster base`() {
        // SQUARE and FOUR_THREE piggyback on the poster global. Music preset rows (SQUARE)
        // store their own absolute heightDp = 100 so they still hit the override branch -
        // the natural base just supplies the fallback if a row ever stores no override.
        assertEquals(Cards.HEIGHT_2X3_DP, naturalCardBaseDp(AspectRatio.TALL))
        assertEquals(Cards.HEIGHT_2X3_DP, naturalCardBaseDp(AspectRatio.SQUARE))
        assertEquals(Cards.HEIGHT_2X3_DP, naturalCardBaseDp(AspectRatio.FOUR_THREE))
    }

    @Test
    fun `InterfaceCustomization cardHeightDp at DEFAULT level matches natural base`() {
        val customization = InterfaceCustomization(prefs = AppPreferences.getDefaultInstance())
        // proto-zero-default lands cardSizeLevel on DEFAULT (100%), so cardHeightDp == HEIGHT_2X3_DP.
        assertEquals(Cards.HEIGHT_2X3_DP, customization.cardHeightDp)
    }

    @Test
    fun `InterfaceCustomization cardHeightDp scales by Card level percent`() {
        val customization =
            InterfaceCustomization(
                enabledDisplayToggles =
                    EnumSet.noneOf(
                        com.github.damontecres.wholphin.preferences.DisplayToggle::class.java,
                    ),
                cardSizeLevel = DisplaySizeLevel.LARGE,
            )
        // LARGE → cardPercent() = 115. 172 * 115 / 100 = 197 (integer truncation).
        assertEquals(197, customization.cardHeightDp)
    }

    @Test
    fun `InterfaceCustomization spacingDp scales by Spacing level percent`() {
        val customization =
            InterfaceCustomization(
                enabledDisplayToggles =
                    EnumSet.noneOf(
                        com.github.damontecres.wholphin.preferences.DisplayToggle::class.java,
                    ),
                spacingLevel = DisplaySizeLevel.LARGE,
            )
        // LARGE → spacingPercent() = 118. BASE_SPACING_DP = 16. 16 * 118 / 100 = 18.
        assertEquals(18, customization.spacingDp)
    }

    @Test
    fun `migrateLegacyRowSpacing preserves row when legacy field is absent`() {
        val row = sampleRow(spacingMultiplier = 100)
        val result = migrateLegacyRowSpacing(row, legacySpacingDp = null)
        assertEquals(100, result.viewOptions.spacingMultiplier)
    }

    @Test
    fun `migrateLegacyRowSpacing preserves row when legacy value matches the default`() {
        val row = sampleRow(spacingMultiplier = 100)
        val result = migrateLegacyRowSpacing(row, legacySpacingDp = 16)
        assertEquals(100, result.viewOptions.spacingMultiplier)
    }

    @Test
    fun `migrateLegacyRowSpacing translates legacy spacing into spacingMultiplier`() {
        val row = sampleRow(spacingMultiplier = 100)
        // legacy 20 dp -> 20 / 16 * 100 = 125 -> snaps to 125.
        val result = migrateLegacyRowSpacing(row, legacySpacingDp = 20)
        assertEquals(125, result.viewOptions.spacingMultiplier)
    }

    @Test
    fun `migrateLegacyRowSpacing snaps to nearest step of 5`() {
        val row = sampleRow(spacingMultiplier = 100)
        // legacy 18 dp -> 18 / 16 * 100 = 112 -> floor-snap to 110.
        val result = migrateLegacyRowSpacing(row, legacySpacingDp = 18)
        assertEquals(110, result.viewOptions.spacingMultiplier)
    }

    @Test
    fun `migrateLegacyRowSpacing clamps extreme values into 50-150 range`() {
        val rowLow = sampleRow(spacingMultiplier = 100)
        // legacy 4 dp -> 25 -> clamped to 50.
        val resultLow = migrateLegacyRowSpacing(rowLow, legacySpacingDp = 4)
        assertEquals(50, resultLow.viewOptions.spacingMultiplier)

        val rowHigh = sampleRow(spacingMultiplier = 100)
        // legacy 40 dp -> 250 -> clamped to 150.
        val resultHigh = migrateLegacyRowSpacing(rowHigh, legacySpacingDp = 40)
        assertEquals(150, resultHigh.viewOptions.spacingMultiplier)
    }

    @Test
    fun `migrateLegacyRowSpacing does not overwrite a non-default spacingMultiplier`() {
        // If the user has already set a new-model multiplier, the legacy value must not clobber it.
        val row = sampleRow(spacingMultiplier = 120)
        val result = migrateLegacyRowSpacing(row, legacySpacingDp = 24)
        assertEquals(120, result.viewOptions.spacingMultiplier)
    }

    private fun sampleRow(spacingMultiplier: Int): HomeRowConfig =
        HomeRowConfig.RecentlyAdded(
            parentId = UUID.randomUUID(),
            viewOptions = HomeRowViewOptions(spacingMultiplier = spacingMultiplier),
        )
}
