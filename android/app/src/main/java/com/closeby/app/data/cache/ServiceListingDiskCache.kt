package com.closeby.app.data.cache

import android.content.Context
import com.closeby.app.data.model.ServiceDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class ServiceCacheEnvelope(
    val savedAt: Long,
    val services: List<ServiceDto>
)

class ServiceListingDiskCache(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun save(services: List<ServiceDto>) {
        val envelope = ServiceCacheEnvelope(
            savedAt = System.currentTimeMillis(),
            services = services
        )
        prefs.edit().putString(KEY_DATA, json.encodeToString(envelope)).apply()
    }

    fun load(): CachedServiceList? {
        val raw = prefs.getString(KEY_DATA, null) ?: return null
        return runCatching {
            val envelope = json.decodeFromString<ServiceCacheEnvelope>(raw)
            CachedServiceList(envelope.services, envelope.savedAt)
        }.getOrNull()
    }

    fun clear() {
        prefs.edit().remove(KEY_DATA).apply()
    }

    data class CachedServiceList(val services: List<ServiceDto>, val savedAt: Long)

    companion object {
        private const val PREFS = "closeby_service_cache"
        private const val KEY_DATA = "services_json"
    }
}
