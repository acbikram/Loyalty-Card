package com.universalwallet.loyalty.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.universalwallet.loyalty.core.utils.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** A processed image: the in-memory bitmap and its persisted file path. */
data class ProcessedImage(val bitmap: Bitmap, val path: String)

/**
 * Imports and normalises images for card capture: decode, apply EXIF rotation,
 * downscale to a sane maximum, then compress to private storage. Rotation and
 * cropping helpers are exposed for the (future) interactive editor and the
 * auto-crop step of screenshot import. All work runs off the main thread.
 */
@Singleton
class ImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
    private val imageStore: CardImageStore,
) {

    /** Full import pipeline: decode → orient → downscale → compress + store. */
    suspend fun importFromUri(uri: Uri, maxDimension: Int = 1600): ProcessedImage = withContext(io) {
        val decoded = decodeBitmap(uri) ?: throw IOException("Unable to decode image: $uri")
        val oriented = applyExifRotation(uri, decoded)
        val scaled = downscale(oriented, maxDimension)
        val path = imageStore.saveBitmap(scaled)
        ProcessedImage(bitmap = scaled, path = path)
    }

    /** Rotates [bitmap] by [degrees] clockwise. */
    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees % 360f == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /** Crops [bitmap] to the given rectangle, clamped to the image bounds. */
    fun cropToRect(bitmap: Bitmap, left: Int, top: Int, width: Int, height: Int): Bitmap {
        val l = left.coerceIn(0, bitmap.width - 1)
        val t = top.coerceIn(0, bitmap.height - 1)
        val w = width.coerceIn(1, bitmap.width - l)
        val h = height.coerceIn(1, bitmap.height - t)
        return Bitmap.createBitmap(bitmap, l, t, w, h)
    }

    private fun decodeBitmap(uri: Uri): Bitmap? =
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }

    private fun applyExifRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        val degrees = context.contentResolver.openInputStream(uri)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        return rotate(bitmap, degrees)
    }

    private fun downscale(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= maxDimension) return bitmap
        val scale = maxDimension.toFloat() / longest
        val w = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }
}
