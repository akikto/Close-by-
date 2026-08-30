package com.closeby.app.core.storage

/**
 * Small local key-value store contract, for lightweight app preferences
 * (e.g. "has completed onboarding", "last selected category").
 * Not used for domain data — that goes through the repository layer
 * backed by Supabase/PostgreSQL.
 */
interface LocalStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String, default: Boolean = false): Boolean
    suspend fun clear(key: String)
}
