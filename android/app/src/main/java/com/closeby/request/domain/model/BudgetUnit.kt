package com.closeby.request.domain.model

/**
 * Unit for the customer's stated budget. This is negotiation information
 * only — never interpreted as a payable amount by the app.
 */
enum class BudgetUnit {
    HOUR,
    DAY,
    TRIP,
    JOB,
    OTHER
}
