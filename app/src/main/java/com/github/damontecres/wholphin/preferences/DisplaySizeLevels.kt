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
                    10 -> 8
                    12 -> 10
                    13 -> 11
                    14 -> 12
                    16 -> 13
                    18 -> 15
                    20 -> 17
                    24 -> 20
                    28 -> 24
                    56 -> 47
                    64 -> 54
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
                    10 -> 12
                    12 -> 14
                    13 -> 15
                    14 -> 16
                    16 -> 19
                    18 -> 21
                    20 -> 23
                    24 -> 28
                    28 -> 32
                    56 -> 65
                    64 -> 74
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
        DisplaySizeLevel.EXTRA_SMALL -> 84
        DisplaySizeLevel.SMALL -> 92
        DisplaySizeLevel.LARGE -> 116
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
