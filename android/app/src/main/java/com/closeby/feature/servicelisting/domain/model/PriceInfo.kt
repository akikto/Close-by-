package com.closeby.feature.servicelisting.domain.model

/**
 * Price is INFORMATION ONLY. Close by never processes payment, checkout,
 * UPI, cards, wallets, transactions, or commissions anywhere in this module.
 * This model exists purely so the UI can render a human readable price
 * string such as "₹500 / Day" or "Starting ₹800".
 */
data class PriceInfo(
    val amount: Double,
    val unit: PriceUnit,
    val isStartingPrice: Boolean = false
) {
    /** Human readable label, e.g. "Starting ₹800" or "₹500 / Day". */
    fun formatted(currencySymbol: String = "₹"): String {
        val amountText = if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString()
        val base = "$currencySymbol$amountText"
        return when {
            isStartingPrice -> "Starting $base"
            unit == PriceUnit.NONE -> base
            else -> "$base / ${unit.label}"
        }
    }
}

enum class PriceUnit(val label: String) {
    HOUR("Hour"),
    DAY("Day"),
    TRIP("Trip"),
    JOB("Job"),
    NONE("")
}
