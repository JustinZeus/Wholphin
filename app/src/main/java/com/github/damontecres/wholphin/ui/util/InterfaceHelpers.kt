package com.github.damontecres.wholphin.ui.util

import com.github.damontecres.wholphin.preferences.AppSliderPreference

internal fun Int.withinBoundsOrDefault(preference: AppSliderPreference<*>): Int {
    val min = preference.min.toInt()
    val max = preference.max.toInt()
    return if (this in min..max) this else preference.defaultValue.toInt()
}
