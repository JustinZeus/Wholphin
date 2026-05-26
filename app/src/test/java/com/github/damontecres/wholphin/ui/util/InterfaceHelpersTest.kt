package com.github.damontecres.wholphin.ui.util

import androidx.compose.ui.unit.Density
import com.github.damontecres.wholphin.data.model.HomeRowConfig
import com.github.damontecres.wholphin.data.model.HomeRowViewOptions
import com.github.damontecres.wholphin.preferences.AppPreference
import com.github.damontecres.wholphin.preferences.AppPreferences
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
        assertEquals(100, 100.withinBoundsOrDefault(AppPreference.CardSize))
        assertEquals(75, 75.withinBoundsOrDefault(AppPreference.CardSize))
        assertEquals(150, 150.withinBoundsOrDefault(AppPreference.CardSize))
    }

    @Test
    fun `withinBoundsOrDefault returns default when below min`() {
        // CardSize is 50..150 default 100; Spacing is 25..175 default 100.
        assertEquals(100, 49.withinBoundsOrDefault(AppPreference.CardSize))
        assertEquals(100, 0.withinBoundsOrDefault(AppPreference.Spacing))
    }

    @Test
    fun `withinBoundsOrDefault returns default when above max`() {
        assertEquals(100, 151.withinBoundsOrDefault(AppPreference.CardSize))
        assertEquals(100, 200.withinBoundsOrDefault(AppPreference.Spacing))
    }

    @Test
    fun `withinBoundsOrDefault includes both bounds inclusively`() {
        // Sanity: min and max themselves are accepted, NOT replaced with default.
        assertEquals(50, 50.withinBoundsOrDefault(AppPreference.CardSize))
        assertEquals(150, 150.withinBoundsOrDefault(AppPreference.CardSize))
        assertEquals(25, 25.withinBoundsOrDefault(AppPreference.Spacing))
        assertEquals(175, 175.withinBoundsOrDefault(AppPreference.Spacing))
    }

    @Test
    fun `resolveCardHeight uses global when heightDp is sentinel 0`() {
        assertEquals(172, resolveCardHeight(0, 100, 172, Cards.HEIGHT_2X3_DP))
        assertEquals(258, resolveCardHeight(0, 100, 258, Cards.HEIGHT_2X3_DP)) // global at 150%
    }

    @Test
    fun `resolveCardHeight uses global when heightDp equals legacy default 172`() {
        // A1 fix: legacy stored heightDp=172 (the pre-PR default) is treated as "no override".
        assertEquals(172, resolveCardHeight(Cards.HEIGHT_2X3_DP, 100, 172, Cards.HEIGHT_2X3_DP))
        assertEquals(258, resolveCardHeight(Cards.HEIGHT_2X3_DP, 100, 258, Cards.HEIGHT_2X3_DP))
    }

    @Test
    fun `resolveCardHeight honours non-default per-row override`() {
        // Display Presets set absolute per-row heights like 128 or 148.
        assertEquals(128, resolveCardHeight(128, 100, 172, Cards.HEIGHT_2X3_DP))
        assertEquals(148, resolveCardHeight(148, 100, 172, Cards.HEIGHT_2X3_DP))
    }

    @Test
    fun `resolveCardHeight applies cardSizeMultiplier on top of global`() {
        // Global card height 172, multiplier 120 -> 206 dp.
        assertEquals(206, resolveCardHeight(0, 120, 172, Cards.HEIGHT_2X3_DP))
        // Global 172, multiplier 75 -> 129.
        assertEquals(129, resolveCardHeight(0, 75, 172, Cards.HEIGHT_2X3_DP))
    }

    @Test
    fun `resolveCardHeight applies cardSizeMultiplier on top of per-row override`() {
        // Preset row at 148 with multiplier 120 -> 177.
        assertEquals(177, resolveCardHeight(148, 120, 172, Cards.HEIGHT_2X3_DP))
        // Episode preset at 128 with multiplier 50 -> 64.
        assertEquals(64, resolveCardHeight(128, 50, 172, Cards.HEIGHT_2X3_DP))
    }

    @Test
    fun `resolveCardHeight uses global when heightDp equals episode natural base 128`() {
        // For an episode-aspect row, the caller passes baseCardHeightDp = HEIGHT_EPISODE (128)
        // and globalCardHeightDp = episodeCardHeightDp. The stored heightDp = 128 then must
        // not be treated as an override - the global Card slider should reach this row.
        assertEquals(128, resolveCardHeight(Cards.HEIGHT_EPISODE, 100, 128, Cards.HEIGHT_EPISODE))
        // Global at 150%: episodeCardHeightDp = 128 * 150 / 100 = 192.
        assertEquals(192, resolveCardHeight(Cards.HEIGHT_EPISODE, 100, 192, Cards.HEIGHT_EPISODE))
    }

    @Test
    fun `resolveCardHeight honours per-row override even when row is episode-natural`() {
        // A user who manually drags an episode row to heightDp = 200 still wins over the global.
        assertEquals(200, resolveCardHeight(200, 100, 192, Cards.HEIGHT_EPISODE))
        // Live-TV-style 96.dp override stays absolute.
        assertEquals(96, resolveCardHeight(96, 100, 192, Cards.HEIGHT_EPISODE))
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
    fun `scaledDensity scales density linearly and leaves fontScale alone`() {
        val base = Density(density = 2.0f, fontScale = 1.0f)
        val scaled = scaledDensity(base, 150)
        // density grows by 1.5 - so dp layouts scale 1.5x.
        assertEquals(3.0f, scaled.density, 0.001f)
        // fontScale stays put - sp text is (density * fontScale) so it also scales linearly.
        // Scaling fontScale too would render text at 1.5^2 = 2.25x.
        assertEquals(1.0f, scaled.fontScale, 0.001f)
    }

    @Test
    fun `scaledDensity at 100 percent returns identity`() {
        val base = Density(density = 2.0f, fontScale = 1.2f)
        val scaled = scaledDensity(base, 100)
        assertEquals(base.density, scaled.density, 0.001f)
        assertEquals(base.fontScale, scaled.fontScale, 0.001f)
    }

    @Test
    fun `scaledDensity preserves user accessibility fontScale across UI scale changes`() {
        // A user with system fontScale 1.3x (accessibility setting) bumping UI scale to 75%
        // should keep fontScale = 1.3 - the UI scale slider is independent of accessibility.
        val base = Density(density = 2.0f, fontScale = 1.3f)
        val scaled = scaledDensity(base, 75)
        assertEquals(1.5f, scaled.density, 0.001f)
        assertEquals(1.3f, scaled.fontScale, 0.001f)
    }

    @Test
    fun `InterfaceCustomization cardHeightDp computes global from slider percent`() {
        val customization = InterfaceCustomization(prefs = AppPreferences.getDefaultInstance())
        // Default cardSizePercent is 100, so cardHeightDp == HEIGHT_2X3_DP.
        assertEquals(Cards.HEIGHT_2X3_DP, customization.cardHeightDp)
    }

    @Test
    fun `InterfaceCustomization cardHeightDp scales linearly`() {
        val customization =
            InterfaceCustomization(
                enabledDisplayToggles =
                    EnumSet.noneOf(
                        com.github.damontecres.wholphin.preferences.DisplayToggle::class.java,
                    ),
                cardSizePercent = 150,
            )
        // 172 * 150 / 100 = 258.
        assertEquals(258, customization.cardHeightDp)
    }

    @Test
    fun `InterfaceCustomization spacingDp scales linearly`() {
        val customization =
            InterfaceCustomization(
                enabledDisplayToggles =
                    EnumSet.noneOf(
                        com.github.damontecres.wholphin.preferences.DisplayToggle::class.java,
                    ),
                spacingPercent = 175,
            )
        // BASE_SPACING_DP = 16. 16 * 175 / 100 = 28.
        assertEquals(28, customization.spacingDp)
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
