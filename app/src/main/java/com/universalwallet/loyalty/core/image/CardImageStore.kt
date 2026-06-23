package com.universalwallet.loyalty.core.image

import android.content.Context
import android.graphics.Bitmap
import com.universalwallet.loyalty.core.utils.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores card images in app-private internal storage (`files/card_images`),
 * which is sandboxed per-app and not world-readable — the appropriate place for
 * a user's loyalty-card images. Returns absolute paths that are persisted on the
 * card and rendered later via Coil.
 */
@Singleton
class CardImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    private val directory: File
        get() = File(context.filesDir, DIR).apply { if (!exists()) mkdirs() }

    /** Compresses [bitmap] to JPEG in private storage and returns its path. */
    suspend fun saveBitmap(bitmap: Bitmap, quality: Int = 90): String = withContext(io) {
        val file = File(directory, "card_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), out)
        }
        file.absolutePath
    }

    /** Deletes a previously stored image; safe to call with a stale path. */
    suspend fun delete(path: String): Boolean = withContext(io) {
        runCatching { File(path).takeIf { it.exists() }?.delete() ?: false }.getOrDefault(false)
    }

    fun fileFor(path: String): File = File(path)

    private companion object {
        const val DIR = "card_images"
    }
}
