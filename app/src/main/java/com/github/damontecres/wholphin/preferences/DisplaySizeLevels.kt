package com.github.damontecres.wholphin.preferences

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val displaySizeLevelDisplayOrder: List<DisplaySizeLevel> =
    listOf(
        DisplaySizeLevel.EXTRA_SMALL,
        DisplaySizeLevel.SMALL,
        DisplaySizeLevel.DEFAULT,
        DisplaySizeLevel.LARGE,
        DisplaySizeLevel.EXTRA_LARGE,
    )

fun DisplaySizeLevel.scaledSp(baseSp: Int): TextUnit {
    if (this == DisplaySizeLevel.DEFAULT || this == DisplaySizeLevel.UNRECOGNIZED) {
        return baseSp.sp
    }
    val mapped =
        when (this) {
            DisplaySizeLevel.EXTRA_SMALL -> {
                when (baseSp) {
                    10 -> 8
                    12 -> 10
                    13 -> 11
                    14 -> 11
                    16 -> 13
                    18 -> 15
                    20 -> 16
                    24 -> 20
                    28 -> 23
                    56 -> 46
                    64 -> 52
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
                    20 -> 18
                    24 -> 22
                    28 -> 26
                    56 -> 52
                    64 -> 59
                    else -> null
                }
            }

            DisplaySizeLevel.LARGE -> {
                when (baseSp) {
                    10 -> 11
                    12 -> 13
                    13 -> 14
                    14 -> 15
                    16 -> 18
                    18 -> 20
                    20 -> 22
                    24 -> 26
                    28 -> 31
                    56 -> 62
                    64 -> 70
                    else -> null
                }
            }

            DisplaySizeLevel.EXTRA_LARGE -> {
                when (baseSp) {
                    10 -> 12
                    12 -> 14
                    13 -> 16
                    14 -> 17
                    16 -> 19
                    18 -> 22
                    20 -> 24
                    24 -> 29
                    28 -> 34
                    56 -> 67
                    64 -> 77
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
        DisplaySizeLevel.EXTRA_SMALL -> 82
        DisplaySizeLevel.SMALL -> 92
        DisplaySizeLevel.LARGE -> 110
        DisplaySizeLevel.EXTRA_LARGE -> 120
        DisplaySizeLevel.DEFAULT, DisplaySizeLevel.UNRECOGNIZED -> 100
    }

fun DisplaySizeLevel.cardPercent(): Int =
    when (this) {
        DisplaySizeLevel.EXTRA_SMALL -> 78
        DisplaySizeLevel.SMALL -> 90
        DisplaySizeLevel.LARGE -> 115
        DisplaySizeLevel.EXTRA_LARGE -> 130
        DisplaySizeLevel.DEFAULT, DisplaySizeLevel.UNRECOGNIZED -> 100
    }

fun DisplaySizeLevel.spacingPercent(): Int =
    when (this) {
        DisplaySizeLevel.EXTRA_SMALL -> 75
        DisplaySizeLevel.SMALL -> 88
        DisplaySizeLevel.LARGE -> 118
        DisplaySizeLevel.EXTRA_LARGE -> 135
        DisplaySizeLevel.DEFAULT, DisplaySizeLevel.UNRECOGNIZED -> 100
    }
