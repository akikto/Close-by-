package com.closeby.app.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.closeby.app.core.network.SupabaseClientProvider
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream
import java.util.UUID

interface ServiceImageUploader {
    suspend fun upload(uri: Uri, providerId: String): Result<String>
}

class SupabaseServiceImageUploader(
    private val context: Context
) : ServiceImageUploader {

    override suspend fun upload(uri: Uri, providerId: String): Result<String> = runCatching {
        val bytes = compressImage(uri)
        val fileName = "$providerId/${UUID.randomUUID()}.jpg"
        val bucket = SupabaseClientProvider.client.storage.from("service-images")
        bucket.upload(fileName, bytes)
        bucket.publicUrl(fileName)
    }

    private fun compressImage(uri: Uri): ByteArray {
        val resolver = context.contentResolver
        val original = resolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            ?: error("Could not read image.")
        val maxDim = 1280
        val scaled = scaleDown(original, maxDim)
        if (scaled !== original) original.recycle()
        return ByteArrayOutputStream().use { stream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            scaled.recycle()
            stream.toByteArray()
        }
    }

    private fun scaleDown(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val largest = maxOf(width, height)
        if (largest <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / largest
        val targetW = (width * ratio).toInt().coerceAtLeast(1)
        val targetH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }
}

/** Returns a placeholder URL when storage is unavailable (mock / offline builds). */
class MockServiceImageUploader : ServiceImageUploader {
    override suspend fun upload(uri: Uri, providerId: String): Result<String> =
        Result.success("https://images.unsplash.com/photo-1581092160562-40aa08e78837?w=400")
}
