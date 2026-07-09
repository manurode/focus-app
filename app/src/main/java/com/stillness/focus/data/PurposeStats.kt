package com.stillness.focus.data

data class PurposeStats(
    val accomplished: Int = 0,
    val notAccomplished: Int = 0,
    val preventedEntries: Int = 0,
) {
    val total: Int get() = accomplished + notAccomplished

    val accomplishmentRate: Float
        get() = if (total == 0) 0f else accomplished.toFloat() / total
}
