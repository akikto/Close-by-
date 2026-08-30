package com.closeby.app.core.permissions

/**
 * Runtime permissions the app may request. Intentionally small: browsing
 * services never requires any of these, so nothing here is requested at
 * launch — only contextually, when a feature actually needs it
 * (e.g. Explore needs Location to show nearby providers).
 */
enum class AppPermission {
    LOCATION
}

enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_REQUESTED
}

/**
 * Contract for checking and requesting runtime permissions.
 * Concrete implementation is wired at the Activity level and provided
 * to feature ViewModels; not implemented in the base project.
 */
interface PermissionManager {
    fun status(permission: AppPermission): PermissionStatus
    suspend fun request(permission: AppPermission): PermissionStatus
}
