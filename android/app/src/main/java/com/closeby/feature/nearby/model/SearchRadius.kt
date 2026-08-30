package com.closeby.feature.nearby.model

/**
 * Search radius options for nearby filtering.
 * [Custom] allows any user-specified radius in kilometers.
 */
sealed class SearchRadius(val kilometers: Double) {
    object OneKm : SearchRadius(1.0)
    object FiveKm : SearchRadius(5.0)
    object TenKm : SearchRadius(10.0)
    object TwentyFiveKm : SearchRadius(25.0)
    data class Custom(val km: Double) : SearchRadius(km) {
        init {
            require(km > 0.0) { "Custom radius must be > 0 km, was $km" }
        }
    }

    companion object {
        /** Preset radius options to show in a picker, in ascending order. */
        val presets: List<SearchRadius> = listOf(OneKm, FiveKm, TenKm, TwentyFiveKm)
    }
}
