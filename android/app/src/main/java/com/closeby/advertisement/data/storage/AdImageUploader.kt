package com.closeby.advertisement.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.closeby.app.core.network.SupabaseClientProvider
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream
import java.util.UUID

interface AdImageUploader {
    suspend fun upload(uri: Uri, ownerId: String): Result<String>
}

class SupabaseAdImageUploader(
    private val context: Context
) : AdImageUploader {

    override suspend fun upload(uri: Uri, ownerId: String): Result<String> = runCatching {
        val bytes = compressImage(uri)
        val fileName = "$ownerId/${UUID.randomUUID()}.jpg"
        val bucket = SupabaseClientProvider.client.storage.from("ad-images")
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

class MockAdImageUploader : AdImageUploader {
    override suspend fun upload(uri: Uri, ownerId: String): Result<String> =
        Result.success("https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=800")
}
