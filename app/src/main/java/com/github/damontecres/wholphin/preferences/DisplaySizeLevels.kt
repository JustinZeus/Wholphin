package com.github.damontecres.wholphin.preferences

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val displaySizeLevelDisplayOrder: List<DisplaySizeLevel> =
    listOf(
        DisplaySizeLevel.EXTRA_SMALL,
        DisplaySizeLevel.SMALL,
        DisplaySizeLevel.DEFAULT,
        DisplaySizeLevel.LARGE,
    )

fun DisplaySizeLevel.scaledSp(baseSp: Int): TextUnit {
    if (this == DisplaySizeLevel.DEFAULT || this == DisplaySizeLevel.UNRECOGNIZED) {
        return baseSp.sp
    }
    val mapped =
        when (this) {
            DisplaySizeLevel.EXTRA_SMALL -> {
                when (baseSp) {
                    10 -> 9
                    12 -> 11
                    13 -> 11
                    14 -> 12
                    16 -> 14
                    18 -> 16
                    20 -> 18
                    24 -> 21
                    28 -> 25
                    56 -> 49
                    64 -> 56
                    else -> null
                }
            }

            DisplaySizeLevel.SMALL -> {
                when (baseSp) {
                    10 -> 9
                    12 -> 11
                    13 -> 12
                    14 -> 13
                    16 -> 15
                    18 -> 17
                    20 -> 19
                    24 -> 23
                    28 -> 26
                    56 -> 53
                    64 -> 60
                    else -> null
                }
            }

            DisplaySizeLevel.LARGE -> {
                when (baseSp) {
                    10 -> 11
                    12 -> 13
                    13 -> 14
                    14 -> 15
                    16 -> 17
                    18 -> 19
                    20 -> 22
                    24 -> 26
                    28 -> 30
                    56 -> 60
                    64 -> 69
                    else -> null
                }
            }

            else -> {
                null
            }
        }
    return mapped?.sp ?: ((baseSp * textPercent() + 50) / 100).sp
}

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
