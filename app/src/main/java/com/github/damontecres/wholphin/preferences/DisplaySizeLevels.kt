package com.github.damontecres.wholphin.preferences

val displaySizeLevelDisplayOrder: List<DisplaySizeLevel> =
    listOf(
        DisplaySizeLevel.EXTRA_SMALL,
        DisplaySizeLevel.SMALL,
        DisplaySizeLevel.DEFAULT,
        DisplaySizeLevel.LARGE,
    )

fun DisplaySizeLevel.textPercent(): Int =
    when (this) {
        DisplaySizeLevel.EXTRA_SMALL -> 88
        DisplaySizeLevel.SMALL -> 94
        DisplaySizeLevel.LARGE -> 108
        DisplaySizeLevel.DEFAULT, DisplaySizeLevel.UNRECOGNIZED -> 100
    }

fun DisplaySizeLevel.cardPercent(): Int =
    when (this) {
        DisplaySizeLevel.EXTRA_SMALL -> 78
        DisplaySizeLevel.SMALL -> 90
        DisplaySizeLevel.LARGE -> 116
        DisplaySizeLevel.DEFAULT, DisplaySizeLevel.UNRECOGNIZED -> 100
    }

fun DisplaySizeLevel.spacingPercent(): Int =
    when (this) {
        DisplaySizeLevel.EXTRA_SMALL -> 75
        DisplaySizeLevel.SMALL -> 88
        DisplaySizeLevel.LARGE -> 125
        DisplaySizeLevel.DEFAULT, DisplaySizeLevel.UNRECOGNIZED -> 100
    }
