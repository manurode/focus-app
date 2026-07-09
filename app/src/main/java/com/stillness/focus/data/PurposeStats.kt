package com.stillness.focus.data

data class PurposeStats(
    val accomplished: Int = 0,
    val notAccomplished: Int = 0,
    val preventedEntries: Int = 0,
) {
    val total: Int get() = accomplished + notAccomplished

    val accomplishmentRate: Float
        get() = if (total == 0) 0f else accomplished.toFloat() / total

    val hasData: Boolean
        get() = total > 0 || preventedEntries > 0

    operator fun plus(other: PurposeStats): PurposeStats = PurposeStats(
        accomplished = accomplished + other.accomplished,
        notAccomplished = notAccomplished + other.notAccomplished,
        preventedEntries = preventedEntries + other.preventedEntries,
    )
}

fun aggregateStats(statsByApp: Map<String, PurposeStats>): PurposeStats =
    statsByApp.values.fold(PurposeStats()) { acc, stats -> acc + stats }
